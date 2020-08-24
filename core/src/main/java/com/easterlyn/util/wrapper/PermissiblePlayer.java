package com.easterlyn.util.wrapper;

import com.easterlyn.EasterlynCore;
import com.easterlyn.util.PermissionUtil;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Phaser;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Effect;
import org.bukkit.EntityEffect;
import org.bukkit.FluidCollisionMode;
import org.bukkit.GameMode;
import org.bukkit.Instrument;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Note;
import org.bukkit.Particle;
import org.bukkit.Server;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.Statistic;
import org.bukkit.WeatherType;
import org.bukkit.World;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.PistonMoveReaction;
import org.bukkit.block.data.BlockData;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Villager;
import org.bukkit.entity.memory.MemoryKey;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.InventoryView.Property;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MainHand;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.map.MapView;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A wrapper for a Player that directly uses our bridge for handling permissions.
 * A note for later:
 * Find (\tpublic \S+ )(\S*\()(\S+ (\S+ ?))*?(\) .*\{\R\t\t)(return )?.*(\R\t\})
 * Replace $1$2$3$5$6player.$2$4);$7
 * Works pretty well, doesn't support multiple arguments in methods.
 *
 * @author Jikoo
 */
public class PermissiblePlayer implements Player {

	private final Player player;
	private final Phaser phaser;

	public PermissiblePlayer(Player player) {
		this.player = player;
		this.phaser = new Phaser(1);
		if (Bukkit.isPrimaryThread()) {
			Bukkit.getScheduler().runTaskAsynchronously(EasterlynCore.getPlugin(EasterlynCore.class), () -> {
				PermissionUtil.loadPermissionData(player.getUniqueId());
				phaser.arriveAndDeregister();
			});
		} else {
			PermissionUtil.loadPermissionData(player.getUniqueId());
			phaser.arriveAndDeregister();
		}
	}

	@Override
	public boolean hasPermission(@NotNull String arg0) {
		phaser.awaitAdvance(phaser.getPhase() + 1);
		return PermissionUtil.hasPermission(getUniqueId(), arg0);
	}

	@Override
	public boolean hasPermission(@NotNull Permission arg0) {
		return this.hasPermission(arg0.getName());
	}

	@Override
	public boolean isPermissionSet(@NotNull String arg0) {
		return true;
	}

	@Override
	public boolean isPermissionSet(@NotNull Permission arg0) {
		return true;
	}

	@Override
	public void closeInventory() {
		player.closeInventory();
	}

	@NotNull
	@Override
	public Inventory getEnderChest() {
		return player.getEnderChest();
	}

	@Override
	public int getExpToLevel() {
		return player.getExpToLevel();
	}

	@Override
	public float getAttackCooldown() {
		return player.getAttackCooldown();
	}

	@Override
	public boolean discoverRecipe(@NotNull NamespacedKey namespacedKey) {
		return player.discoverRecipe(namespacedKey);
	}

	@Override
	public int discoverRecipes(@NotNull Collection<NamespacedKey> collection) {
		return player.discoverRecipes(collection);
	}

	@Override
	public boolean undiscoverRecipe(@NotNull NamespacedKey namespacedKey) {
		return player.undiscoverRecipe(namespacedKey);
	}

	@Override
	public int undiscoverRecipes(@NotNull Collection<NamespacedKey> collection) {
		return player.undiscoverRecipes(collection);
	}

	@Override
	public boolean hasDiscoveredRecipe(@NotNull NamespacedKey namespacedKey) {
		return player.hasDiscoveredRecipe(namespacedKey);
	}

	@Override
	public @NotNull Set<NamespacedKey> getDiscoveredRecipes() {
		return player.getDiscoveredRecipes();
	}

	@Deprecated
	@Override
	public Entity getShoulderEntityLeft() {
		return player.getShoulderEntityLeft();
	}

	@Deprecated
	@Override
	public void setShoulderEntityLeft(Entity entity) {
		player.setShoulderEntityLeft(entity);
	}

	@Deprecated
	@Override
	public Entity getShoulderEntityRight() {
		return player.getShoulderEntityRight();
	}

	@Deprecated
	@Override
	public void setShoulderEntityRight(Entity entity) {
		player.setShoulderEntityRight(entity);
	}

	@NotNull
	@Override
	public GameMode getGameMode() {
		return player.getGameMode();
	}

	@NotNull
	@Override
	public PlayerInventory getInventory() {
		return player.getInventory();
	}

	@NotNull
	@Override
	@Deprecated
	public ItemStack getItemInHand() {
		return player.getItemInHand();
	}

	@NotNull
	@Override
	public ItemStack getItemOnCursor() {
		return player.getItemOnCursor();
	}

	@NotNull
	@Override
	public String getName() {
		if (player.isOnline()) {
			return player.getName();
		}
		String name = Bukkit.getOfflinePlayer(getUniqueId()).getName();
		return name != null ? name : getUniqueId().toString();
	}

	@NotNull
	@Override
	public InventoryView getOpenInventory() {
		return player.getOpenInventory();
	}

	@Override
	public int getSleepTicks() {
		return player.getSleepTicks();
	}

	@Override
	public boolean isBlocking() {
		return player.isBlocking();
	}

	@Override
	public boolean isHandRaised() {
		return player.isHandRaised();
	}

	@Override
	public boolean isSleeping() {
		return player.isSleeping();
	}

	@Override
	public InventoryView openEnchanting(Location location, boolean force) {
		return player.openEnchanting(location, force);
	}

