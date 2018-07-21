package com.easterlyn.machines.type;

import com.easterlyn.Easterlyn;
import com.easterlyn.chat.Language;
import com.easterlyn.events.Events;
import com.easterlyn.machines.Machines;
import com.easterlyn.machines.utilities.Direction;
import com.easterlyn.machines.utilities.Shape;
import com.easterlyn.users.Users;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

/**
 * Framework for all Machine block assemblies.
 *
 * @author Jikoo
 */
public abstract class Machine {

	private final Easterlyn plugin;
	private final Language lang;
	private final Machines machines;
	private final Users users;
	private final Shape shape;
	private final String name;

	/**
	 * Constructor for a Machine.
	 *
	 * @param plugin the Plugin loading the Machine
	 * @param machines the Machines instance loading the Machine
	 * @param shape the in-world representation of the machine
	 */
	protected Machine(Easterlyn plugin, Machines machines, Shape shape, String name) {
		this.plugin = plugin;
		this.lang = plugin.getModule(Language.class);
		this.machines = machines;
		this.users = plugin.getModule(Users.class);
		this.shape = shape;
		this.name = name;
	}

	/**
	 * Gets the name of this Machine.
	 *
	 * @return the name
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Gets the Easterlyn instance loading this Machine.
	 *
	 * @return the Easterlyn
	 */
	protected Easterlyn getPlugin() {
		return this.plugin;
	}

	/**
	 * Gets the Language instance.
	 *
	 * @return the Language
	 */
	protected Language getLang() {
		return this.lang;
	}

	/**
	 * Gets the Machines instance loading this Machine.
	 *
	 * @return the Machines
	 */
	protected Machines getMachines() {
		return this.machines;
	}

