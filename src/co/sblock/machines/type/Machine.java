package co.sblock.machines.type;

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
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;

import co.sblock.Sblock;
import co.sblock.data.SblockData;
import co.sblock.machines.MachineManager;
import co.sblock.machines.SblockMachines;
import co.sblock.machines.utilities.Direction;
import co.sblock.machines.utilities.MachineType;
import co.sblock.machines.utilities.Shape;
import co.sblock.users.User;

/**
 * Framework for all Machine block assemblies.
 * 
 * @author Jikoo
 */
public abstract class Machine {

	/** The Location of the key Block of the Machine. */
	protected Location key;

	/** The owner and any other data stored for the Machine. */
	protected String owner, data;

	/** Machine facing */
	protected Direction direction;

	/** The Shape of the Machine */
	protected Shape shape;

	/** A Set of all Locations defined as part of the Machine. */
	protected HashMap<Location, MaterialData> blocks;

	/**
	 * @param key the Location of the key Block of this Machine
	 * @param owner the UUID of the Machine's owner
	 * @param direction the facing direction of the Machine
	 */
	Machine(Location key, String owner, Direction direction) {
		this.key = key;
		this.owner = owner;
		this.direction = direction;
		this.shape = new Shape(key.clone());
		shape.addBlock(new Vector(0, 0, 0), this.getType().getUniqueDrop().getData());
	}

	/**
	 * @param key the Location of the key Block of this Machine
	 * @param owner any additional data stored in this machine, e.g. creator name
	 */
	Machine(Location key, String owner) {
		this(key, owner, Direction.NORTH);
	}

	/**
	 * Gets the Location of the key Block of this Machine.
	 * 
	 * @return the Location
	 */
	public Location getKey() {
		return key;
	}

	/**
	 * Gets the Location of the key Block of this Machine in String form.
	 * <p>
	 * Primarily intended for saving to database.
	 * 
	 * @return the Location String
	 */
	public String getLocationString() {
		return key.getWorld().getName() + "," + key.getBlockX() + "," + key.getBlockY() + "," + key.getBlockZ();
	}

	/**
	 * Gets the owner of this Machine.
	 * 
	 * @return the UUID of the owner of the Machine
	 */
	public String getOwner() {
		return this.owner;
	}

