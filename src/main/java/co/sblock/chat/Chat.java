package co.sblock.chat;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import co.sblock.Sblock;
import co.sblock.chat.ai.CleverHal;
import co.sblock.chat.ai.Halculator;
import co.sblock.chat.channel.Channel;
import co.sblock.chat.message.Message;
import co.sblock.chat.message.MessageBuilder;
import co.sblock.events.event.SblockAsyncChatEvent;
import co.sblock.module.Module;
import co.sblock.users.Users;
import co.sblock.utilities.DummyPlayer;

import net.md_5.bungee.api.ChatColor;

public class Chat extends Module {

	private final ChannelManager channelManager;
	private final DummyPlayer buffer;

	private Users users;
	private CleverHal cleverHal;
	private Halculator halculator;

	public Chat(Sblock plugin) {
		super(plugin);
		this.channelManager = new ChannelManager(this);
		this.buffer = new DummyPlayer();
	}

	@Override
	protected void onEnable() {
		this.users = getPlugin().getModule(Users.class);
		this.cleverHal = new CleverHal(getPlugin());
		this.halculator = new Halculator(getPlugin());
		this.channelManager.loadAllChannels();
		this.channelManager.createDefaultSet();
	}

	@Override
	protected void onDisable() {
		channelManager.saveAllChannels();
	}

	public ChannelManager getChannelManager() {
		return channelManager;
	}

	public MessageBuilder getHalBase() {
		return new MessageBuilder(getPlugin()).setSender(ChatColor.DARK_RED + getPlugin().getBotName())
				.setNameClick("/report ").setNameHover(ChatColor.RED + "Artifical Intelligence");
	}

	public CleverHal getHal() {
		return cleverHal;
	}

	/**
	 * @return the halculator
	 */
	public Halculator getHalculator() {
		return halculator;
	}

	@Override
	public String getName() {
		return "Chat";
	}

	public boolean testForMute(Player sender) {
		return testForMute(sender, "Mute test.", "@test@");
	}

	public synchronized boolean testForMute(Player sender, String msg, String channelName) {
		if (sender == null || msg == null || channelName == null) {
			throw new IllegalArgumentException("Null values not allowed for mute testing!");
		}

		if (Bukkit.getPluginManager().isPluginEnabled("GriefPrevention")
				&& me.ryanhamshire.GriefPrevention.GriefPrevention.instance.dataStore.isSoftMuted(sender.getUniqueId())) {
			return true;
		}

		Channel channel = getChannelManager().getChannel(channelName);
		if (channel == null) {
			throw new IllegalArgumentException("Given channel does not exist!");
		}
		MessageBuilder builder = new MessageBuilder(getPlugin())
				.setSender(users.getUser(sender.getUniqueId())).setChannel(channel)
				.setMessage(msg).setChannel(channel);
		Message message = builder.toMessage();

		SblockAsyncChatEvent event = new SblockAsyncChatEvent(false, sender, message, false);
		// Add a dummy player so WG doesn't cancel the event if there are no recipients
		event.getRecipients().add(buffer);

		Bukkit.getPluginManager().callEvent(event);

		return event.isCancelled() && !event.isGlobalCancelled();
	}
}
