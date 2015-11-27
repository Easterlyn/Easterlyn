package co.sblock.machines.type;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.ConfigurationSection;
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
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import co.sblock.Sblock;
import co.sblock.chat.Color;
import co.sblock.machines.Machines;
import co.sblock.machines.utilities.Direction;
import co.sblock.machines.utilities.Shape;
import co.sblock.users.User;
import co.sblock.users.Users;

/**
 * Framework for all Machine block assemblies.
 * 
 * @author Jikoo
 */
public abstract class Machine {

	private final Sblock plugin;
	private final Machines machines;
	private final Users users;
	private final Shape shape;

	/**
	 * Constructor for a Machine.
	 * 
	 * @param plugin the Plugin loading the Machine
	 * @param shape the in-world representation of the machine
	 */
	Machine(Sblock plugin, Machines machines, Shape shape) {
		this.plugin = plugin;
		this.machines = machines;
		this.users = plugin.getModule(Users.class);
		this.shape = shape;
	}

	/**
	 * Gets the Sblock instance loading this Machine.
	 * 
	 * @return the Sblock
	 */
	public Sblock getPlugin() {
		return this.plugin;
	}

	/**
	 * Gets the Machines instance loading this Machine.
	 * 
	 * @return the Machines
	 */
	public Machines getMachines() {
		return this.machines;
	}

	/**
	 * Gets the Users instance used to check User data.
	 * 
	 * @return the Users
	 */
	public Users getUsers() {
		return this.users;
	}

	/**
	 * Gets the Machine's shape.
	 * 
	 * @return the Shape of the Machine
	 */
	public Shape getShape() {
		return shape;
	}

	/**
	 * Checks if the Machine is free. Free machines can be broken by anyone, and do not yield any
	 * drops.
	 * 
	 * @return true if the Machine's cost is 0
	 */
	public boolean isFree() {
		return getCost() < 1;
	}

	/**
	 * Gets the grist cost for creating this Machine.
	 * 
	 * @return the cost
	 */
	public int getCost() {
		return 0;
	}

	/**
	 * Gets the key Location of a Machine from a ConfigurationSection.
	 * 
	 * @param storage the Machine's ConfigurationSection
	 * @return the Location
	 */
	public Location getKey(ConfigurationSection storage) {
		return Machines.locFromString(storage.getCurrentPath());
	}

	/**
	 * Gets the Direction of a Machine from a ConfigurationSection.
	 * 
	 * @param storage the Machine's ConfigurationSection
	 * @return the Direction
	 */
	public Direction getDirection(ConfigurationSection storage) {
		return Direction.valueOf(storage.getString("direction"));
	}

	/**
	 * Gets UUID of the owner of a Machine from a ConfigurationSection.
	 * 
	 * @param storage the Machine's ConfigurationSection
	 * @return the UUID
	 */
	public UUID getOwner(ConfigurationSection storage) {
		return UUID.fromString(storage.getString("owner"));
	}

	/**
	 * If a Player attempts to break a Machine, this condition must be met.
	 * <p>
	 * For most Machines, placing Player name is compared to the owner.
	 * 
	 * @param event the BlockPlaceEvent
	 * @return boolean
	 */
	public boolean meetsAdditionalBreakConditions(BlockBreakEvent event, ConfigurationSection storage) {
		return this.isFree() || event.getPlayer().hasPermission("sblock.denizen")
				|| event.getPlayer().getUniqueId().toString().equals(storage.getString("owner"));
	}