	/**
	 * Sets additional data stored for the Machine.
	 * 
	 * @param data the data to set
	 */
	public void setData(String data) {
		this.data = data;
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
	 * For most Machines, placing Player name is compared to the owner.
	 * 
	 * @param event the BlockPlaceEvent
	 * @return boolean
	 */
	public boolean meetsAdditionalBreakConditions(BlockBreakEvent event) {
		return this.getType().isFree() || event.getPlayer().hasPermission("group.denizen")
				|| owner.equals(event.getPlayer().getUniqueId().toString());
	}

	/**
	 * Sets up the Machine Block configuration using a BlockPlaceEvent.
	 * 
	 * @param event the BlockPlaceEvent
	 * @return 
	 */
	public void assemble(BlockPlaceEvent event) {
		for (Location l : blocks.keySet()) {
			if (!l.equals(this.key) && (!l.getBlock().isEmpty()
					|| MachineManager.getManager().isExploded(l.getBlock()))) {
				event.setCancelled(true);
				event.getPlayer().sendMessage(ChatColor.RED + "There isn't enough space to build this Machine here.");
				this.assemblyFailed();
				return;
			}
		}
		this.assemble();
		User u = User.getUser(event.getPlayer().getUniqueId());
		if (u != null && u.isServer() && data.equals(u.getPlayerName())) {
			this.data = u.getClient().toString();
		}
		SblockData.getDB().saveMachine(this);
	}

	/**
	 * Helper method for assembling the Machine.
	 */
	@SuppressWarnings("deprecation")
	protected void assemble() {
		for (Entry<Location, MaterialData> e : blocks.entrySet()) {
			Block b = e.getKey().getBlock();
			b.setType(e.getValue().getItemType());
			b.setData(e.getValue().getData());
		}
		this.triggerPostAssemble();
	}

	/**
	 * For use when Machine would be affected by an explosion without CreeperHeal enabled.
	 * <p>
	 * Machines are very abusable in combination with CreeperHeal, so instead of flat out cancelling
	 * the event, making it rather difficult to do anything fun like drop a meteor on someone's
	 * computer, we'll hook it and play nice.
	 */
	@SuppressWarnings("deprecation")
	public void reassemble() {
		final HashMap<Location, ItemStack[]> invents = new HashMap<Location, ItemStack[]>();
		for (Location l : blocks.keySet()) {
			Block b = l.getBlock();
			if (b.getState() instanceof InventoryHolder) {
				InventoryHolder ih = (InventoryHolder) b.getState();
				invents.put(l, ih.getInventory().getContents().clone());
				ih.getInventory().clear();
			}
			b.setTypeId(0, false);
		}

		Bukkit.getScheduler().scheduleSyncDelayedTask(Sblock.getInstance(), new Runnable() {
			@Override
			public void run() {
				assemble();
				for (Entry<Location, ItemStack[]> e : invents.entrySet()) {
					try {
						((InventoryHolder) e.getKey().getBlock().getState()).getInventory().setContents(e.getValue());
					} catch (ClassCastException e1) {
						for (ItemStack is : e.getValue()) {
							key.getWorld().dropItem(key, is);
						}
					}
				}
			}
		});
	}

	/**
	 * Removes this machine's blocks and listing.
	 */
	public void remove() {
		disable();
		for (Location l : this.getLocations()) {
			l.getBlock().setType(Material.AIR);
		}
		getKey().getBlock().setType(Material.AIR);
		SblockMachines.getMachines().getManager().removeMachineListing(getKey());
	}

	/**
	 * Gets a Set of all non-key Locations of Blocks in a Machine.
	 * 
	 * @return the Set
	 */
	public Set<Location> getLocations() {
		if (blocks == null) {
			blocks = shape.getBuildLocations(getFacingDirection());
		}
		return blocks.keySet();
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
		return direction;
	}

	/**
	 * Handles Machine deconstruction.
	 * 
	 * @param event the BlockBreakEvent
	 * 
	 * @return true if event should be cancelled
	 */
	public boolean handleBreak(BlockBreakEvent event) {
		if (!meetsAdditionalBreakConditions(event) && !event.getPlayer().hasPermission("group.denizen")) {
			return true;
		}
		if (event.getPlayer().getGameMode() == GameMode.SURVIVAL && !getType().isFree()) {
			getKey().getWorld().dropItemNaturally(getKey(), getType().getUniqueDrop());
		}
		remove();
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
	public boolean handleInteract(PlayerInteractEvent event) {
		for (Location l : this.blocks.keySet()) {
			if (MachineManager.getManager().isExploded(l.getBlock())) {
				event.getPlayer().sendMessage(ChatColor.RED + "This machine is too damaged to use!");
				return true;
			}
		}
		return false;
	}

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
	 * Handles Furnaces in the Machine smelting items.
	 * 
	 * @param event the FurnaceSmeltEvent
	 * 
	 * @return true if the event should be cancelled
	 */
	public boolean handleFurnaceSmelt(FurnaceSmeltEvent event) {
		return true;
	}

	/**
	 * Triggers postAssemble method on a synchronous 0 tick delay.
	 */
	private void triggerPostAssemble() {
		Bukkit.getScheduler().scheduleSyncDelayedTask(Sblock.getInstance(), new Runnable() {
			@SuppressWarnings("deprecation")
			public void run() {
				MaterialData m = blocks.get(key);
				Block b = key.getBlock();
				b.setType(m.getItemType());
				b.setData(m.getData());
			}
		});
	}

	/**
	 * Removes this Machine's listing on a synchronous 0 tick delay.
	 */
	protected void assemblyFailed() {
		Bukkit.getScheduler().scheduleSyncDelayedTask(Sblock.getInstance(), new Runnable() {
			public void run() {
				disable();
				SblockMachines.getMachines().getManager().removeMachineListing(key);
			}
		});
	}

	/**
	 * Used to trigger cleanup when a Machine listing is removed on plugin disable.
	 */
	public void disable() {
		// Most machines do not do anything when disabled.
	}
}
