package co.sblock.data;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Achievement;
import org.bukkit.Effect;
import org.bukkit.EntityEffect;
import org.bukkit.GameMode;
import org.bukkit.Instrument;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.Server;
import org.bukkit.Sound;
import org.bukkit.Statistic;
import org.bukkit.WeatherType;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.InventoryView.Property;
import org.bukkit.map.MapView;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.Vector;

public class MockPlayer implements Player
{

	@Override
	public void closeInventory() {
		// Not in my task list mk
		
	}

	@Override
	public Inventory getEnderChest() {
		// Not in my task list mk
		return null;
	}

	@Override
	public int getExpToLevel() {
		// Not in my task list mk
		return 0;
	}

	@Override
	public GameMode getGameMode() {
		// Not in my task list mk
		return null;
	}

	@Override
	public PlayerInventory getInventory() {
		// Not in my task list mk
		return null;
	}

	@Override
	public ItemStack getItemInHand() {
		// Not in my task list mk
		return null;
	}

	@Override
	public ItemStack getItemOnCursor() {
		// Not in my task list mk
		return null;
	}

	@Override
	public String getName() {
		// Not in my task list mk
		return "Test Player";
	}

	@Override
	public InventoryView getOpenInventory() {
		// Not in my task list mk
		return null;
	}

	@Override
	public int getSleepTicks() {
		// Not in my task list mk
		return 0;
	}

	@Override
	public boolean isBlocking() {
		// Not in my task list mk
		return false;
	}

	@Override
	public boolean isSleeping() {
		// Not in my task list mk
		return false;
	}

	@Override
	public InventoryView openEnchanting(Location arg0, boolean arg1) {
		// Not in my task list mk
		return null;
	}

	@Override
	public InventoryView openInventory(Inventory arg0) {
		// Not in my task list mk
		return null;
	}

	@Override
	public void openInventory(InventoryView arg0) {
		// Not in my task list mk
		
	}

	@Override
	public InventoryView openWorkbench(Location arg0, boolean arg1) {
		// Not in my task list mk
		return null;
	}

	@Override
	public void setGameMode(GameMode arg0) {
		// Not in my task list mk
		
	}

	@Override
	public void setItemInHand(ItemStack arg0) {
		// Not in my task list mk
		
	}

	@Override
	public void setItemOnCursor(ItemStack arg0) {
		// Not in my task list mk
		
	}

	@Override
	public boolean setWindowProperty(Property arg0, int arg1) {
		// Not in my task list mk
		return false;
	}

	@Override
	public int _INVALID_getLastDamage() {
		// Not in my task list mk
		return 0;
	}

	@Override
	public void _INVALID_setLastDamage(int arg0) {
		// Not in my task list mk
		
	}

	@Override
	public boolean addPotionEffect(PotionEffect arg0) {
		// Not in my task list mk
		return false;
	}

	@Override
	public boolean addPotionEffect(PotionEffect arg0, boolean arg1) {
		// Not in my task list mk
		return false;
	}

	@Override
	public boolean addPotionEffects(Collection<PotionEffect> arg0) {
		// Not in my task list mk
		return false;
	}

	@Override
	public Collection<PotionEffect> getActivePotionEffects() {
		// Not in my task list mk
		return null;
	}

	@Override
	public boolean getCanPickupItems() {
		// Not in my task list mk
		return false;
	}

	@Override
	public String getCustomName() {
		// Not in my task list mk
		return null;
	}

	@Override
	public EntityEquipment getEquipment() {
		// Not in my task list mk
		return null;
	}

	@Override
	public double getEyeHeight() {
		// Not in my task list mk
		return 0;
	}

	@Override
	public double getEyeHeight(boolean arg0) {
		// Not in my task list mk
		return 0;
	}

	@Override
	public Location getEyeLocation() {
		// Not in my task list mk
		return null;
	}

	@Override
	public Player getKiller() {
		// Not in my task list mk
		return null;
	}

	@Override
	public double getLastDamage() {
		// Not in my task list mk
		return 0;
	}

	@Override
	public List<Block> getLastTwoTargetBlocks(HashSet<Byte> arg0, int arg1) {
		// Not in my task list mk
		return null;
	}

	@Override
	public Entity getLeashHolder() throws IllegalStateException {
		// Not in my task list mk
		return null;
	}

	@Override
	public List<Block> getLineOfSight(HashSet<Byte> arg0, int arg1) {
		// Not in my task list mk
		return null;
	}

	@Override
	public int getMaximumAir() {
		// Not in my task list mk
		return 0;
	}

