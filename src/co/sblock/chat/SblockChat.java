package co.sblock.chat;

import co.sblock.chat.ai.Halculator;
import co.sblock.chat.ai.MegaHal;
import co.sblock.module.Module;

public class SblockChat extends Module {

	private static SblockChat instance;
	private ChannelManager cm = new ChannelManager();
	private static boolean computersRequired = false; //Hardcoded override, will be set to true come Entry
	private MegaHal megaHal;
	private Halculator halculator;

	@Override
	protected void onEnable() {
		instance = this;
		this.cm.loadAllChannels();
		this.cm.createDefaultSet();

		this.megaHal = new MegaHal();
		this.halculator = new Halculator();
	}

	@Override
	protected void onDisable() {
		cm.saveAllChannels();
		megaHal.saveLogs();
	}

	public ChannelManager getChannelManager() {
		return cm;
	}

	public MegaHal getHal() {
		return megaHal;
	}

	/**
	 * @return the halculator
	 */
	public Halculator getHalculator() {
		return halculator;
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