	@Override
	public InventoryView openMerchant(@NotNull Villager paramVillager, boolean paramBoolean) {
		return player.openMerchant(paramVillager, paramBoolean);
	}

	@Override
	public InventoryView openInventory(@NotNull Inventory inventory) {
		return player.openInventory(inventory);
	}

	@Override
	public void openInventory(@NotNull InventoryView inventory) {
		player.openInventory(inventory);
	}

	@Override
	public InventoryView openWorkbench(Location location, boolean force) {
		return player.openWorkbench(location, force);
	}

	@Override
	public void setGameMode(@NotNull GameMode mode) {
		player.setGameMode(mode);
	}

	@Override
	@Deprecated
	public void setItemInHand(ItemStack item) {
		player.setItemInHand(item);
	}

	@Override
	public void setItemOnCursor(ItemStack item) {
		player.setItemOnCursor(item);
	}

	@Override
	public boolean setWindowProperty(@NotNull Property prop, int value) {
		return player.setWindowProperty(prop, value);
	}

	@Override
	public boolean addPotionEffect(@NotNull PotionEffect effect) {
		return player.addPotionEffect(effect);
	}

	@Override
	@Deprecated
	public boolean addPotionEffect(@NotNull PotionEffect effect, boolean force) {
		return player.addPotionEffect(effect, force);
	}

	@Override
	public boolean addPotionEffects(@NotNull Collection<PotionEffect> effects) {
		return player.addPotionEffects(effects);
	}

	@NotNull
	@Override
	public Collection<PotionEffect> getActivePotionEffects() {
		return player.getActivePotionEffects();
	}

	@Override
	public boolean getCanPickupItems() {
		return player.getCanPickupItems();
	}

	@Override
	public EntityEquipment getEquipment() {
		return player.getEquipment();
	}

	@Override
	public double getEyeHeight() {
		return player.getEyeHeight();
	}

	@Override
	public double getEyeHeight(boolean ignoreSneaking) {
		return player.getEyeHeight(ignoreSneaking);
	}

	@NotNull
	@Override
	public Location getEyeLocation() {
		return player.getEyeLocation();
	}

	@Override
	public Player getKiller() {
		return player.getKiller();
	}

	@Override
	public double getLastDamage() {
		return player.getLastDamage();
	}

	@NotNull
	@Override
	public List<Block> getLastTwoTargetBlocks(Set<Material> transparent, int maxDistance) {
		return player.getLastTwoTargetBlocks(transparent, maxDistance);
	}

	@Override
	public Block getTargetBlockExact(int i) {
		return player.getTargetBlockExact(i);
	}

	@Override
	public Block getTargetBlockExact(int i, @NotNull FluidCollisionMode fluidCollisionMode) {
		return player.getTargetBlockExact(i, fluidCollisionMode);
	}

	@Override
	public RayTraceResult rayTraceBlocks(double v) {
		return player.rayTraceBlocks(v);
	}

	@Override
	public RayTraceResult rayTraceBlocks(double v, @NotNull FluidCollisionMode fluidCollisionMode) {
		return player.rayTraceBlocks(v, fluidCollisionMode);
	}

	@NotNull
	@Override
	public Entity getLeashHolder() throws IllegalStateException {
		return player.getLeashHolder();
	}

	@NotNull
	@Override
	public List<Block> getLineOfSight(Set<Material> transparent, int maxDistance) {
		return player.getLineOfSight(transparent, maxDistance);
	}

	@Override
	public int getMaximumAir() {
		return player.getMaximumAir();
	}

	@Override
	public int getMaximumNoDamageTicks() {
		return player.getMaximumNoDamageTicks();
	}

	@Override
	public int getNoDamageTicks() {
		return player.getNoDamageTicks();
	}

	@Override
	public int getRemainingAir() {
		return player.getRemainingAir();
	}

	@Override
	public boolean getRemoveWhenFarAway() {
		return player.getRemoveWhenFarAway();
	}

	@NotNull
	@Override
	public Block getTargetBlock(Set<Material> transparent, int maxDistance) {
		return player.getTargetBlock(transparent, maxDistance);
	}

	@Override
	public boolean hasLineOfSight(@NotNull Entity other) {
		return player.hasLineOfSight(other);
	}

	@Override
	public boolean hasPotionEffect(@NotNull PotionEffectType type) {
		return player.hasPotionEffect(type);
	}

	@Override
	public PotionEffect getPotionEffect(@NotNull PotionEffectType type) {
		return player.getPotionEffect(type);
	}

	@Override
	public boolean isLeashed() {
		return player.isLeashed();
	}

	@Override
	public void removePotionEffect(@NotNull PotionEffectType type) {
		player.removePotionEffect(type);
	}

	@Override
	public void setCanPickupItems(boolean pickup) {
		player.setCanPickupItems(pickup);
	}

	@Override
	public void setLastDamage(double damage) {
		player.setLastDamage(damage);
	}

	@Override
	public boolean setLeashHolder(Entity holder) {
		return player.setLeashHolder(holder);
	}

	@Override
	public void setMaximumAir(int ticks) {
		player.setMaximumAir(ticks);
	}

	@Override
	public void setMaximumNoDamageTicks(int ticks) {
		player.setMaximumNoDamageTicks(ticks);
	}

	@Override
	public void setNoDamageTicks(int ticks) {
		player.setNoDamageTicks(ticks);
	}

	@Override
	public void setRemainingAir(int ticks) {
		player.setRemainingAir(ticks);
	}

