package co.sblock.chat;

import co.sblock.chat.ai.MegaHal;
import co.sblock.module.Module;

public class SblockChat extends Module {

	private static SblockChat instance;
	private ChannelManager cm = new ChannelManager();
	private ChatCommands clistener = new ChatCommands();
	private static boolean computersRequired = false; //Hardcoded override, will be set to true come Entry
	private MegaHal hal;

	@Override
	protected void onEnable() {
		instance = this;
		this.registerCommands(clistener);
		this.cm.loadAllChannels();
		this.cm.createDefaultSet();

		this.hal = new MegaHal();
	}

	@Override
	protected void onDisable() {
		cm.saveAllChannels();
	}

	public ChannelManager getChannelManager() {
		return cm;
	}

	public MegaHal getHal() {
		return hal;
	}

	public static SblockChat getChat() {
		return instance;
	}

	public static boolean getComputerRequired() {
		return computersRequired;
	}

	@Override
	protected String getModuleName() {
		return "ChatModule";
	}
}
