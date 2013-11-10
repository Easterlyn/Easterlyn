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
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.material.Bed;

import com.bergerkiller.bukkit.common.events.PacketReceiveEvent;
import com.bergerkiller.bukkit.common.events.PacketSendEvent;
import com.bergerkiller.bukkit.common.protocol.PacketFields;
import com.bergerkiller.bukkit.common.protocol.PacketListener;
import com.bergerkiller.bukkit.common.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;

import co.sblock.Sblock.Sblock;
import co.sblock.Sblock.Chat.ChatUser;
import co.sblock.Sblock.Chat.ChatUserManager;
import co.sblock.Sblock.Chat.Channel.Channel;
import co.sblock.Sblock.Chat.Channel.ChannelManager;
import co.sblock.Sblock.Database.DBManager;
import co.sblock.Sblock.Events.Packets.AbstractPacket;
import co.sblock.Sblock.Events.Packets.Packet11UseBed;
import co.sblock.Sblock.Events.Packets.Packet12Animation;
import co.sblock.Sblock.Events.Packets.Packet18SpawnMob;
import co.sblock.Sblock.Events.Packets.Packet26EntityStatus;
import co.sblock.Sblock.Events.Packets.SleepTeleport;
import co.sblock.Sblock.Events.Packets.SendPacket;
import co.sblock.Sblock.UserData.Region;
import co.sblock.Sblock.UserData.SblockUser;
import co.sblock.Sblock.Utilities.Sblogger;
import co.sblock.Sblock.Utilities.Inventory.InventoryManager;

/**
 * @author Jikoo
 */
public class EventListener implements Listener, PacketListener {

	/** A <code>Map</code> of all scheduled tasks by ID. */
	public Map<String, Integer> tasks;
	/**
	 * A <code>Set</code> of the names of all <code>Player</code>s queuing to
	 * sleep teleport.
	 */
	public Set<String> teleports;
	/** The fake UUID used for mob spawn faking. */
	private short fake_UUID;

	public EventListener() {
		tasks = new HashMap<String, Integer>();
		teleports = new HashSet<String>();
		fake_UUID = 25000;
	}

	/**
	 * The event handler for <code>ServerListPingEvent</code>s.
	 * <p>
	 * If the IP pinging has played before, customize MOTD with their name.
	 * 
	 * @param event the <code>ServerListPingEvent</code>
	 */
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onServerListPing(ServerListPingEvent event) {
		String MOTD;
		if (EventModule.getEventModule().getStatus().hasMOTDChange()) {
			MOTD = EventModule.getEventModule().getStatus().getMOTDChange();
		} else {
			MOTD = event.getMotd().replaceAll("Player",
					DBManager.getDBM()
					.getUserFromIP(event.getAddress().getHostAddress()));
		}
		event.setMotd(MOTD);
	}

	/**
	 * The event handler for <code>PlayerLoginEvent</code>s.
	 * 
	 * @param event
	 *            the <code>PlayerLoginEvent</code>
	 */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerLogin(PlayerLoginEvent event) {
		switch (event.getResult()) {
		case ALLOWED:
		case KICK_FULL:
		case KICK_WHITELIST:
			return;
		case KICK_BANNED:
		case KICK_OTHER:
			String reason = DBManager.getDBM().getBanReason(
					event.getPlayer().getName(),
					event.getAddress().getHostAddress());
			if (reason != null) {
				event.setKickMessage(reason);
			}
			return;
		default:
			return;
		}
	}

	/**
	 * The event handler for <code>PlayerJoinEvent</code>s.
	 * 
	 * @param event
	 *            the <code>PlayerJoinEvent</code>
	 */
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		ChatUser u = DBManager.getDBM().loadUserData(event.getPlayer().getName());
		
