package co.sblock.chat;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

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

	private static Chat instance;
	private final ChannelManager cm = new ChannelManager();
	private static boolean computersRequired = false; //Hardcoded override, will be set to true come Entry
	private CleverHal megaHal;
	private Halculator halculator;
	private final DummyPlayer buffer = new DummyPlayer();

	@Override
	protected void onEnable() {
		instance = this;
		this.cm.loadAllChannels();
		this.cm.createDefaultSet();

		this.halculator = new Halculator();
		this.megaHal = new CleverHal();
	}

	@Override
	protected void onDisable() {
		cm.saveAllChannels();
	}

	public ChannelManager getChannelManager() {
		return cm;
	}

	public CleverHal getHal() {
		return megaHal;
	}

	/**
	 * @return the halculator
	 */
	public Halculator getHalculator() {
		return halculator;
	}

	public static Chat getChat() {
		return instance;
	}

	public static boolean getComputerRequired() {
		return computersRequired;
	}

	@Override
	protected String getModuleName() {
		return "Sblock Chat";
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

		Channel channel = ChannelManager.getChannelManager().getChannel(channelName);
		if (channel == null) {
			throw new IllegalArgumentException("Given channel does not exist!");
		}
		MessageBuilder builder = new MessageBuilder()
				.setSender(Users.getGuaranteedUser(sender.getUniqueId())).setChannel(channel)
				.setMessage(msg).setChannel(channel);
		Message message = builder.toMessage();

		Set<Player> players = new HashSet<>();
		channel.getListening().forEach(uuid -> {
			Player player = Bukkit.getPlayer(uuid);
			if (player != null) {
				players.add(player);
			}
		});
		// Add a dummy player so WG doesn't cancel the event if there are no recipients
		players.add(buffer);

		SblockAsyncChatEvent event = new SblockAsyncChatEvent(false, sender, players, message, false);

		Bukkit.getPluginManager().callEvent(event);

		return event.isCancelled() && !event.isGlobalCancelled();
	}
}