	/**
	 * Gets the Users instance used to check User data.
	 *
	 * @return the Users
	 */
	protected Users getUsers() {
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
	 * Gets the mana cost for creating this Machine.
	 *
	 * @return the cost
	 */
	public int getCost() {
		return 0;
	}

	/**
	 * Gets the key Location of a Machine from a ConfigurationSection.
	 *
	 * @param storage the ConfigurationSection of data specific to the given Machine
	 *
	 * @return the Location
	 */
	public Location getKey(ConfigurationSection storage) {
		return Machines.locFromPath(storage.getCurrentPath());
	}

	/**
	 * Gets the Direction of a Machine from a ConfigurationSection.
	 *
	 * @param storage the ConfigurationSection of data specific to the given Machine
	 *
	 * @return the Direction
	 */
	public Direction getDirection(ConfigurationSection storage) {
		return Direction.valueOf(storage.getString("direction"));
	}

	/**
	 * Gets UUID of the owner of a Machine from a ConfigurationSection.
	 *
	 * @param storage the ConfigurationSection of data specific to the given Machine
	 *
	 * @return the UUID
	 */
	public UUID getOwner(ConfigurationSection storage) {
		return UUID.fromString(storage.getString("owner"));
	}

	/**
	 * Sets up the Machine Block configuration using a BlockPlaceEvent.
	 *
	 * @param player the Player assembling the Machine
	 * @param storage the ConfigurationSection of data specific to the given Machine
	 *
	 * @return false if assembly failed due to space requirements
	 */
	public boolean assemble(Player player, ConfigurationSection storage) {
		Location key = getKey(storage);
		Direction direction = getDirection(storage);
		for (Location location : shape.getBuildLocations(key, direction).keySet()) {
			if (!location.equals(key) && (!location.getBlock().isEmpty()
					|| getMachines().isExploded(location.getBlock()))
					|| location.getBlockY() > 255) {
				player.sendMessage(Language.getColor("bad") + "There isn't enough space to build this Machine here.");
				this.assemblyFailed(storage);
				return false;
			}
		}
		this.assemble(key, direction, storage);
		return true;
	}

	/**
	 * Helper method for assembling the Machine.
	 *
	 * @param key the key Location of the Machine
	 * @param direction the facing of the Machine
	 * @param storage the ConfigurationSection of data specific to the given Machine
	 */
	protected void assemble(Location key, Direction direction, ConfigurationSection storage) {
		HashMap<Location, Shape.MaterialDataValue> buildData = shape.getBuildLocations(key, direction);
		for (Entry<Location, Shape.MaterialDataValue> entry : buildData.entrySet()) {
			if (key.equals(entry.getKey())) {
				// Key cannot be set instantly, it must be set on a delay
				// A cancelled BlockPlaceEvent results in the block being restored to its previous state.
				// Additionally, keys with tile entities (Looking at you, Transportalizer)
				// can cause damage which is not fixed until the tile entity is accessed.
				assembleKeyLater(key, entry.getValue(), storage);
				continue;
			}
			entry.getValue().build(entry.getKey().getBlock());
		}
	}


	/**
	 * Helper method for assembling the Machine. Key cannot be set instantly, it must be set on a
	 * delay. A cancelled BlockPlaceEvent results in the block being restored to its previous state.
	 * Additionally, keys with tile entities can cause damage which is not fixed until the tile
	 * entity is accessed (generally manually).
	 *
	 * @param key the key Location of the Machine
	 * @param data the MaterialData the key is supposed to be set to
	 * @param storage the ConfigurationSection of data specific to the given Machine
	 */
	private void assembleKeyLater(Location key, Shape.MaterialDataValue data, ConfigurationSection storage) {
		new BukkitRunnable() {
			@Override
			public void run() {
				Block block = key.getBlock();
				if (!getMachines().isMachine(block)) {
					return;
				}
				data.build(block);
			}
		}.runTask(getPlugin());
	}

	/**
	 * For use when Machine would be affected by an explosion without CreeperHeal enabled.
	 * <p>
	 * Machines are very abusable in combination with CreeperHeal, so instead of flat out cancelling
	 * the event, making it rather difficult to do anything fun like drop a meteor on someone's
	 * computer, we'll hook it and play nice.
	 *
	 * @param storage the ConfigurationSection of data specific to the given Machine
	 */
	public void reassemble(ConfigurationSection storage) {
		final Location key = getKey(storage);
		final HashMap<Location, ItemStack[]> invents = new HashMap<>();
		for (Location l : getMachines().getMachineBlocks(key)) {
			Block b = l.getBlock();
			if (b.getState() instanceof InventoryHolder) {
				InventoryHolder ih = (InventoryHolder) b.getState();
				invents.put(l, ih.getInventory().getContents().clone());
				ih.getInventory().clear();
			}
			b.setType(Material.AIR, false);
		}

		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
			assemble(key, getDirection(storage), storage);
			for (Entry<Location, ItemStack[]> e : invents.entrySet()) {
				try {
					((InventoryHolder) e.getKey().getBlock().getState()).getInventory().setContents(e.getValue());
				} catch (ClassCastException e1) {
					for (ItemStack is : e.getValue()) {
						if (is != null) {
							key.getWorld().dropItem(key, is);
						}
					}
				}
			}
		});
	}

	/**
	 * Removes this machine's blocks and listing.
	 *
	 * @param storage the ConfigurationSection of data specific to the given Machine
	 */
	public void remove(ConfigurationSection storage) {
		disable(storage);
		Location key = getKey(storage);
		for (Location l : this.getMachines().getMachineBlocks(key)) {
			l.getBlock().setType(Material.AIR, false);
			plugin.getModule(Events.class).getBlockUpdateManager().queueBlock(l.getBlock());
		}
		key.getBlock().setType(Material.AIR);
		getMachines().deleteMachine(key);
	}

	/**
	 * Handles Machine deconstruction.
	 *
	 * @param event the BlockBreakEvent
	 * @param storage the ConfigurationSection of data specific to the given Machine
	 *
	 * @return true if event should be cancelled
	 */
	public boolean handleBreak(BlockBreakEvent event, ConfigurationSection storage) {
		if (event.getPlayer().getGameMode() == GameMode.SURVIVAL) {
			Location key = getKey(storage);
			key.getWorld().dropItemNaturally(key.add(0.5, 0, 0.5), getUniqueDrop());
		}
		remove(storage);
		return true;
	}

	/**
	 * Handles Block growth caused by the Machine's Block(s).
	 *
	 * @param event the BlockGrowEvent
	 * @param storage the ConfigurationSection of data specific to the given Machine
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
	 * @param storage the ConfigurationSection of data specific to the given Machine
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
	 * @param storage the ConfigurationSection of data specific to the given Machine
	 *
	 * @return true if event should be cancelled
	 */
	public boolean handleIgnite(BlockIgniteEvent event, ConfigurationSection storage) {
		return true;
	}

	/**
	 * Handles piston pushes on Blocks in the Machine.
	 *
	 * @param event the BlockPistonExtendEvent
	 * @param storage the ConfigurationSection of data specific to the given Machine
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
	 * @param storage the ConfigurationSection of data specific to the given Machine
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
	 * @param storage the ConfigurationSection of data specific to the given Machine
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
	 * @param storage the ConfigurationSection of data specific to the given Machine
	 *
	 * @return true if event should be cancelled
	 */
	public boolean handleInteract(PlayerInteractEvent event, ConfigurationSection storage) {
		for (Location l : getMachines().getMachineBlocks(getKey(storage))) {
			if (getMachines().isExploded(l.getBlock())) {
				event.getPlayer().sendMessage(Language.getColor("bad") + "This machine is too damaged to use!");
				return true;
			}
		}
		return false;
	}

	/**
	 * Handles hopper interaction with Blocks in the Machine.
	 *
	 * @param event the InventoryMoveItemEvent
	 * @param storage the ConfigurationSection of data specific to the given Machine
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
	 * @param storage the ConfigurationSection of data specific to the given Machine
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
	 * @param storage the ConfigurationSection of data specific to the given Machine
	 *
	 * @return true if event should be cancelled
	 */
	public boolean handleClick(InventoryClickEvent event, ConfigurationSection storage) {
		event.setResult(Result.DENY);
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
	 * @param storage the ConfigurationSection of data specific to the given Machine
	 *
	 * @return true if event should be cancelled
	 */
	public boolean handleClick(InventoryDragEvent event, ConfigurationSection storage) {
		event.setResult(Result.DENY);
		return true;
	}

	/**
	 * Handles Inventory opening for blocks inside the Machine. This is not fired by interaction
	 * opening custom inventories handled by the MachineInventoryTracker.
	 *
	 * @param event the InventoryOpenEvent
	 * @param storage the ConfigurationSection of data specific to the given Machine
	 */
	public boolean handleOpen(InventoryOpenEvent event, ConfigurationSection storage) {
		return false;
	}

	/**
	 * Handles Blocks in the Machine being changed by liquids.
	 *
	 * @param event the BlockFromToEvent
	 * @param storage the ConfigurationSection of data specific to the given Machine
	 *
	 * @return true if the event should be cancelled
	 */
	public boolean handleFromTo(BlockFromToEvent event, ConfigurationSection storage) {
		return true;
	}

	/**
	 * Removes this Machine's listing on a synchronous 0 tick delay.
	 */
	private void assemblyFailed(ConfigurationSection storage) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
			disable(storage);
			getMachines().deleteMachine(getKey(storage));
		});
	}

	/**
	 * Used to trigger additional setup when a machine listing is loaded.
	 */
	public void enable(ConfigurationSection storage) {
		// Most machines do not do anything when enabled.
	}

	/**
	 * Used to trigger cleanup when a Machine listing is removed.
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