	@Override
	public void setRemoveWhenFarAway(boolean remove) {
		player.setRemoveWhenFarAway(remove);
	}

	@Override
	public boolean eject() {
		return player.eject();
	}

	@Override
	public String getCustomName() {
		return player.getCustomName();
	}

	@Override
	public int getEntityId() {
		return player.getEntityId();
	}

	@Override
	public float getFallDistance() {
		return player.getFallDistance();
	}

	@Override
	public int getFireTicks() {
		return player.getFireTicks();
	}

	@Override
	public EntityDamageEvent getLastDamageCause() {
		return player.getLastDamageCause();
	}

	@NotNull
	@Override
	public Location getLocation() {
		return player.getLocation();
	}

	@Override
	public Location getLocation(Location arg0) {
		return player.getLocation(arg0);
	}

	@Override
	public int getMaxFireTicks() {
		return player.getMaxFireTicks();
	}

	@NotNull
	@Override
	public List<Entity> getNearbyEntities(double arg0, double arg1, double arg2) {
		return player.getNearbyEntities(arg0, arg1, arg2);
	}

	@Deprecated
	@Override
	public Entity getPassenger() {
		return player.getPassenger();
	}

	@NotNull
	@Override
	public Server getServer() {
		return player.getServer();
	}

	@Deprecated
	@Override
	public boolean isPersistent() {
		return player.isPersistent();
	}

	@Deprecated
	@Override
	public void setPersistent(boolean b) {
		player.setPersistent(b);
	}

	@Override
	public int getTicksLived() {
		return player.getTicksLived();
	}

	@NotNull
	@Override
	public EntityType getType() {
		return player.getType();
	}

	@NotNull
	@Override
	public UUID getUniqueId() {
		return player.getUniqueId();
	}

	@Override
	public Entity getVehicle() {
		return player.getVehicle();
	}

	@NotNull
	@Override
	public Vector getVelocity() {
		return player.getVelocity();
	}

	@NotNull
	@Override
	public World getWorld() {
		return player.getWorld();
	}

	@Deprecated
	@Override
	public void setRotation(float v, float v1) {
		player.setRotation(v, v1);
	}

	@Override
	public boolean isCustomNameVisible() {
		return player.isCustomNameVisible();
	}

	@Override
	public boolean isDead() {
		return player.isDead();
	}

	@Override
	public boolean isEmpty() {
		return player.isEmpty();
	}

	@Override
	public boolean isInsideVehicle() {
		return player.isInsideVehicle();
	}

	@Override
	public boolean isValid() {
		return player.isValid();
	}

	@Override
	public boolean leaveVehicle() {
		return player.leaveVehicle();
	}

	@Override
	public void playEffect(@NotNull EntityEffect arg0) {
		player.playEffect(arg0);
	}

	@Override
	public void remove() {
		player.remove();
	}

	@Override
	public void setCustomName(String arg0) {
		player.setCustomName(arg0);
	}

	@Override
	public void setCustomNameVisible(boolean arg0) {
		player.setCustomNameVisible(arg0);
	}

	@Override
	public void setFallDistance(float arg0) {
		player.setFallDistance(arg0);
	}

	@Override
	public void setFireTicks(int arg0) {
		player.setFireTicks(arg0);
	}

	@Override
	public void setLastDamageCause(EntityDamageEvent arg0) {
		player.setLastDamageCause(arg0);
	}

	@Deprecated
	@Override
	public boolean setPassenger(@NotNull Entity arg0) {
		return player.setPassenger(arg0);
	}

	@Override
	public void setTicksLived(int arg0) {
		player.setTicksLived(arg0);
	}

	@Override
	public void setVelocity(@NotNull Vector arg0) {
		player.setVelocity(arg0);
	}

	@Override
	public boolean teleport(@NotNull Location arg0) {
		return player.teleport(arg0);
	}

	@Override
	public boolean teleport(@NotNull Entity arg0) {
		return player.teleport(arg0);
	}

	@Override
	public boolean teleport(@NotNull Location location, @NotNull TeleportCause arg1) {
		return player.teleport(location, arg1);
	}

	@Override
	public boolean teleport(@NotNull Entity entity, @NotNull TeleportCause arg1) {
		return player.teleport(entity, arg1);
	}

	@NotNull
	@Override
	public List<MetadataValue> getMetadata(@NotNull String arg0) {
		return player.getMetadata(arg0);
	}

	@Override
	public boolean hasMetadata(@NotNull String arg0) {
		return player.hasMetadata(arg0);
	}

	@Override
	public void removeMetadata(@NotNull String arg0, @NotNull Plugin arg1) {
		player.removeMetadata(arg0, arg1);
	}

	@Override
	public void setMetadata(@NotNull String arg0, @NotNull MetadataValue arg1) {
		player.setMetadata(arg0, arg1);
	}

	@Override
	public void sendMessage(@NotNull String arg0) {
		player.sendMessage(arg0);
	}

	@Override
	public void sendMessage(@NotNull String[] arg0) {
		player.sendMessage(arg0);
	}

	@NotNull
	@Override
	public PermissionAttachment addAttachment(@NotNull Plugin arg0) {
		return player.addAttachment(arg0);
	}

	@Override
	public PermissionAttachment addAttachment(@NotNull Plugin arg0, int arg1) {
		return player.addAttachment(arg0, arg1);
	}

	@NotNull
	@Override
	public PermissionAttachment addAttachment(@NotNull Plugin arg0, @NotNull String arg1, boolean arg2) {
		return player.addAttachment(arg0, arg1, arg2);
	}

