package co.sblock.chat;

import org.bukkit.Bukkit;

import co.sblock.Module;
import co.sblock.chat.channel.ChannelManager;
import co.sblock.chat.chester.ChesterListener;

public class SblockChat extends Module {

	private static SblockChat instance;
	private ChannelManager cm = new ChannelManager();
	private ChatCommands clistener = new ChatCommands();
	private static boolean computersRequired = false;	//Hardcoded override, will be set to true come Entry

	@Override
	protected void onEnable() {
		instance = this;
		this.registerCommands(clistener);
		cm.loadAllChannels();
		this.cm.createDefaultSet();

		if (Bukkit.getPluginManager().isPluginEnabled("Chester")) {
			ChesterListener cl = new ChesterListener();
			this.registerEvents(cl);
			this.registerCommands(cl);
		}
	}

	@Override
	protected void onDisable() {
		cm.saveAllChannels();
		instance = null;
	}

	public ChannelManager getChannelManager() {
		return cm;
	}

	public static SblockChat getChat() {
		return instance;
	}
	
	public static boolean getComputerRequired()	{
		return computersRequired;
	}
}
