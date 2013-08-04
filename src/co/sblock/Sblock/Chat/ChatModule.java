package co.sblock.Sblock.Chat;

import co.sblock.Sblock.Module;
import co.sblock.Sblock.Chat.Channel.ChannelManager;
import co.sblock.Sblock.Utilities.Sblogger;

public class ChatModule extends Module {

	private static ChatModule instance;
	private ChannelManager cm = new ChannelManager();
	private ChatModuleListener listener = new ChatModuleListener();
	private ChatModuleCommandListener clistener = new ChatModuleCommandListener();
	private static Sblogger log = new Sblogger("SblockChat");

	@Override
	protected void onEnable() {
		slog().info("Enabling Chat.");
		instance = this;
		this.registerEvents(listener);
		this.registerCommands(clistener);
		cm.loadAllChannels();
		this.cm.createDefaultSet();
		slog().info("Chat enabled.");
	}

	@Override
	protected void onDisable() {
		cm.saveAllChannels();
	}

	public ChannelManager getChannelManager() {
		return cm;
	}

	public static ChatModule getInstance() {
		return instance;
	}

	public static Sblogger slog() {
		return log;
	}
}