	@Override
	public PermissionAttachment addAttachment(@NotNull Plugin arg0, @NotNull String arg1, boolean arg2, int arg3) {
		return player.addAttachment(arg0, arg1, arg2, arg3);
	}

	@NotNull
	@Override
	public Set<PermissionAttachmentInfo> getEffectivePermissions() {
		return player.getEffectivePermissions();
	}

	@Override
	public void recalculatePermissions() {
		player.recalculatePermissions();
	}

	@Override
	public void removeAttachment(@NotNull PermissionAttachment arg0) {
		player.removeAttachment(arg0);
	}

	@Override
	public boolean isOp() {
		return player.isOp();
	}

	@Override
	public void setOp(boolean arg0) {
		player.setOp(arg0);
	}

	@Override
	public void damage(double arg0) {
		player.damage(arg0);
	}

	@Override
	public void damage(double arg0, Entity arg1) {
		player.damage(arg0, arg1);
	}

	@Override
	public double getHealth() {
		return player.getHealth();
	}

	@Deprecated
	@Override
	public double getMaxHealth() {
		return player.getMaxHealth();
	}

	@Deprecated
	@Override
	public void resetMaxHealth() {
		player.resetMaxHealth();
	}

	@Override
	public void setHealth(double arg0) {
		player.setHealth(arg0);
	}

	@Override
	public double getAbsorptionAmount() {
		return player.getAbsorptionAmount();
	}

	@Override
	public void setAbsorptionAmount(double v) {
		player.setAbsorptionAmount(v);
	}

	@Deprecated
	@Override
	public void setMaxHealth(double arg0) {
		player.setMaxHealth(arg0);
	}

	@NotNull
	@Override
	public <T extends Projectile> T launchProjectile(@NotNull Class<? extends T> arg0) {
		return player.launchProjectile(arg0);
	}

	@NotNull
	@Override
	public <T extends Projectile> T launchProjectile(@NotNull Class<? extends T> arg0, Vector arg1) {
		return player.launchProjectile(arg0, arg1);
	}

	@Override
	public void abandonConversation(@NotNull Conversation arg0) {
		player.abandonConversation(arg0);
	}

	@Override
	public void abandonConversation(@NotNull Conversation arg0, @NotNull ConversationAbandonedEvent arg1) {
		player.abandonConversation(arg0, arg1);
	}

	@Override
	public void acceptConversationInput(@NotNull String arg0) {
		player.acceptConversationInput(arg0);
	}

	@Override
	public boolean beginConversation(@NotNull Conversation arg0) {
		return player.beginConversation(arg0);
	}

	@Override
	public boolean isConversing() {
		return player.isConversing();
	}

	@Override
	public long getFirstPlayed() {
		return player.getFirstPlayed();
	}

	@Override
	@Deprecated
	public long getLastPlayed() {
		return player.getLastPlayed();
	}

	@Override
	public Player getPlayer() {
		return player.getPlayer();
	}

	@Override
	public boolean hasPlayedBefore() {
		return player.hasPlayedBefore();
	}

	@Override
	public boolean isBanned() {
		return player.isBanned();
	}

	@Override
	public boolean isOnline() {
		return player.isOnline();
	}

	@Override
	public boolean isWhitelisted() {
		return player.isWhitelisted();
	}

	@Override
	public void setWhitelisted(boolean arg0) {
		player.setWhitelisted(arg0);
	}

	@NotNull
	@Override
	public Map<String, Object> serialize() {
		return player.serialize();
	}

	@NotNull
	@Override
	public Set<String> getListeningPluginChannels() {
		return player.getListeningPluginChannels();
	}

	@Override
	public void sendPluginMessage(@NotNull Plugin arg0, @NotNull String arg1, @NotNull byte[] arg2) {
		player.sendPluginMessage(arg0, arg1, arg2);
	}

	@Override
	public boolean canSee(@NotNull Player arg0) {
		return player.canSee(arg0);
	}

	@Override
	public void chat(@NotNull String arg0) {
		player.chat(arg0);
	}

	@Override
	public void decrementStatistic(@NotNull Statistic arg0) throws IllegalArgumentException {
		player.decrementStatistic(arg0);
	}

	@Override
	public void decrementStatistic(@NotNull Statistic arg0, int arg1) throws IllegalArgumentException {
		player.decrementStatistic(arg0, arg1);
	}

	@Override
	public void decrementStatistic(@NotNull Statistic arg0, @NotNull Material arg1) throws IllegalArgumentException {
		player.decrementStatistic(arg0, arg1);
	}

	@Override
	public void decrementStatistic(@NotNull Statistic arg0, @NotNull EntityType arg1) throws IllegalArgumentException {
		player.decrementStatistic(arg0, arg1);
	}

	@Override
	public void decrementStatistic(@NotNull Statistic arg0, @NotNull Material arg1, int arg2)
			throws IllegalArgumentException {
		player.decrementStatistic(arg0, arg1, arg2);
	}

	@Override
	public void decrementStatistic(@NotNull Statistic arg0, @NotNull EntityType arg1, int arg2) {
		player.decrementStatistic(arg0, arg1, arg2);
	}

	@Override
	public InetSocketAddress getAddress() {
		return player.getAddress();
	}

	@Override
	public boolean getAllowFlight() {
		return player.getAllowFlight();
	}

	@Override
	public Location getBedSpawnLocation() {
		return player.getBedSpawnLocation();
	}

