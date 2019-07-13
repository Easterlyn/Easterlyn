package com.easterlyn;

import com.easterlyn.event.ReportableEvent;
import com.easterlyn.machine.Machine;
import com.easterlyn.util.BlockUtil;
import com.easterlyn.util.CoordinateUtil;
import com.easterlyn.util.Direction;
import com.easterlyn.util.event.SimpleListener;
import com.easterlyn.util.inventory.ItemUtil;
import com.easterlyn.util.tuple.Pair;
import com.easterlyn.util.wrapper.BlockMap;
import com.easterlyn.util.wrapper.BlockMultiMap;
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
import java.util.function.Function;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.Action;
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
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reflections.Reflections;

public class EasterlynMachines extends JavaPlugin {

	private final BlockMap<Block> blocksToKeys = new BlockMap<>();
	private final BlockMultiMap<Block> keysToBlocks = new BlockMultiMap<>();
	private final Map<String, Machine> nameRegistry = new HashMap<>();
	private final Map<ItemStack, Machine> iconRegistry = new HashMap<>();
	private final BlockMap<Boolean> exploded = new BlockMap<>();

	@Override
	public void onEnable() {
		saveDefaultConfig();

		new Reflections("com.easterlyn.machine", getClassLoader()).getSubTypesOf(Machine.class).stream()
		.filter(clazz -> !Modifier.isAbstract(clazz.getModifiers())).forEach(clazz -> {
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

		BlockPlaceEvent.getHandlerList().register(new SimpleListener<>(BlockPlaceEvent.class, event -> {

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
					getServer().getPluginManager().callEvent(new ReportableEvent(String.format(
							"Repairing broken %s at %s %s %s after internal placement by %s", pair.getLeft().getName(),
							event.getBlock().getX(), event.getBlock().getY(), event.getBlock().getZ(), event.getPlayer().getName())));
				}
				return;
			}

			// Machine place logic
			for (Map.Entry<ItemStack, Machine> entry : iconRegistry.entrySet()) {
				if (entry.getKey().isSimilar(event.getItemInHand())) {
					pair = createMachine(event.getBlock(), entry.getValue(), event.getPlayer().getUniqueId(),
							Direction.getFacingDirection(event.getPlayer()));
					if (pair == null) {
						event.setCancelled(true);
						return;
					}
					if (!pair.getLeft().assemble(pair.getRight())) {
						event.setCancelled(true);
					}
					if (!event.isCancelled() && player.getGameMode() != GameMode.CREATIVE) {
						if (player.getInventory().getItemInMainHand().equals(event.getItemInHand())) {
							player.getInventory().setItemInMainHand(ItemUtil.decrement(event.getItemInHand(), 1));
						} else {
							player.getInventory().setItemInOffHand(ItemUtil.decrement(event.getItemInHand(), 1));
						}
					}
					event.setCancelled(true);
					break;
				}
			}
		}, this, EventPriority.HIGHEST));

		hookCreeperHeal();

		// Machine event handlers
		PlayerInteractEvent.getHandlerList().register(new SimpleListener<>(PlayerInteractEvent.class, event -> {
			if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getClickedBlock() == null) {
				return;
			}
			Pair<Machine, ConfigurationSection> machine = getMachine(event.getClickedBlock());
			if (machine != null) {
				machine.getLeft().handleInteract(event, machine.getRight());
			}
		}, this, EventPriority.LOW));
		InventoryMoveItemEvent.getHandlerList().register(new SimpleListener<>(InventoryMoveItemEvent.class, event -> {
			InventoryHolder inventoryHolder = event.getDestination().getHolder();
			// For now, sending inv is not checked, as no machines require it.
			if (inventoryHolder instanceof BlockState) {
				Pair<Machine, ConfigurationSection> pair = getMachine(((BlockState) inventoryHolder).getBlock());
				if (pair != null) {
					pair.getLeft().handleHopperMoveItem(event, pair.getRight());
				}
			}
		}, this, EventPriority.LOW));
		InventoryPickupItemEvent.getHandlerList().register(new SimpleListener<>(InventoryPickupItemEvent.class, event -> {
			InventoryHolder inventoryHolder = event.getInventory().getHolder();
			// For now, sending inv is not checked, as no machines require it.
			if (inventoryHolder instanceof BlockState) {
				Pair<Machine, ConfigurationSection> pair = getMachine(((BlockState) inventoryHolder).getBlock());
				if (pair != null) {
					pair.getLeft().handleHopperPickupItem(event, pair.getRight());
				}
			}
		}, this, EventPriority.LOW));
		registerInventoryEvent(InventoryClickEvent.class, InventoryClickEvent.getHandlerList(), machine -> machine::handleClick);
		registerInventoryEvent(InventoryDragEvent.class, InventoryDragEvent.getHandlerList(), machine -> machine::handleDrag);
		registerInventoryEvent(InventoryOpenEvent.class, InventoryOpenEvent.getHandlerList(), machine -> machine::handleOpen);
		BlockPistonExtendEvent.getHandlerList().register(new SimpleListener<>(BlockPistonExtendEvent.class, event -> {
			if (isMachine(event.getBlock()) || event.getBlocks().stream().anyMatch(this::isMachine)) {
				event.setCancelled(true);
			}
		}, this, EventPriority.LOW));
		BlockPistonRetractEvent.getHandlerList().register(new SimpleListener<>(BlockPistonRetractEvent.class, event -> {
			if (isMachine(event.getBlock()) || event.getBlocks().stream().anyMatch(this::isMachine)) {
				event.setCancelled(true);
			}
		}, this, EventPriority.LOW));
		SimpleListener<PlayerBucketEvent> bucketListener = new SimpleListener<>(PlayerBucketEvent.class, event -> {
			if (isMachine(event.getBlockClicked().getRelative(event.getBlockFace()))) {
				// If we do end up creating a lava well etc. this will need to be added to an event.
				event.setCancelled(true);
			}
		}, this, EventPriority.LOW);
		PlayerBucketEmptyEvent.getHandlerList().register(bucketListener);
		PlayerBucketFillEvent.getHandlerList().register(bucketListener);
		registerBlockEvent(BlockBreakEvent.class, BlockBreakEvent.getHandlerList(), machine -> machine::handleBreak);
		registerBlockEvent(BlockFadeEvent.class, BlockFadeEvent.getHandlerList(), machine -> machine::handleFade);
		registerBlockEvent(BlockFromToEvent.class, BlockFromToEvent.getHandlerList(), machine -> machine::handleFromTo);
		registerBlockEvent(BlockIgniteEvent.class, BlockIgniteEvent.getHandlerList(), machine -> machine::handleIgnite);
		registerBlockEvent(BlockSpreadEvent.class, BlockSpreadEvent.getHandlerList(), machine -> machine::handleSpread);