	/**
	 * Sets up the Machine Block configuration using a BlockPlaceEvent.
	 * 
	 * @param event the BlockPlaceEvent
	 * @return 
	 */
	public void assemble(BlockPlaceEvent event, ConfigurationSection storage) {
		Location key = getKey(storage);
		Direction direction = getDirection(storage);
		for (Location location : shape.getBuildLocations(key, direction).keySet()) {
			if (!location.equals(key) && (!location.getBlock().isEmpty()
					|| getMachines().isExploded(location.getBlock()))) {
				event.setCancelled(true);
				event.getPlayer().sendMessage(Color.BAD + "There isn't enough space to build this Machine here.");
				this.assemblyFailed(storage);
				return;
			}
		}
		this.assemble(key, direction, storage);
		User user = getUsers().getUser(event.getPlayer().getUniqueId());
		if (user.isServer() && user.getUUID().toString().equals(storage.getString("owner"))) {
			storage.set("owner", user.getClient().toString());
		}
	}

	/**
	 * Helper method for assembling the Machine.
	 */
	protected void assemble(Location key, Direction direction, ConfigurationSection storage) {
		HashMap<Location, MaterialData> buildData = shape.getBuildLocations(key, direction);
		for (Entry<Location, MaterialData> entry : buildData.entrySet()) {
			Block block = entry.getKey().getBlock();
			if (block.isEmpty()) {
				block.setType(entry.getValue().getItemType(), false);
			}
			block.getState().setData(entry.getValue());
		}
		this.triggerPostAssemble(buildData, storage);
	}

