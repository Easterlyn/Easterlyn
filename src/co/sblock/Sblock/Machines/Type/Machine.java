package co.sblock.Sblock.Machines.Type;

import java.util.HashMap;
import java.util.Set;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.Event.Result;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import co.sblock.Sblock.Sblock;
import co.sblock.Sblock.Machines.MachineModule;

/**
 * Framework for all "machine" block assemblies.
 * 
 * @author Jikoo
 * 
 */
public abstract class Machine {

	/** The <code>Location</code> of the key <code>Block</code> of the <code>Machine</code>. */
	protected Location l;
	/** Additional data stored in the <code>Machine</code>, e.g. creator name. */
	private String data;
	/** <code>Machine</code> facing */
	protected Direction d;
	/** The <code>Shape</code> of the <code>Machine</code> */
	protected Shape shape;
	/** A <code>Set</code> of all <code>Locations</code> defined as part of the <code>Machine</code>. */
	protected HashMap<Location, ItemStack> blocks;

	/**
	 * @param l
	 *            the <code>Location</code> of the key <code>Block</code> of
	 *            this <code>Machine</code>
	 * @param data
	 *            any additional data stored in this machine, e.g. creator name
	 * @param d
	 *            the facing direction of the <code>Machine</code>
	 */
	Machine(Location l, String data, Direction d) {
		this.l = l;
		this.data = data;
		this.d = d;
		this.shape = new Shape(l.clone());
	}

	/**
	 * @param l
	 *            the <code>Location</code> of the key <code>Block</code> of
	 *            this <code>Machine</code>
	 * @param data
	 *            any additional data stored in this machine, e.g. creator name
	 */
	Machine(Location l, String data) {
		this.l = l;
		this.data = data;
		this.d = Direction.NORTH;
		this.shape = new Shape(l.clone());
	}

	/**
	 * Gets the <code>Location</code> of the key <code>Block</code> of this
	 * <code>Machine</code>.
	 * 
	 * @return the <code>Location</code>
	 */
	public Location getKey() {
		return l;
	}

	/**
	 * Gets the <code>Location</code> of the key <code>Block</code> of this
	 * <code>Machine</code> in <code>String</code> form.
	 * <p>
	 * Primarily intended for saving to database.
	 * 
	 * @return the <code>Location</code> <code>String</code>
	 */
	public String getLocationString() {
		return l.getWorld().getName() + "," + l.getBlockX() + "," + l.getBlockY() + "," + l.getBlockZ();
	}

	/**
	 * Gets any additional data used to identify this <code>Machine</code>.
	 * 
	 * @return <code>String</code>
	 */
	public String getData() {
		return data;
	}

	/**
	 * If a <code>Player</code> attempts to break a <code>Machine</code>, this condition must be met.
	 * <p>
	 * For most <code>Machine</code>s, <code>Player</code> name is compared to data.
	 * 
	 * @param event
	 *            the <code>BlockPlaceEvent</code>
	 * @return boolean
	 */
	public boolean meetsAdditionalBreakConditions(BlockBreakEvent event) {
		return getData().equals(event.getPlayer().getName()) || event.getPlayer().hasPermission("group.denizen");
	}

	/**
	 * Sets up the <code>Machine</code> <code>Block</code> configuration using a
	 * <code>BlockPlaceEvent</code>.
	 * 
	 * @param event
	 *            the <code>BlockPlaceEvent</code>
	 */
	@SuppressWarnings("deprecation")
	public void assemble(BlockPlaceEvent event) {
		for (Location l : blocks.keySet()) {
			if (!l.getBlock().isEmpty()) {
				event.setCancelled(true);
				event.getPlayer().sendMessage(ChatColor.RED + "There isn't enough space to build this Machine here.");
				this.assemblyFailed();
				return;
			}
		}
		for (Entry<Location, ItemStack> e : blocks.entrySet()) {
			Block b = e.getKey().getBlock();
			b.setType(e.getValue().getType());
			b.setData(e.getValue().getData().getData());
		}
		this.triggerPostAssemble();
	}

	/**
	 * Gets a <code>Set</code> of all non-key <code>Location</code>s of
	 * <code>Block</code>s in a <code>Machine</code>.
	 * 
	 * @return the <code>Set<Location></code>
	 */
	public Set<Location> getLocations() {
		if (blocks == null) {
			return shape.getBuildLocations(getFacingDirection()).keySet();
		} else {
			return blocks.keySet();
		}
	}

	/**
	 * Gets the <code>MachineType</code>.
	 * 
	 * @return the <code>MachineType</code>
	 */
	public abstract MachineType getType();

	/**
	 * Gets the <code>Direction</code> the machine was placed in.
	 * 
	 * @return the <code>Direction</code>
	 */
	public Direction getFacingDirection() {
		return d;
	}

	public void setDirection(Direction d) {
		this.d = d;
	}