	@NotNull
	@Override
	public Location getCompassTarget() {
		return player.getCompassTarget();
	}

	@NotNull
	@Override
	public String getDisplayName() {
		return player.getDisplayName();
	}

	@Override
	public float getExhaustion() {
		return player.getExhaustion();
	}

	@Override
	public float getExp() {
		return player.getExp();
	}

	@Override
	public float getFlySpeed() {
		return player.getFlySpeed();
	}

	@Override
	public int getFoodLevel() {
		return player.getFoodLevel();
	}

	@Override
	public double getHealthScale() {
		return player.getHealthScale();
	}

	@Override
	public int getLevel() {
		return player.getLevel();
	}

	@NotNull
	@Override
	public String getPlayerListName() {
		return player.getPlayerListName();
	}

	@Override
	public long getPlayerTime() {
		return player.getPlayerTime();
	}

	@Override
	public long getPlayerTimeOffset() {
		return player.getPlayerTimeOffset();
	}

	@Override
	public WeatherType getPlayerWeather() {
		return player.getPlayerWeather();
	}

	@Override
	public float getSaturation() {
		return player.getSaturation();
	}

	@NotNull
	@Override
	public Scoreboard getScoreboard() {
		return player.getScoreboard();
	}

	@Override
	public Entity getSpectatorTarget() {
		return player.getSpectatorTarget();
	}

	@Override
	public int getStatistic(@NotNull Statistic arg0) throws IllegalArgumentException {
		return player.getStatistic(arg0);
	}

	@Override
	public int getStatistic(@NotNull Statistic arg0, @NotNull Material arg1) throws IllegalArgumentException {
		return player.getStatistic(arg0, arg1);
	}

	@Override
	public int getStatistic(@NotNull Statistic arg0, @NotNull EntityType arg1) throws IllegalArgumentException {
		return player.getStatistic(arg0, arg1);
	}

	@Override
	public int getTotalExperience() {
		return player.getTotalExperience();
	}

	@Override
	public float getWalkSpeed() {
		return player.getWalkSpeed();
	}

	@Override
	public void giveExp(int arg0) {
		player.giveExp(arg0);
	}

	@Override
	public void giveExpLevels(int arg0) {
		player.giveExpLevels(arg0);
	}

	@Deprecated
	@Override
	public void hidePlayer(@NotNull Player arg0) {
		player.hidePlayer(arg0);
	}

	@Override
	public void hidePlayer(@NotNull Plugin plugin, @NotNull Player player) {
		player.hidePlayer(plugin, player);
	}

	@Override
	public void incrementStatistic(@NotNull Statistic arg0) throws IllegalArgumentException {
		player.incrementStatistic(arg0);
	}

	@Override
	public void incrementStatistic(@NotNull Statistic arg0, int arg1) throws IllegalArgumentException {
		player.incrementStatistic(arg0, arg1);
	}

	@Override
	public void incrementStatistic(@NotNull Statistic arg0, @NotNull Material arg1) throws IllegalArgumentException {
		player.incrementStatistic(arg0, arg1);
	}

	@Override
	public void incrementStatistic(@NotNull Statistic arg0, @NotNull EntityType arg1) throws IllegalArgumentException {
		player.incrementStatistic(arg0, arg1);
	}

	@Override
	public void incrementStatistic(@NotNull Statistic arg0, @NotNull Material arg1, int arg2)
			throws IllegalArgumentException {
		player.incrementStatistic(arg0, arg1, arg2);
	}

	@Override
	public void incrementStatistic(@NotNull Statistic arg0, @NotNull EntityType arg1, int arg2)
			throws IllegalArgumentException {
		player.incrementStatistic(arg0, arg1, arg2);
	}

	@Override
	public boolean isFlying() {
		return player.isFlying();
	}

	@Override
	public boolean isHealthScaled() {
		return player.isHealthScaled();
	}

	@Override
	@Deprecated
	public boolean isOnGround() {
		return player.isOnGround();
	}

	@Override
	public boolean isPlayerTimeRelative() {
		return player.isPlayerTimeRelative();
	}

	@Override
	public boolean isSleepingIgnored() {
		return player.isSleepingIgnored();
	}

	@Override
	public boolean isSneaking() {
		return player.isSneaking();
	}

	@Override
	public boolean isSprinting() {
		return player.isSprinting();
	}

	@Override
	public void kickPlayer(String arg0) {
		player.kickPlayer(arg0);
	}

	@Override
	public void loadData() {
		player.loadData();
	}

	@Override
	public boolean performCommand(@NotNull String arg0) {
		return player.performCommand(arg0);
	}

	@Override
	@Deprecated
	public void playEffect(@NotNull Location arg0, @NotNull Effect arg1, int arg2) {
		player.playEffect(arg0, arg1, arg2);
	}

	@Override
	public <T> void playEffect(@NotNull Location arg0, @NotNull Effect arg1, T arg2) {
		player.playEffect(arg0, arg1, arg2);
	}

	@Override
	@Deprecated
	public void playNote(@NotNull Location arg0, byte arg1, byte arg2) {
		player.playNote(arg0, arg1, arg2);
	}

	@Override
	public void playNote(@NotNull Location arg0, @NotNull Instrument arg1, @NotNull Note arg2) {
		player.playNote(arg0, arg1, arg2);
	}

	@Override
	public void playSound(@NotNull Location arg0, @NotNull String arg1, float arg2, float arg3) {
		player.playSound(arg0, arg1, arg2, arg3);
	}

