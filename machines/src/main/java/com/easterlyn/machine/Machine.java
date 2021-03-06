package com.easterlyn.machine;

import com.easterlyn.EasterlynCore;
import com.easterlyn.EasterlynMachines;
import com.github.jikoo.planarwrappers.collections.BlockMap;
import com.github.jikoo.planarwrappers.world.Direction;
import com.github.jikoo.planarwrappers.world.Shape;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Event.Result;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.inventory.TradeSelectEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Framework for all Machine block assemblies.
 *
 * @author Jikoo
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public abstract class Machine {

  private final EasterlynMachines machines;
  private final Shape shape;
  private final String name;

  /**
   * Constructor for a Machine.
   *
   * @param shape the in-world representation of the machine
   * @param name the friendly name of the Machine
   */
  public Machine(@NotNull EasterlynMachines machines, @NotNull Shape shape, @NotNull String name) {
    this.machines = machines;
    this.shape = shape;
    this.name = name;
  }

  /**
   * Gets the plugin instance for this Machine.
   *
   * @return the plugin
   */
  public @NotNull EasterlynMachines getMachines() {
    return machines;
  }

  /**
   * Gets the name of this Machine.
   *
   * @return the name
   */
  public @NotNull String getName() {
    return this.name;
  }

  /**
   * Gets the Machine's shape.
   *
   * @return the Shape of the Machine
   */
  public @NotNull Shape getShape() {
    return shape;
  }

  /**
   * Gets the mana cost of the key item of this machine.
   *
   * @return the cost
   */
  public double getCost() {
    return Double.POSITIVE_INFINITY;
  }

  /**
   * Gets the key Location of a Machine from a ConfigurationSection.
   *
   * @param storage the ConfigurationSection of data specific to the given Machine
   * @return the Location
   */
  public @NotNull Location getKey(@NotNull ConfigurationSection storage) {
    return EasterlynMachines.locFromPath(Objects.requireNonNull(storage.getCurrentPath()));
  }

  /**
   * Gets the Direction of a Machine from a ConfigurationSection.
   *
   * @param storage the ConfigurationSection of data specific to the given Machine
   * @return the Direction
   */
  public @NotNull Direction getDirection(@NotNull ConfigurationSection storage) {
    return Direction.safeValue(storage.getString("direction"));
  }

  /**
   * Gets UUID of the owner of a Machine from a ConfigurationSection.
   *
   * @param storage the ConfigurationSection of data specific to the given Machine
   * @return the UUID
   */
  public @NotNull UUID getOwner(@NotNull ConfigurationSection storage) {
    return UUID.fromString(Objects.requireNonNull(storage.getString("owner")));
  }

  /**
   * Sets up the Machine Block configuration using a BlockPlaceEvent.
   *
   * @param storage the ConfigurationSection of data specific to the given Machine
   * @return false if assembly failed due to space requirements
   */
  public boolean failedAssembly(@NotNull ConfigurationSection storage) {
    Location key = getKey(storage);
    Direction direction = getDirection(storage);
    for (Block block : shape.getBuildLocations(key.getBlock(), direction).keySet()) {
      if (!block.getLocation().equals(key)
              && (!block.isEmpty() || getMachines().isExplodedMachine(block))
          || block.getY() > 255) {
        this.assemblyFailed(storage);
        return true;
      }
    }
    this.assemble(key.getBlock(), direction, storage);
    return false;
  }

  /**
   * Helper method for assembling the Machine.
   *
   * @param key the key Location of the Machine
   * @param direction the facing of the Machine
   * @param storage the ConfigurationSection of data specific to the given Machine
   */
  private void assemble(
      @NotNull Block key, @NotNull Direction direction, @NotNull ConfigurationSection storage) {
    Map<Block, BlockData> buildData = shape.getBuildLocations(key, direction);
    for (Entry<Block, BlockData> entry : buildData.entrySet()) {
      if (key.equals(entry.getKey())) {
        // Key cannot be set instantly, it must be set on a delay
        // A cancelled BlockPlaceEvent results in the block being restored to its previous state.
        // Additionally, keys with tile entities (Looking at you, Transportalizer)
        // can cause damage which is not fixed until the tile entity is accessed.
        assembleKeyLater(key, entry.getValue());
        continue;
      }
      entry.getKey().setBlockData(entry.getValue());
    }
  }

  /**
   * Helper method for assembling the Machine. Key cannot be set instantly, it must be set on a
   * delay. A cancelled BlockPlaceEvent results in the block being restored to its previous state.
   * Additionally, keys with tile entities can cause damage which is not fixed until the tile entity
   * is accessed (generally manually).
   *
   * @param key the key Location of the Machine
   * @param data the BlockData the key is supposed to be set to
   */
  private void assembleKeyLater(@NotNull Block key, @NotNull BlockData data) {
    new BukkitRunnable() {
      @Override
      public void run() {
        if (!getMachines().isMachine(key)) {
          return;
        }
        key.setBlockData(data);
      }
    }.runTask(getMachines());
  }

  /**
   * For use when Machine would be affected by an explosion without CreeperHeal enabled.
   *
   * <p>Machines can be abused in combination with CreeperHeal, so instead of flat out cancelling
   * the event, making it rather difficult to do anything fun like drop a meteor on someone's
   * computer, we'll hook it and play nice.
   *
   * @param storage the ConfigurationSection of data specific to the given Machine
   */
  public void reassemble(@NotNull ConfigurationSection storage) {
    final Location key = getKey(storage);
    final BlockMap<ItemStack[]> inventories = new BlockMap<>();
    for (Block block : getMachines().getMachineBlocks(key.getBlock())) {
      if (block.getState() instanceof InventoryHolder) {
        InventoryHolder inventoryHolder = (InventoryHolder) block.getState();
        inventories.put(block, inventoryHolder.getInventory().getContents());
        inventoryHolder.getInventory().clear();
      }
      block.setType(Material.AIR, false);
    }

    Bukkit.getScheduler()
        .scheduleSyncDelayedTask(
            getMachines(),
            () -> {
              assemble(key.getBlock(), getDirection(storage), storage);
              for (Entry<Block, ItemStack[]> e : inventories.entrySet()) {
                try {
                  ((InventoryHolder) e.getKey().getState())
                      .getInventory()
                      .setContents(e.getValue());
                } catch (Exception e1) {
                  for (ItemStack is : e.getValue()) {
                    if (key.getWorld() != null && is != null && is.getType() != Material.AIR) {
                      key.getWorld().dropItem(key, is);
                    }
                  }
                }
              }
            });
  }

  /** Removes this Machine's listing on a synchronous 0 tick delay. */
  private void assemblyFailed(@NotNull ConfigurationSection storage) {
    Bukkit.getScheduler()
        .scheduleSyncDelayedTask(
            getMachines(),
            () -> {
              disable(storage);
              getMachines().removeMachineFromMemory(getKey(storage).getBlock());
            });
  }

  /** Used to trigger additional setup when a machine listing is loaded. */
  public void enable(@NotNull ConfigurationSection storage) {
    // Most machines do not do anything when enabled.
  }

  /** Used to trigger cleanup when a Machine listing is removed. */
  public void disable(@NotNull ConfigurationSection storage) {
    // Most machines do not do anything when disabled.
  }

  /**
   * Removes this machine's blocks and listing.
   *
   * @param storage the ConfigurationSection of data specific to the given Machine
   */
  void remove(@NotNull ConfigurationSection storage) {
    disable(storage);
    Block key = getKey(storage).getBlock();
    for (Block block : this.getMachines().getMachineBlocks(key)) {
      RegisteredServiceProvider<EasterlynCore> easterlynRSP =
          Bukkit.getServer().getServicesManager().getRegistration(EasterlynCore.class);
      block.setType(Material.AIR, easterlynRSP == null);
      if (easterlynRSP != null) {
        easterlynRSP.getProvider().getBlockUpdateManager().queueBlock(block);
      }
    }
    key.setType(Material.AIR);
    getMachines().removeMachineFromMemory(key);
  }

  /**
   * Handles Machine deconstruction.
   *
   * @param event the BlockBreakEvent
   * @param storage the ConfigurationSection of data specific to the given Machine
   */
  public void handleBreak(@NotNull BlockBreakEvent event, @NotNull ConfigurationSection storage) {
    if (event.getPlayer().getGameMode() == GameMode.SURVIVAL) {
      Location key = getKey(storage);
      if (key.getWorld() != null) {
        key.getWorld().dropItemNaturally(key.add(0.5, 0, 0.5), getUniqueDrop());
      }
    }
    remove(storage);
    event.setCancelled(true);
  }

  /**
   * Handles fading of Blocks in the Machine.
   *
   * @param event the BlockFadeEvent
   * @param storage the ConfigurationSection of data specific to the given Machine
   */
  public void handleFade(@NotNull BlockFadeEvent event, @NotNull ConfigurationSection storage) {
    event.setCancelled(true);
  }

  /**
   * Handles Blocks in the Machine being changed by liquids.
   *
   * @param event the BlockFromToEvent
   * @param storage the ConfigurationSection of data specific to the given Machine
   */
  public void handleFromTo(@NotNull BlockFromToEvent event, @NotNull ConfigurationSection storage) {
    event.setCancelled(true);
  }

  /**
   * Handles ignition of Blocks in the Machine.
   *
   * @param event the BlockIgniteEvent
   * @param storage the ConfigurationSection of data specific to the given Machine
   */
  public void handleIgnite(@NotNull BlockIgniteEvent event, @NotNull ConfigurationSection storage) {
    event.setCancelled(true);
  }

  /**
   * Handles Block spread caused by the Machine's Block(s).
   *
   * @param event the BlockSpreadEvent
   * @param storage the ConfigurationSection of data specific to the given Machine
   */
  public void handleSpread(@NotNull BlockSpreadEvent event, @NotNull ConfigurationSection storage) {
    event.setCancelled(true);
  }

  /**
   * Handles Player interaction with Blocks in the Machine.
   *
   * <p>This should be handled based on type of Machine.
   *
   * @param event the PlayerInteractEvent
   * @param storage the ConfigurationSection of data specific to the given Machine
   */
  public void handleInteract(
      @NotNull PlayerInteractEvent event, @NotNull ConfigurationSection storage) {
    for (Block block : getMachines().getMachineBlocks(getKey(storage).getBlock())) {
      if (getMachines().isExplodedMachine(block)) {
        event.getPlayer().sendMessage("This machine is too damaged to use!");
        event.setUseInteractedBlock(Result.DENY);
      }
    }
  }

  /**
   * Handles hopper interaction with Blocks in the Machine.
   *
   * @param event the InventoryMoveItemEvent
   * @param storage the ConfigurationSection of data specific to the given Machine
   */
  public void handleHopperMoveItem(
      @NotNull InventoryMoveItemEvent event, @NotNull ConfigurationSection storage) {
    event.setCancelled(true);
  }

  /**
   * Handles hoppers in the Machine picking up items.
   *
   * @param event the InventoryPickupItemEvent
   * @param storage the ConfigurationSection of data specific to the given Machine
   */
  public void handleHopperPickupItem(
      @NotNull InventoryPickupItemEvent event, @NotNull ConfigurationSection storage) {
    event.setCancelled(true);
  }

  /**
   * Handles Inventory clicks for the Machine.
   *
   * <p>N.B.: Due to the way Machines that are detected via InventoryHolder status are detected,
   * Machines that directly extend InventoryHolder may be passed a null storage
   * ConfigurationSection.
   *
   * @param event the InventoryClickEvent
   * @param storage the ConfigurationSection of data specific to the given Machine
   */
  public void handleClick(
      @NotNull InventoryClickEvent event, @Nullable ConfigurationSection storage) {
    event.setResult(Result.DENY);
    event.setCancelled(true);
  }

  /**
   * Handles Inventory clicks for the Machine.
   *
   * <p>N.B.: Due to the way Machines that are detected via InventoryHolder status are detected,
   * Machines that directly extend InventoryHolder may be passed a null storage
   * ConfigurationSection.
   *
   * @param event the InventoryClickEvent
   * @param storage the ConfigurationSection of data specific to the given Machine
   */
  public void handleDrag(
      @NotNull InventoryDragEvent event, @Nullable ConfigurationSection storage) {
    event.setResult(Result.DENY);
    event.setCancelled(true);
  }

  /**
   * Handles Inventory opening for blocks inside the Machine.
   *
   * @param event the InventoryOpenEvent
   * @param storage the ConfigurationSection of data specific to the given Machine
   */
  public void handleOpen(
      @NotNull InventoryOpenEvent event, @Nullable ConfigurationSection storage) {}

  /**
   * Handles Inventory closing for blocks inside the Machine.
   *
   * @param event the InventoryCloseEvent
   * @param storage the ConfigurationSection of data specific to the given Machine
   */
  public void handleClose(
      @NotNull InventoryCloseEvent event, @Nullable ConfigurationSection storage) {}

  /**
   * Gets the unique drop for this Machine.
   *
   * @return the unique drop.
   */
  public @NotNull abstract ItemStack getUniqueDrop();

  /**
   * Handles selection of a trade offered by the Machine.
   *
   * @param event the TradeSelectEvent
   * @param storage the ConfigurationSection of data specific to the given Machine
   */
  public void selectTrade(TradeSelectEvent event, @NotNull ConfigurationSection storage) {}
}
