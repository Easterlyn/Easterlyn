package co.sblock.progression;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import co.sblock.Sblock;
import co.sblock.captcha.Captcha;
import co.sblock.events.packets.ParticleEffectWrapper;
import co.sblock.events.packets.ParticleUtils;
import co.sblock.machines.Machines;
import co.sblock.machines.type.Machine;
import co.sblock.micromodules.Meteorite;
import co.sblock.module.Dependency;
import co.sblock.users.OfflineUser;
import co.sblock.users.OnlineUser;
import co.sblock.users.ProgressionState;
import co.sblock.users.Region;
import co.sblock.users.Users;
import co.sblock.utilities.InventoryUtils;

import net.md_5.bungee.api.ChatColor;

/**
 * Class containing functions controlling the Entry sequence.
 * 
 * @author Jikoo
 */
@Dependency("HolographicDisplays")
public class Entry {

	private static Entry instance;

	private final Material[] materials;
	private final HashMap<UUID, Triple<Meteorite, EntryTimer, Material>> data;

	public Entry() {
		materials = createMaterialList();
		data = new HashMap<>();
	}
	public boolean canStart(OfflineUser user) {
		if (!data.containsKey(user.getUUID()) && user.getPrograms().contains("SburbClient")
				&& user.getProgression() == ProgressionState.NONE) {
			return true;
		}
		// User has started or finished Entry already or not installed the SburbClient.
		return false;
	}

	public boolean isEntering(OfflineUser user) {
		return data.containsKey(user.getUUID());
	}


	public void startEntry(OfflineUser user, Location cruxtruder) {
		if (!canStart(user)) {
			return;
		}

		user.setProgression(ProgressionState.ENTRY_UNDERWAY);

		// Center hologram inside the space above the block
		final Location holoLoc = cruxtruder.clone().add(new Vector(0.5, 0.4, 0.5));
		// 4:13 = 253 seconds, 2 second display of 0:00
		EntryTimer task = new EntryTimer(holoLoc, user.getUUID());
		task.runTaskTimer(Sblock.getInstance(), 20L, 20L);
		Meteorite meteorite = new Meteorite(holoLoc, Material.NETHERRACK.name(), 3, true, -1);
		// 254 seconds * 20 ticks per second = 5080
		meteorite.hoverMeteorite(5080);
		Material material = materials[(int) (materials.length *  Math.random())];
		ItemStack is = new ItemStack(material);
		ItemMeta im = is.getItemMeta();
		im.setDisplayName(ChatColor.AQUA + "Cruxite " + InventoryUtils.getMaterialDataName(is.getType(), is.getDurability()));
		is.setItemMeta(im);
		is = Captcha.captchaToPunch(Captcha.itemToCaptcha(is));
		if (Bukkit.getOfflinePlayer(user.getServer()).isOnline()
				&& Users.getGuaranteedUser(user.getServer()) instanceof OnlineUser
				&& ((OnlineUser) Users.getGuaranteedUser(user.getServer())).isServer()) {
			Bukkit.getPlayer(user.getServer()).getInventory().addItem(is);
		} else {
			Player player = user.getPlayer();
			player.getWorld().dropItem(player.getLocation(), is);
		}
		data.put(user.getUUID(), new ImmutableTriple<>(meteorite, task, material));
	}

	private void finish(OfflineUser user) {
		if (!isEntering(user)) {
			return;
		}

		// Drop the Meteor created.
		Meteorite meteorite = data.remove(user.getUUID()).getLeft();
		if (!meteorite.hasDropped()) {
			meteorite.dropMeteorite();
		}

		// Kicks the server out of server mode
		OfflineUser server = Users.getGuaranteedUser(user.getServer());
		if (server instanceof OnlineUser && ((OnlineUser) server).isServer()) {
			((OnlineUser) server).stopServerMode();
		}
	}

