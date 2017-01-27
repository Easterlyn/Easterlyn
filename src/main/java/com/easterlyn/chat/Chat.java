package com.easterlyn.chat;

import com.easterlyn.Easterlyn;
import com.easterlyn.chat.ai.CleverHal;
import com.easterlyn.chat.ai.Halculator;
import com.easterlyn.chat.channel.Channel;
import com.easterlyn.chat.message.Message;
import com.easterlyn.chat.message.MessageBuilder;
import com.easterlyn.events.event.SblockAsyncChatEvent;
import com.easterlyn.module.Module;
import com.easterlyn.users.UserRank;
import com.easterlyn.users.Users;
import com.easterlyn.utilities.DummyPlayer;
import com.easterlyn.utilities.PermissionUtils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import net.md_5.bungee.api.ChatColor;

public class Chat extends Module {

	private final ChannelManager channelManager;
	private final DummyPlayer buffer;

	private Language lang;
	private Users users;
	private CleverHal cleverHal;
	private Halculator halculator;

	public Chat(Easterlyn plugin) {
		super(plugin);
		this.channelManager = new ChannelManager(this);
		this.buffer = new DummyPlayer();

		// Permission to use >greentext
		PermissionUtils.getOrCreate("sblock.chat.greentext", PermissionDefault.TRUE);
		// Legacy support: old node as parent
		PermissionUtils.addParent("sblock.chat.greentext", "sblockchat.greentext");

		// Permission for messages to automatically color using name color
		PermissionUtils.getOrCreate("sblock.chat.color", PermissionDefault.FALSE);
		// Legacy support: old node as parent
		PermissionUtils.getOrCreate("sblockchat.color", PermissionDefault.FALSE);
		PermissionUtils.addParent("sblock.chat.color", "sblockchat.color", PermissionDefault.FALSE);

		// Permission to bypass chat filtering
		PermissionUtils.addParent("sblock.chat.unfiltered", UserRank.FELT.getPermission());
		PermissionUtils.addParent("sblock.chat.unfiltered", "sblock.spam");
		// Permission to be recognized as a moderator in every channel
		PermissionUtils.addParent("sblock.chat.channel.moderator", UserRank.FELT.getPermission());
		// Permission to be recognized as an owner in every channel
		PermissionUtils.addParent("sblock.chat.channel.owner", UserRank.DENIZEN.getPermission());

		// Permission to have name a certain color
		Permission parentPermission;
		Permission childPermission;
		for (ChatColor chatColor : ChatColor.values()) {
			// Legacy support: old node as parent
			String color = chatColor.name().toLowerCase();
			parentPermission = PermissionUtils.getOrCreate("sblockchat." + color, PermissionDefault.FALSE);
			childPermission = PermissionUtils.getOrCreate("sblock.chat.color." + color, PermissionDefault.FALSE);
			childPermission.addParent(parentPermission, true);
		}
	}

	@Override
	protected void onEnable() {
		this.lang = this.getPlugin().getModule(Language.class);
		this.users = this.getPlugin().getModule(Users.class);
		this.cleverHal = new CleverHal(this.getPlugin());
		this.halculator = new Halculator(this.getPlugin());
		this.channelManager.loadAllChannels();
		this.channelManager.createDefaultSet();
	}

	@Override
	protected void onDisable() {
		this.channelManager.saveAllChannels();
	}

	public ChannelManager getChannelManager() {
		return this.channelManager;
	}

	public MessageBuilder getHalBase() {
		return new MessageBuilder(this.getPlugin()).setSender(lang.getValue("core.bot_name"))
				.setNameClick("/report ").setNameHover(lang.getValue("core.bot_hover"))
				.setChannel(this.channelManager.getChannel("#"));
	}

	public CleverHal getHal() {
		return this.cleverHal;
	}

	/**
	 * @return the halculator
	 */
	public Halculator getHalculator() {
		return this.halculator;
	}

	@Override
	public boolean isRequired() {
		return true;
	}

	@Override
	public String getName() {
		return "Chat";
	}

	public boolean testForMute(Player sender) {
		return this.testForMute(sender, "Mute test.", "@test@");
	}

	public synchronized boolean testForMute(Player sender, String msg, String channelName) {
		if (sender == null || msg == null || channelName == null) {
			throw new IllegalArgumentException("Null values not allowed for mute testing!");
		}

		if (Bukkit.getPluginManager().isPluginEnabled("GriefPrevention")
				&& me.ryanhamshire.GriefPrevention.GriefPrevention.instance.dataStore.isSoftMuted(sender.getUniqueId())) {
			return true;
		}

		Channel channel = this.getChannelManager().getChannel(channelName);
		if (channel == null) {
			throw new IllegalArgumentException("Given channel does not exist!");
		}
		MessageBuilder builder = new MessageBuilder(this.getPlugin())
				.setSender(this.users.getUser(sender.getUniqueId())).setChannel(channel)
				.setMessage(msg).setChannel(channel);
		Message message = builder.toMessage();

		SblockAsyncChatEvent event = new SblockAsyncChatEvent(false, sender, message, false);
		// Add a dummy player so WG doesn't cancel the event if there are no recipients
		event.getRecipients().add(this.buffer);

		Bukkit.getPluginManager().callEvent(event);

		return event.isCancelled() && !event.isGlobalCancelled();
	}

}
