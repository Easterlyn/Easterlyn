package com.easterlyn;

import com.easterlyn.event.ReportableEvent;
import com.easterlyn.machine.Machine;
import com.easterlyn.plugin.EasterlynPlugin;
import com.easterlyn.util.BlockUtil;
import com.easterlyn.util.inventory.ItemUtil;
import com.github.jikoo.planarwrappers.collections.BlockMap;
import com.github.jikoo.planarwrappers.collections.BlockMultimap;
import com.github.jikoo.planarwrappers.event.Event;
import com.github.jikoo.planarwrappers.tuple.Pair;
import com.github.jikoo.planarwrappers.util.Coords;
import com.github.jikoo.planarwrappers.world.Direction;
import com.nitnelave.CreeperHeal.config.CreeperConfig;
import com.nitnelave.CreeperHeal.events.CHBlockHealEvent;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.inventory.TradeSelectEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantInventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;

public class EasterlynMachines extends EasterlynPlugin {

  private final BlockMap<Block> blocksToKeys = new BlockMap<>();
  private final BlockMultimap<Block> keysToBlocks = new BlockMultimap<>();
  private final Map<String, Machine> nameRegistry = new HashMap<>();
  private final Map<ItemStack, Machine> iconRegistry = new HashMap<>();
  private final BlockMap<Boolean> exploded = new BlockMap<>();
  private final Map<Merchant, Pair<Machine, ConfigurationSection>> merchants = new HashMap<>();