	@Override
	public void playSound(@NotNull Location arg0, @NotNull Sound arg1, float arg2, float arg3) {
		player.playSound(arg0, arg1, arg2, arg3);
	}

	@Override
	public void resetPlayerTime() {
		player.resetPlayerTime();
	}

	@Override
	public void resetPlayerWeather() {
		player.resetPlayerWeather();
	}

	@Override
	@Deprecated
	public void resetTitle() {
		player.resetTitle();
	}

	@Override
	public void saveData() {
		player.saveData();
	}

	@Override
	@Deprecated
	public void sendBlockChange(@NotNull Location arg0, @NotNull Material arg1, byte arg2) {
		player.sendBlockChange(arg0, arg1, arg2);
	}

	@Override
	public void sendBlockChange(@NotNull Location location, @NotNull BlockData blockData) {
		player.sendBlockChange(location, blockData);
	}

	@Override
	@Deprecated
	public boolean sendChunkChange(@NotNull Location arg0, int arg1, int arg2, int arg3, @NotNull byte[] arg4) {
		return player.sendChunkChange(arg0, arg1, arg2, arg3, arg4);
	}

	@Override
	public void sendMap(@NotNull MapView arg0) {
		player.sendMap(arg0);
	}

	@Override
	public void sendRawMessage(@NotNull String arg0) {
		player.sendRawMessage(arg0);
	}

	@Override
	public void sendSignChange(@NotNull Location arg0, String[] arg1) throws IllegalArgumentException {
		player.sendSignChange(arg0, arg1);
	}

	@Override
	public void sendSignChange(@NotNull Location location, @Nullable String[] strings,
			@NotNull DyeColor dyeColor) throws IllegalArgumentException {
		player.sendSignChange(location, strings, dyeColor);
	}

	@Override
	@Deprecated
	public void sendTitle(String arg0, String arg1) {
		player.sendTitle(arg0, arg1);
	}

	@Override
	public void setAllowFlight(boolean arg0) {
		player.setAllowFlight(arg0);
	}

	@Override
	public void setBedSpawnLocation(Location arg0) {
		player.setBedSpawnLocation(arg0);
	}

	@Override
	public void setBedSpawnLocation(Location arg0, boolean arg1) {
		player.setBedSpawnLocation(arg0, arg1);
	}

	@Override
	public boolean sleep(@NotNull Location location, boolean b) {
		return player.sleep(location, b);
	}

	@Override
	public void wakeup(boolean b) {
		player.wakeup(b);
	}

	@NotNull
	@Override
	public Location getBedLocation() {
		return player.getBedLocation();
	}

	@Override
	public void setCompassTarget(@NotNull Location arg0) {
		player.setCompassTarget(arg0);
	}

	@Override
	public void setDisplayName(String arg0) {
		player.setDisplayName(arg0);
	}

	@Override
	public void setExhaustion(float arg0) {
		player.setExhaustion(arg0);
	}

	@Override
	public void setExp(float arg0) {
		player.setExp(arg0);
	}

	@Override
	public void setFlySpeed(float arg0) throws IllegalArgumentException {
		player.setFlySpeed(arg0);
	}

	@Override
	public void setFlying(boolean arg0) {
		player.setFlying(arg0);
	}

	@Override
	public void setFoodLevel(int arg0) {
		player.setFoodLevel(arg0);
	}

	@Override
	public void setHealthScale(double arg0) throws IllegalArgumentException {
		player.setHealthScale(arg0);
	}

	@Override
	public void setHealthScaled(boolean arg0) {
		player.setHealthScaled(arg0);
	}

	@Override
	public void setLevel(int arg0) {
		player.setLevel(arg0);
	}

	@Override
	public void setPlayerListName(String arg0) {
		player.setPlayerListName(arg0);
	}

	@Deprecated
	@Override
	public String getPlayerListHeader() {
		return player.getPlayerListHeader();
	}

	@Deprecated
	@Override
	public String getPlayerListFooter() {
		return player.getPlayerListFooter();
	}

	@Deprecated
	@Override
	public void setPlayerListHeader(String s) {
		player.setPlayerListHeader(s);
	}

	@Deprecated
	@Override
	public void setPlayerListFooter(String s) {
		player.setPlayerListFooter(s);
	}

	@Deprecated
	@Override
	public void setPlayerListHeaderFooter(String s, String s1) {
		player.setPlayerListHeaderFooter(s, s1);
	}

	@Override
	public void setPlayerTime(long arg0, boolean arg1) {
		player.setPlayerTime(arg0, arg1);
	}

	@Override
	public void setPlayerWeather(@NotNull WeatherType arg0) {
		player.setPlayerWeather(arg0);
	}

	@Override
	@Deprecated
	public void setResourcePack(@NotNull String arg0) {
		player.setResourcePack(arg0);
	}

	@Override
	public void setResourcePack(@NotNull String arg0, @NotNull byte[] arg1) {
		player.setResourcePack(arg0, arg1);
	}

	@Override
	public void setSaturation(float arg0) {
		player.setSaturation(arg0);
	}

	@Override
	public void setScoreboard(@NotNull Scoreboard arg0) throws IllegalArgumentException,
			IllegalStateException {
		player.setScoreboard(arg0);
	}

	@Override
	public void setSleepingIgnored(boolean arg0) {
		player.setSleepingIgnored(arg0);
	}

	@Override
	public void setSneaking(boolean arg0) {
		player.setSneaking(arg0);
	}

