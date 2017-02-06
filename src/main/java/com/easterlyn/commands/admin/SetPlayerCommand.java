package com.easterlyn.commands.admin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.easterlyn.Easterlyn;
import com.easterlyn.commands.EasterlynAsynchronousCommand;
import com.easterlyn.users.User;
import com.easterlyn.users.UserAspect;
import com.easterlyn.users.UserClass;
import com.easterlyn.users.UserRank;
import com.easterlyn.users.Users;

import com.google.common.collect.ImmutableList;

import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;

/**
 * EasterlynCommand for setting User data.
 * 
 * @author Jikoo
 */
public class SetPlayerCommand extends EasterlynAsynchronousCommand {

	private final Users users;
	private final String[] primaryArgs;

	public SetPlayerCommand(Easterlyn plugin) {
		super(plugin, "setplayer");
		this.setDescription("Set player data manually.");
		this.setPermissionLevel(UserRank.DENIZEN);
		this.setUsage("/setplayer <playername> <class|aspect|land|dream|prevloc|progression> <value>");
		this.users = plugin.getModule(Users.class);
		primaryArgs = new String[] {"class", "aspect", "land", "dream", "prevloc", "progression"};
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (args == null || args.length < 3) {
			return false;
		}
		UUID uuid = getUniqueId(args[0]);
		if (uuid == null) {
			sender.sendMessage(getLang().getValue("core.error.invalidUser").replace("{PLAYER}", args[0]));
			return true;
		}
		User user = users.getUser(uuid);
		args[1] = args[1].toLowerCase();
		if (args[1].equals("class")) {
			user.setUserClass(UserClass.getClass(args[2]));
		} else if (args[1].equals("aspect")) {
			user.setUserAspect(UserAspect.getAspect(args[2]));
		} else if (args[1].replaceAll("m(edium_?)?planet", "land").equals("land")) {
			user.setMediumPlanet(args[2]);
		} else if (args[1].replaceAll("d(ream_?)?planet", "dream").equals("dream")) {
			user.setDreamPlanet(args[2]);
		} else if (args[1].equals("prevloc")) {
			user.setPreviousLocation(user.getPlayer().getLocation());
		} else {
			return false;
		}
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args)
			throws IllegalArgumentException {
		if (!sender.hasPermission(this.getPermission())) {
			return ImmutableList.of();
		}
		if (args.length < 2) {
			return super.tabComplete(sender, alias, args);
		}
		ArrayList<String> matches = new ArrayList<>();
		args[1] = args[1].toLowerCase();
		if (args.length == 2) {
			for (String argument : primaryArgs) {
				if (argument.startsWith(args[1])) {
					matches.add(argument);
				}
			}
			return matches;
		}
		if (args[1].equals("class") && args.length == 3) {
			args[2] = args[2].toUpperCase();
			for (UserClass userclass : UserClass.values()) {
				if (StringUtil.startsWithIgnoreCase(userclass.getDisplayName(), args[2])) {
					matches.add(userclass.getDisplayName());
				}
			}
			return matches;
		}
		if (args[1].equals("aspect") && args.length == 3) {
			args[2] = args[2].toUpperCase();
			for (UserAspect aspect : UserAspect.values()) {
				if (StringUtil.startsWithIgnoreCase(aspect.getDisplayName(), args[2])) {
					matches.add(aspect.getDisplayName());
				}
			}
			return matches;
		}
		if (args[1].equals("land") && args.length == 3) {
			args[2] = args[2].toUpperCase();
			for (String land : new String[]{"LOFAF", "LOHAC", "LOLAR", "LOWAS"}) {
				if (land.startsWith(args[2])) {
					matches.add(land);
				}
			}
			return matches;
		}
		if (args[1].equals("dream") && args.length == 3) {
			args[2] = args[2].toUpperCase();
			for (String dream : new String[]{"PROSPIT", "DERSE"}) {
				if (dream.startsWith(args[2])) {
					matches.add(dream);
				}
			}
			return matches;
		}
		return ImmutableList.of();
	}
}
