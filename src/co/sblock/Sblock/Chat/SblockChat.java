package co.sblock.Sblock.Chat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import co.sblock.Sblock.Module;
import co.sblock.Sblock.Sblock;
import co.sblock.Sblock.Chat.Channel.ChannelManager;
import co.sblock.Sblock.Chat.Chester.ChesterListener;
import co.sblock.Sblock.UserData.SblockUser;
import co.sblock.Sblock.UserData.UserManager;

public class SblockChat extends Module {

	private static SblockChat instance;
	private ChannelManager cm = new ChannelManager();
	private ChatCommands clistener = new ChatCommands();
	private static boolean computersRequired = false;	//Hardcoded override, will be set to true come Entry

	public static ArrayList<String> chester = new ArrayList<String>();

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
		} else {
			return;
		}

		File chesterCFG = new File(Sblock.getInstance().getDataFolder().getParent() + "/Chester/", "config.yml");
		if (chesterCFG.exists()) {
			YamlConfiguration chesterYML = YamlConfiguration.loadConfiguration(chesterCFG);
			List<String> triggers = chesterYML.getStringList("triggerwords");
			if (triggers != null) {
				chester = new ArrayList<String>(triggers);
			}
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