	@Override
	public void setSpectatorTarget(Entity arg0) {
		player.setSpectatorTarget(arg0);
	}

	@Override
	public void setSprinting(boolean arg0) {
		player.setSprinting(arg0);
	}

	@Override
	public void setStatistic(@NotNull Statistic arg0, int arg1) throws IllegalArgumentException {
		player.setStatistic(arg0, arg1);
	}

	@Override
	public void setStatistic(@NotNull Statistic arg0, @NotNull Material arg1, int arg2)
			throws IllegalArgumentException {
		player.setStatistic(arg0, arg1, arg2);
	}

	@Override
	public void setStatistic(@NotNull Statistic arg0, @NotNull EntityType arg1, int arg2) {
		player.setStatistic(arg0, arg1, arg2);
	}

	@Override
	@Deprecated
	public void setTexturePack(@NotNull String arg0) {
		player.setTexturePack(arg0);
	}

	@Override
	public void setTotalExperience(int arg0) {
		player.setTotalExperience(arg0);
	}

	@Override
	public void sendExperienceChange(float v) {
		player.sendExperienceChange(v);
	}

	@Override
	public void sendExperienceChange(float v, int i) {
		player.sendExperienceChange(v, i);
	}

	@Override
	public void setWalkSpeed(float arg0) throws IllegalArgumentException {
		player.setWalkSpeed(arg0);
	}

	@Deprecated
	@Override
	public void showPlayer(@NotNull Player arg0) {
		player.showPlayer(arg0);
	}

	@Override
	public void showPlayer(@NotNull Plugin plugin, @NotNull Player player) {
		player.showPlayer(plugin, player);
	}

	@NotNull
	@Override
	public Spigot spigot() {
		return player.spigot();
	}

	@Override
	public void updateInventory() {
		player.updateInventory();
	}

	@Override
	public AttributeInstance getAttribute(@NotNull Attribute attribute) {
		return player.getAttribute(attribute);
	}

	@Override
	public boolean isGlowing() {
		return player.isGlowing();
	}

	@Override
	public void setGlowing(boolean flag) {
		player.setGlowing(flag);
	}

	@Override
	public void spawnParticle(@NotNull Particle particle, @NotNull Location location, int count) {
		player.spawnParticle(particle, location, count);
	}

	@Override
	public <T> void spawnParticle(@NotNull Particle particle, @NotNull Location location, int count, T data) {
		player.spawnParticle(particle, location, count, data);
	}

	@Override
	public void spawnParticle(@NotNull Particle particle, double x, double y, double z, int count) {
		player.spawnParticle(particle, x, y, z, count);
	}

	@Override
	public <T> void spawnParticle(@NotNull Particle particle, double x, double y, double z, int count, T data) {
		player.spawnParticle(particle, x, y, z, count,  data);
	}

	@Override
	public void spawnParticle(@NotNull Particle particle, @NotNull Location location, int count, double offsetX,
							  double offsetY, double offsetZ) {
		player.spawnParticle(particle, location, count, offsetX, offsetY, offsetZ);
	}

	@Override
	public <T> void spawnParticle(@NotNull Particle particle, @NotNull Location location, int count, double offsetX,
								  double offsetY, double offsetZ, T data) {
		player.spawnParticle(particle, location, count, offsetX, offsetY, offsetZ, data);
	}

	@Override
	public void spawnParticle(@NotNull Particle particle, @NotNull Location location, int count, double offsetX,
							  double offsetY, double offsetZ, double extra) {
		player.spawnParticle(particle, location, count, offsetX, offsetY, offsetZ, extra);
	}

	@Override
	public void spawnParticle(@NotNull Particle particle, double x, double y, double z, int count,
							  double offsetX, double offsetY, double offsetZ) {
		player.spawnParticle(particle, x, y, z, count, offsetX, offsetY, offsetZ);
	}

	@Override
	public <T> void spawnParticle(@NotNull Particle particle, @NotNull Location location, int count, double offsetX,
								  double offsetY, double offsetZ, double extra, T data) {
		player.spawnParticle(particle, location, count, offsetX, offsetY, offsetZ, extra, data);
	}

	@Override
	public <T> void spawnParticle(@NotNull Particle particle, double x, double y, double z, int count,
								  double offsetX, double offsetY, double offsetZ, T data) {
		player.spawnParticle(particle, x, y, z, count, offsetX, offsetY, offsetZ, data);
	}

	@Override
	public void spawnParticle(@NotNull Particle particle, double x, double y, double z, int count,
							  double offsetX, double offsetY, double offsetZ, double extra) {
		player.spawnParticle(particle, x, y, z, count, offsetX, offsetY, offsetZ, extra);
	}

	@Override
	public <T> void spawnParticle(@NotNull Particle particle, double x, double y, double z, int count,
								  double offsetX, double offsetY, double offsetZ, double extra, T data) {
		player.spawnParticle(particle, x, y, z, count, offsetX, offsetY, offsetZ, extra, data);
	}

	@NotNull
	@Override
	public AdvancementProgress getAdvancementProgress(@NotNull Advancement advancement) {
		return player.getAdvancementProgress(advancement);
	}

	@Override
	public int getClientViewDistance() {
		return player.getClientViewDistance();
	}

	@NotNull
	@Override
	public String getLocale() {
		return player.getLocale();
	}

	@Override
	public void updateCommands() {
		player.updateCommands();
	}

	@Override
	public void openBook(@NotNull ItemStack itemStack) {
		player.openBook(itemStack);
	}

