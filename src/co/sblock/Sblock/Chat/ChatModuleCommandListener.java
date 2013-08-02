/**
 * 
 */
package co.sblock.Sblock.Chat;

import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_6_R2.command.ColouredConsoleSender;
import org.bukkit.entity.Player;

import co.sblock.Sblock.CommandListener;
import co.sblock.Sblock.DatabaseManager;
import co.sblock.Sblock.SblockCommand;
import co.sblock.Sblock.Chat.Channel.AccessLevel;
import co.sblock.Sblock.Chat.Channel.Channel;
import co.sblock.Sblock.Chat.Channel.ChannelManager;
import co.sblock.Sblock.Chat.Channel.ChannelType;
import co.sblock.Sblock.UserData.SblockUser;
import co.sblock.Sblock.UserData.UserManager;
import co.sblock.Sblock.Utilities.Sblogger;

/**
 * @author Dublek
 * @author Jikoo
 */
public class ChatModuleCommandListener implements CommandListener {

	@SblockCommand(consoleFriendly = true, mergeLast = true)
	public boolean lel(CommandSender sender, String text) {
		if (!(sender instanceof Player) || sender.hasPermission("group.denizen")) {
			String lelOut = new String();
			for (int i = 0; i < text.length(); i++) {
				for (int j = 0; j < ColorDef.RAINBOW.length; j++) {
					if (i >= text.length())
						break;
					lelOut = lelOut + ChatColor.valueOf(ColorDef.RAINBOW[j]) + text.charAt(i);
					i++;
				}
			}
			//new Sblogger("LE").info(lelOut);
			ColouredConsoleSender.getInstance().sendMessage(lelOut);
			for(Player p: Bukkit.getServer().getOnlinePlayers()) {
				p.sendMessage(ChatColor.MAGIC + lelOut);
			}
		} else {
			sender.sendMessage(ChatColor.BLACK + "lul.");
		}
		return true;
	}

	@SblockCommand(consoleFriendly = true, mergeLast = true)
	public boolean le(CommandSender sender, String text) {
		if (!(sender instanceof Player) || sender.hasPermission("group.denizen")) {
			String leOut = new String();
			for (int i = 0; i < text.length(); i++) {
				for (int j = 0; j < ColorDef.RAINBOW.length; j++) {
					if (i >= text.length())
						break;
					leOut = leOut + ChatColor.valueOf(ColorDef.RAINBOW[j]) + text.charAt(i);
					i++;
				}
			}
			new Sblogger("LE").info(leOut);
			//ColouredConsoleSender.getInstance().sendMessage(leOut);
			for(Player p: Bukkit.getServer().getOnlinePlayers()) {
				p.sendMessage(leOut);
			}
		} else {
			sender.sendMessage(ChatColor.BLACK + "Aren't you fancy.");
		}
		return true;
	}
	

	@SblockCommand(consoleFriendly = true)
	public boolean whois(CommandSender sender, String target) {
		if (!(sender instanceof Player) || sender.hasPermission("group.denizen")) {
			SblockUser u = UserManager.getUserManager().getUser(target);
			u.toString();
			return true;
		} else {
			sender.sendMessage(ChatColor.BLACK +
					"There are mysteries into which it behooves one not to delve too deeply...");
			return true;
		}
	}
	
	@SblockCommand(consoleFriendly = true, mergeLast = true)
	public boolean o(CommandSender sender, String text) {
		if (!(sender instanceof Player) ||
				sender.hasPermission("groups.horrorterror") || sender.isOp()) {
			new Sblogger("o").info(text);
			for (Player p : Bukkit.getServer().getOnlinePlayers()) {
				p.sendMessage(ChatColor.BOLD + "[o] " + text);
			}
			return true;
		} else {
			sender.sendMessage(ChatColor.BOLD + "[o] "
					+"You try to be the white text guy, but fail to be the white text guy."
					+ "No one can be the white text guy except for the white text guy.");
			return true;
		}
	}

	@SblockCommand(consoleFriendly = true, mergeLast = true)
	public boolean sban(CommandSender sender, String target, String reason) {
		if (!(sender instanceof Player) || sender.isOp()) {
			SblockUser victim = UserManager.getUserManager().getUser(target);
			victim.getPlayer().kickPlayer(reason);
			victim.getPlayer().setBanned(true);
			Bukkit.banIP(victim.getUserIP());
			DatabaseManager.getDatabaseManager().deleteUser(victim);
			Bukkit.dispatchCommand(sender, "lwc admin purge " + target);
			for (SblockUser u : UserManager.getUserManager().getUserlist()) {
				u.sendMessage(ChatColor.DARK_RED + victim.getPlayerName() +
						"has been superbanned for " + reason);
			}
			ChatStorage cs = new ChatStorage();
			cs.setBan(target, reason);
			cs.removeGlobalMute(target);
			return true;
		} else {
			sender.sendMessage(ChatColor.BLACK +
					"There are mysteries into which it behooves one not to delve too deeply...");
			return true;
		}
	}

