package co.sblock.commands.admin;

import java.io.File;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import co.sblock.Sblock;
import co.sblock.commands.SblockCommand;
import co.sblock.users.User;
import co.sblock.users.Users;

/**
 * SblockCommand featuring a SuperBan along with several protection removal commands.
 * 
 * @author Jikoo
 */
public class UltraBanCommand extends SblockCommand {

	private final Users users;

	public UltraBanCommand(Sblock plugin) {
		super(plugin, "ultraban");
		this.users = plugin.getModule(Users.class);
		this.setPermissionLevel("horrorterror");
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (!((Sblock) getPlugin()).getCommandMap().getCommand("sban").execute(sender, label, args)) {
			// sban will return its own usage failure, no need to double message.
			return true;
		}

		Player player = Bukkit.getPlayer(args[0]);
		if (player != null) {
			User victim = users.getUser(player.getUniqueId());
			File folder = new File(getPlugin().getDataFolder(), "users");
			if (folder.exists()) {
				File file = new File(folder, victim.getUUID().toString() + ".yml");
				if (file.exists()) {
					file.delete();
				}
			}
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lwc admin purge " + player.getUniqueId());
		}
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lwc admin purge " + args[0]);
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "deleteallclaims " + args[0]);
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "shopkeeper remove " + args[0]);
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "shopkeeper confirm");
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args)
			throws IllegalArgumentException {
		if (!sender.hasPermission(this.getPermission()) || args.length != 1) {
			return com.google.common.collect.ImmutableList.of();
		}
		return super.tabComplete(sender, alias, args);
	}
}