		//RegionChannel handling
		u.setCurrentRegion(Region.getLocationRegion(event.getPlayer().getLocation()));
		u.syncJoinChannel("#" + u.getCurrentRegion().toString());
		//u.syncSetCurrentChannel("#");
	}


	/**
	 * The event handler for <code>AsyncPlayerChatEvent</code>s.
	 * 
	 * @param event
	 *            the <code>AsyncPlayerChatEvent</code>
	 */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		if (SblockUser.getUser(event.getPlayer().getName()) != null) {
			event.setCancelled(true);
			if (event.getMessage().charAt(0) == '\u002F') {
				event.getPlayer().performCommand(
						event.getMessage().substring(1));
			} else {
				ChatUserManager.getUserManager().getUser(event.getPlayer().getName()).chat(event);
			}
		}
		else	{
			event.getPlayer().sendMessage(ChatColor.RED + "You are not in the SblockUser Database! Seek help immediately!");
		}
	}

	/**
	 * The event handler for <code>PlayerChangedWorldEvent</code>s.
	 * 
	 * @param event
	 *            the <code>PlayerChangedWorldEvent</code>
	 */
	@EventHandler
	public void onPlayerChangedWorlds(PlayerChangedWorldEvent event) {
		ChatUserManager.getUserManager().getUser(event.getPlayer().getName())
				.updateCurrentRegion(Region.getLocationRegion(event.getPlayer().getLocation()));
	}

	/**
	 * The event handler for <code>PlayerQuitEvent</code>s.
	 * 
	 * @param event
	 *            the <code>PlayerQuitEvent</code>
	 */
	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerQuit(PlayerQuitEvent event) {
		InventoryManager.restoreInventory(event.getPlayer());
		ChatUser u = ChatUserManager.getUserManager().getUser(event.getPlayer().getName());
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
		DBManager.getDBM().saveUserData(event.getPlayer().getName());
	}

	/**
	 * The event handler for <code>SignChangeEvent</code>s.
	 * <p>
	 * Allows signs to be colored using &codes.
	 * 
	 * @param event the <code>SignChangeEvent</code>
	 */
	@EventHandler
	public void onSignPlace(SignChangeEvent event) {
		for (int i = 0; i < 4; i++) {
			event.setLine(i, ChatColor.translateAlternateColorCodes('\u0026', event.getLine(i)));
		}
	}

	/**
	 * The event handler for <code>PlayerTeleportEvent</code>s.
	 * 
	 * @param event
	 *            the <code>PlayerTeleportEvent</code>
	 */
	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		Player p = event.getPlayer();
		if (!event.getFrom().getWorld().equals(event.getTo().getWorld()) && teleports.remove(p.getName())) {
			SblockUser u = SblockUser.getUser(p.getName());
			if (!u.isGodTier()) {
				u.setIsSleeping(event.getTo().getWorld().getName().contains("Circle"));
				u.setPreviousLocation(event.getFrom());
			}
		}
	}

	/**
	 * The event handler for <code>PlayerInteractEvent</code>s.
	 * 
	 * @param event
	 *            the <code>PlayerInteractEvent</code>
	 */
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (!event.isCancelled() && event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
			Block b = event.getClickedBlock();
			if (b.getType().equals(Material.BED_BLOCK)) {
				if (SblockUser.getUser(event.getPlayer().getName()).isGodTier()) {
					// future feature
					return;
				}
				Bed bed = (Bed) b.getState().getData();
				Location head;
				if (bed.isHeadOfBed()) {
					head = b.getLocation();
				} else {
					head = b.getRelative(bed.getFacing()).getLocation();
					// getFace does not seem to work in most cases - Adam test and fix
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

	/**
	 * Sends a <code>Player</code> a fake packet for starting sleeping and
	 * schedules them to be teleported to their <code>DreamPlanet</code>.
	 * 
	 * @param p
	 *            the <code>Player</code>
	 * @param bed
	 *            the <code>Location</code> of the bed to sleep in
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
			Sblogger.err(e);
		}
		scheduleSleepTeleport(p);
	}

	/**
	 * Schedules a <code>SleepTeleport</code> for a <code>Player</code>.
	 * 
	 * @param p
	 *            the <code>Player</code>
	 */
	private void scheduleSleepTeleport(Player p) {
		tasks.put(p.getName(),
				Bukkit.getScheduler().scheduleSyncDelayedTask(Sblock.getInstance(),
						new SleepTeleport(p), 100L));
	}

	/**
	 * Sends a <code>Player</code> a fake packet for waking up.
	 * 
	 * @param p
	 *            the <code>Player</code>
	 */
	public void fakeWakeUp(Player p) {
		Packet12Animation packet = new Packet12Animation();
		packet.setEntityID(p.getEntityId());
		packet.setAnimation((byte) 3);

		try {
			ProtocolLibrary.getProtocolManager().sendServerPacket(p, packet.getHandle());
		} catch (InvocationTargetException e) {
			Sblogger.err(e);
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

	/**
	 * The event handler for packets received from clients.
	 * 
	 * @param event
	 *            the <code>PacketReceiveEvent</code>
	 * @see com.bergerkiller.bukkit.common.protocol.PacketListener#onPacketReceive(PacketReceiveEvent)
	 * @see <a href="http://wiki.vg/Protocol#Entity_Action_.280x13.29">Minecraft packet protocol</a>
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

	/**
	 * The event handler for packets sent to clients.
	 * 
	 * @param event
	 *            the <code>PacketSendEvent</code>
	 * @see com.bergerkiller.bukkit.common.protocol.PacketListener#onPacketSend(PacketSendEvent)
	 * @see <a href="http://wiki.vg/Protocol#Entity_Action_.280x13.29">Minecraft
	 *      packet protocol</a>
	 */
	@Override
	public void onPacketSend(PacketSendEvent event) {
		
	}

	/**
	 * Forcibly close game client by sending bad packets.
	 * 
	 * @param p
	 *            the <code>Player</code> whose client should be crashed.
	 */
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
		packet.setEntityStatus(Packet26EntityStatus.Status.ENTITY_DEAD);

		fake_UUID++;

		try {
			ProtocolLibrary.getProtocolManager().sendServerPacket(p, spawn.getHandle());
			ProtocolLibrary.getProtocolManager().sendServerPacket(p, packet.getHandle());
		} catch (InvocationTargetException e) {
			Sblogger.err(e);
		}
	}

	/**
	 * Fake spawn and kill an EnderDragon at specified <code>Location</code>
	 * using packets.
	 * 
	 * @param l
	 *            the <code>Location</code>
	 */
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
		packet.setEntityStatus(Packet26EntityStatus.Status.ENTITY_DEAD);

		fake_UUID++;

		try {
			for (Player p : Bukkit.getOnlinePlayers()) {
				if (p.getWorld().equals(l.getWorld()) && p.getLocation().distanceSquared(l) <= 2304) {
					// 2304 = 48^2. Spigot by default does not render mobs beyond this point.
					ProtocolLibrary.getProtocolManager().sendServerPacket(p, spawn.getHandle());
					// Ideally this will fix what I suspect is the issue - the packet is
					// probably sent too soon, or something of the sort.
					// Adam task tracking
					this.schedulePacket(p, packet);
				}
			}
		} catch (InvocationTargetException e) {
			Sblogger.err(e);
		}
	}

	/**
	 * Schedule sending a packet to a <code>Player</code>.
	 * 
	 * @param p
	 *            the <code>Player</code> to send a packet to
	 * @param packet
	 *            the <code>AbstractPacket</code> to send
	 */
	private void schedulePacket(Player p, AbstractPacket packet) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(Sblock.getInstance(), new SendPacket(p, packet));
	}
}
