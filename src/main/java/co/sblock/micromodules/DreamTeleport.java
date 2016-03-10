package co.sblock.micromodules;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.material.Bed;
import org.bukkit.scheduler.BukkitTask;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.wrappers.BlockPosition;

import co.sblock.Sblock;
import co.sblock.events.packets.SleepTeleport;
import co.sblock.events.packets.WrapperPlayServerAnimation;
import co.sblock.events.packets.WrapperPlayServerBed;
import co.sblock.module.Module;
import co.sblock.users.Users;
import co.sblock.utilities.TextUtils;

/**
 * 
 * 
 * @author Jikoo
 */
public class DreamTeleport extends Module {

	private final HashMap<UUID, BukkitTask> sleep;
	private Users users;

	public DreamTeleport(Sblock plugin) {
		super(plugin);
		this.sleep = new HashMap<>();
	}

	@Override
	protected void onEnable() {
		this.users = getPlugin().getModule(Users.class);
	}

	@Override
	protected void onDisable() {
		for (Entry<UUID, BukkitTask> entry : this.sleep.entrySet()) {
			entry.getValue().cancel();
			Player player = Bukkit.getPlayer(entry.getKey());
			if (player != null) {
				fakeWakeUp(player);
			}
		}
	}

	/**
	 * Gets the HashMap of all teleports scheduled for players by UUID.
	 */
	public HashMap<UUID, BukkitTask> getSleepTasks() {
		return sleep;
	}

	/**
	 * Initiates sleep teleportation.
	 * 
	 * @param player the Player interacting
	 * @param block the Block clicked
	 * @param bed the Bed clicked
	 * 
	 * @return true if teleportation has been initiated and interaction should be cancelled
	 */
	@SuppressWarnings("deprecation")
	public boolean handleBedInteract(Player player, Block block, Bed bed) {
		if (!this.isEnabled()) {
			return false;
		}
		Location head;
		if (bed.isHeadOfBed()) {
			head = block.getLocation();
		} else {
			// bed.getFacing does not return correctly in most cases.
			BlockFace relative;
			switch (bed.getData()) {
			case 0:
				relative = BlockFace.SOUTH;
				break;
			case 1:
				relative = BlockFace.WEST;
				break;
			case 2:
				relative = BlockFace.EAST;
				break;
			case 3:
				relative = BlockFace.NORTH;
				break;
			default:
				relative = BlockFace.SELF;
				break;
			}
			head = block.getRelative(relative).getLocation();
		}

		switch (users.getUser(player.getUniqueId()).getCurrentRegion()) {
		case EARTH:
		case PROSPIT:
		case LOFAF:
		case LOHAC:
		case LOLAR:
		case LOWAS:
		case DERSE:
			fakeSleepDream(player, head);
			return true;
		default:
			break;
		}
		return false;
	}

	/**
	 * Sends a Player a fake packet for starting sleeping and schedules them to
	 * be teleported to their DreamPlanet.
	 * 
	 * @param p the Player
	 * @param bed the Location of the bed to sleep in
	 */
	private void fakeSleepDream(Player p, Location bed) {
		if (!this.isEnabled()) {
			return;
		}
		WrapperPlayServerBed packet = new WrapperPlayServerBed();
		packet.setEntityId(p.getEntityId());
		packet.setLocation(new BlockPosition(bed.getBlockX(), bed.getBlockY(), bed.getBlockZ()));

		try {
			ProtocolLibrary.getProtocolManager().sendServerPacket(p, packet.getHandle());
		} catch (InvocationTargetException e) {
			getLogger().warning(TextUtils.getTrace(e));
		}
		sleep.put(p.getUniqueId(), new SleepTeleport(getPlugin(), p.getUniqueId()).runTaskLater(getPlugin(), 100L));
	}

	/**
	 * Sends a Player a fake packet for waking up.
	 * 
	 * @param p the Player
	 */
	public void fakeWakeUp(Player p) {
		WrapperPlayServerAnimation packet = new WrapperPlayServerAnimation();
		packet.setEntityId(p.getEntityId());
		packet.setAnimation(2); // http://wiki.vg/Protocol#Animation_2

		try {
			ProtocolLibrary.getProtocolManager().sendServerPacket(p, packet.getHandle());
		} catch (InvocationTargetException e) {
			getLogger().warning(TextUtils.getTrace(e));
		}

		BukkitTask task = sleep.remove(p.getUniqueId());
		if (task != null) {
			task.cancel();
		}
	}

	@Override
	public boolean isRequired() {
		return false;
	}

	@Override
	public String getName() {
		return "DreamTeleport";
	}

}