	@Override
	public int getMaximumNoDamageTicks() {
		// Not in my task list mk
		return 0;
	}

	@Override
	public int getNoDamageTicks() {
		// Not in my task list mk
		return 0;
	}

	@Override
	public int getRemainingAir() {
		// Not in my task list mk
		return 0;
	}

	@Override
	public boolean getRemoveWhenFarAway() {
		// Not in my task list mk
		return false;
	}

	@Override
	public Block getTargetBlock(HashSet<Byte> arg0, int arg1) {
		// Not in my task list mk
		return null;
	}

	@Override
	public boolean hasLineOfSight(Entity arg0) {
		// Not in my task list mk
		return false;
	}

	@Override
	public boolean hasPotionEffect(PotionEffectType arg0) {
		// Not in my task list mk
		return false;
	}

	@Override
	public boolean isCustomNameVisible() {
		// Not in my task list mk
		return false;
	}

	@Override
	public boolean isLeashed() {
		// Not in my task list mk
		return false;
	}

	@Override
	public void removePotionEffect(PotionEffectType arg0) {
		// Not in my task list mk
		
	}

	@Override
	public void setCanPickupItems(boolean arg0) {
		// Not in my task list mk
		
	}

	@Override
	public void setCustomName(String arg0) {
		// Not in my task list mk
		
	}

	@Override
	public void setCustomNameVisible(boolean arg0) {
		// Not in my task list mk
		
	}

	@Override
	public void setLastDamage(double arg0) {
		// Not in my task list mk
		
	}

	@Override
	public boolean setLeashHolder(Entity arg0) {
		// Not in my task list mk
		return false;
	}

	@Override
	public void setMaximumAir(int arg0) {
		// Not in my task list mk
		
	}

	@Override
	public void setMaximumNoDamageTicks(int arg0) {
		// Not in my task list mk
		
	}

	@Override
	public void setNoDamageTicks(int arg0) {
		// Not in my task list mk
		
	}

	@Override
	public void setRemainingAir(int arg0) {
		// Not in my task list mk
		
	}

	@Override
	public void setRemoveWhenFarAway(boolean arg0) {
		// Not in my task list mk
		
	}

	@Override
	public Arrow shootArrow() {
		// Not in my task list mk
		return null;
	}

	@Override
	public Egg throwEgg() {
		// Not in my task list mk
		return null;
	}

	@Override
	public Snowball throwSnowball() {
		// Not in my task list mk
		return null;
	}

	@Override
	public boolean eject() {
		// Not in my task list mk
		return false;
	}

	@Override
	public int getEntityId() {
		// Not in my task list mk
		return 0;
	}

	@Override
	public float getFallDistance() {
		// Not in my task list mk
		return 0;
	}

	@Override
	public int getFireTicks() {
		// Not in my task list mk
		return 0;
	}

	@Override
	public EntityDamageEvent getLastDamageCause() {
		// Not in my task list mk
		return null;
	}

	@Override
	public Location getLocation() {
		// Not in my task list mk
		return null;
	}

	@Override
	public Location getLocation(Location arg0) {
		// Not in my task list mk
		return null;
	}

	@Override
	public int getMaxFireTicks() {
		// Not in my task list mk
		return 0;
	}

	@Override
	public List<Entity> getNearbyEntities(double arg0, double arg1,
			double arg2) {
		// Not in my task list mk
		return null;
	}

	@Override
	public Entity getPassenger() {
		// Not in my task list mk
		return null;
	}

	@Override
	public Server getServer() {
		// Not in my task list mk
		return null;
	}

	@Override
	public int getTicksLived() {
		// Not in my task list mk
		return 0;
	}

	@Override
	public EntityType getType() {
		// Not in my task list mk
		return null;
	}

	@Override
	public UUID getUniqueId() {
		return UUID.randomUUID();
	}

	@Override
	public Entity getVehicle() {
		// Not in my task list mk
		return null;
	}

	@Override
	public Vector getVelocity() {
		// Not in my task list mk
		return null;
	}

	@Override
	public World getWorld() {
		return new MockWorld();
	}

	@Override
	public boolean isDead() {
		// Not in my task list mk
		return false;
	}

	@Override
	public boolean isEmpty() {
		// Not in my task list mk
		return false;
	}

	@Override
	public boolean isInsideVehicle() {
		// Not in my task list mk
		return false;
	}

	@Override
	public boolean isValid() {
		// Not in my task list mk
		return false;
	}

