package co.sblock.events.region;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Runnable used to update the Regions of all Players.
 * 
 * @author Jikoo, Dublek
 */
public class RegionCheck extends BukkitRunnable {
	@SuppressWarnings("unused")
	private final World[] medium = new World[] {Bukkit.getWorld("LOWAS"),Bukkit.getWorld("LOFAF"),Bukkit.getWorld("LOHAC"),Bukkit.getWorld("LOLAR")};

	private HashMap<Player, String> playerQueue = new HashMap<Player, String>();
	private HashMap<Player, Integer> LOWASQueue = new HashMap<Player, Integer>();
	private HashMap<Player, Integer> LOLARQueue = new HashMap<Player, Integer>();
	private HashMap<Player, Integer> LOHACQueue = new HashMap<Player, Integer>();
	private HashMap<Player, Integer> LOFAFQueue = new HashMap<Player, Integer>();

	private final int planetaryRadius = 2631;
	private final int planetaryOffset = 128;
	private final int teleportDelay = 5;

	/**
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		//		for (World world : medium) {
		//			for (Player p : world.getPlayers()) {
		//				// TODO: calculate relative planets
		//				// Up: Dream
		//				// N:
		//				// S:
		//				// E:
		//				// W:
		//				// may just want separate loops because destination will be per-location per-planet anyway
		//				// I.E. southeast planet, triangle northwest would have destination Derspit(InnerCircle)
		//				//     east and south would have destination Derspit (OuterCircle) and other directions obvious
		//			}
		//		}
	}

	private Location calculateIncipisphereCoords(World world, Player p) {
		Location IC = new Location(world, p.getLocation().getX(), p.getLocation().getY(), p.getLocation().getZ());
		switch(world.getName()) {
		case "LOWAS":
			IC.setX(IC.getX()-planetaryRadius-planetaryOffset);
			IC.setZ(IC.getZ()+planetaryRadius+planetaryOffset);
			break;
		case "LOLAR":
			IC.setX(IC.getX()-planetaryRadius-planetaryOffset);
			IC.setZ(IC.getZ()-planetaryRadius-planetaryOffset);
			break;
		case "LOHAC":
			IC.setX(IC.getX()+planetaryRadius+planetaryOffset);
			IC.setZ(IC.getZ()-planetaryRadius-planetaryOffset);
			break;
		case "LOFAF":
			IC.setX(IC.getX()+planetaryRadius+planetaryOffset);
			IC.setZ(IC.getZ()+planetaryRadius+planetaryOffset);
			break;
		}
		return IC;
	}

	private void calculateRegionTransfer(Location IC, Player p) {
		String newWorld = ""; 
		if(IC.getX() < 0) {
			if(IC.getZ() < 0) {			//LOLAR --
				newWorld = "LOLAR";
			}
			else if(IC.getZ() > 0) { 	//LOWAS -+
				newWorld = "LOWAS";
			}
		}
		else if(IC.getX() > 0) {
			if(IC.getZ() < 0) {			//LOHAC +-
				newWorld = "LOHAC";
			}
			else if(IC.getZ() > 0) { 	//LOFAF ++
				newWorld = "LOFAF";
			}
		}

		if(IC.getWorld().getName() != newWorld) {
			if(playerQueue.get(p) == null) {
				playerQueue.put(p, newWorld);
			}
			switch(newWorld) {
			case "LOWAS":
				if(LOWASQueue.get(p) == null) {
					LOWASQueue.put(p, teleportDelay);
				}
				else {
					LOWASQueue.put(p, LOWASQueue.get(p)-1);
				}
				break;
			case "LOLAR":
				if(LOLARQueue.get(p) == null) {
					LOLARQueue.put(p, teleportDelay);
				}
				else {
					LOLARQueue.put(p, LOLARQueue.get(p)-1);
				}
				break;
			case "LOHAC":
				if(LOHACQueue.get(p) == null) {
					LOHACQueue.put(p, teleportDelay);
				}
				else {
					LOHACQueue.put(p, LOHACQueue.get(p)-1);
				}
				break;
			case "LOFAF":
				if(LOFAFQueue.get(p) == null) {
					LOFAFQueue.put(p, teleportDelay);
				}
				else {
					LOFAFQueue.put(p, LOFAFQueue.get(p)-1);
				}
				break;
			}
		}
		else {
			//Check if player is in any current queue, and remove from queue
			if(!playerQueue.containsKey(p)) {
				return;
			}
			switch(playerQueue.get(p)) {
			case "LOWAS":
				LOWASQueue.remove(p);
				break;
			case "LOLAR":
				LOLARQueue.remove(p);
				break;
			case "LOHAC":
				LOHACQueue.remove(p);
				break;
			case "LOFAF":
				LOFAFQueue.remove(p);
				break;
			}
			playerQueue.remove(p);
		}
	}
}