  @Override
  protected void enable() {
    saveDefaultConfig();

    ConfigurationBuilder configuration = new ConfigurationBuilder()
        .setClassLoaders(new ClassLoader[]{getClassLoader()})
        .forPackage("com.easterlyn.machine", getClassLoader());
    new Reflections(configuration)
        .getSubTypesOf(Machine.class).stream()
            .filter(clazz -> !Modifier.isAbstract(clazz.getModifiers()))
            .forEach(
                clazz -> {
                  Constructor<? extends Machine> constructor;
                  Machine machine;
                  try {
                    constructor = clazz.getConstructor(this.getClass());
                    machine = constructor.newInstance(this);
                  } catch (ReflectiveOperationException e) {
                    getLogger().severe("Unable to load machine " + clazz.getName());
                    e.printStackTrace();
                    return;
                  }
                  nameRegistry.put(machine.getClass().getSimpleName().toLowerCase(), machine);
                  nameRegistry.put(machine.getName().toLowerCase(), machine);
                  iconRegistry.put(machine.getUniqueDrop(), machine);
                });

    Event.register(
        BlockPlaceEvent.class,
        event -> {
          Player player = event.getPlayer();

          Pair<Machine, ConfigurationSection> pair = getMachine(event.getBlock());
          if (pair != null) {
            // Block registered as part of a machine. Most likely removed by explosion or similar.
            // Prevents place PGO as diamond block, blow up PGO, place and break dirt in PGO's
            // location to unregister, wait for CreeperHeal to regenerate diamond block for profit.
            event.setCancelled(true);
            player.sendMessage("machines.noTouch");
            // If the blocks are not exploded, there's a larger issue. Rather than shaft the person,
            // fire a reportable event and repair.
            if (exploded.get(event.getBlock()) == null) {
              pair.getLeft().reassemble(pair.getRight());
              ReportableEvent.call(
                  String.format(
                      "Repairing broken %s at %s %s %s after internal placement by %s",
                      pair.getLeft().getName(),
                      event.getBlock().getX(),
                      event.getBlock().getY(),
                      event.getBlock().getZ(),
                      event.getPlayer().getName()));
            }
            return;
          }

          // Machine place logic
          for (Map.Entry<ItemStack, Machine> entry : iconRegistry.entrySet()) {
            if (entry.getKey().isSimilar(event.getItemInHand())) {
              pair =
                  createMachine(
                      event.getBlock(),
                      entry.getValue(),
                      event.getPlayer().getUniqueId(),
                      Direction.getFacingDirection(event.getPlayer()));
              if (pair == null) {
                event.setCancelled(true);
                return;
              }
              if (pair.getLeft().failedAssembly(pair.getRight())) {
                event.setCancelled(true);
              }
              if (!event.isCancelled() && player.getGameMode() != GameMode.CREATIVE) {
                if (player.getInventory().getItemInMainHand().equals(event.getItemInHand())) {
                  player
                      .getInventory()
                      .setItemInMainHand(ItemUtil.decrement(event.getItemInHand(), 1));
                } else {
                  player
                      .getInventory()
                      .setItemInOffHand(ItemUtil.decrement(event.getItemInHand(), 1));
                }
              }
              event.setCancelled(true);
              break;
            }
          }
        },
        this,
        EventPriority.HIGHEST);

    hookCreeperHeal();

    // Machine event handlers
    Event.register(
        PlayerInteractEvent.class,
        event -> {
          if (event.getClickedBlock() == null) {
            return;
          }
          Pair<Machine, ConfigurationSection> machine = getMachine(event.getClickedBlock());
          if (machine != null) {
            machine.getLeft().handleInteract(event, machine.getRight());
          }
        },
        this,
        EventPriority.LOW);
    Event.register(
        InventoryMoveItemEvent.class,
        event -> {
          InventoryHolder inventoryHolder = event.getDestination().getHolder();
          // For now, sending inv is not checked, as no machines require it.
          if (inventoryHolder instanceof BlockState) {
            Pair<Machine, ConfigurationSection> pair =
                getMachine(((BlockState) inventoryHolder).getBlock());
            if (pair != null) {
              pair.getLeft().handleHopperMoveItem(event, pair.getRight());
            }
          }
        },
        this,
        EventPriority.LOW);
    Event.register(
        InventoryPickupItemEvent.class,
        event -> {
          InventoryHolder inventoryHolder = event.getInventory().getHolder();
          // For now, sending inv is not checked, as no machines require it.
          if (inventoryHolder instanceof BlockState) {
            Pair<Machine, ConfigurationSection> pair =
                getMachine(((BlockState) inventoryHolder).getBlock());
            if (pair != null) {
              pair.getLeft().handleHopperPickupItem(event, pair.getRight());
            }
          }
        },
        this,
        EventPriority.LOW);
    registerInventoryEvent(InventoryClickEvent.class, machine -> machine::handleClick);
    registerInventoryEvent(InventoryDragEvent.class, machine -> machine::handleDrag);
    registerInventoryEvent(InventoryOpenEvent.class, machine -> machine::handleOpen);
    registerInventoryEvent(InventoryCloseEvent.class, machine -> machine::handleClose);
    Event.register(
        InventoryCloseEvent.class,
        event -> {
          if (event.getView().getTopInventory() instanceof MerchantInventory) {
            merchants.remove(((MerchantInventory) event.getView().getTopInventory()).getMerchant());
          }
        },
        this,
        EventPriority.HIGH);
    Event.register(
        TradeSelectEvent.class,
        event -> {
          Pair<Machine, ConfigurationSection> machineData = merchants.get(event.getMerchant());
          if (machineData != null) {
            machineData.getLeft().selectTrade(event, machineData.getRight());
          }
        },
        this);

    Event.register(
        BlockPistonExtendEvent.class,
        event -> {
          if (isMachine(event.getBlock()) || event.getBlocks().stream().anyMatch(this::isMachine)) {
            event.setCancelled(true);
          }
        },
        this,
        EventPriority.LOW);
    Event.register(
        BlockPistonRetractEvent.class,
        event -> {
          if (isMachine(event.getBlock()) || event.getBlocks().stream().anyMatch(this::isMachine)) {
            event.setCancelled(true);
          }
        },
        this,
        EventPriority.LOW);
    Event.register(
        PlayerBucketEmptyEvent.class,
        event -> {
          if (isMachine(event.getBlockClicked().getRelative(event.getBlockFace()))) {
            // If we do end up creating a lava well etc. this will need to be added to an event.
            event.setCancelled(true);
          }
        },
        this,
        EventPriority.LOW);
    Consumer<PlayerBucketEvent> bucketConsumer =
        event -> {
          if (isMachine(event.getBlockClicked().getRelative(event.getBlockFace()))) {
            event.setCancelled(true);
          }
        };
    Event.register(PlayerBucketEmptyEvent.class, bucketConsumer::accept, this, EventPriority.LOW);
    Event.register(PlayerBucketFillEvent.class, bucketConsumer::accept, this, EventPriority.LOW);
    registerBlockEvent(BlockBreakEvent.class, machine -> machine::handleBreak);
    registerBlockEvent(BlockFadeEvent.class, machine -> machine::handleFade);
    registerBlockEvent(BlockFromToEvent.class, machine -> machine::handleFromTo);
    registerBlockEvent(BlockIgniteEvent.class, machine -> machine::handleIgnite);
    registerBlockEvent(BlockSpreadEvent.class, machine -> machine::handleSpread);

    // Prevent all external machine manipulation
    Event.register(
        BlockPhysicsEvent.class,
        event -> {
          if (isMachine(event.getBlock())) {
            event.setCancelled(true);
          }
        },
        this);
    Event.register(
        EntityChangeBlockEvent.class,
        event -> {
          if (isMachine(event.getBlock())) {
            event.setCancelled(true);
          }
        },
        this);

    Event.register(
        ChunkLoadEvent.class,
        event ->
            getServer().getScheduler().runTask(this, () -> loadChunkMachines(event.getChunk())),
        this);
    // TODO periodic save system (timer triggered by chunk unload?)
    Event.register(
        ChunkUnloadEvent.class,
        event ->
            getServer().getScheduler().runTask(this, () -> unloadChunkMachines(event.getChunk())),
        this);

    for (World world : getServer().getWorlds()) {
      if (getConfig().getStringList("+disabled-worlds+").contains(world.getName())) {
        continue;
      }
      for (Chunk chunk : world.getLoadedChunks()) {
        loadChunkMachines(chunk);
      }
    }
  }