	@Override
	public boolean leaveVehicle() {
		// Not in my task list mk
		return false;
	}

	@Override
	public void playEffect(EntityEffect arg0) {
		// Not in my task list mk
		
	}

	@Override
	public void remove() {
		// Not in my task list mk
		
	}

	@Override
	public void setFallDistance(float arg0) {
		// Not in my task list mk
		
	}

	@Override
	public void setFireTicks(int arg0) {
		// Not in my task list mk
		
	}

	@Override
	public void setLastDamageCause(EntityDamageEvent arg0) {
		// Not in my task list mk
		
	}

	@Override
	public boolean setPassenger(Entity arg0) {
		// Not in my task list mk
		return false;
	}

	@Override
	public void setTicksLived(int arg0) {
		// Not in my task list mk
		
	}

	@Override
	public void setVelocity(Vector arg0) {
		// Not in my task list mk
		
	}

	@Override
	public boolean teleport(Location arg0) {
		// Not in my task list mk
		return false;
	}

	@Override
	public boolean teleport(Entity arg0) {
		// Not in my task list mk
		return false;
	}

	@Override
	public boolean teleport(Location arg0, TeleportCause arg1) {
		// Not in my task list mk
		return false;
	}

	@Override
	public boolean teleport(Entity arg0, TeleportCause arg1) {
		// Not in my task list mk
		return false;
	}

	@Override
	public List<MetadataValue> getMetadata(String arg0) {
		// Not in my task list mk
		return null;
	}

	@Override
	public boolean hasMetadata(String arg0) {
		// Not in my task list mk
		return false;
	}

	@Override
	public void removeMetadata(String arg0, Plugin arg1) {
		// Not in my task list mk
		
	}

	@Override
	public void setMetadata(String arg0, MetadataValue arg1) {
		// Not in my task list mk
		
	}

	@Override
	public void _INVALID_damage(int arg0) {
		// Not in my task list mk
		
	}

	@Override
	public void _INVALID_damage(int arg0, Entity arg1) {
		// Not in my task list mk
		
	}

	@Override
	public int _INVALID_getHealth() {
		// Not in my task list mk
		return 0;
	}

	@Override
	public int _INVALID_getMaxHealth() {
		// Not in my task list mk
		return 0;
	}

	@Override
	public void _INVALID_setHealth(int arg0) {
		// Not in my task list mk
		
	}

	@Override
	public void _INVALID_setMaxHealth(int arg0) {
		// Not in my task list mk
		
	}

	@Override
	public void damage(double arg0) {
		// Not in my task list mk
		
	}

	@Override
	public void damage(double arg0, Entity arg1) {
		// Not in my task list mk
		
	}

	@Override
	public double getHealth() {
		// Not in my task list mk
		return 0;
	}

	@Override
	public double getMaxHealth() {
		// Not in my task list mk
		return 0;
	}

	@Override
	public void resetMaxHealth() {
		// Not in my task list mk
		
	}

	@Override
	public void setHealth(double arg0) {
		// Not in my task list mk
		
	}

	@Override
	public void setMaxHealth(double arg0) {
		// Not in my task list mk
		
	}

	@Override
	public <T extends Projectile> T launchProjectile(Class<? extends T> arg0) {
		// Not in my task list mk
		return null;
	}

	@Override
	public <T extends Projectile> T launchProjectile(
			Class<? extends T> arg0, Vector arg1) {
		// Not in my task list mk
		return null;
	}

	@Override
	public PermissionAttachment addAttachment(Plugin arg0) {
		// Not in my task list mk
		return null;
	}

	@Override
	public PermissionAttachment addAttachment(Plugin arg0, int arg1) {
		// Not in my task list mk
		return null;
	}

	@Override
	public PermissionAttachment addAttachment(Plugin arg0, String arg1,
			boolean arg2) {
		// Not in my task list mk
		return null;
	}

	@Override
	public PermissionAttachment addAttachment(Plugin arg0, String arg1,
			boolean arg2, int arg3) {
		// Not in my task list mk
		return null;
	}

	@Override
	public Set<PermissionAttachmentInfo> getEffectivePermissions() {
		// Not in my task list mk
		return null;
	}

	@Override
	public boolean hasPermission(String arg0) {
		// Not in my task list mk
		return false;
	}

	@Override
	public boolean hasPermission(Permission arg0) {
		// Not in my task list mk
		return false;
	}

	@Override
	public boolean isPermissionSet(String arg0) {
		// Not in my task list mk
		return false;
	}

