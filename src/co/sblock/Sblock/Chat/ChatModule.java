package co.sblock.Sblock.Chat;

import co.sblock.Sblock.Module;
import co.sblock.Sblock.Chat.Channel.ChannelManager;
import co.sblock.Sblock.UserData.SblockUser;
import co.sblock.Sblock.UserData.UserManager;

public class ChatModule extends Module {

	private static ChatModule instance;
	private ChannelManager cm = new ChannelManager();
	private ChatCommands clistener = new ChatCommands();
	private static boolean computersRequired = false;	//Hardcoded override, will be set to true come Entry

	@Override
	protected void onEnable() {
		instance = this;
		this.registerCommands(clistener);
		cm.loadAllChannels();
		this.cm.createDefaultSet();
	}

	@Override
	protected void onDisable() {
		cm.saveAllChannels();
		for (SblockUser u : UserManager.getUserManager().getUserlist()) {
			UserManager.getUserManager().removeUser(u.getPlayerName());
		}
		instance = null;
	}

	public ChannelManager getChannelManager() {
		return cm;
	}

	public static ChatModule getChatModule() {
		return instance;
	}
	
	public static boolean getComputerRequired()	{
		return computersRequired;
	}
}
