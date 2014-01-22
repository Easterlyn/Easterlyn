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
import co.sblock.Sblock.Machines.SblockMachines;

/**
 * Framework for all Machine block assemblies.
 * 
 * @author Jikoo
 */
public abstract class Machine {

	/** The Location of the key Block of the Machine. */
	protected Location l;

	/** Additional data stored in the Machine, e.g. creator name. */
	private String data;

	/** Machine facing */
	protected Direction d;

	/** The Shape of the Machine */
	protected Shape shape;

	/** A Set of all Locations defined as part of the Machine. */
	protected HashMap<Location, ItemStack> blocks;

	/**
	 * @param l the Location of the key Block of this Machine
	 * @param data any additional data stored in this machine, e.g. creator name
	 * @param d the facing direction of the Machine
	 */
	Machine(Location l, String data, Direction d) {
		this.l = l;
		this.data = data;
		this.d = d;
		this.shape = new Shape(l.clone());
	}

	/**
	 * @param l the Location of the key Block of this Machine
	 * @param data any additional data stored in this machine, e.g. creator name
	 */
	Machine(Location l, String data) {
		this.l = l;
		this.data = data;
		this.d = Direction.NORTH;
		this.shape = new Shape(l.clone());
	}

	/**
	 * Gets the Location of the key Block of this Machine.
	 * 
	 * @return the Location
	 */
	public Location getKey() {
		return l;
	}

	/**
	 * Gets the Location of the key Block of this Machine in String form.
	 * <p>
	 * Primarily intended for saving to database.
	 * 
	 * @return the Location String
	 */
	public String getLocationString() {
		return l.getWorld().getName() + "," + l.getBlockX() + "," + l.getBlockY() + "," + l.getBlockZ();
	}

	/**
	 * Gets any additional data used to identify this Machine.
	 * 
	 * @return String
	 */
	public String getData() {
		return data;
	}

	/**
	 * If a Player attempts to break a Machine, this condition must be met.
	 * <p>
	 * For most Machines, placing Player name is compared to data.
	 * 
	 * @param event the BlockPlaceEvent
	 * @return boolean
	 */
	public boolean meetsAdditionalBreakConditions(BlockBreakEvent event) {
		return getData().equals(event.getPlayer().getName()) || event.getPlayer().hasPermission("group.denizen");
	}

	/**
	 * Sets up the Machine Block configuration using a BlockPlaceEvent.
	 * 
	 * @param event the BlockPlaceEvent
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
	 * Gets a Set of all non-key Locations of Blocks in a Machine.
	 * 
	 * @return the Set
	 */
	public Set<Location> getLocations() {
		if (blocks == null) {
			return shape.getBuildLocations(getFacingDirection()).keySet();
		} else {
			return blocks.keySet();
		}
	}

	/**
	 * Gets the MachineType.
	 * 
	 * @return the MachineType
	 */
	public abstract MachineType getType();

	/**
	 * Gets the Direction the machine was placed in.
	 * 
	 * @return the Direction
	 */
	public Direction getFacingDirection() {
		return d;
	}

	/**
	 * Handles Machine deconstruction.
	 * 
	 * @param event the BlockBreakEvent
	 * 
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
			SblockMachines.getMachines().getManager().removeMachineListing(getKey());
		}
		return true;
	}

	/**
	 * Handles Block growth caused by the Machine's Block(s).
	 * 
	 * @param event the BlockGrowEvent
	 * 
	 * @return true if event should be cancelled
	 */
	public boolean handleGrow(BlockGrowEvent event) {
		return true;
	}

	/**
	 * Handles fading of Blocks in the Machine.
	 * 
	 * @param event the BlockFadeEvent
	 * 
	 * @return true if event should be cancelled
	 */
	public boolean handleFade(BlockFadeEvent event) {
		return true;
	}

	/**
	 * Handles ignition of Blocks in the Machine.
	 * 
	 * @param event the BlockIgniteEvent
	 * 
	 * @return true if event should be cancelled
	 */
	public boolean handleIgnite(BlockIgniteEvent event) {
		return true;
	}

	/**
	 * Handles physics on Blocks in the Machine.
	 * 
	 * @param event the BlockPhysicsEvent
	 * 
	 * @return true if event should be cancelled
	 */
	public boolean handlePhysics(BlockPhysicsEvent event) {
		return true;
	}

	/**
	 * Handles piston pushes on Blocks in the Machine.
	 * 
	 * @param event the BlockPistonExtendEvent
	 * 
	 * @return true if event should be cancelled
	 */
	public boolean handlePush(BlockPistonExtendEvent event) {
		return true;
	}

	/**
	 * Handles piston pulls on Blocks in the Machine.
	 * 
	 * @param event the BlockPistonRetractEvent
	 * 
	 * @return true if event should be cancelled
	 */
	public boolean handlePull(BlockPistonRetractEvent event) {
		return true;
	}

	/**
	 * Handles Block spread caused by the Machine's Block(s).
	 * 
	 * @param event the BlockSpreadEvent
	 * 
	 * @return true if event should be cancelled
	 */
	public boolean handleSpread(BlockSpreadEvent event) {
		return true;
	}

	/**
	 * Handles Player interaction with Blocks in the Machine.
	 * <p>
	 * This should be handled based on type of Machine.
	 * 
	 * @param event the PlayerInteractEvent
	 * 
	 * @return true if event should be cancelled
	 */
	public abstract boolean handleInteract(PlayerInteractEvent event);

	/**
	 * Handles hopper interaction with Blocks in the Machine.
	 * 
	 * @param event the InventoryMoveItemEvent
	 * 
	 * @return true if event should be cancelled
	 */
	public boolean handleHopper(InventoryMoveItemEvent event) {
		return true;
	}

	/**
	 * Handles Inventory clicks for the Machine.
	 * 
	 * @param event the InventoryClickEvent
	 * 
	 * @return true if event should be cancelled
	 */
	public boolean handleClick(InventoryClickEvent event) {
		event.setResult(Result.DENY);
		return true;
	}

	/**
	 * Handles Furnaces in the Machine consuming fuel.
	 * 
	 * @param event the FurnaceBurnEvent
	 * 
	 * @return true if event should be cancelled
	 */
	public boolean handleBurnFuel(FurnaceBurnEvent event) {
		return true;
	}

	/**
	 * Triggers any events to occur on completion of Machine construction.
	 * <p>
	 * Primarily intended for changing the key block Material.
	 */
	protected abstract void postAssemble();

	/**
	 * Triggers postAssemble method on a synchronous 0 tick delay.
	 */
	private void triggerPostAssemble() {
		Bukkit.getScheduler().scheduleSyncDelayedTask(Sblock.getInstance(), new Runnable() {
			public void run() {
				postAssemble();
			}
		});
	}

	/**
	 * Removes this Machine's listing on a synchronous 0 tick delay.
	 */
	protected void assemblyFailed() {
		Bukkit.getScheduler().scheduleSyncDelayedTask(Sblock.getInstance(), new Runnable() {
			public void run() {
				SblockMachines.getMachines().getManager().removeMachineListing(l);
			}
		});
	}
}