	@Override
	public boolean isPermissionSet(Permission arg0) {
		// Not in my task list mk
		return false;
	}

	@Override
	public void recalculatePermissions() {
		// Not in my task list mk
		
	}

	@Override
	public void removeAttachment(PermissionAttachment arg0) {
		// Not in my task list mk
		
	}

	@Override
	public boolean isOp() {
		// Not in my task list mk
		return false;
	}

	@Override
	public void setOp(boolean arg0) {
		// Not in my task list mk
		
	}

	@Override
	public void abandonConversation(Conversation arg0) {
		// Not in my task list mk
		
	}

	@Override
	public void abandonConversation(Conversation arg0,
			ConversationAbandonedEvent arg1) {
		// Not in my task list mk
		
	}

	@Override
	public void acceptConversationInput(String arg0) {
		// Not in my task list mk
		
	}

	@Override
	public boolean beginConversation(Conversation arg0) {
		// Not in my task list mk
		return false;
	}

	@Override
	public boolean isConversing() {
		// Not in my task list mk
		return false;
	}

	@Override
	public void sendMessage(String arg0) {
		// Not in my task list mk
		
	}

	@Override
	public void sendMessage(String[] arg0) {
		// Not in my task list mk
		
	}

	@Override
	public long getFirstPlayed() {
		// Not in my task list mk
		return 0;
	}

	@Override
	public long getLastPlayed() {
		// Not in my task list mk
		return 0;
	}

	@Override
	public Player getPlayer() {
		// Not in my task list mk
		return null;
	}

	@Override
	public boolean hasPlayedBefore() {
		// Not in my task list mk
		return false;
	}

	@Override
	public boolean isBanned() {
		// Not in my task list mk
		return false;
	}

	@Override
	public boolean isOnline() {
		// Not in my task list mk
		return false;
	}

	@Override
	public boolean isWhitelisted() {
		// Not in my task list mk
		return false;
	}

	@Override
	public void setBanned(boolean arg0) {
		// Not in my task list mk
		
	}

	@Override
	public void setWhitelisted(boolean arg0) {
		// Not in my task list mk
		
	}

	@Override
	public Map<String, Object> serialize() {
		// Not in my task list mk
		return null;
	}

	@Override
	public Set<String> getListeningPluginChannels() {
		// Not in my task list mk
		return null;
	}

	@Override
	public void sendPluginMessage(Plugin arg0, String arg1, byte[] arg2) {
		// Not in my task list mk
		
	}

	@Override
	public void awardAchievement(Achievement arg0) {
		// Not in my task list mk
		
	}

	@Override
	public boolean canSee(Player arg0) {
		// Not in my task list mk
		return false;
	}

	@Override
	public void chat(String arg0) {
		// Not in my task list mk
		
	}

	@Override
	public void decrementStatistic(Statistic arg0)
			throws IllegalArgumentException {
		// Not in my task list mk
		
	}

	@Override
	public void decrementStatistic(Statistic arg0, int arg1)
			throws IllegalArgumentException {
		// Not in my task list mk
		
	}

	@Override
	public void decrementStatistic(Statistic arg0, Material arg1)
			throws IllegalArgumentException {
		// Not in my task list mk
		
	}

	@Override
	public void decrementStatistic(Statistic arg0, EntityType arg1)
			throws IllegalArgumentException {
		// Not in my task list mk
		
	}

	@Override
	public void decrementStatistic(Statistic arg0, Material arg1, int arg2)
			throws IllegalArgumentException {
		// Not in my task list mk
		
	}

	@Override
	public void decrementStatistic(Statistic arg0, EntityType arg1, int arg2) {
		// Not in my task list mk
		
	}

	@Override
	public InetSocketAddress getAddress() {
		// Not in my task list mk
		return null;
	}

	@Override
	public boolean getAllowFlight() {
		// Not in my task list mk
		return false;
	}

	@Override
	public Location getBedSpawnLocation() {
		// Not in my task list mk
		return null;
	}

	@Override
	public Location getCompassTarget() {
		// Not in my task list mk
		return null;
	}

	@Override
	public String getDisplayName() {
		// Not in my task list mk
		return null;
	}

	@Override
	public float getExhaustion() {
		// Not in my task list mk
		return 0;
	}

	@Override
	public float getExp() {
		// Not in my task list mk
		return 0;
	}

	@Override
	public float getFlySpeed() {
		// Not in my task list mk
		return 0;
	}

	@Override
	public int getFoodLevel() {
		// Not in my task list mk
		return 0;
	}