	public void fail(OfflineUser user) {

		if (user == null) {
			return;
		}

		finish(user);
		if (user.getProgression().ordinal() > ProgressionState.ENTRY_UNDERWAY.ordinal()) {
			return;
		}

		// Removes all free machines placed by the User or their server
		for (Pair<Machine, ConfigurationSection> pair : Machines.getInstance().getMachines(user.getUUID())) {
			if (pair.getLeft().isFree()) {
				pair.getLeft().remove(pair.getRight());
			}
		}
	}

	public void succeed(final OfflineUser user) {
		finish(user);

		user.setProgression(ProgressionState.ENTRY);

		final Player player = user.getPlayer();

		// Put player on top of the world because we can
		player.teleport(player.getWorld().getHighestBlockAt(player.getLocation()).getLocation().add(new Vector(0, 1, 0)));
		player.getWorld().playEffect(player.getLocation(), Effect.EXPLOSION_HUGE, 0);

		final Firework firework = (Firework) player.getWorld().spawnEntity(player.getLocation(), EntityType.FIREWORK);
		FireworkMeta fm = firework.getFireworkMeta();
		fm.setPower(4);
		firework.setFireworkMeta(fm);
		firework.setPassenger(player);

		ParticleUtils.getInstance().addEntity(firework, new ParticleEffectWrapper(Effect.FIREWORKS_SPARK, 5));

		Bukkit.getScheduler().scheduleSyncDelayedTask(Sblock.getInstance(), new Runnable() {

			@Override
			public void run() {
				try {
					// Bukkit.getScheduler().cancelTask(particleTask);
					firework.remove();
					Location target = getEntryLocation(user.getMediumPlanet());
					player.teleport(target);
					player.setBedSpawnLocation(target);
					ItemStack house = new ItemStack(Material.ENDER_CHEST);
					ItemMeta im = house.getItemMeta();
					im.setDisplayName(ChatColor.AQUA + "Prebuilt House");
					ArrayList<String> lore = new ArrayList<>();
					lore.add(ChatColor.YELLOW + "Structure: " + ChatColor.AQUA + ChatColor.ITALIC + "house");
					lore.add(ChatColor.YELLOW + "Place in a free space to build!");
					im.setLore(lore);
					house.setItemMeta(im);
					target.getWorld().dropItem(target, house).setPickupDelay(0);
					for (Entity e : target.getWorld().getEntitiesByClasses(Zombie.class, Skeleton.class, Creeper.class, Slime.class)) {
						if (((LivingEntity) e).getLocation().distanceSquared(target) < 2048) {
							e.remove();
						}
					}
				} catch (Exception e) {
					// Player is null
				}
			}
		}, 40L);
	}

	public static Entry getEntry() {
		if (instance == null) {
			instance = new Entry();
		}
		return instance;
	}

	private Material[] createMaterialList() {
		return new Material[] { Material.MELON, Material.ARROW, Material.COAL, Material.WATER_LILY,
				Material.INK_SACK, Material.CARROT_STICK, Material.LAVA_BUCKET,
				Material.WATER_BUCKET, Material.APPLE, Material.EGG, Material.SULPHUR,
				Material.SUGAR, Material.QUARTZ, Material.BLAZE_ROD };
	}
	public Material[] getMaterialList() {
		return materials;
	}

	private Location getEntryLocation(Region mPlanet) {
		double angle = Math.random() * Math.PI * 2;
		Location l = Bukkit.getWorld(mPlanet.getWorldName())
				.getHighestBlockAt((int) (Math.cos(angle) * 2600), (int) (Math.sin(angle) * 2600))
				.getLocation().add(new Vector(0, 1, 0));
		if (isSafeLocation(l)) {
			return l;
		}
		return getEntryLocation(mPlanet);
	}

	private boolean isSafeLocation(Location l) {
		return !l.getBlock().getType().isSolid()
				&& !l.clone().add(new Vector(0, 1, 0)).getBlock().getType().isSolid()
				&& l.clone().add(new Vector(0, -1, 0)).getBlock().getType().isSolid();
	}
	
	public HashMap<UUID, Triple<Meteorite, EntryTimer, Material>> getData() {
		return data;
	}
}