	@SuppressWarnings("unused")
	@SblockCommand(consoleFriendly = true, mergeLast = true)
	public boolean sc(CommandSender sender, String action, String arguments) {
		boolean isConsole = !(sender instanceof Player);
		boolean isHelper = !isConsole && sender.hasPermission("group.helper");
		boolean isMod = !isConsole && sender.hasPermission("group.denizen");
		if (isConsole) { // TODO console-friendly stuff
			sender.sendMessage(ChatColor.DARK_RED + "No commands programmed yet! :D");
		} else { // ingame commands
			SblockUser user = UserManager.getUserManager().getUser(sender.getName());
			String[] args = arguments.split(" ");
			if (action == null) {
				return false;
			}
			if (action.equalsIgnoreCase("c")) { // setChannel
				if (arguments == null) {
					sender.sendMessage(ChatColor.YELLOW
							+ "Set current channel:\n\t/sc c <$channelname>");
					return true;
				}
				try {
					user.setCurrent(ChatModule.getInstance().getChannelManager()
							.getChannel(args[0]));
				} catch (NullPointerException e) {
					sender.sendMessage(ChatColor.RED + "Channel "
							+ ChatColor.GOLD + args[0] + ChatColor.RED
							+ " does not exist!");
				}
				return true;
			}
			if (action.equalsIgnoreCase("l")) { // addListening
				if (arguments == null) {
					sender.sendMessage(ChatColor.YELLOW
							+ "Listen to a channel:\n\t/sc l <$channelname>");
					return true;
				}
				try {
					user.addListening(ChatModule.getInstance()
							.getChannelManager().getChannel(args[0]));
				} catch (NullPointerException e) {
					sender.sendMessage(ChatColor.RED + "Channel "
							+ ChatColor.GOLD + args[0] + ChatColor.RED
							+ " does not exist!");
				}
				return true;
			}
			if (action.equalsIgnoreCase("leave")) { // removeListening
				if (arguments == null) {
					sender.sendMessage(ChatColor.YELLOW
							+ "Stop listening to a channel:\n\t/sc leave <$channelname>");
					return true;
				}
				try {
					user.removeListening(ChatModule.getInstance()
							.getChannelManager().getChannel(args[0]));
				} catch (NullPointerException e) {
					sender.sendMessage(ChatColor.RED + "Channel "
							+ ChatColor.GOLD + args[0] + ChatColor.RED
							+ " does not exist!");
				}
				return true;
			}
			if (action.equalsIgnoreCase("list")) { // listListening
				String clist = ChatColor.YELLOW + "Currently pestering: ";
				for (String s : user.getListening()) {
					clist += s + " ";
				}
				sender.sendMessage(clist);
				return true;
			}
			if (action.equalsIgnoreCase("listall")) { // listAll
				String clist = ChatColor.YELLOW + "All channels: ";
				Map<String, Channel> channels = ChannelManager
						.getChannelList();
				for (Channel c : channels.values()) {
					String next;
					if (user.getListening().contains(c)) {
						next = ChatColor.YELLOW + c.getName() + " ";
						clist += next;
					} else if (c.getAccess().equals(AccessLevel.PUBLIC)) {
						next = ChatColor.GREEN + c.getName() + " ";
						clist += next;
					} else if (c.getAccess().equals(AccessLevel.PRIVATE)) {
						next = ChatColor.RED + c.getName() + " ";
						clist += next;
					}
				}
				sender.sendMessage(clist);
				return true;
			}
			if (action.equalsIgnoreCase("new")) { // newChannel
				if (args.length != 3) {
					sender.sendMessage(ChatColor.YELLOW
							+ "Create a new channel:\n\t/sc new <$channelname> <$sAccess> <$lAccess>\n\t"
							+ "sendAccess and listenAccess must be either PUBLIC or PRIVATE");
					return true;
				}
				ChatModule.getInstance().getChannelManager().createNewChannel(args[0],
						AccessLevel.valueOf(args[1]),
						user.getPlayerName(), ChannelType.valueOf(args[2])); // TODO better method
				return true;
			}
			if (action.equalsIgnoreCase("channel")) { // ChannelOwner/Mod
														// commands
				Channel c = user.getCurrent();
				String helpMod = ChatColor.YELLOW
						+ "Channel Mod commands:\n"
						+ "\t/sc channel kick <$user>\tKick a user from the channel\n"
						+ "\t/sc channel ban <$user>\tBan a user from the channel\n"
						+ "\t/sc channel setalias <$alias>\tSet an alias for the channel\n"
						+ "\t/sc channel rmalias\tRemove the channel alias\n"
						+ "\t/sc channel getListeners\tList all users currently listening to this channel";
				String helpOwner = ChatColor.YELLOW
						+ "Channel Owner commands:\n"
						+ "\t/sc channel mod <add/remove> <$user>\tAdd or remove a channelMod\n"
						+ "\t/sc channel <ban/unban> <$user>\t(Un)bans a user from the channel\n"
						+ "\t/sc disband\tDisband coming soon!";
				if (c.isMod(user) || isHelper) {
					if (arguments == null) {
						if (c.isMod(user) || isHelper) {
							sender.sendMessage(helpMod);
							return true;
						} else if (c.isOwner(user) || isMod) {
							sender.sendMessage(helpOwner);
							return true;
						} else {
							sender.sendMessage(ChatColor.BLACK +
									"There are mysteries into which it behooves one not to delve too deeply...");
							return true;
						}
					}
					if (args[0].equalsIgnoreCase("kick")) {
						c.kickUser(
								UserManager.getUserManager().getUser(
										args[1]), user);
						return true;
					}
					if (args[0].equalsIgnoreCase("ban")) {
						c.banUser(
								UserManager.getUserManager().getUser(
										args[1]), user);
						return true;
					}
					if (args[0].equalsIgnoreCase("getlisteners")) {
						String listenerList = ChatColor.YELLOW
								+ "Channel members: ";
						for (String s : c.getListening()) {
							SblockUser u = UserManager
									.getUserManager().getUser(s);
							if (u.getCurrent().equals(c)) {
								listenerList += ChatColor.GREEN
										+ u.getPlayerName() + " ";
							} else {
								listenerList += ChatColor.YELLOW
										+ u.getPlayerName() + " ";
							}
						}
						sender.sendMessage(listenerList);
						return true;
					}
					if (c.isOwner(user) || isMod) {
						if (args[0].equalsIgnoreCase("mod")) {
							if (args[1].equalsIgnoreCase("add")) {
								c.addMod(UserManager.getUserManager()
										.getUser(args[2]), user);
								return true;
							} else if (args[1].equalsIgnoreCase("remove")) {
								c.removeMod(UserManager.getUserManager()
										.getUser(args[2]), user);
								return true;
							} else {
								sender.sendMessage(helpOwner);
								return true;
							}
						}
						if (args[0].equalsIgnoreCase("unban")) {
							ChatModule.getInstance().getChannelManager()
									.getChannel(c.getName())
									.unbanUser(
											UserManager.getUserManager()
													.getUser(args[1]), user);
							return true;
						}
						if (args[0].equalsIgnoreCase("disband")) {
							sender.sendMessage(ChatColor.YELLOW
									+ "Command coming soon!"); // TODO
							return true;
						}
					} else {
						sender.sendMessage(ChatColor.BLACK +
								"There are mysteries into which it behooves one not to delve too deeply...");
						return true;
					}
				}
			}
			if (action.equalsIgnoreCase("global")) {
				if (isMod || sender.isOp()) {
					SblockUser victim = UserManager.getUserManager()
							.getUser(args[1]);
					if (args[0].equalsIgnoreCase("mute")) {
						victim.setMute(true);
						new ChatStorage().setGlobalMute(args[1]);
						return true;
					}
					if (args[0].equalsIgnoreCase("unmute")) {
						victim.setMute(false);
						new ChatStorage().removeGlobalMute(args[1]);
						return true;
					}
					if (args[0].equalsIgnoreCase("setnick")) {
						victim.setNick(args[2]);
						new ChatStorage().setGlobalNick(args[1], args[2]);
						return true;
					}
					if (args[0].equalsIgnoreCase("rmnick")) {
						victim.setNick(victim.getPlayerName());
						new ChatStorage().removeGlobalNick(args[1]);
						return true;
					}
				}
			}

			// Global powers
			// setcurrent //sc c <channelname>
			// addlistening //sc l <channelname>
			// removelistening //sc leave <channelname>
			// getlistening //sc list
			// newchannel //sc new <name, sAccess, lAccess>
			// listallchannels //sc listall
			// whois(limited) //sc whois

			// Channelowner powers //prereq: Channel must be current
			// mod //sc channel mod add <user>
			// demod //sc channel mod remove <user>
			// unban //sc channel unban <user>
			// disband //sc channel disband

			// Channelmod powers
			// kick //sc channel kick <player>
			// ban //sc channel ban <player>
			// setalias //sc channel alias set <alias>
			// rmalias //sc channel alias remove
			// getListeners //sc channel listeners

			// Mod powers
			// Mute //sc global mute <player>
			// unmute //sc global unmute <player>
			// setnick //sc global setnick <player> <nick>
			// rmnick //sc global rmnick <player>
		}
		
		
		return true;
	}
}