	@Override
	public double getHealthScale() {
		// Not in my task list mk
		return 0;
	}

	@Override
	public int getLevel() {
		// Not in my task list mk
		return 0;
	}

	@Override
	public String getPlayerListName() {
		// Not in my task list mk
		return null;
	}

	@Override
	public long getPlayerTime() {
		// Not in my task list mk
		return 0;
	}

	@Override
	public long getPlayerTimeOffset() {
		// Not in my task list mk
		return 0;
	}

	@Override
	public WeatherType getPlayerWeather() {
		// Not in my task list mk
		return null;
	}

	@Override
	public float getSaturation() {
		// Not in my task list mk
		return 0;
	}

	@Override
	public Scoreboard getScoreboard() {
		// Not in my task list mk
		return null;
	}

	@Override
	public int getStatistic(Statistic arg0) throws IllegalArgumentException {
		// Not in my task list mk
		return 0;
	}

	@Override
	public int getStatistic(Statistic arg0, Material arg1)
			throws IllegalArgumentException {
		// Not in my task list mk
		return 0;
	}

	@Override
	public int getStatistic(Statistic arg0, EntityType arg1)
			throws IllegalArgumentException {
		// Not in my task list mk
		return 0;
	}

	@Override
	public int getTotalExperience() {
		// Not in my task list mk
		return 0;
	}

	@Override
	public float getWalkSpeed() {
		// Not in my task list mk
		return 0;
	}

	@Override
	public void giveExp(int arg0) {
		// Not in my task list mk
		
	}

	@Override
	public void giveExpLevels(int arg0) {
		// Not in my task list mk
		
	}

	@Override
	public boolean hasAchievement(Achievement arg0) {
		// Not in my task list mk
		return false;
	}

	@Override
	public void hidePlayer(Player arg0) {
		// Not in my task list mk
		
	}

	@Override
	public void incrementStatistic(Statistic arg0)
			throws IllegalArgumentException {
		// Not in my task list mk
		
	}

	@Override
	public void incrementStatistic(Statistic arg0, int arg1)
			throws IllegalArgumentException {
		// Not in my task list mk
		
	}

	@Override
	public void incrementStatistic(Statistic arg0, Material arg1)
			throws IllegalArgumentException {
		// Not in my task list mk
		
	}

	@Override
	public void incrementStatistic(Statistic arg0, EntityType arg1)
			throws IllegalArgumentException {
		// Not in my task list mk
		
	}

	@Override
	public void incrementStatistic(Statistic arg0, Material arg1, int arg2)
			throws IllegalArgumentException {
		// Not in my task list mk
		
	}

	@Override
	public void incrementStatistic(Statistic arg0, EntityType arg1, int arg2)
			throws IllegalArgumentException {
		// Not in my task list mk
		
	}

	@Override
	public boolean isFlying() {
		// Not in my task list mk
		return false;
	}

	@Override
	public boolean isHealthScaled() {
		// Not in my task list mk
		return false;
	}

	@Override
	public boolean isOnGround() {
		// Not in my task list mk
		return false;
	}

	@Override
	public boolean isPlayerTimeRelative() {
		// Not in my task list mk
		return false;
	}

	@Override
	public boolean isSleepingIgnored() {
		// Not in my task list mk
		return false;
	}

	@Override
	public boolean isSneaking() {
		// Not in my task list mk
		return false;
	}

	@Override
	public boolean isSprinting() {
		// Not in my task list mk
		return false;
	}

	@Override
	public void kickPlayer(String arg0) {
		// Not in my task list mk
		
	}

	@Override
	public void loadData() {
		// Not in my task list mk
		
	}

	@Override
	public boolean performCommand(String arg0) {
		// Not in my task list mk
		return false;
	}

	@Override
	public void playEffect(Location arg0, Effect arg1, int arg2) {
		// Not in my task list mk
		
	}

	@Override
	public <T> void playEffect(Location arg0, Effect arg1, T arg2) {
		// Not in my task list mk
		
	}

	@Override
	public void playNote(Location arg0, byte arg1, byte arg2) {
		// Not in my task list mk
		
	}

	@Override
	public void playNote(Location arg0, Instrument arg1, Note arg2) {
		// Not in my task list mk
		
	}

	@Override
	public void playSound(Location arg0, Sound arg1, float arg2, float arg3) {
		// Not in my task list mk
		
	}

	@Override
	public void playSound(Location arg0, String arg1, float arg2, float arg3) {
		// Not in my task list mk
		
	}