  @Override
  protected void register(@NotNull EasterlynCore plugin) {
    ItemUtil.addUniqueCheck(
        itemStack -> iconRegistry.keySet().stream().anyMatch(itemStack::isSimilar));
    BlockUtil.addRightClickFunction(((block, itemStack) -> isMachine(block)));

    plugin.registerCommands(this, getClassLoader(), "com.easterlyn.machine.command");
    plugin.getLocaleManager().addLocaleSupplier(this);
    // TODO commandcompletion @machines
  }

  @Override
  public void onDisable() {
    for (Player player : getServer().getOnlinePlayers()) {
      if (getInventoryMachine(player.getOpenInventory().getTopInventory()) != null) {
        player.closeInventory();
      }
    }

    keysToBlocks.entrySet().stream()
        .distinct()
        .forEach(
            entry -> {
              ConfigurationSection section =
                  getConfig().getConfigurationSection(pathFromLoc(entry.getKey().getLocation()));
              if (section == null) {
                return;
              }

              String type = section.getString("type");
              Machine machine = nameRegistry.get(type);

              if (machine == null) {
                return;
              }

              machine.disable(section);
            });

    saveConfig();
    iconRegistry.clear();
    nameRegistry.clear();
  }

  public @Nullable Machine getByName(@NotNull String name) {
    return nameRegistry.get(name.toLowerCase());
  }