		// Prevent all external machine manipulation
		BlockPhysicsEvent.getHandlerList().register(new SimpleListener<>(BlockPhysicsEvent.class, event -> {
			if (isMachine(event.getBlock())) {
				event.setCancelled(true);
			}
		}, this));
		EntityChangeBlockEvent.getHandlerList().register(new SimpleListener<>(EntityChangeBlockEvent.class, event -> {
			if (isMachine(event.getBlock())) {
				event.setCancelled(true);
			}
		}, this));

		ChunkLoadEvent.getHandlerList().register(new SimpleListener<>(ChunkLoadEvent.class,
				event -> getServer().getScheduler().runTask(this, () -> loadChunkMachines(event.getChunk())), this));
		// TODO periodic save system (timer triggered by chunk unload?)
		ChunkUnloadEvent.getHandlerList().register(new SimpleListener<>(ChunkUnloadEvent.class,
				event -> getServer().getScheduler().runTask(this, () -> unloadChunkMachines(event.getChunk())), this));

		for (World world : getServer().getWorlds()) {
			if (!getConfig().getStringList("+disabled-worlds+").contains(world.getName())) {
				continue;
			}
			for (Chunk chunk : world.getLoadedChunks()) {
				loadChunkMachines(chunk);
			}
		}

		RegisteredServiceProvider<EasterlynCore> registration = getServer().getServicesManager().getRegistration(EasterlynCore.class);
		if (registration != null) {
			register(registration.getProvider());
		}