	@Override
	public void removeAchievement(Achievement arg0) {
		// Not in my task list mk
		
	}

	@Override
	public void resetPlayerTime() {
		// Not in my task list mk
		
	}

	@Override
	public void resetPlayerWeather() {
		// Not in my task list mk
		
	}

	@Override
	public void saveData() {
		// Not in my task list mk
		
	}

	@Override
	public void sendBlockChange(Location arg0, Material arg1, byte arg2) {
		// Not in my task list mk
		
	}

	@Override
	public void sendBlockChange(Location arg0, int arg1, byte arg2) {
		// Not in my task list mk
		
	}

	@Override
	public boolean sendChunkChange(Location arg0, int arg1, int arg2,
			int arg3, byte[] arg4) {
		// Not in my task list mk
		return false;
	}

	@Override
	public void sendMap(MapView arg0) {
		// Not in my task list mk
		
	}

	@Override
	public void sendRawMessage(String arg0) {
		// Not in my task list mk
		
	}

	@Override
	public void sendSignChange(Location arg0, String[] arg1)
			throws IllegalArgumentException {
		// Not in my task list mk
		
	}

	@Override
	public void setAllowFlight(boolean arg0) {
		// Not in my task list mk
		
	}

	@Override
	public void setBedSpawnLocation(Location arg0) {
		// Not in my task list mk
		
	}

	@Override
	public void setBedSpawnLocation(Location arg0, boolean arg1) {
		// Not in my task list mk
		
	}

	@Override
	public void setCompassTarget(Location arg0) {
		// Not in my task list mk
		
	}

	@Override
	public void setDisplayName(String arg0) {
		// Not in my task list mk
		
	}

	@Override
	public void setExhaustion(float arg0) {
		// Not in my task list mk
		
	}

	@Override
	public void setExp(float arg0) {
		// Not in my task list mk
		
	}

	@Override
	public void setFlySpeed(float arg0) throws IllegalArgumentException {
		// Not in my task list mk
		
	}

	@Override
	public void setFlying(boolean arg0) {
		// Not in my task list mk
		
	}

	@Override
	public void setFoodLevel(int arg0) {
		// Not in my task list mk
		
	}

	@Override
	public void setHealthScale(double arg0) throws IllegalArgumentException {
		// Not in my task list mk
		
	}

	@Override
	public void setHealthScaled(boolean arg0) {
		// Not in my task list mk
		
	}

	@Override
	public void setLevel(int arg0) {
		// Not in my task list mk
		
	}

	@Override
	public void setPlayerListName(String arg0) {
		// Not in my task list mk
		
	}

	@Override
	public void setPlayerTime(long arg0, boolean arg1) {
		// Not in my task list mk
		
	}

	@Override
	public void setPlayerWeather(WeatherType arg0) {
		// Not in my task list mk
		
	}

	@Override
	public void setResourcePack(String arg0) {
		// Not in my task list mk
		
	}

	@Override
	public void setSaturation(float arg0) {
		// Not in my task list mk
		
	}

	@Override
	public void setScoreboard(Scoreboard arg0)
			throws IllegalArgumentException, IllegalStateException {
		// Not in my task list mk
		
	}

	@Override
	public void setSleepingIgnored(boolean arg0) {
		// Not in my task list mk
		
	}

	@Override
	public void setSneaking(boolean arg0) {
		// Not in my task list mk
		
	}

	@Override
	public void setSprinting(boolean arg0) {
		// Not in my task list mk
		
	}

	@Override
	public void setStatistic(Statistic arg0, int arg1)
			throws IllegalArgumentException {
		// Not in my task list mk
		
	}

	@Override
	public void setStatistic(Statistic arg0, Material arg1, int arg2)
			throws IllegalArgumentException {
		// Not in my task list mk
		
	}

	@Override
	public void setStatistic(Statistic arg0, EntityType arg1, int arg2) {
		// Not in my task list mk
		
	}

	@Override
	public void setTexturePack(String arg0) {
		// Not in my task list mk
		
	}

	@Override
	public void setTotalExperience(int arg0) {
		// Not in my task list mk
		
	}

	@Override
	public void setWalkSpeed(float arg0) throws IllegalArgumentException {
		// Not in my task list mk
		
	}

	@Override
	public void showPlayer(Player arg0) {
		// Not in my task list mk
		
	}

	@Override
	public Spigot spigot() {
		// Not in my task list mk
		return null;
	}

	@Override
	public void updateInventory() {
		// Not in my task list mk
		
	}
	
}