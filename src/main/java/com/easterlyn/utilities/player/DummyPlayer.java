package com.easterlyn.utilities.player;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
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
 * A barebones Player implementation, mostly for use in chat.
 *
 * @author Jikoo
 */
public class DummyPlayer implements Player {

	@Override
	public void closeInventory() {}

	@NotNull
	@Override
	public Inventory getEnderChest() {
		throw new UnsupportedOperationException("DummyPlayer#getEnderChest");
	}

	@Override
	public int getExpToLevel() {
		return 0;
	}

	@Override
	public boolean discoverRecipe(@NotNull NamespacedKey namespacedKey) {
		return false;
	}

	@Override
	public int discoverRecipes(@NotNull Collection<NamespacedKey> collection) {
		return 0;
	}

	@Override
	public boolean undiscoverRecipe(@NotNull NamespacedKey namespacedKey) {
		return false;
	}

	@Override
	public int undiscoverRecipes(@NotNull Collection<NamespacedKey> keys) {
		return 0;
	}

	@Override
	public Entity getShoulderEntityLeft() {
		return null;
	}

	@Override
	public void setShoulderEntityLeft(Entity entity) {}

	@Override
	public Entity getShoulderEntityRight() {
		return null;
	}

	@Override
	public void setShoulderEntityRight(Entity entity) {}

	@NotNull
	@Override
	public GameMode getGameMode() {
		return GameMode.CREATIVE;
	}

	@NotNull
	@Override
	public PlayerInventory getInventory() {
		throw new UnsupportedOperationException("DummyPlayer#getInventory");
	}

	@NotNull
	@Override
	public ItemStack getItemInHand() {
		throw new UnsupportedOperationException("DummyPlayer#getItemInHand");
	}

	@NotNull
	@Override
	public ItemStack getItemOnCursor() {
		throw new UnsupportedOperationException("DummyPlayer#getItemOnCursor");
	}

	@NotNull
	@Override
	public String getName() {
		return "Dummy";
	}

	@NotNull
	@Override
	public InventoryView getOpenInventory() {
		throw new UnsupportedOperationException("DummyPlayer#getOpenInventory");
	}

	@Override
	public int getSleepTicks() {
		return 0;
	}

	@Override
	public boolean isBlocking() {
		return false;
	}

	@Override
	public boolean isSleeping() {
		return false;
	}

	@Override
	public InventoryView openEnchanting(Location arg0, boolean arg1) {
		return null;
	}

	@Override
	public InventoryView openInventory(@NotNull Inventory arg0) {
		return null;
	}

	@Override
	public void openInventory(@NotNull InventoryView arg0) {}

	@Override
	public InventoryView openWorkbench(Location arg0, boolean arg1) {
		return null;
	}

	@Override
	public void setGameMode(@NotNull GameMode arg0) {}

	@Override
	public void setItemInHand(ItemStack arg0) {}

	@Override
	public void setItemOnCursor(ItemStack arg0) {}

	@Override
	public boolean setWindowProperty(@NotNull Property arg0, int arg1) {
		return false;
	}

	@Override
	public boolean addPotionEffect(@NotNull PotionEffect arg0) {
		return false;
	}

	@Override
	public boolean addPotionEffect(@NotNull PotionEffect arg0, boolean arg1) {
		return false;
	}

	@Override
	public boolean addPotionEffects(@NotNull Collection<PotionEffect> arg0) {
		return false;
	}

	@NotNull
	@Override
	public Collection<PotionEffect> getActivePotionEffects() {
		throw new UnsupportedOperationException("DummyPlayer#getActivePotionEffects");
	}

	@Override
	public boolean getCanPickupItems() {
		return false;
	}

	@Override
	public EntityEquipment getEquipment() {
		return null;
	}

	@Override
	public double getEyeHeight() {
		return 0;
	}

	@Override
	public double getEyeHeight(boolean arg0) {
		return 0;
	}