	/**
	 * For use when Machine would be affected by an explosion without CreeperHeal enabled.
	 * <p>
	 * Machines are very abusable in combination with CreeperHeal, so instead of flat out cancelling
	 * the event, making it rather difficult to do anything fun like drop a meteor on someone's
	 * computer, we'll hook it and play nice.
	 */
	public void reassemble(ConfigurationSection storage) {
		final Location key = getKey(storage);
		final HashMap<Location, ItemStack[]> invents = new HashMap<Location, ItemStack[]>();
		for (Location l : getMachines().getMachineBlocks(key)) {
			Block b = l.getBlock();
			if (b.getState() instanceof InventoryHolder) {
				InventoryHolder ih = (InventoryHolder) b.getState();
				invents.put(l, ih.getInventory().getContents().clone());
				ih.getInventory().clear();
			}
			b.setType(Material.AIR, false);
		}

		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			@Override
			public void run() {
				assemble(key, getDirection(storage), storage);
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
	public void remove(ConfigurationSection storage) {
		disable(storage);
		Location key = getKey(storage);
		for (Location l : getMachines().getMachineBlocks(key)) {
			l.getBlock().setType(Material.AIR);
		}
		key.getBlock().setType(Material.AIR);
		getMachines().deleteMachine(key);
	}

	/**
	 * Handles Machine deconstruction.
	 * 
	 * @param event the BlockBreakEvent
	 * 
	 * @return true if event should be cancelled
	 */
	public boolean handleBreak(BlockBreakEvent event, ConfigurationSection storage) {
		if (!meetsAdditionalBreakConditions(event, storage) && !event.getPlayer().hasPermission("sblock.denizen")) {
			return true;
		}
		if (event.getPlayer().getGameMode() == GameMode.SURVIVAL && !isFree()) {
			Location key = getKey(storage);
			key.getWorld().dropItemNaturally(key, getUniqueDrop());
		}
		remove(storage);
		return true;
	}

	/**
	 * Handles Block growth caused by the Machine's Block(s).
	 * 
	 * @param event the BlockGrowEvent
	 * 
	 * @return true if event should be cancelled
	 */
	public boolean handleGrow(BlockGrowEvent event, ConfigurationSection storage) {
		return true;
	}

	/**
	 * Handles fading of Blocks in the Machine.
	 * 
	 * @param event the BlockFadeEvent
	 * 
	 * @return true if event should be cancelled
	 */
	public boolean handleFade(BlockFadeEvent event, ConfigurationSection storage) {
		return true;
	}

	/**
	 * Handles ignition of Blocks in the Machine.
	 * 
	 * @param event the BlockIgniteEvent
	 * 
	 * @return true if event should be cancelled
	 */
	public boolean handleIgnite(BlockIgniteEvent event, ConfigurationSection storage) {
		return true;
	}

	/**
	 * Handles physics on Blocks in the Machine.
	 * 
	 * @param event the BlockPhysicsEvent
	 * 
	 * @return true if event should be cancelled
	 */
	public boolean handlePhysics(BlockPhysicsEvent event, ConfigurationSection storage) {
		return true;
	}

	/**
	 * Handles piston pushes on Blocks in the Machine.
	 * 
	 * @param event the BlockPistonExtendEvent
	 * 
	 * @return true if event should be cancelled
	 */
	public boolean handlePush(BlockPistonExtendEvent event, ConfigurationSection storage) {
		return true;
	}

	/**
	 * Handles piston pulls on Blocks in the Machine.
	 * 
	 * @param event the BlockPistonRetractEvent
	 * 
	 * @return true if event should be cancelled
	 */
	public boolean handlePull(BlockPistonRetractEvent event, ConfigurationSection storage) {
		return true;
	}

	/**
	 * Handles Block spread caused by the Machine's Block(s).
	 * 
	 * @param event the BlockSpreadEvent
	 * 
	 * @return true if event should be cancelled
	 */
	public boolean handleSpread(BlockSpreadEvent event, ConfigurationSection storage) {
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
	public boolean handleInteract(PlayerInteractEvent event, ConfigurationSection storage) {
		for (Location l : getMachines().getMachineBlocks(getKey(storage))) {
			if (getMachines().isExploded(l.getBlock())) {
				event.getPlayer().sendMessage(Color.BAD + "This machine is too damaged to use!");
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
	public boolean handleHopperMoveItem(InventoryMoveItemEvent event, ConfigurationSection storage) {
		return true;
	}

	/**
	 * Handles hoppers in the Machine picking up items.
	 * 
	 * @param event the InventoryPickupItemEvent
	 * 
	 * @return true if the event should be cancelled
	 */
	public boolean handleHopperPickupItem(InventoryPickupItemEvent event, ConfigurationSection storage) {
		return true;
	}

	/**
	 * Handles Inventory clicks for the Machine.
	 * <p>
	 * N.B.: Due to the way Machines that are detected via InventoryHolder status are detected,
	 * Machines that directly extend InventoryHolder may be passed a null storage
	 * ConfigurationSection.
	 * 
	 * @param event the InventoryClickEvent
	 * 
	 * @return true if event should be cancelled
	 */
	public boolean handleClick(InventoryClickEvent event, ConfigurationSection storage) {
		event.setResult(Result.DENY);
		return true;
	}

	/**
	 * Triggers postAssemble method on a synchronous 0 tick delay.
	 */
	private void triggerPostAssemble(HashMap<Location, MaterialData> buildData, ConfigurationSection storage) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			@Override
			public void run() {
				for (Entry<Location, MaterialData> entry : buildData.entrySet()) {
					Block block = entry.getKey().getBlock();
					if (block.getType() != entry.getValue().getItemType()) {
						block.setType(entry.getValue().getItemType(), false);
					}
					BlockState state = block.getState();
					if (!state.getData().equals(entry.getValue())) {
						state.setData(entry.getValue());
					}
					state.update(true);
				}
			}
		});
	}

	/**
	 * Removes this Machine's listing on a synchronous 0 tick delay.
	 */
	protected void assemblyFailed(ConfigurationSection storage) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			@Override
			public void run() {
				disable(storage);
				getMachines().deleteMachine(getKey(storage));
			}
		});
	}

	/**
	 * Used to trigger cleanup when a Machine listing is removed on plugin disable.
	 */
	public void disable(ConfigurationSection storage) {
		// Most machines do not do anything when disabled.
	}

	/**
	 * Gets the unique drop for this Machine.
	 * 
	 * @return the unique drop.
	 */
	public abstract ItemStack getUniqueDrop();

}
