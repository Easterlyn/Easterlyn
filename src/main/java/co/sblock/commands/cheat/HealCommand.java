package co.sblock.commands.cheat;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import com.google.common.collect.ImmutableList;

import co.sblock.Sblock;
import co.sblock.chat.Language;
import co.sblock.commands.SblockCommand;

/**
 * 
 * 
 * @author Jikoo
 */
public class HealCommand extends SblockCommand {

	public HealCommand(Sblock plugin) {
		super(plugin, "heal");
		this.setAliases("eat", "feed");
		this.setDescription("Heal yourself or another player.");
		this.setPermissionLevel("felt");
		this.setUsage("/<heal|feed> [player]");
		Permission permission;
		try {
			permission = new Permission("sblock.command.heal.other", PermissionDefault.OP);
			Bukkit.getPluginManager().addPermission(permission);
		} catch (IllegalArgumentException e) {
			permission = Bukkit.getPluginManager().getPermission("sblock.command.heal.other");
			permission.setDefault(PermissionDefault.OP);
		}
		permission.addParent("sblock.command.*", true).recalculatePermissibles();
		permission.addParent("sblock.denizen", true).recalculatePermissibles();
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player) && (!sender.hasPermission("sblock.command.heal.other")
				|| args.length < 1)) {
			return false;
		}
		if (args.length == 0 || !sender.hasPermission("sblock.command.heal.other")) {
			heal((Player) sender, label);
			sender.sendMessage(Language.getColor("good") + "All patched up!");
			return true;
		}
		List<Player> players = Bukkit.matchPlayer(args[0]);
		if (players.size() == 0) {
			return false;
		}
		heal(players.get(0), label);
		sender.sendMessage(Language.getColor("good") + "All patched up!");
		return true;
	}

	private void heal(Player player, String label) {
		if (label.equalsIgnoreCase("heal")) {
			player.setHealth(player.getMaxHealth());
		}
		player.setFoodLevel(20);
		player.setSaturation(20);
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args)
			throws IllegalArgumentException {
		if (!sender.hasPermission(this.getPermission()) || args.length > 1
				|| !sender.hasPermission("sblock.command.heal.other")) {
			return ImmutableList.of();
		}
		return super.tabComplete(sender, alias, args);
	}

}