	@NotNull
	@Override
	public Location getEyeLocation() {
		throw new UnsupportedOperationException("DummyPlayer#getEyeLocation");
	}

	@Override
	public Player getKiller() {
		return null;
	}

	@Override
	public double getLastDamage() {
		return 0;
	}

	@NotNull
	@Override
	public List<Block> getLastTwoTargetBlocks(Set<Material> arg0, int arg1) {
		throw new UnsupportedOperationException("DummyPlayer#getLastTwoTargetBlocks");
	}

	@Override
	public Block getTargetBlockExact(int i) {
		return null;
	}

	@Override
	public Block getTargetBlockExact(int i, @NotNull FluidCollisionMode fluidCollisionMode) {
		return null;
	}

	@Override
	public RayTraceResult rayTraceBlocks(double v) {
		return null;
	}

	@Override
	public RayTraceResult rayTraceBlocks(double v, @NotNull FluidCollisionMode fluidCollisionMode) {
		return null;
	}

	@NotNull
	@Override
	public Entity getLeashHolder() throws IllegalStateException {
		throw new UnsupportedOperationException("DummyPlayer#getLeashHolder");
	}

	@NotNull
	@Override
	public List<Block> getLineOfSight(Set<Material> arg0, int arg1) {
		throw new UnsupportedOperationException("DummyPlayer#getLineOfSight");
	}

	@Override
	public int getMaximumAir() {
		return 0;
	}

	@Override
	public int getMaximumNoDamageTicks() {
		return 0;
	}

	@Override
	public int getNoDamageTicks() {
		return 0;
	}

	@Override
	public int getRemainingAir() {
		return 0;
	}

	@Override
	public boolean getRemoveWhenFarAway() {
		return false;
	}

	@NotNull
	@Override
	public Block getTargetBlock(Set<Material> arg0, int arg1) {
		throw new UnsupportedOperationException("DummyPlayer#getTargetBlock");
	}

	@Override
	public boolean hasLineOfSight(@NotNull Entity arg0) {
		return false;
	}

	@Override
	public boolean hasPotionEffect(@NotNull PotionEffectType arg0) {
		return false;
	}

	@Override
	public PotionEffect getPotionEffect(@NotNull PotionEffectType type) {
		return null;
	}

	@Override
	public boolean isLeashed() {
		return false;
	}

	@Override
	public void removePotionEffect(@NotNull PotionEffectType arg0) {}

	@Override
	public void setCanPickupItems(boolean arg0) {}

	@Override
	public void setLastDamage(double arg0) {}

	@Override
	public boolean setLeashHolder(Entity arg0) {
		return false;
	}

	@Override
	public void setMaximumAir(int arg0) {}

	@Override
	public void setMaximumNoDamageTicks(int arg0) {}

	@Override
	public void setNoDamageTicks(int arg0) {}

	@Override
	public void setRemainingAir(int arg0) {}

	@Override
	public void setRemoveWhenFarAway(boolean arg0) {}

	@Override
	public boolean eject() {
		return false;
	}

	@Override
	public String getCustomName() {
		return null;
	}

	@Override
	public int getEntityId() {
		return 0;
	}

	@Override
	public float getFallDistance() {
		return 0;
	}

	@Override
	public int getFireTicks() {
		return 0;
	}

	@Override
	public EntityDamageEvent getLastDamageCause() {
		return null;
	}

	@NotNull
	@Override
	public Location getLocation() {
		return new Location(Bukkit.getWorlds().get(0), 0, 0, 0);
	}

	@Override
	public Location getLocation(Location arg0) {
		return arg0;
	}

	@Override
	public int getMaxFireTicks() {
		return 0;
	}

	@NotNull
	@Override
	public List<Entity> getNearbyEntities(double arg0, double arg1, double arg2) {
		throw new UnsupportedOperationException("DummyPlayer#getNearbyEntities");
	}

	@Override
	public Entity getPassenger() {
		return null;
	}