  private void loadChunkMachines(Chunk chunk) {
    String worldName = chunk.getWorld().getName();
    if (!getConfig().isSet(worldName)
        || getConfig().getStringList("+disabled-worlds+").contains(worldName)) {
      return;
    }
    String path = chunk.getWorld().getName() + '.' + chunk.getX() + '_' + chunk.getZ();
    if (!getConfig().isConfigurationSection(path)) {
      return;
    }
    ConfigurationSection chunkSection = getConfig().getConfigurationSection(path);
    if (chunkSection == null) {
      return;
    }
    Set<String> chunkKeys = chunkSection.getKeys(false);
    for (Iterator<String> iterator = chunkKeys.iterator(); iterator.hasNext(); ) {
      String xyz = iterator.next();
      ConfigurationSection machineSection = chunkSection.getConfigurationSection(xyz);
      if (machineSection == null) {
        iterator.remove();
        continue;
      }
      String[] split = xyz.split("_");
      try {
        Block key =
            chunk
                .getWorld()
                .getBlockAt(
                    Integer.parseInt(split[0]),
                    Integer.parseInt(split[1]),
                    Integer.parseInt(split[2]));
        Pair<Machine, ConfigurationSection> machine = loadMachine(key, machineSection);
        machine.getLeft().enable(machine.getRight());
      } catch (NumberFormatException e) {
        getLogger().warning("Machine cannot be parsed from " + Arrays.toString(split));
      } catch (IllegalArgumentException e) {
        iterator.remove();
      }
    }
    if (chunkKeys.isEmpty()) {
      getConfig().set(path, null);
    }
  }

  private void unloadChunkMachines(Chunk chunk) {
    blocksToKeys.remove(chunk).stream()
        .distinct()
        .forEach(
            key -> {
              ConfigurationSection section =
                  getConfig()
                      .getConfigurationSection(pathFromLoc(key.getLocation()));
              if (section == null) {
                return;
              }

              String type = section.getString("type");
              Machine machine = nameRegistry.get(type);

              if (machine == null) {
                return;
              }

              machine.disable(section);
            });
    keysToBlocks.remove(chunk);
  }

  /**
   * Checks if a Block is part of a Machine.
   *
   * @param block the Block to check
   * @return true if the Block is part of a Machine
   */
  public boolean isMachine(@NotNull Block block) {
    return blocksToKeys.get(block) != null;
  }

  public boolean isExplodedMachine(@NotNull Block block) {
    return exploded.get(block) != null;
  }

  /**
   * Gets a Machine by Block.
   *
   * @param block the Block
   * @return the Machine or null if the Block is not part of a Machine
   * @throws IllegalStateException if the Machine is malformed
   */
  private @Nullable Pair<Machine, ConfigurationSection> getMachine(@NotNull Block block)
      throws IllegalStateException {
    Block key = blocksToKeys.get(block);
    if (key == null) {
      return null;
    }

    ConfigurationSection section =
        getConfig().getConfigurationSection(pathFromLoc(key.getLocation()));

    if (section == null) {
      throw new IllegalStateException("No ConfigurationSection available for stored key block!");
    }

    String type = section.getString("type");
    Machine machine = nameRegistry.get(type);

    if (machine == null) {
      throw new IllegalStateException(
          String.format("Invalid machine type specified in ConfigurationSection: %s", type));
    }

    return new Pair<>(machine, section);
  }

  /**
   * Gets all Blocks associated with a Machine containing the given Block.
   *
   * @param block the Block
   * @return the associated Blocks
   */
  public @NotNull Collection<Block> getMachineBlocks(@NotNull Block block) {
    Block key = blocksToKeys.get(block);

    if (key == null) {
      return Collections.emptyList();
    }

    Collection<Block> blocks = keysToBlocks.get(block);

    return blocks != null ? blocks : Collections.emptyList();
  }

  /**
   * Removes a Machine from memory and storage.
   *
   * <p>This does NOT remove the Machine's blocks from the world.
   *
   * @param block a Block in the Machine
   */
  public void removeMachineFromMemory(@NotNull Block block) {
    Block key = blocksToKeys.get(block);
    if (key == null) {
      return;
    }

    Collection<Block> blocks = keysToBlocks.remove(block);

    if (blocks != null) {
      blocks.forEach(blocksToKeys::remove);
    }

    getConfig().set(pathFromLoc(key.getLocation()), null);
  }

