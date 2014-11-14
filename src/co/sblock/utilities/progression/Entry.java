package co.sblock.utilities.progression;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
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

import com.comphenix.protocol.ProtocolLibrary;
import com.google.common.collect.HashBiMap;

import co.sblock.Sblock;
import co.sblock.events.packets.WrapperPlayServerWorldParticles;
import co.sblock.machines.MachineManager;
import co.sblock.machines.utilities.Icon;
import co.sblock.machines.type.Machine;
import co.sblock.users.ProgressionState;
import co.sblock.users.Region;
import co.sblock.users.User;
import co.sblock.users.UserManager;
import co.sblock.utilities.captcha.Captcha;
import co.sblock.utilities.inventory.InventoryUtils;
import co.sblock.utilities.meteors.Meteorite;

/**
 * Class containing functions controlling the Entry sequence.
 * 
 * @author Jikoo
 */
public class Entry {

	public class EntryStorage {
		public Meteorite meteorite;
		private final Material cruxtype;
		public EntryStorage(Meteorite meteorite, Material cruxtype) {
			this.meteorite = meteorite;
			this.cruxtype = cruxtype;
		}

		public Material getCruxtype() {
			return cruxtype;
		}
	}

	private static Entry instance;

	private final Material[] materials;
	private HashBiMap<EntryTimer, UUID> holograms;
	private HashMap<UUID, EntryStorage> data;

	public Entry() {
		materials = createMaterialList();
		holograms = HashBiMap.create();
		data = new HashMap<>();
	}
	public boolean canStart(User user) {
		if (!holograms.values().contains(user.getUUID()) && user.getPrograms().contains(Icon.SBURBCLIENT.getProgramID())
				&& user.getProgression() == ProgressionState.NONE) {
			return true;
		}
		// User has started or finished Entry already or not installed the SburbClient.
		return false;
	}

	public boolean isEntering(User user) {
		return holograms.containsValue(user.getUUID());
	}


	public void startEntry(User user, Location cruxtruder) {
		if (!canStart(user)) {
			return;
		}

		user.setProgression(ProgressionState.ENTRY_UNDERWAY);

		// Center hologram inside the space above the block
		final Location holoLoc = cruxtruder.clone().add(new Vector(0.5, 0.4, 0.5));
		// 4:13 = 253 seconds, 2 second display of 0:00
		EntryTimer task = new EntryTimer(holoLoc, user.getUUID());
		task.runTaskTimer(Sblock.getInstance(), 20L, 20L);
		holograms.put(task, user.getUUID());
		Meteorite meteorite = new Meteorite(holoLoc, Material.NETHERRACK.name(), 3, true, -1);
		// 254 seconds * 20 ticks per second = 5080
		meteorite.hoverMeteorite(5080);
		Material material = materials[(int) (materials.length *  Math.random())];
		ItemStack is = new ItemStack(material);
		ItemMeta im = is.getItemMeta();
		im.setDisplayName(ChatColor.AQUA + "Cruxite " + InventoryUtils.getMaterialDataName(is.getData()));
		is.setItemMeta(im);
		is = Captcha.captchaToPunch(Captcha.itemToCaptcha(is));
		if (Bukkit.getOfflinePlayer(user.getServer()).isOnline() && UserManager.getUser(user.getServer()).isServer()) {
			Bukkit.getPlayer(user.getServer()).getInventory().addItem(is);
		} else {
			Player player = user.getPlayer();
			player.getWorld().dropItem(player.getLocation(), is);
		}
		data.put(user.getUUID(), new EntryStorage(meteorite, material));
	}

	private void finish(User user) {
		if (!isEntering(user)) {
			return;
		}

		// Drop the Meteor created.
		Meteorite meteorite = data.remove(user.getUUID()).meteorite;
		if (!meteorite.hasDropped()) {
			meteorite.dropMeteorite();
		}

		// Kicks the server out of server mode
		User server = UserManager.getUser(user.getServer());
		if (server != null && server.isServer()) {
			server.stopServerMode();
		}
	}

	public void fail(User user) {

		if (user == null) {
			return;
		}

		finish(user);
		if (user.getProgression() != ProgressionState.NONE) {
			return;
		}

		// Uninstalls the client program
		user.getPrograms().remove(Icon.SBURBCLIENT.getProgramID());

		// Removes all free machines placed by the User or their server
		for (Machine m : MachineManager.getManager().getMachines(user.getUUID())) {
			if (m.getType().isFree()) {
				m.remove();
			}
		}
	}

	public void succeed(final User user) {
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

		final int particleTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(Sblock.getInstance(), new Runnable() {

			@Override
			public void run() {
				WrapperPlayServerWorldParticles packet = new WrapperPlayServerWorldParticles();
				packet.setParticleEffect(WrapperPlayServerWorldParticles.ParticleEffect.FIREWORKS_SPARK);
				packet.setNumberOfParticles(5);
				packet.setLocation(firework.getLocation());
				packet.setOffset(new Vector(0.5, 0.5, 0.5));

				ProtocolLibrary.getProtocolManager().broadcastServerPacket(packet.getHandle(), firework.getLocation(), 64);
			}
		}, 0, 1L);

		Bukkit.getScheduler().scheduleSyncDelayedTask(Sblock.getInstance(), new Runnable() {

			@Override
			public void run() {
				try {
					Bukkit.getScheduler().cancelTask(particleTask);
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
		return new Material[] { Material.WOOL, Material.MELON_BLOCK, Material.TORCH,
				Material.LADDER, Material.WATER_LILY, Material.REDSTONE_TORCH_ON,
				Material.CARROT_STICK, Material.LAVA_BUCKET, Material.WATER_BUCKET, Material.APPLE,
				Material.EGG, Material.SAPLING, Material.SUGAR_CANE, Material.QUARTZ,
				Material.BLAZE_ROD };
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
	
	public HashMap<UUID, EntryStorage> getData() {
		return data;
	}
}