	@NotNull
	@Override
	public Server getServer() {
		return Bukkit.getServer();
	}

	@Deprecated
	@Override
	public boolean isPersistent() {
		return false;
	}

	@Deprecated
	@Override
	public void setPersistent(boolean b) {}

	@Override
	public int getTicksLived() {
		return 0;
	}

	@NotNull
	@Override
	public EntityType getType() {
		return EntityType.PLAYER;
	}

	@NotNull
	@Override
	public UUID getUniqueId() {
		return UUID.randomUUID();
	}

	@Override
	public Entity getVehicle() {
		return null;
	}

	@NotNull
	@Override
	public Vector getVelocity() {
		return new Vector();
	}

	@NotNull
	@Override
	public World getWorld() {
		return Bukkit.getWorlds().get(0);
	}

	@Deprecated
	@Override
	public void setRotation(float v, float v1) {}

	@Override
	public boolean isCustomNameVisible() {
		return false;
	}

	@Override
	public boolean isDead() {
		return false;
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	@Override
	public boolean isInsideVehicle() {
		return false;
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public boolean leaveVehicle() {
		return false;
	}

	@Override
	public void playEffect(@NotNull EntityEffect arg0) {}

	@Override
	public void remove() {}

	@Override
	public void setCustomName(String arg0) {}

	@Override
	public void setCustomNameVisible(boolean arg0) {}

	@Override
	public void setFallDistance(float arg0) {}

	@Override
	public void setFireTicks(int arg0) {}

	@Override
	public void setLastDamageCause(EntityDamageEvent arg0) {}

	@Override
	public boolean setPassenger(@NotNull Entity arg0) {
		return false;
	}

	@Override
	public void setTicksLived(int arg0) {}

	@Override
	public void setVelocity(@NotNull Vector arg0) {}

	@Override
	public boolean teleport(@NotNull Location arg0) {
		return false;
	}

	@Override
	public boolean teleport(@NotNull Entity arg0) {
		return false;
	}

	@Override
	public boolean teleport(@NotNull Location arg0, @NotNull TeleportCause arg1) {
		return false;
	}

	@Override
	public boolean teleport(@NotNull Entity arg0, @NotNull TeleportCause arg1) {
		return false;
	}

	@NotNull
	@Override
	public List<MetadataValue> getMetadata(@NotNull String arg0) {
		return Collections.emptyList();
	}

	@Override
	public boolean hasMetadata(@NotNull String arg0) {
		return false;
	}

	@Override
	public void removeMetadata(@NotNull String arg0, @NotNull Plugin arg1) {}

	@Override
	public void setMetadata(@NotNull String arg0, @NotNull MetadataValue arg1) {}

	@Override
	public void sendMessage(@NotNull String message) {}

	@Override
	public void sendMessage(@NotNull String[] messages) {}

	@NotNull
	@Override
	public PermissionAttachment addAttachment(@NotNull Plugin plugin) {
		throw new UnsupportedOperationException("DummyPlayer#addAttachment");
	}

	@Override
	public PermissionAttachment addAttachment(@NotNull Plugin plugin, int ticks) {
		throw new UnsupportedOperationException("DummyPlayer#addAttachment");
	}

	@NotNull
	@Override
	public PermissionAttachment addAttachment(@NotNull Plugin plugin, @NotNull String name, boolean value) {
		throw new UnsupportedOperationException("DummyPlayer#addAttachment");
	}

	@Override
	public PermissionAttachment addAttachment(@NotNull Plugin plugin, @NotNull String name, boolean value, int ticks) {
		throw new UnsupportedOperationException("DummyPlayer#addAttachment");
	}

	@NotNull
	@Override
	public Set<PermissionAttachmentInfo> getEffectivePermissions() {
		return Collections.emptySet();
	}

	@Override
	public boolean hasPermission(@NotNull String name) {
		return false;
	}

	@Override
	public boolean hasPermission(@NotNull Permission perm) {
		return false;
	}

	@Override
	public boolean isPermissionSet(@NotNull String name) {
		return false;
	}

	@Override
	public boolean isPermissionSet(@NotNull Permission perm) {
		return false;
	}

	@Override
	public void recalculatePermissions() {}

	@Override
	public void removeAttachment(@NotNull PermissionAttachment attachment) {}

	@Override
	public boolean isOp() {
		return false;
	}

	@Override
	public void setOp(boolean arg0) {}

	@Override
	public void damage(double arg0) {}

	@Override
	public void damage(double arg0, Entity arg1) {}

	@Override
	public double getHealth() {
		return 0;
	}

	@Override
	public double getMaxHealth() {
		return 0;
	}

	@Override
	public void resetMaxHealth() {}

	@Override
	public void setHealth(double arg0) {}

	@Override
	public double getAbsorptionAmount() {
		return 0;
	}

	@Override
	public void setAbsorptionAmount(double v) {}

	@Override
	public void setMaxHealth(double arg0) {}

	@NotNull
	@Override
	public <T extends Projectile> T launchProjectile(@NotNull Class<? extends T> arg0) {
		throw new UnsupportedOperationException("DummyPlayer#launchProjectile");
	}

	@NotNull
	@Override
	public <T extends Projectile> T launchProjectile(@NotNull Class<? extends T> arg0, Vector arg1) {
		throw new UnsupportedOperationException("DummyPlayer#launchProjectile");
	}

	@Override
	public void abandonConversation(@NotNull Conversation arg0) {}

	@Override
	public void abandonConversation(@NotNull Conversation arg0, @NotNull ConversationAbandonedEvent arg1) {}

	@Override
	public void acceptConversationInput(@NotNull String arg0) {}

	@Override
	public boolean beginConversation(@NotNull Conversation arg0) {
		return false;
	}

	@Override
	public boolean isConversing() {
		return false;
	}

	@Override
	public long getFirstPlayed() {
		return 0;
	}

	@Override
	public long getLastPlayed() {
		return 0;
	}

	@Override
	public Player getPlayer() {
		return this;
	}

	@Override
	public boolean hasPlayedBefore() {
		return true;
	}

	@Override
	public boolean isBanned() {
		return false;
	}

	@Override
	public boolean isOnline() {
		return true;
	}

	@Override
	public boolean isWhitelisted() {
		return false;
	}

	@Override
	public void setWhitelisted(boolean arg0) {}

	@NotNull
	@Override
	public Map<String, Object> serialize() {
		throw new UnsupportedOperationException("DummyPlayer#serialize");
	}

	@NotNull
	@Override
	public Set<String> getListeningPluginChannels() {
		return Collections.emptySet();
	}

	@Override
	public void sendPluginMessage(@NotNull Plugin arg0, @NotNull String arg1, @NotNull byte[] arg2) {}

	@Override
	public boolean canSee(@NotNull Player arg0) {
		return false;
	}

	@Override
	public void chat(@NotNull String arg0) {}

	@Override
	public void decrementStatistic(@NotNull Statistic arg0) throws IllegalArgumentException {}

	@Override
	public void decrementStatistic(@NotNull Statistic arg0, int arg1) throws IllegalArgumentException {}

	@Override
	public void decrementStatistic(@NotNull Statistic arg0, @NotNull Material arg1) throws IllegalArgumentException {}

	@Override
	public void decrementStatistic(@NotNull Statistic arg0, @NotNull EntityType arg1) throws IllegalArgumentException {}

	@Override
	public void decrementStatistic(@NotNull Statistic arg0, @NotNull Material arg1, int arg2)
			throws IllegalArgumentException {}

	@Override
	public void decrementStatistic(@NotNull Statistic arg0, @NotNull EntityType arg1, int arg2) {}

	@Override
	public InetSocketAddress getAddress() {
		return null;
	}

	@Override
	public boolean getAllowFlight() {
		return false;
	}

	@Override
	public Location getBedSpawnLocation() {
		return null;
	}

	@NotNull
	@Override
	public Location getCompassTarget() {
		throw new UnsupportedOperationException("DummyPlayer#getCompassTarget");
	}

	@NotNull
	@Override
	public String getDisplayName() {
		return getName();
	}

	@Override
	public float getExhaustion() {
		return 0;
	}

	@Override
	public float getExp() {
		return 0;
	}

	@Override
	public float getFlySpeed() {
		return 0;
	}

	@Override
	public int getFoodLevel() {
		return 0;
	}

	@Override
	public double getHealthScale() {
		return 0;
	}

	@Override
	public int getLevel() {
		return 0;
	}

	@NotNull
	@Override
	public String getPlayerListName() {
		return getName();
	}

	@Override
	public long getPlayerTime() {
		return 0;
	}

	@Override
	public long getPlayerTimeOffset() {
		return 0;
	}

	@Override
	public WeatherType getPlayerWeather() {
		return null;
	}

	@Override
	public float getSaturation() {
		return 0;
	}

	@NotNull
	@Override
	public Scoreboard getScoreboard() {
		throw new UnsupportedOperationException("DummyPlayer#getScoreboard");
	}

	@Override
	public int getStatistic(@NotNull Statistic arg0) throws IllegalArgumentException {
		return 0;
	}

	@Override
	public int getStatistic(@NotNull Statistic arg0, @NotNull Material arg1) throws IllegalArgumentException {
		return 0;
	}

	@Override
	public int getStatistic(@NotNull Statistic arg0, @NotNull EntityType arg1) throws IllegalArgumentException {
		return 0;
	}

	@Override
	public int getTotalExperience() {
		return 0;
	}

	@Override
	public float getWalkSpeed() {
		return 0;
	}

	@Override
	public void giveExp(int arg0) {}

	@Override
	public void giveExpLevels(int arg0) {}

	@Override
	public void hidePlayer(@NotNull Player arg0) {}

	@Override
	public void hidePlayer(@NotNull Plugin plugin, @NotNull Player player) {}

	@Override
	public void incrementStatistic(@NotNull Statistic arg0) throws IllegalArgumentException {}

	@Override
	public void incrementStatistic(@NotNull Statistic arg0, int arg1) throws IllegalArgumentException {}

	@Override
	public void incrementStatistic(@NotNull Statistic arg0, @NotNull Material arg1) throws IllegalArgumentException {}

	@Override
	public void incrementStatistic(@NotNull Statistic arg0, @NotNull EntityType arg1) throws IllegalArgumentException {}

	@Override
	public void incrementStatistic(@NotNull Statistic arg0, @NotNull Material arg1, int arg2)
			throws IllegalArgumentException {}

	@Override
	public void incrementStatistic(@NotNull Statistic arg0, @NotNull EntityType arg1, int arg2)
			throws IllegalArgumentException {}

	@Override
	public boolean isFlying() {
		return false;
	}

	@Override
	public boolean isHealthScaled() {
		return false;
	}

	@Override
	public boolean isOnGround() {
		return false;
	}

	@Override
	public boolean isPlayerTimeRelative() {
		return false;
	}

	@Override
	public boolean isSleepingIgnored() {
		return true;
	}

	@Override
	public boolean isSneaking() {
		return false;
	}

	@Override
	public boolean isSprinting() {
		return false;
	}

	@Override
	public void kickPlayer(String arg0) {}

	@Override
	public void loadData() {}

	@Override
	public boolean performCommand(@NotNull String command) {
		return false;
	}

	@Override
	public void playEffect(@NotNull Location arg0, @NotNull Effect arg1, int arg2) {}

	@Override
	public <T> void playEffect(@NotNull Location arg0, @NotNull Effect arg1, T arg2) {}

	@Override
	public void playNote(@NotNull Location arg0, byte arg1, byte arg2) {}

	@Override
	public void playNote(@NotNull Location arg0, @NotNull Instrument arg1, @NotNull Note arg2) {}

	@Override
	public void playSound(@NotNull Location arg0, @NotNull Sound arg1, float arg2, float arg3) {}

	@Override
	public void playSound(@NotNull Location arg0, @NotNull String arg1, float arg2, float arg3) {}

	@Override
	public void resetPlayerTime() {}

	@Override
	public void resetPlayerWeather() {}

	@Override
	public void saveData() {}

	@Override
	public void sendBlockChange(@NotNull Location arg0, @NotNull Material arg1, byte arg2) {}

	@Override
	public void sendBlockChange(@NotNull Location location, @NotNull BlockData blockData) {}

	@Override
	public boolean sendChunkChange(@NotNull Location arg0, int arg1, int arg2, int arg3, @NotNull byte[] arg4) {
		return false;
	}

	@Override
	public void sendMap(@NotNull MapView arg0) {}

	@Override
	public void sendRawMessage(@NotNull String arg0) {}

	@Override
	public void sendSignChange(@NotNull Location arg0, String[] arg1) throws IllegalArgumentException {}

	@Override
	public void sendSignChange(@NotNull Location location, @Nullable String[] strings,
			@NotNull DyeColor dyeColor) throws IllegalArgumentException {}

	@Override
	public void setAllowFlight(boolean arg0) {}

	@Override
	public void setBedSpawnLocation(Location arg0) {}

	@Override
	public void setBedSpawnLocation(Location arg0, boolean arg1) {}

	@Override
	public boolean sleep(@NotNull Location location, boolean b) {
		return false;
	}

	@Override
	public void wakeup(boolean b) {

	}

	@NotNull
	@Override
	public Location getBedLocation() {
		throw new UnsupportedOperationException("DummyPlayer#getBedLocation");
	}

	@Override
	public void setCompassTarget(@NotNull Location arg0) {}

	@Override
	public void setDisplayName(String displayName) {
	}

	@Override
	public void setExhaustion(float arg0) {}

	@Override
	public void setExp(float arg0) {}

	@Override
	public void setFlySpeed(float arg0) throws IllegalArgumentException {}

	@Override
	public void setFlying(boolean arg0) {}

	@Override
	public void setFoodLevel(int arg0) {}

	@Override
	public void setHealthScale(double arg0) throws IllegalArgumentException {}

	@Override
	public void setHealthScaled(boolean arg0) {}

	@Override
	public void setLevel(int arg0) {}

	@Override
	public void setPlayerListName(String arg0) {}

	@Deprecated
	@Override
	public String getPlayerListHeader() {
		return null;
	}

	@Deprecated
	@Override
	public String getPlayerListFooter() {
		return null;
	}

	@Deprecated
	@Override
	public void setPlayerListHeader(String s) {}

	@Deprecated
	@Override
	public void setPlayerListFooter(String s) {}

	@Deprecated
	@Override
	public void setPlayerListHeaderFooter(String s, String s1) {}

	@Override
	public void setPlayerTime(long arg0, boolean arg1) {}

	@Override
	public void setPlayerWeather(@NotNull WeatherType arg0) {}

	@Override
	public void setResourcePack(@NotNull String arg0) {}

	@Override
	public void setSaturation(float arg0) {}

	@Override
	public void setScoreboard(@NotNull Scoreboard arg0) throws IllegalArgumentException,
			IllegalStateException {}

	@Override
	public void setSleepingIgnored(boolean arg0) {}

	@Override
	public void setSneaking(boolean arg0) {}

	@Override
	public void setSprinting(boolean arg0) {}

	@Override
	public void setStatistic(@NotNull Statistic arg0, int arg1) throws IllegalArgumentException {}

	@Override
	public void setStatistic(@NotNull Statistic arg0, @NotNull Material arg1, int arg2)
			throws IllegalArgumentException {}

	@Override
	public void setStatistic(@NotNull Statistic arg0, @NotNull EntityType arg1, int arg2) {}

	@Override
	public void setTexturePack(@NotNull String arg0) {}

	@Override
	public void setTotalExperience(int arg0) {}

	@Override
	public void sendExperienceChange(float v) {}

	@Override
	public void sendExperienceChange(float v, int i) {}

	@Override
	public void setWalkSpeed(float arg0) throws IllegalArgumentException {}

	@Override
	public void showPlayer(@NotNull Player arg0) {}

	@Override
	public void showPlayer(@NotNull Plugin plugin, @NotNull Player player) {}

	@NotNull
	@Override
	public Spigot spigot() {
		throw new UnsupportedOperationException("DummyPlayer#spigot");
	}

	@Override
	public void updateInventory() {}

	@Override
	public Entity getSpectatorTarget() {
		return null;
	}

	@Override
	public void resetTitle() {}

	@Override
	public void sendTitle(String arg0, String arg1) {}

	@Override
	public void setSpectatorTarget(Entity arg0) {}

	@Override
	public InventoryView openMerchant(@NotNull Villager trader, boolean force) {
		return null;
	}

	@Override
	public AttributeInstance getAttribute(@NotNull Attribute attribute) {
		return null;
	}

	@Override
	public boolean isGlowing() {
		return false;
	}

	@Override
	public void setGlowing(boolean flag) {}

	@Override
	public void spawnParticle(@NotNull Particle particle, @NotNull Location location, int count) {}

	@Override
	public <T> void spawnParticle(@NotNull Particle particle, @NotNull Location location, int count, T data) {}

	@Override
	public void spawnParticle(@NotNull Particle particle, double x, double y, double z, int count) {}

	@Override
	public <T> void spawnParticle(@NotNull Particle particle, double x, double y, double z, int count, T data) {}

	@Override
	public void spawnParticle(@NotNull Particle particle, @NotNull Location location, int count, double offsetX,
							  double offsetY, double offsetZ) {}

	@Override
	public <T> void spawnParticle(@NotNull Particle particle, @NotNull Location location, int count, double offsetX,
								  double offsetY, double offsetZ, T data) {}

	@Override
	public void spawnParticle(@NotNull Particle particle, @NotNull Location location, int count, double offsetX,
							  double offsetY, double offsetZ, double extra) {}

	@Override
	public void spawnParticle(@NotNull Particle particle, double x, double y, double z, int count,
							  double offsetX, double offsetY, double offsetZ) {}

	@Override
	public <T> void spawnParticle(@NotNull Particle particle, @NotNull Location location, int count, double offsetX,
								  double offsetY, double offsetZ, double extra, T data) {}

	@Override
	public <T> void spawnParticle(@NotNull Particle particle, double x, double y, double z, int count,
								  double offsetX, double offsetY, double offsetZ, T data) {}

	@Override
	public void spawnParticle(@NotNull Particle particle, double x, double y, double z, int count,
							  double offsetX, double offsetY, double offsetZ, double extra) {}

	@Override
	public <T> void spawnParticle(@NotNull Particle particle, double x, double y, double z, int count,
								  double offsetX, double offsetY, double offsetZ, double extra, T data) {}

	@NotNull
	@Override
	public AdvancementProgress getAdvancementProgress(@NotNull Advancement advancement) {
		throw new UnsupportedOperationException("DummyPlayer#getAdvancementProgress");
	}

	@Override
	public int getClientViewDistance() {
		return 0;
	}

	@NotNull
	@Override
	public String getLocale() {
		return "en_us";
	}

	@Override
	public void updateCommands() {}

	@Override
	public void openBook(@NotNull ItemStack itemStack) {}

	@NotNull
	@Override
	public MainHand getMainHand() {
		return MainHand.RIGHT;
	}

	@Override
	public boolean isGliding() {
		return false;
	}

	@Override
	public void setGliding(boolean arg0) {}

	@Override
	public boolean isSwimming() {
		return false;
	}

	@Override
	public void setSwimming(boolean b) {}

	@Deprecated
	@Override
	public boolean isRiptiding() {
		return false;
	}

	@Override
	public boolean hasAI() {
		return false;
	}

	@Override
	public void setAI(boolean arg0) {}

	@Override
	public boolean isInvulnerable() {
		return false;
	}

	@Override
	public void setInvulnerable(boolean arg0) {}

	@Override
	public boolean isCollidable() {
		return false;
	}

	@Nullable
	@Override
	public <T> T getMemory(@NotNull MemoryKey<T> memoryKey) {
		return null;
	}

	@Override
	public <T> void setMemory(@NotNull MemoryKey<T> memoryKey, @Nullable T t) {}

	@Override
	public void setCollidable(boolean arg0) {}

	@Override
	public boolean isSilent() {
		return false;
	}

	@Override
	public void setSilent(boolean arg0) {}

	@Override
	public boolean hasGravity() {
		return false;
	}

	@Override
	public void setGravity(boolean gravity) {}

	@Override
	public void stopSound(@NotNull Sound paramSound) {}

	@Override
	public void stopSound(@NotNull String paramString) {}

	@Override
	public boolean isHandRaised() {
		return false;
	}

	@Override
	public int getPortalCooldown() {
		return 0;
	}

	@Override
	public void setPortalCooldown(int arg0) {}

	@Override
	public boolean addScoreboardTag(@NotNull String arg0) {
		return false;
	}

	@NotNull
	@Override
	public Set<String> getScoreboardTags() {
		throw new UnsupportedOperationException("DummyPlayer#getScoreboardTags");
	}

	@Override
	public boolean removeScoreboardTag(@NotNull String arg0) {
		return false;
	}

	@NotNull
	@Override
	public PistonMoveReaction getPistonMoveReaction() {
		return PistonMoveReaction.IGNORE;
	}

	@NotNull
	@Override
	public BlockFace getFacing() {
		return BlockFace.NORTH;
	}

	@NotNull
	@Override
	public Pose getPose() {
		throw new UnsupportedOperationException("DummyPlayer#getPose");
	}

	@Override
	public void playSound(@NotNull Location arg0, @NotNull Sound arg1, @NotNull SoundCategory arg2, float arg3, float arg4) {}

	@Override
	public void playSound(@NotNull Location arg0, @NotNull String arg1, @NotNull SoundCategory arg2, float arg3, float arg4) {}

	@Override
	public void stopSound(@NotNull Sound arg0, SoundCategory arg1) {}

	@Override
	public void stopSound(@NotNull String arg0, SoundCategory arg1) {}

	@Override
	public InventoryView openMerchant(@NotNull Merchant arg0, boolean arg1) {
		return null;
	}

	@Override
	public void sendTitle(String arg0, String arg1, int arg2, int arg3, int arg4) {}

	@Override
	public boolean addPassenger(@NotNull Entity passenger) {
		return false;
	}

	@NotNull
	@Override
	public List<Entity> getPassengers() {
		return Collections.emptyList();
	}

	@Override
	public boolean removePassenger(@NotNull Entity passenger) {
		return false;
	}

	@Override
	public void setResourcePack(@NotNull String arg0, @NotNull byte[] arg1) {}

	@Override
	public double getHeight() {
		return 0;
	}

	@Override
	public double getWidth() {
		return 0;
	}

	@NotNull
	@Override
	public BoundingBox getBoundingBox() {
		throw new UnsupportedOperationException("DummyPlayer#getBoundingBox");
	}

	@Override
	public int getCooldown(@NotNull Material arg0) {
		return 0;
	}

	@Override
	public boolean hasCooldown(@NotNull Material arg0) {
		return false;
	}

	@Override
	public void setCooldown(@NotNull Material arg0, int arg1) {}

	@NotNull
	@Override
	public PersistentDataContainer getPersistentDataContainer() {
		throw new UnsupportedOperationException("DummyPlayer#getPeristentDataContainer");
	}
}