	/**
	 * Handles <code>Machine</code> deconstruction.
	 * 
	 * @param event
	 *            the <code>BlockBreakEvent</code>
	 * @return true if event should be cancelled
	 */
	public boolean handleBreak(BlockBreakEvent event) {
		if (meetsAdditionalBreakConditions(event) || event.getPlayer().hasPermission("group.denizen")) {
			if (event.getPlayer().getGameMode() == GameMode.SURVIVAL) {
				getKey().getWorld().dropItemNaturally(getKey(), getType().getUniqueDrop());
			}
			for (Location l : this.getLocations()) {
				l.getBlock().setType(Material.AIR);
			}
			getKey().getBlock().setType(Material.AIR);
			MachineModule.getInstance().getManager().removeMachineListing(getKey());
		}
		return true;
	}

	/**
	 * Handles <code>Block</code> growth caused by the <code>Machine</code>'s
	 * <code>Block</code>(s).
	 * 
	 * @param event
	 *            the <code>BlockGrowEvent</code>
	 * @return true if event should be cancelled
	 */
	public boolean handleGrow(BlockGrowEvent event) {
		return true;
	}

	/**
	 * Handles fading of <code>Block</code>s in the <code>Machine</code>.
	 * 
	 * @param event
	 *            the <code>BlockFadeEvent</code>
	 * @return true if event should be cancelled
	 */
	public boolean handleFade(BlockFadeEvent event) {
		return true;
	}

	/**
	 * Handles ignition of <code>Block</code>s in the <code>Machine</code>.
	 * 
	 * @param event
	 *            the <code>BlockIgniteEvent</code>
	 * @return true if event should be cancelled
	 */
	public boolean handleIgnite(BlockIgniteEvent event) {
		return true;
	}

	/**
	 * Handles physics on <code>Block</code>s in the <code>Machine</code>.
	 * 
	 * @param event
	 *            the <code>BlockPhysicsEvent</code>
	 * @return true if event should be cancelled
	 */
	public boolean handlePhysics(BlockPhysicsEvent event) {
		return true;
	}

	/**
	 * Handles piston pushes on <code>Block</code>s in the <code>Machine</code>.
	 * 
	 * @param event
	 *            the <code>BlockPistonExtendEvent</code>
	 * @return true if event should be cancelled
	 */
	public boolean handlePush(BlockPistonExtendEvent event) {
		return true;
	}

	/**
	 * Handles piston pulls on <code>Block</code>s in the <code>Machine</code>.
	 * 
	 * @param event
	 *            the <code>BlockPistonRetractEvent</code>
	 * @return true if event should be cancelled
	 */
	public boolean handlePull(BlockPistonRetractEvent event) {
		return true;
	}

	/**
	 * Handles <code>Block</code> spread caused by the <code>Machine</code>'s
	 * <code>Block</code>(s).
	 * 
	 * @param event
	 *            the <code>BlockSpreadEvent</code>
	 * @return true if event should be cancelled
	 */
	public boolean handleSpread(BlockSpreadEvent event) {
		return true;
	}

	/**
	 * Handles <code>Player</code> interaction with <code>Block</code>s in the
	 * <code>Machine</code>.
	 * <p>
	 * This should be handled based on type of <code>Machine</code>.
	 * 
	 * @param event
	 *            the <code>PlayerInteractEvent</code>
	 * @return true if event should be cancelled
	 */
	public abstract boolean handleInteract(PlayerInteractEvent event);

	/**
	 * Handles hopper interaction with <code>Block</code>s in the <code>Machine</code>.
	 * 
	 * @param event
	 *            the <code>InventoryMoveItemEvent</code>
	 * @return true if event should be cancelled
	 */
	public boolean handleHopper(InventoryMoveItemEvent event) {
		return true;
	}

	/**
	 * Handles <code>Inventory</code> clicks for the <code>Machine</code>.
	 * 
	 * @param event
	 *            the <code>InventoryClickEvent</code>
	 * @return true if event should be cancelled
	 */
	public boolean handleClick(InventoryClickEvent event) {
		event.setResult(Result.DENY);
		return true;
	}

	/**
	 * Handles Furnaces in the <code>Machine</code> consuming fuel.
	 * 
	 * @param event
	 *            the <code>FurnaceBurnEvent</code>
	 * @return true if event should be cancelled
	 */
	public boolean handleBurnFuel(FurnaceBurnEvent event) {
		return true;
	}

	/**
	 * Triggers any events to occur on completion of <code>Machine</code> construction.
	 * <p>
	 * Primarily intended for changing the key block <code>Material</code>.
	 */
	protected abstract void postAssemble();

	/**
	 * Triggers postAssemble method on a synchronous 0 tick delay.
	 */
	private void triggerPostAssemble() {
		Bukkit.getScheduler().scheduleSyncDelayedTask(Sblock.getInstance(),
				new Runnable() {
					public void run() {
						postAssemble();
					}
				});
	}

	/**
	 * Removes this Machine's listing on a synchronous 0 tick delay.
	 */
	protected void assemblyFailed() {
		Bukkit.getScheduler().scheduleSyncDelayedTask(Sblock.getInstance(),
				new Runnable() {
					public void run() {
						MachineModule.getInstance().getManager().removeMachineListing(l);
					}
				});
	}
}