	@NotNull
	@Override
	public MainHand getMainHand() {
		return player.getMainHand();
	}

	@Override
	public boolean isGliding() {
		return player.isGliding();
	}

	@Override
	public void setGliding(boolean arg0) {
		player.setGliding(arg0);
	}

	@Override
	public boolean isSwimming() {
		return player.isSwimming();
	}

	@Override
	public void setSwimming(boolean swimming) {
		player.setSwimming(swimming);
	}

	@Deprecated
	@Override
	public boolean isRiptiding() {
		return player.isRiptiding();
	}

	@Override
	public boolean hasAI() {
		return player.hasAI();
	}

	@Override
	public void attack(@NotNull Entity entity) {
		player.attack(entity);
	}

	@Override
	public void swingMainHand() {
		player.swingMainHand();
	}

	@Override
	public void swingOffHand() {
		player.swingOffHand();
	}

	@Override
	public void setAI(boolean arg0) {
		player.setAI(arg0);
	}

	@Override
	public boolean isInvulnerable() {
		return player.isInvulnerable();
	}

	@Override
	public void setInvulnerable(boolean arg0) {
		player.setInvulnerable(arg0);
	}

	@Override
	public boolean isCollidable() {
		return player.isCollidable();
	}

	@Override
	public @NotNull Set<UUID> getCollidableExemptions() {
		return player.getCollidableExemptions();
	}

	@Nullable
	@Override
	public <T> T getMemory(@NotNull MemoryKey<T> memoryKey) {
		return player.getMemory(memoryKey);
	}

	@Override
	public <T> void setMemory(@NotNull MemoryKey<T> memoryKey, @Nullable T t) {
		player.setMemory(memoryKey, t);
	}

	@Override
	public void setCollidable(boolean arg0) {
		player.setCollidable(arg0);
	}

	@Override
	public boolean isSilent() {
		return player.isSilent();
	}

	@Override
	public void setSilent(boolean arg0) {
		player.setSilent(arg0);
	}

	@Override
	public boolean hasGravity() {
		return player.hasGravity();
	}

	@Override
	public void setGravity(boolean gravity) {
		player.setGravity(gravity);
	}

	@Override
	public void stopSound(@NotNull Sound paramSound) {
		player.stopSound(paramSound);
	}

	@Override
	public void stopSound(@NotNull String paramString) {
		player.stopSound(paramString);
	}

	@Override
	public int getPortalCooldown() {
		return player.getPortalCooldown();
	}

	@Override
	public void setPortalCooldown(int arg0) {
		player.setPortalCooldown(arg0);
	}

	@Override
	public boolean addScoreboardTag(@NotNull String arg0) {
		return player.addScoreboardTag(arg0);
	}

	@NotNull
	@Override
	public Set<String> getScoreboardTags() {
		return player.getScoreboardTags();
	}

	@Override
	public boolean removeScoreboardTag(@NotNull String arg0) {
		return player.removeScoreboardTag(arg0);
	}

	@NotNull
	@Override
	public PistonMoveReaction getPistonMoveReaction() {
		return player.getPistonMoveReaction();
	}

	@NotNull
	@Override
	public BlockFace getFacing() {
		return player.getFacing();
	}

	@NotNull
	@Override
	public Pose getPose() {
		return player.getPose();
	}

	@Override
	public void playSound(@NotNull Location arg0, @NotNull Sound arg1, @NotNull SoundCategory arg2, float arg3, float arg4) {
		player.playSound(arg0, arg1, arg2, arg3, arg4);
	}

	@Override
	public void playSound(@NotNull Location arg0, @NotNull String arg1, @NotNull SoundCategory arg2, float arg3, float arg4) {
		player.playSound(arg0, arg1, arg2, arg3, arg4);
	}

	@Override
	public void stopSound(@NotNull Sound arg0, SoundCategory arg1) {
		player.stopSound(arg0, arg1);
	}

	@Override
	public void stopSound(@NotNull String arg0, SoundCategory arg1) {
		player.stopSound(arg0, arg1);
	}

	@Override
	public InventoryView openMerchant(@NotNull Merchant arg0, boolean arg1) {
		return player.openMerchant(arg0, arg1);
	}

	@Override
	public void sendTitle(String arg0, String arg1, int arg2, int arg3, int arg4) {
		player.sendTitle(arg0, arg1, arg2, arg3, arg4);
	}

	@Override
	public boolean addPassenger(@NotNull Entity passenger) {
		return player.addPassenger(passenger);
	}

	@NotNull
	@Override
	public List<Entity> getPassengers() {
		return player.getPassengers();
	}

	@Override
	public boolean removePassenger(@NotNull Entity passenger) {
		return player.removePassenger(passenger);
	}

	@Override
	public double getHeight() {
		return player.getHeight();
	}

	@Override
	public double getWidth() {
		return player.getWidth();
	}

	@NotNull
	@Override
	public BoundingBox getBoundingBox() {
		return player.getBoundingBox();
	}

	@Override
	public int getCooldown(@NotNull Material material) {
		return player.getCooldown(material);
	}

	@Override
	public boolean hasCooldown(@NotNull Material material) {
		return player.hasCooldown(material);
	}

	@Override
	public void setCooldown(@NotNull Material material, int duration) {
		player.setCooldown(material, duration);
	}

	@NotNull
	@Override
	public PersistentDataContainer getPersistentDataContainer() {
		return player.getPersistentDataContainer();
	}

}
