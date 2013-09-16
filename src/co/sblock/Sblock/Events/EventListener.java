/**
 * 
 */
package co.sblock.Sblock.Events;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.material.Bed;

import com.bergerkiller.bukkit.common.events.PacketReceiveEvent;
import com.bergerkiller.bukkit.common.events.PacketSendEvent;
import com.bergerkiller.bukkit.common.protocol.PacketFields;
import com.bergerkiller.bukkit.common.protocol.PacketListener;
import com.bergerkiller.bukkit.common.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;

import co.sblock.Sblock.DatabaseManager;
import co.sblock.Sblock.Sblock;
import co.sblock.Sblock.Chat.Channel.Channel;
import co.sblock.Sblock.Chat.Channel.ChannelManager;
import co.sblock.Sblock.Events.Packet26EntityStatus.Status;
import co.sblock.Sblock.UserData.DreamPlanet;
import co.sblock.Sblock.UserData.Region;
import co.sblock.Sblock.UserData.SblockUser;
import co.sblock.Sblock.UserData.UserManager;

/**
 * @author Jikoo
 *
 */
public class EventListener implements Listener, PacketListener {

	private Map<String, Integer> tasks;
	private Set<String> teleports;
	private short fake_UUID;
	private Set<String> specialCommands;

	public EventListener() {
		tasks = new HashMap<String, Integer>();
		teleports = new HashSet<String>();
		fake_UUID = 25000;
		specialCommands = new HashSet<String>();
		specialCommands.add("pl");
		specialCommands.add("plugins");
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onServerListPing(ServerListPingEvent event) {
		event.setMotd(event.getMotd().replaceAll("Player",
				DatabaseManager.getDatabaseManager()
				.getUserFromIP(event.getAddress().getHostAddress())));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerLogin(PlayerLoginEvent event) {
		switch (event.getResult()) {
		case ALLOWED:
		case KICK_FULL:
		case KICK_WHITELIST:
			return;
		case KICK_BANNED:
		case KICK_OTHER:
			String reason = DatabaseManager.getDatabaseManager().getBanReason(
					event.getPlayer().getName(),
					event.getAddress().getHostAddress());
			if (reason != null) {
				System.out.println("[DEBUG] Changing disconnect to " + reason);
				event.setKickMessage(reason);
			}
			return;
		default:
			return;
		}
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		SblockUser u = SblockUser.getUser(event.getPlayer().getName());
		if (u == null) {
			UserManager.getUserManager().addUser(event.getPlayer());
			u = SblockUser.getUser(event.getPlayer().getName());
		}
		//RegionChannel handling
		u.setCurrentRegion(Region.getLocationRegion(event.getPlayer().getLocation()));
		Channel regionC = ChannelManager.getChannelManager().getChannel("#" + u.getCurrentRegion().toString());
		regionC.addListening(u.getPlayerName());
		u.addListening(regionC);
		//u.syncSetCurrentChannel("#");
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerPreprocessCommand(PlayerCommandPreprocessEvent event) {
		if (specialCommands.contains(parseCdName(event.getMessage()))) {
			if (!event.getPlayer().hasPermission("group.denizen")) {
				event.getPlayer().sendMessage(ChatColor.BOLD +
						"[o] Pay no attention to the man behind the curtain.");
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		if (SblockUser.getUser(event.getPlayer().getName()) != null) {
			event.setCancelled(true);
			if (event.getMessage().startsWith("/")) {
				event.getPlayer().performCommand(
						event.getMessage().substring(1));
			} else {
				SblockUser.getUser(event.getPlayer().getName()).chat(event);
			}
		}
		else	{
			event.getPlayer().sendMessage(ChatColor.RED + "You are not in the SblockUser Database! Seek help immediately!");
		}
	}

	@EventHandler
	public void onPlayerChangedWorlds(PlayerChangedWorldEvent event) {
		SblockUser u = UserManager.getUserManager().getUser(event.getPlayer().getName());
		u.updateCurrentRegion(Region.getLocationRegion(event.getPlayer().getLocation()));
	}

	public int initiateRegionChecks() {
		return Bukkit.getScheduler().scheduleSyncRepeatingTask(Sblock.getInstance(), new RegionCheck(), 0L, 100L);
	}

	public class RegionCheck implements Runnable {
		@Override
		public void run() {
			try {
				for (Player p : Bukkit.getWorld("Medium").getPlayers()) {
					SblockUser u = SblockUser.getUser(p.getName());
					Region r = Region.getLocationRegion(p.getLocation());
					if (!u.getCurrentRegion().equals(r)) {
						u.updateCurrentRegion(r);
					}
				}
			} catch (NullPointerException e) {
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerQuit(PlayerQuitEvent event) {
		SblockUser u = SblockUser.getUser(event.getPlayer().getName());
		if (u == null) {
			return; // We don't want to make another db call just to announce quit.
		}
		if (tasks.containsKey(u.getPlayerName())) {
			Bukkit.getScheduler().cancelTask(tasks.remove(u.getPlayerName()));
		}
		Channel regionC = ChannelManager.getChannelManager().getChannel("#" + u.getCurrentRegion().toString());
		u.removeListening(regionC.getName());
		for (String s : u.getListening()) {
			u.removeListeningQuit(s);
		}
		UserManager.getUserManager().removeUser(event.getPlayer());
	}

	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent event) {
//		if (event.getCause().equals(TeleportCause.END_PORTAL)) {
//			if (Math.abs(event.getTo().getBlockX()) - Bukkit.getSpawnRadius() < 0) {
//				if (Math.abs(event.getTo().getBlockZ() - Bukkit.getSpawnRadius()) < 0) {
//					event.setCancelled(true);
//				}
//			}
//		}
		Player p = event.getPlayer();
		if (!event.getFrom().getWorld().equals(event.getTo().getWorld())) {
			SblockUser u = SblockUser.getUser(p.getName());
			if (!u.isGodTier()) {
				u.setIsSleeping(event.getTo().getWorld().getName().contains("Circle"));
				Sblogger.info("DEBUG", "Teleport occurring: Leaving " + event.getFrom().getWorld().getName() + " at " + event.getFrom().getX() + ", " + event.getFrom().getZ() + ". Arriving in " + event.getTo().getWorld().getName() + " at " + event.getTo().getX() + ", " + event.getTo().getZ() + ".");
				if (teleports.remove(p.getName())) {
					u.setPreviousLocation(event.getFrom());
				}
			}
		}
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (!event.isCancelled() && event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
			Block b = event.getClickedBlock();
			if (b.getType().equals(Material.BED_BLOCK)) {
				if (SblockUser.getUser(event.getPlayer().getName()).isGodTier()) {
					// TODO? Godtiers probably have alternate transport to dPlanet
					return;
				}
				Bed bed = (Bed) b.getState().getData();
				Location head;
				if (bed.isHeadOfBed()) {
					head = b.getLocation();
				} else {
					head = b.getRelative(bed.getFacing()).getLocation();
					// getFace does not seem to work in most cases - TODO test and fix
				}
				switch (Region.uValueOf(head.getWorld().getName())) {
				case EARTH:
				//case MEDIUM: // In time, my precious.
				case INNERCIRCLE:
				case OUTERCIRCLE:
					fakeSleepDream(event.getPlayer(), head);
					event.setCancelled(true);
					return;
				default:
					return;
				}
			}
		}
	}

	/*
	 * @EventHandler public void onPlayerTagEvent(PlayerReceiveNameTagEvent
	 * event) { Player p = event.getNamedPlayer();
	 * 
	 * if (p.hasPermission("group.horrorterror")) {
	 * event.setTag(ColorDef.RANK_ADMIN + p.getName()); } else if
	 * (p.hasPermission("group.denizen")) { event.setTag(ColorDef.RANK_MOD +
	 * p.getName()); } else if (p.hasPermission("group.helper")) {
	 * event.setTag(ColorDef.RANK_HELPER + p.getName()); } else if
	 * (p.hasPermission("group.godtier")) { event.setTag(ColorDef.RANK_GODTIER +
	 * p.getName()); } else if (p.hasPermission("group.donator")) {
	 * event.setTag(ColorDef.RANK_DONATOR + p.getName()); } else if
	 * (p.hasPermission("group.hero")) { event.setTag(ColorDef.RANK_HERO +
	 * p.getName()); }
	 * 
	 * }
	 */
	

	private void fakeSleepDream(Player p, Location bed) {
		ProtocolManager pm = ProtocolLibrary.getProtocolManager();

		Packet11UseBed packet = new Packet11UseBed();
		packet.setEntityId(p.getEntityId());
		packet.setX(bed.getBlockX());
		packet.setY((byte) bed.getBlockY());
		packet.setZ(bed.getBlockZ());

		try {
			pm.sendServerPacket(p, packet.getHandle());
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		scheduleSleepTeleport(p);
	}

	private void fakeWakeUp(Player p) {
		Packet12Animation packet = new Packet12Animation();
		packet.setEntityID(p.getEntityId());
		packet.setAnimation((byte) 3);

		try {
			ProtocolLibrary.getProtocolManager().sendServerPacket(p, packet.getHandle());
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see com.bergerkiller.bukkit.common.protocol.PacketListener#onPacketReceive(com.bergerkiller.bukkit.common.events.PacketReceiveEvent)
	 */
	@Override
	public void onPacketReceive(PacketReceiveEvent event) {
		if (event.getType().equals(PacketType.ENTITY_ACTION)) {
			if (event.getPacket().read(PacketFields.ENTITY_ACTION.animation) == 3) {
				Player p = event.getPlayer();
				if (tasks.containsKey(p.getName())) {
					event.setCancelled(true);
					Bukkit.getScheduler().cancelTask(tasks.remove(p.getName()));
					this.fakeWakeUp(p);
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.bergerkiller.bukkit.common.protocol.PacketListener#onPacketSend(com.bergerkiller.bukkit.common.events.PacketSendEvent)
	 * @see <a href="http://wiki.vg/Protocol#Entity_Action_.280x13.29">Minecraft packet protocol</a>
	 */
	@Override
	public void onPacketSend(PacketSendEvent event) {
		
	}

	private void scheduleSleepTeleport(Player p) {
		tasks.put(p.getName(), Bukkit.getScheduler()
				.scheduleSyncDelayedTask(Sblock.getInstance(),
						new SleepTeleport(p), 100L));
	}

	private class SleepTeleport implements Runnable {

		private Player p;

		public SleepTeleport(Player p) {
			this.p = p;
		}

		@Override
		public void run() {
			SblockUser user = SblockUser.getUser(p.getName());
			if (p != null && user != null) {
				Location l = p.getLocation();
				Sblogger.info("DEBUG", "Sleep teleport initiating at " + l.getX() + ", " + l.getY() + ", " + l.getZ());
				switch (Region.getLocationRegion(p.getLocation())) {
				case EARTH:
//				case MEDIUM: // Someday, my pretties.
//				case LOFAF:
//				case LOHAC:
//				case LOLAR:
//				case LOWAS:
					if (user.getDPlanet().equals(DreamPlanet.NONE)) {
						Sblogger.info("DEBUG", "User does not have a dream planet defined!")
						break;
					} else {
						Sblogger.info("DEBUG", "User data:\nDream Planet: " + user.getDPlanet().getDisplayName() + "\nCurrent world: " + p.getWorld().getName() + "\nPrevious location world: " + user.getPreviousLocation().getWorld().getName());
						teleports.add(p.getName());
						if (p.getWorld().equals(user.getPreviousLocation().getWorld())) {
							Sblogger.info("DEBUG", "User's previous location is in current world.")
							p.teleport(EventModule.getEventModule().getTowerData()
									.getLocation(user.getTower(),
											user.getDPlanet(), (byte) 0));
						} else {
							p.teleport(user.getPreviousLocation());
						}
					}
					break;
				case FURTHESTRING:
				case INNERCIRCLE:
					Sblogger.info("DEBUG", "User data:\nDream Planet: " + user.getDPlanet().getDisplayName() + "\nCurrent world: " + p.getWorld().getName() + "\nPrevious location world: " + user.getPreviousLocation().getWorld().getName());
					teleports.add(p.getName());
					p.teleport(user.getPreviousLocation());
					break;
				default:
					break;
				}

				fakeWakeUp(p);

			}
			tasks.remove(p.getName());
		}
	}

	private String parseCdName(String s) {
		if (s.startsWith("/")) {
			s = s.replaceFirst("/", "");
		}
		return s.length() < 1 ? s : s.substring(
				1, (s.indexOf(" ") != -1 ? s.indexOf(" ") : s.length() - 1));
	}

	public void forceCloseClient(Player p) {
		Location l = p.getLocation();
		Packet18SpawnMob spawn = new Packet18SpawnMob();
		spawn.setEntityID(fake_UUID);
		spawn.setType(EntityType.ENDER_DRAGON);
		spawn.setX(l.getX());
		spawn.setY(l.getY());
		spawn.setZ(l.getZ());
		spawn.setPitch(l.getPitch());
		spawn.setYaw(l.getYaw());
		spawn.setHeadYaw(l.getYaw());
		spawn.setVelocityX(0);
		spawn.setVelocityY(0);
		spawn.setVelocityZ(0);

		Packet26EntityStatus packet = new Packet26EntityStatus();
		packet.setEntityId(fake_UUID);
		packet.setEntityStatus(Status.ENTITY_DEAD);

		fake_UUID++;

		try {
			ProtocolLibrary.getProtocolManager().sendServerPacket(p, spawn.getHandle());
			ProtocolLibrary.getProtocolManager().sendServerPacket(p, packet.getHandle());
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	public void dragon(Location l) {
		Packet18SpawnMob spawn = new Packet18SpawnMob();
		spawn.setEntityID(fake_UUID);
		spawn.setType(EntityType.ENDER_DRAGON);
		spawn.setX(l.getX());
		spawn.setY(l.getY());
		spawn.setZ(l.getZ());
		spawn.setPitch(l.getPitch());
		spawn.setYaw(l.getYaw());
		spawn.setHeadYaw(l.getYaw());
		spawn.setVelocityX(0);
		spawn.setVelocityY(0);
		spawn.setVelocityZ(0);

		Packet26EntityStatus packet = new Packet26EntityStatus();
		packet.setEntityId(fake_UUID);
		packet.setEntityStatus(Status.ENTITY_DEAD);

		fake_UUID++;

		try {
			for (Player p : Bukkit.getOnlinePlayers()) {
				if (p.getWorld().equals(l.getWorld()) && p.getLocation().distanceSquared(l) <= 2304) {
					// 2304 = 48^2. Spigot by default does not render mobs beyond this point.
					ProtocolLibrary.getProtocolManager().sendServerPacket(p, spawn.getHandle());
					ProtocolLibrary.getProtocolManager().sendServerPacket(p, packet.getHandle());
				}
			}
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}
}
