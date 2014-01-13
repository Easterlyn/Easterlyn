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
import org.bukkit.block.Dispenser;
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
import org.bukkit.event.vehicle.VehicleBlockCollisionEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Bed;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;

import co.sblock.Sblock.Sblock;
import co.sblock.Sblock.Chat.ChatUser;
import co.sblock.Sblock.Chat.ChatUserManager;
import co.sblock.Sblock.Chat.Channel.Channel;
import co.sblock.Sblock.Chat.Channel.ChannelManager;
import co.sblock.Sblock.Database.DBManager;
import co.sblock.Sblock.Events.Packets.Packet11UseBed;
import co.sblock.Sblock.Events.Packets.Packet12Animation;
import co.sblock.Sblock.Events.Packets.SleepTeleport;
import co.sblock.Sblock.SblockEffects.Cooldowns;
import co.sblock.Sblock.UserData.Region;
import co.sblock.Sblock.UserData.SblockUser;
import co.sblock.Sblock.Utilities.Log;
import co.sblock.Sblock.Utilities.Inventory.InventoryManager;

/**
 * @author Jikoo
 */
public class EventListener extends PacketAdapter implements Listener {

	/** A <code>Map</code> of all scheduled tasks by <code>Player</code>. */
	public Map<String, Integer> tasks;
	/**
	 * A <code>Set</code> of the names of all <code>Player</code>s queuing to
	 * sleep teleport.
	 */
	public Set<String> teleports;

	public EventListener() {
		super(Sblock.getInstance(), PacketType.Play.Client.ENTITY_ACTION);
		tasks = new HashMap<String, Integer>();
		teleports = new HashSet<String>();
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
					DBManager.getDBM().getUserFromIP(event.getAddress().getHostAddress()));
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
		DBManager.getDBM().loadUserData(event.getPlayer().getName());
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
		} else {
			event.getPlayer().sendMessage(ChatColor.RED
					+ "[Lil Hal] Your Sblock playerdata appears to not be loaded."
					+ "\nI'll take care of that for you, but make sure your /profile is correct.");
			DBManager.getDBM().loadUserData(event.getPlayer().getName());
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
		try {
			ChatUserManager.getUserManager().getUser(event.getPlayer().getName())
					.updateCurrentRegion(Region.getLocationRegion(event.getPlayer().getLocation()));
		} catch (NullPointerException e) {
			Log.fineDebug("Error updating region, user is likely entering same overall region.");
		}
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
		for (String s : u.getListening()) {
			u.removeListeningQuit(s);
		}
		try {
			Channel regionC = ChannelManager.getChannelManager().getChannel("#" + u.getCurrentRegion().toString());
			u.removeListening(regionC.getName());
		} catch (NullPointerException e) {
			Log.warning("SblockChat", "User's region channel was invalid!");
		}
		DBManager.getDBM().saveUserData(event.getPlayer().getName());
		Cooldowns.cleanup(event.getPlayer().getName());
	}

	/**
	 * The event handler for <code>SignChangeEvent</code>s.
	 * <p>
	 * Allows signs to be colored using &codes.
	 * 
	 * @param event
	 *            the <code>SignChangeEvent</code>
	 */
	@EventHandler
	public void onSignPlace(SignChangeEvent event) {
		for (int i = 0; i < 4; i++) {
			event.setLine(i, ChatColor.translateAlternateColorCodes('\u0026', event.getLine(i)));
		}
	}

	/**
	 * Minecarts are automatically placed in dispensers upon collision.
	 * 
	 * @param event
	 *            the <code>VehicleBlockCollisionEvent</code>
	 */
	@EventHandler
	public void onVehicleBlockCollisionEvent(VehicleBlockCollisionEvent event) {
		if (event.getVehicle().getType() == EntityType.MINECART
				&& event.getBlock().getType() == Material.DISPENSER) {
			Block b = event.getBlock();
			Dispenser disp = (Dispenser)b.getState();
			disp.getInventory().addItem(new ItemStack(Material.MINECART));
			event.getVehicle().eject();
			event.getVehicle().remove();
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
				u.setPreviousLocation(event.getFrom());
				u.updateSleepstate();
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
		if (SblockUser.getUser(event.getPlayer().getName()).isServer()) {
			event.setCancelled(true);
			return;
		}
		if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
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
					// getFace does not seem to work in most cases - adam test and fix
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
			Log.err(e);
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
		packet.setAnimation((byte) Packet12Animation.Animations.LEAVE_BED);

		try {
			ProtocolLibrary.getProtocolManager().sendServerPacket(p, packet.getHandle());
		} catch (InvocationTargetException e) {
			Log.err(e);
		}
	}

	/**
	 * Check a packet from the client.
	 * <p>
	 * Currently intercepts only packets sent when client leaves bed.
	 * 
	 * @see com.comphenix.protocol.events.PacketAdapter#onPacketReceiving(PacketEvent)
	 * @param event the <code>PacketEvent</code>
	 */
	@Override
	public void onPacketReceiving(PacketEvent event) {
		if (event.getPacket().getType().equals(PacketType.Play.Client.ENTITY_ACTION)) {
			int action = event.getPacket().getIntegers().read(1);
			if (action == 3 && teleports.contains(event.getPlayer().getName())) {
				event.setCancelled(true);
				fakeWakeUp(event.getPlayer());
			}
		}
	}
}
