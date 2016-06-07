package co.sblock.commands.admin;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import co.sblock.Sblock;
import co.sblock.commands.SblockAsynchronousCommand;
import co.sblock.utilities.PlayerLoader;

import net.minecraft.server.v1_9_R2.EntityPlayer;
import net.minecraft.server.v1_9_R2.WorldSettings;

import org.bukkit.craftbukkit.v1_9_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer;

/**
 * SblockCommand for forcing all users into the main world group for inventory management plugin
 * swapping purposes.
 * 
 * @author Jikoo
 */
public class DerspitRemovalCommand extends SblockAsynchronousCommand {

	public DerspitRemovalCommand(Sblock plugin) {
		super(plugin, "derspitremoval");
		this.setDescription("Unless you are me, do NOT use this command.");
		this.setUsage("Are you Adam? No? Stop before you seriously screw things up.");
		this.setPermission("sblock.ask.adam.before.touching");
		this.setPermissionLevel("horrorterror");
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (args.length < 1 || !args[0].equals("confirm")) {
			return false;
		}

		CraftWorld to = (CraftWorld) getPlugin().getServer().getWorlds().get(0);
		Location spawn = to.getSpawnLocation();

		int moved = 0;

		for (OfflinePlayer offline : getPlugin().getServer().getOfflinePlayers()) {
			if (offline.isOnline()) {
				// Skip, could cause decently severe desync
				continue;
			}
			// Don't use cached player - we need as fresh data as possible.
			Player player = PlayerLoader.getPlayer(getPlugin(), offline.getUniqueId(), false).getPlayer();
			if (player == null) {
				continue;
			}
			EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
			// No matter what, force survival. Survival inventories will be being loaded at the same time.
			entityPlayer.playerInteractManager.setGameMode(WorldSettings.EnumGamemode.SURVIVAL);
			World world = player.getWorld();
			if (world == null || !world.getName().contains("Earth")) {
				entityPlayer.world = to.getHandle();
				entityPlayer.setLocation(spawn.getX(), spawn.getY(), spawn.getZ(), spawn.getYaw(), spawn.getPitch());
				player.saveData();
				Bukkit.getConsoleSender().sendMessage("Moved " + player.getName() + " to Earth's spawn.");
				moved++;
			}
		}

		sender.sendMessage("Completed migration. Moved " + moved + " players to Earth's spawn.");
		return true;
	}

}
