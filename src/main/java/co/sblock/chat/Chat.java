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

public class Chat extends Module {

	private final ChannelManager channelManager;
	private final DummyPlayer buffer;

	private Language lang;
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
				.setNameClick("/report ").setNameHover(Language.getColor("bot_text") + "Artifical Intelligence");
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