  /**
   * Creates data for a new Machine. Note that this does not place the machine's physical structure.
   *
   * @param key the central block of the machine
   * @param machine the Machine to build
   * @param owner the owner of the machine
   * @param direction the direction the machine is facing
   * @return the
   */
  private @Nullable Pair<Machine, ConfigurationSection> createMachine(
      @NotNull Block key,
      @NotNull Machine machine,
      @NotNull UUID owner,
      @NotNull Direction direction) {
    if (getConfig().getStringList("+disabled-worlds+").contains(key.getWorld().getName())) {
      return null;
    }
    ConfigurationSection section =
        getConfig().createSection(pathFromLoc(key.getLocation()));
    section.set("type", machine.getName().toLowerCase());
    section.set("owner", owner.toString());
    section.set("direction", direction.name());

    machine
        .getShape()
        .getBuildLocations(key, direction)
        .forEach(
            (block, data) -> {
              blocksToKeys.put(block, key);
              keysToBlocks.put(key, block);
            });

    return new Pair<>(machine, section);
  }

  /**
   * Loads a machine from the given ConfigurationSection.
   *
   * @param key the machine's key location or null if it is to be parsed from storage
   * @param storage the ConfigurationSection used to store the machine data
   * @return a Pair containing the Machine and its corresponding data
   * @throws IllegalArgumentException if the provided ConfigurationSection cannot be used to load a
   *     Machine.
   */
  private @NotNull Pair<Machine, ConfigurationSection> loadMachine(
      @Nullable Block key, @NotNull ConfigurationSection storage) throws IllegalArgumentException {

    if (storage.getCurrentPath() == null) {
      throw new IllegalArgumentException(
          "Machine storage must be a ConfigurationSection stored at its location.");
    }

    if (key == null) {
      key = locFromPath(storage.getCurrentPath()).getBlock();
    }

    if (getConfig().getStringList("+disabled-worlds+").contains(key.getWorld().getName())) {
      throw new IllegalArgumentException("Invalid machine at " + storage.getCurrentPath());
    }

    Machine machine = nameRegistry.get(storage.getString("type"));

    if (machine == null) {
      throw new IllegalArgumentException("Invalid machine at " + storage.getCurrentPath());
    }

    for (Block block :
        machine.getShape().getBuildLocations(key, machine.getDirection(storage)).keySet()) {
      blocksToKeys.put(block, key);
      keysToBlocks.put(key, block);
    }

    return new Pair<>(machine, storage);
  }

  public Merchant getMerchant(String name, Machine machine, ConfigurationSection data) {
    Merchant merchant = getServer().createMerchant(name);
    merchants.put(merchant, new Pair<>(machine, data));
    return merchant;
  }

  public @Nullable Pair<Machine, ConfigurationSection> getMerchantMachine(
      @Nullable Merchant merchant) {
    return merchants.get(merchant);
  }

  private void hookCreeperHeal() {
    Event.register(
        EntityExplodeEvent.class,
        event -> {
          if (Bukkit.getPluginManager().isPluginEnabled("CreeperHeal")
              && CreeperConfig.getWorld(event.getEntity().getWorld().getName())
                  .shouldReplace(event.getEntity())
              && event.getEntityType() != EntityType.ENDER_DRAGON) {
            event
                .blockList()
                .forEach(
                    block -> {
                      if (blocksToKeys.get(block) != null) {
                        exploded.put(block, true);
                      }
                    });
            return;
          }

          // CreeperHeal is not set to heal whatever destroyed this machine. Prevent damage.
          event.blockList().removeIf(block -> blocksToKeys.get(block) != null);
        },
        this);

    try {
      Class.forName("com.nitnelave.CreeperHeal.events.CHBlockHealEvent");
    } catch (ClassNotFoundException e) {
      return;
    }
    Event.register(
        CHBlockHealEvent.class,
        event -> {
          Boolean shouldRestore = this.exploded.get(event.getBlock().getBlock());
          if (shouldRestore != null) {
            exploded.remove(event.getBlock().getBlock());
            if (!shouldRestore) {
              event.setCancelled(true);
            }
            return;
          }
          if (blocksToKeys.get(event.getBlock().getBlock()) != null) {
            if (event.shouldDrop()) {
              event.getBlock().drop(true);
            }
            event.setCancelled(true);
          }
        },
        this);
  }