		PluginEnableEvent.getHandlerList().register(new SimpleListener<>(PluginEnableEvent.class,
				pluginEnableEvent -> {
					if (pluginEnableEvent.getPlugin() instanceof EasterlynCore) {
						register((EasterlynCore) pluginEnableEvent.getPlugin());
					}
				}, this));
	}

	private void register(@NotNull EasterlynCore plugin) {
		ItemUtil.addUniqueCheck(itemStack -> iconRegistry.keySet().stream().anyMatch(itemStack::isSimilar));
		BlockUtil.addRightClickFunction(((block, itemStack) -> isMachine(block)));

		plugin.registerCommands(this, getClassLoader(), "com.easterlyn.machine.command");
	}

	@Override
	public void onDisable() {
		saveConfig();
		iconRegistry.clear();
		nameRegistry.clear();
	}

	@Nullable
	public Machine getByName(@NotNull String name) {
		return nameRegistry.get(name.toLowerCase());
	}

	private void loadChunkMachines(Chunk chunk) {
		String worldName = chunk.getWorld().getName();
		if (!getConfig().isSet(worldName) || !getConfig().getStringList("+disabled-worlds+").contains(worldName)) {
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
		for (Iterator<String> iterator = chunkKeys.iterator(); iterator.hasNext();) {
			String xyz = iterator.next();
			ConfigurationSection machineSection = chunkSection.getConfigurationSection(xyz);
			if (machineSection == null) {
				iterator.remove();
				continue;
			}
			String[] split = xyz.split("_");
			try {
				Block key = chunk.getWorld().getBlockAt(Integer.valueOf(split[0]), Integer.valueOf(split[1]), Integer.valueOf(split[2]));
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
		blocksToKeys.remove(chunk).stream().distinct().forEach(key -> {
			ConfigurationSection section = getConfig().getConfigurationSection(CoordinateUtil.pathFromLoc(key.getLocation()));
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
	@Nullable
	private Pair<Machine, ConfigurationSection> getMachine(@NotNull Block block) throws IllegalStateException {
		Block key = blocksToKeys.get(block);
		if (key == null) {
			return null;
		}

		ConfigurationSection section = getConfig().getConfigurationSection(CoordinateUtil.pathFromLoc(key.getLocation()));

		if (section == null) {
			throw new IllegalStateException("No ConfigurationSection available for stored key block!");
		}

		String type = section.getString("type");
		Machine machine = nameRegistry.get(type);

		if (machine == null) {
			throw new IllegalStateException(String.format("Invalid machine type specified in ConfigurationSection: %s", type));
		}

		return new Pair<>(machine, section);
	}

	/**
	 * Gets all Blocks associated with a Machine containing the given Block.
	 *
	 * @param block the Block
	 * @return the associated Blocks
	 */
	@NotNull
	public Collection<Block> getMachineBlocks(@NotNull Block block) {
		Block key = blocksToKeys.get(block);

		if (key == null) {
			return Collections.emptyList();
		}

		Collection<Block> blocks = keysToBlocks.get(block);

		return blocks != null ? blocks : Collections.emptyList();
	}

	/**
	 * Removes a Machine from memory and storage.
	 * <p>
	 * This does NOT remove the Machine's blocks from the world.
	 * </p>
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

		getConfig().set(CoordinateUtil.pathFromLoc(key.getLocation()), null);
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
	@Nullable
	private Pair<Machine, ConfigurationSection> createMachine(@NotNull Block key, @NotNull Machine machine,
			@NotNull UUID owner, @NotNull Direction direction) {
		if (getConfig().getStringList("+disabled-worlds+").contains(key.getWorld().getName())) {
			return null;
		}
		ConfigurationSection section = getConfig().createSection(CoordinateUtil.pathFromLoc(key.getLocation()));
		section.set("type", machine.getName().toLowerCase());
		section.set("owner", owner.toString());
		section.set("direction", direction.name());

		machine.getShape().getBuildLocations(key, direction).forEach((block, data) -> {
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
	 * @throws IllegalArgumentException if the provided ConfigurationSection cannot be used to load a Machine.
	 */
	@NotNull
	private Pair<Machine, ConfigurationSection> loadMachine(@Nullable Block key, @NotNull ConfigurationSection storage)
			throws IllegalArgumentException {

		if (storage.getCurrentPath() == null) {
			throw new IllegalArgumentException("Machine storage must be a ConfigurationSection stored at its location.");
		}

		if (key == null) {
			key = CoordinateUtil.locFromPath(storage.getCurrentPath()).getBlock();
		}

		if (getConfig().getStringList("+disabled-worlds+").contains(key.getWorld().getName())) {
			throw new IllegalArgumentException("Invalid machine at " + storage.getCurrentPath());
		}

		Machine machine = nameRegistry.get(storage.getString("type"));

		if (machine == null) {
			throw new IllegalArgumentException("Invalid machine at " + storage.getCurrentPath());
		}

		for (Block block : machine.getShape().getBuildLocations(key, machine.getDirection(storage)).keySet()) {
			blocksToKeys.put(block, key);
			keysToBlocks.put(key, block);
		}

		return new Pair<>(machine, storage);
	}

	private void hookCreeperHeal() {
		EntityExplodeEvent.getHandlerList().register(new SimpleListener<>(EntityExplodeEvent.class, event -> {
			if (Bukkit.getPluginManager().isPluginEnabled("CreeperHeal")
					&& CreeperConfig.getWorld(event.getLocation().getWorld().getName()).shouldReplace(event.getEntity())
					&& event.getEntityType() != EntityType.ENDER_DRAGON) {
				event.blockList().forEach(block -> {
					if (blocksToKeys.get(block) != null) {
						exploded.put(block, true);
					}
				});
				return;
			}

			// CreeperHeal is not set to heal whatever destroyed this machine. Prevent damage.
			event.blockList().removeIf(block -> blocksToKeys.get(block) != null);
		}, this));

		try {
			Class.forName("com.nitnelave.CreeperHeal.events.CHBlockHealEvent");
		} catch (ClassNotFoundException e) {
			return;
		}
		CHBlockHealEvent.getHandlerList().register(new SimpleListener<>(CHBlockHealEvent.class, event -> {
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
		}, this));
	}

	private <T extends BlockEvent> void registerBlockEvent(Class<T> clazz, HandlerList handlerList,
			Function<Machine, BiConsumer<T, ConfigurationSection>> consumer) {
		handlerList.register(new SimpleListener<>(clazz, event -> {
			Pair<Machine, ConfigurationSection> machine = getMachine(event.getBlock());
			if (machine == null) {
				return;
			}
			try {
				consumer.apply(machine.getLeft()).accept(event, machine.getRight());
			} catch (Exception exception) {
				getServer().getPluginManager().callEvent(new ReportableEvent("Caught exception handling Machine event", exception, 5));
			}
		}, this, EventPriority.LOW, true));
	}

	private <T extends InventoryEvent> void registerInventoryEvent(Class<T> clazz, HandlerList handlerList,
			Function<Machine, BiConsumer<T, ConfigurationSection>> consumer) {
		handlerList.register(new SimpleListener<>(clazz, event -> {
			InventoryHolder holder = event.getView().getTopInventory().getHolder();
			Machine machine = null;
			ConfigurationSection section = null;
			if (holder instanceof Machine) {
				machine = (Machine) holder;
				if (event.getView().getTopInventory().getLocation() != null) {
					section = getConfig().getConfigurationSection(CoordinateUtil.pathFromLoc(event.getView().getTopInventory().getLocation()));
				}
			} else if (holder instanceof BlockState) {
				BlockState blockState = (BlockState) holder;
				Pair<Machine, ConfigurationSection> machineData = getMachine(blockState.getBlock());
				if (machineData != null) {
					machine = machineData.getLeft();
					section = machineData.getRight();
				}
			}
			if (machine == null) {
				return;
			}

			try {
				consumer.apply(machine).accept(event, section);
			} catch (Exception exception) {
				getServer().getPluginManager().callEvent(new ReportableEvent("Caught exception handling Machine event", exception, 5));
			}
		}, this, EventPriority.LOW, true));
	}

}
