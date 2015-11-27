package co.sblock.chat;

import java.util.HashSet;
import java.util.Set;

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

	private static boolean computersRequired = false; //Hardcoded override, will be set to true come Entry

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

	public CleverHal getHal() {
		return cleverHal;
	}

	/**
	 * @return the halculator
	 */
	public Halculator getHalculator() {
		return halculator;
	}

	public static boolean getComputerRequired() {
		return computersRequired;
	}

	@Override
	public String getName() {
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

		Channel channel = getChannelManager().getChannel(channelName);
		if (channel == null) {
			throw new IllegalArgumentException("Given channel does not exist!");
		}
		MessageBuilder builder = new MessageBuilder(getPlugin())
				.setSender(users.getUser(sender.getUniqueId())).setChannel(channel)
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