  private <T extends BlockEvent> void registerBlockEvent(
      Class<T> clazz, Function<Machine, BiConsumer<T, ConfigurationSection>> consumer) {
    Event.register(
        clazz,
        event -> {
          Pair<Machine, ConfigurationSection> machine = getMachine(event.getBlock());
          if (machine == null) {
            return;
          }
          try {
            consumer.apply(machine.getLeft()).accept(event, machine.getRight());
          } catch (Exception exception) {
            ReportableEvent.call("Caught exception handling Machine event", exception, 5);
          }
        },
        this,
        EventPriority.LOW);
  }

  private <T extends InventoryEvent> void registerInventoryEvent(
      Class<T> clazz, Function<Machine, BiConsumer<T, ConfigurationSection>> consumer) {
    Event.register(
        clazz,
        event -> {
          Machine machine;
          ConfigurationSection section = null;
          Pair<Machine, ConfigurationSection> machineData =
              getInventoryMachine(event.getView().getTopInventory());
          if (machineData != null) {
            machine = machineData.getLeft();
            section = machineData.getRight();
          } else if (event.getView().getTopInventory().getHolder() instanceof Machine) {
            machine = (Machine) event.getView().getTopInventory().getHolder();
            if (event.getView().getTopInventory().getLocation() != null) {
              section =
                  getConfig()
                      .getConfigurationSection(
                          pathFromLoc(event.getView().getTopInventory().getLocation()));
            }
          } else {
            return;
          }
          if (machine == null) {
            return;
          }

          try {
            consumer.apply(machine).accept(event, section);
          } catch (Exception exception) {
            ReportableEvent.call("Caught exception handling Machine event", exception, 5);
          }
        },
        this,
        EventPriority.LOW,
        true);
  }

  private @Nullable Pair<Machine, ConfigurationSection> getInventoryMachine(Inventory inventory) {
    if (inventory instanceof MerchantInventory) {
      Merchant merchant = ((MerchantInventory) inventory).getMerchant();
      return merchants.get(merchant);
    }
    InventoryHolder holder = inventory.getHolder();
    if (holder instanceof BlockState) {
      return getMachine(((BlockState) holder).getBlock());
    }
    if (inventory.getLocation() != null) {
      return getMachine(inventory.getLocation().getBlock());
    }
    return null;
  }

  /**
   * Creates a configuration-friendly path from a location.
   *
   * @param location the Location
   * @return the path created
   */
  public static String pathFromLoc(Location location) {
    if (location.getWorld() == null) {
      throw new IllegalArgumentException("Cannot get location path with null world!");
    }
    int blockX = location.getBlockX();
    int blockZ = location.getBlockZ();
    return location.getWorld().getName()
        + '.'
        + Coords.blockToChunk(blockX)
        + '_'
        + Coords.blockToChunk(blockZ)
        + '.'
        + blockX
        + '_'
        + location.getBlockY()
        + '_'
        + blockZ;
  }

  /**
   * Gets a location from a configuration-friendly path.
   *
   * @param string the path
   * @return the Location created
   */
  public static Location locFromPath(String string) {
    String[] pathSplit = string.split("\\.");
    if (pathSplit.length < 3) {
      throw new IllegalArgumentException("Invalid location path: " + string);
    }
    String[] xyz = pathSplit[2].split("_");
    if (xyz.length < 3) {
      throw new IllegalArgumentException("Invalid location path: " + string);
    }
    return new Location(
        Bukkit.getWorld(pathSplit[0]),
        Integer.parseInt(xyz[0]),
        Integer.parseInt(xyz[1]),
        Integer.parseInt(xyz[2]));
  }
}
