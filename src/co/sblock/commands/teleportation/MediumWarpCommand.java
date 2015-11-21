package co.sblock.commands.teleportation;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import co.sblock.chat.Color;
import co.sblock.commands.SblockCommand;
import co.sblock.users.OfflineUser;
import co.sblock.users.ProgressionState;
import co.sblock.users.Region;
import co.sblock.users.Users;

/**
 * SblockCommand for teleporting a user who has completed Entry to the selected Medium planet.
 * <p>
 * If the user is already in the Medium, they can travel for free. If they are not, a nether star
 * must be present (for removal) from their inventory.
 * 
 * @author Jikoo
 */
public class MediumWarpCommand extends SblockCommand {

	public MediumWarpCommand() {
		super("mediumwarp");
		this.setDescription("Teleports a player to the Medium planet of choice. Removes cost if required.");
		this.setUsage("/mediumwarp <medium planet> <player>");
		this.setPermissionLevel("felt");
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (args.length < 2) {
			return false;
		}
		Player player = Bukkit.getPlayer(args[1]);
		if (player == null) {
			sender.sendMessage(Color.BAD + "Invalid player.");
			return false;
		}
		Region medium = Region.getRegion(args[0]);
		if (!medium.isMedium()) {
			sender.sendMessage(Color.BAD + "Region is not in the Medium.");
			return false;
		}
		OfflineUser user = Users.getGuaranteedUser(player.getUniqueId());
		if (user.getProgression().ordinal() < ProgressionState.ENTRY.ordinal()) {
			sender.sendMessage(Color.BAD + "Entry not completed.");
			return false;
		}
		if (!Users.getGuaranteedUser(player.getUniqueId()).getCurrentRegion().isMedium()) {
			if (player.getInventory().removeItem(new ItemStack(Material.NETHER_STAR)).size() > 0) {
				player.sendMessage(Color.BAD + "You must have a nether star to fuel your journey to the Medium.");
				return false;
			}
		}
		World world = Bukkit.getWorld(medium.getWorldName());
		if (world == null) {
			sender.sendMessage(Color.BAD + "Nonexistant world " + medium.getWorldName());
			return false;
		}
		player.teleport(world.getSpawnLocation());
		return true;
	}

}