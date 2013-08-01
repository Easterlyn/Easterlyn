package co.sblock.Sblock.Chat;

import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import co.sblock.Sblock.Module;
import co.sblock.Sblock.Chat.Channel.AccessLevel;
import co.sblock.Sblock.Chat.Channel.Channel;
import co.sblock.Sblock.Chat.Channel.ChannelManager;
import co.sblock.Sblock.Chat.Channel.ChannelType;
import co.sblock.Sblock.UserData.SblockUser;
import co.sblock.Sblock.UserData.UserManager;

public class ChatModule extends Module {

	private static ChatModule instance;
	private ChannelManager cm = new ChannelManager();
	private ChatModuleListener listener = new ChatModuleListener();
	private ChatModuleCommandListener clistener = new ChatModuleCommandListener();

	@Override
	protected void onEnable() {
		instance = this;
		this.registerEvents(listener);
		this.registerCommands(clistener);
		cm.loadAllChannels();
		this.cm.createDefaultSet();
	}

	@Override
	protected void onDisable() {
		cm.saveAllChannels();
	}

	public boolean onCommand(CommandSender sender, Command cmd,
			String commandLabel, String[] args) {
		boolean isConsole = !(sender instanceof Player);
		boolean isHelper = !isConsole && (sender.hasPermission("group.helper")
						|| sender.hasPermission("group.denizen")
						|| sender.hasPermission("group.horrorterror"));
		boolean isMod = !isConsole
				&& (sender.hasPermission("group.denizen")
						|| sender.hasPermission("group.horrorterror"));
		boolean isAdmin = !isConsole && sender.hasPermission("group.horrorterror");
		if (cmd.getName().equalsIgnoreCase("o")) { // [o] Be Doc Scratch
			if (isConsole || isAdmin || sender.isOp()) {
				String output = "";
				for (String s : args) {
					output = output + s + " ";
				}
				Logger.getLogger("Minecraft").info("[o] " + output);
				for (Player p : Bukkit.getServer().getOnlinePlayers()) {
					p.sendMessage(ChatColor.BOLD + "[o] " + output);
				}
				return true;
			}
		}
		if (cmd.getName().equalsIgnoreCase("sban")) { // superban
			if (isConsole || sender.isOp()) {
				Player victim = Bukkit.getServer().getPlayer(args[0]);
				String ip = victim.getAddress().getAddress().getHostAddress();
				// normal ban goes here
				Bukkit.getServer().banIP(ip);
				// Also need banreason
				// also broadcast to all players
				// Also send to Logger
				return true;
			}
		}
		if (cmd.getName().equalsIgnoreCase("whois")) {
			// Master PlayerData output
			if (isConsole || isMod) {
				Player subject = Bukkit.getServer().getPlayer(args[0]);
				SblockUser u = UserManager.getUserManager().getUser(
						subject.getName());
				u.toString();
				return true;
			}
		}
		return true;
	}

	public ChannelManager getChannelManager() {
		return cm;
	}

	public static ChatModule getInstance() {
		return instance;
	}
}
