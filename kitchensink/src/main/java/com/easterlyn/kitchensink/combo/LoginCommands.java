package com.easterlyn.kitchensink.combo;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Flags;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import com.easterlyn.EasterlynCore;
import com.easterlyn.command.CommandRank;
import com.easterlyn.command.CoreContexts;
import com.easterlyn.user.User;
import com.easterlyn.user.UserRank;
import com.easterlyn.util.PermissionUtil;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

@CommandAlias("onlogin")
@Description("Manage commands run on login.")
@CommandPermission("easterlyn.command.onlogin")
@CommandRank(UserRank.MODERATOR)
public class LoginCommands extends BaseCommand implements Listener {

	private static final String ONLOGIN = "kitchensink.onlogin";

	@Dependency
	EasterlynCore core;

	public LoginCommands() {
		PermissionUtil.addParent("easterlyn.command.onlogin.other", UserRank.MODERATOR.getPermission());
		PermissionUtil.addParent("easterlyn.command.onlogin.more", UserRank.MODERATOR.getPermission());
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onPlayerJoin(PlayerJoinEvent event) {
		User user = core.getUserManager().getUser(event.getPlayer().getUniqueId());
		user.getStorage().getStringList(ONLOGIN).forEach(string -> event.getPlayer().chat(string));
	}

	@Subcommand("list")
	@Description("List login commands.")
	@Syntax("/onlogin list")
	@CommandCompletion("@player")
	public void list(@Flags(CoreContexts.ONLINE_WITH_PERM) User user) {
		CommandIssuer issuer = getCurrentCommandIssuer();
		List<String> list = user.getStorage().getStringList(ONLOGIN);
		if (list.isEmpty()) {
			issuer.sendMessage("No login commands set!");
		}
		for (int i = 0; i < list.size(); ++i) {
			issuer.sendMessage((i + 1) + ": " + list.get(i));
		}
	}

	@Subcommand("add")
	@Description("Add a login command.")
	@Syntax("/onlogin add /<command text>")
	@CommandCompletion("@player")
	public void add(@Flags(CoreContexts.ONLINE_WITH_PERM) User user, String command) {
		CommandIssuer issuer = getCurrentCommandIssuer();
		List<String> list = new ArrayList<>(user.getStorage().getStringList(ONLOGIN));
		if (!issuer.hasPermission("easterlyn.command.onlogin.more") && list.size() >= 2 || list.size() >= 5) {
			issuer.sendMessage("Too many login commands!");
			return;
		}
		if (command == null || command.length() < 2 || command.charAt(0) != '/'
				|| command.matches("/(\\w+:)?me .*")) {
			issuer.sendMessage("Please specify a command to add.");
			return;
		}
		list.add(command);
		user.getStorage().set(ONLOGIN, list);
		issuer.sendMessage("Added " + command);
	}

	@Subcommand("remove")
	@Description("Remove a login command.")
	@Syntax("/onlogin remove <index>")
	@CommandCompletion("@player|@integer @integer")
	public void remove(@Flags(CoreContexts.ONLINE_WITH_PERM) User user, int commandIndex) {
		CommandIssuer issuer = getCurrentCommandIssuer();
		List<String> list = new ArrayList<>(user.getStorage().getStringList(ONLOGIN));
		if (list.size() == 0) {
			issuer.sendMessage("No login commands are set!");
			return;
		}
		if (commandIndex < 1 || commandIndex > list.size()) {
			issuer.sendMessage("Please specify a number between 1 and " + list.size());
			return;
		}
		String command = list.remove(commandIndex - 1);
		user.getStorage().set(ONLOGIN, list);
		issuer.sendMessage("Removed " + command);
	}

}
