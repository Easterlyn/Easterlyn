/**
 * 
 */
package co.sblock.Sblock.Events;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.material.Bed;
import com.bergerkiller.bukkit.common.events.PacketReceiveEvent;
import com.bergerkiller.bukkit.common.events.PacketSendEvent;
import com.bergerkiller.bukkit.common.protocol.PacketFields;
import com.bergerkiller.bukkit.common.protocol.PacketListener;
import com.bergerkiller.bukkit.common.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;

import co.sblock.Sblock.Sblock;
import co.sblock.Sblock.Chat.ChatStorage;
import co.sblock.Sblock.UserData.Region;
import co.sblock.Sblock.UserData.SblockUser;
import co.sblock.Sblock.UserData.UserManager;

/**
 * @author Jikoo
 *
 */
public class EventListener implements Listener, PacketListener {

	private Map<String, Integer> tasks = new HashMap<String, Integer>();

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerLogin(PlayerLoginEvent event) {
		switch (event.getResult()) {
		case ALLOWED:
		case KICK_FULL:
		case KICK_WHITELIST:
			return;
		case KICK_BANNED:
		case KICK_OTHER:
			String reason = new ChatStorage().getBan(event.getPlayer().getName());
			System.out.println("[DEBUG] Changing disconnect to " + reason);
			if (reason != null) {
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
		//u.syncSetCurrentChannel("#");
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		if (SblockUser.getUser(event.getPlayer().getName()) != null) {
			event.setCancelled(true);
			if (event.getMessage().indexOf("/") == 0) {
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

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerQuit(PlayerQuitEvent event) {
		SblockUser u = SblockUser.getUser(event.getPlayer().getName());
		if (u == null) {
			return; // We don't want to make another db call just to announce quit.
		}
		if (tasks.containsKey(u.getPlayerName())) {
			Bukkit.getScheduler().cancelTask(tasks.remove(u.getPlayerName()));
		}
		for (String s : u.getListening()) {
			u.removeListeningQuit(s);
		}
		UserManager.getUserManager().removeUser(event.getPlayer());
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (!event.isCancelled() && event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
			Block b = event.getClickedBlock();
			if (b.getType().equals(Material.BED_BLOCK)) {
				Bed bed = (Bed) b.getState().getData();
				Location head;
				if (bed.isHeadOfBed()) {
					head = b.getLocation();
				} else {
					head = b.getRelative(bed.getFacing().getOppositeFace()).getLocation();
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
		p.setPlayerTime(20000, true);
		
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

	/* (non-Javadoc)
	 * @see com.bergerkiller.bukkit.common.protocol.PacketListener#onPacketReceive(com.bergerkiller.bukkit.common.events.PacketReceiveEvent)
	 */
	@Override
	public void onPacketReceive(PacketReceiveEvent event) {
		if (event.getType().equals(PacketType.ENTITY_ACTION)) {
			if(event.getPacket().read(PacketFields.ENTITY_ACTION.animation) == 3) {
				event.setCancelled(true);
				if (tasks.containsKey(event.getPlayer())) {
					Bukkit.getScheduler().cancelTask(tasks.get(event.getPlayer().getName()));
					event.getPlayer().resetPlayerTime();
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
		// Using ProtocolLib to send packets, not BKCommonLib
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
				switch (Region.getLocationRegion(p.getLocation())) { // TODO finish
				case EARTH:
//				case MEDIUM: // Someday, my pretties.
//				case LOFAF:
//				case LOHAC:
//				case LOLAR:
//				case LOWAS:
					p.setBedSpawnLocation(p.getLocation());
					user.setPreviousLocation(p.getLocation());
					p.teleport(EventModule.getEventModule().getTowerData()
							.getLocation((byte) user.getTower(),
									user.getDPlanet(), (byte) 0));
					p.resetPlayerTime();
					break;
				case FURTHESTRING:
				case INNERCIRCLE:
					p.teleport(user.getPreviousLocation());
					p.resetPlayerTime();
					break;
				default:
					break;
				}
			}
			tasks.remove(p.getName());
		}
	}
}
