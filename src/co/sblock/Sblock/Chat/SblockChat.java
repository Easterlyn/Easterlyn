package co.sblock.Sblock.Chat;

import org.bukkit.Bukkit;

import co.sblock.Sblock.Module;
import co.sblock.Sblock.Chat.Channel.ChannelManager;
import co.sblock.Sblock.Chat.Chester.ChesterListener;
import co.sblock.Sblock.UserData.SblockUser;
import co.sblock.Sblock.UserData.UserManager;

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
		Object[] users = UserManager.getUserManager().getUserlist().toArray();
		for (int i = 0; i < users.length; i++) {
			UserManager.getUserManager().removeUser(((SblockUser) users[i]).getPlayerName());
		}
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
