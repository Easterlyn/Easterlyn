package co.sblock.events.region;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import co.sblock.machines.utilities.Direction;
import co.sblock.users.Region;

/**
 * Runnable used to update the Regions of all Players.
 * 
 * @author Jikoo, Dublek
 */
public class RegionCheck extends BukkitRunnable {
	private final World[] medium = new World[] {Bukkit.getWorld("LOWAS"),Bukkit.getWorld("LOFAF"),Bukkit.getWorld("LOHAC"),Bukkit.getWorld("LOLAR")};

	private HashMap<Player, String> playerQueue = new HashMap<Player, String>();
	private HashMap<String, HashMap<Player, Integer>> worldMap = new HashMap<String, HashMap<Player, Integer>>();
	private HashMap<Player, Integer> LOWASQueue = new HashMap<Player, Integer>();
	private HashMap<Player, Integer> LOLARQueue = new HashMap<Player, Integer>();
	private HashMap<Player, Integer> LOHACQueue = new HashMap<Player, Integer>();
	private HashMap<Player, Integer> LOFAFQueue = new HashMap<Player, Integer>();

	private final int planetaryRadius = 2631;
	private final int planetaryOffset = 128;
	private final int teleportDelay = 5;

	public RegionCheck() {
		worldMap.put("LOWAS", LOWASQueue);
		worldMap.put("LOLAR", LOLARQueue);
		worldMap.put("LOHAC", LOHACQueue);
		worldMap.put("LOFAF", LOFAFQueue);
	}
	
	/**
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		for(World world : medium) {
			for(Player p : world.getPlayers()) {
				Location iC = calculateIncipisphereCoords(world, p);
				calculateRegionTransfer(p, iC);
			}
		}
	}

	private Location calculateIncipisphereCoords(World world, Player p) {
		Location iC = new Location(world, p.getLocation().getX(), p.getLocation().getY(), p.getLocation().getZ());
		switch(world.getName()) {
		case "LOWAS":
			iC.setX(iC.getX()-planetaryRadius-planetaryOffset);
			iC.setZ(iC.getZ()+planetaryRadius+planetaryOffset);
			break;
		case "LOLAR":
			iC.setX(iC.getX()-planetaryRadius-planetaryOffset);
			iC.setZ(iC.getZ()-planetaryRadius-planetaryOffset);
			break;
		case "LOHAC":
			iC.setX(iC.getX()+planetaryRadius+planetaryOffset);
			iC.setZ(iC.getZ()-planetaryRadius-planetaryOffset);
			break;
		case "LOFAF":
			iC.setX(iC.getX()+planetaryRadius+planetaryOffset);
			iC.setZ(iC.getZ()+planetaryRadius+planetaryOffset);
			break;
		}
		return iC;
	}

	private void calculateRegionTransfer(Player p, Location iC) {
		String newWorld = ""; 
		if(iC.getX() < 0) {
			if(iC.getZ() < 0) {			//LOLAR --
				newWorld = "LOLAR";
			}
			else if(iC.getZ() > 0) { 	//LOWAS -+
				newWorld = "LOWAS";
			}
		}
		else if(iC.getX() > 0) {
			if(iC.getZ() < 0) {			//LOHAC +-
				newWorld = "LOHAC";
			}
			else if(iC.getZ() > 0) { 	//LOFAF ++
				newWorld = "LOFAF";
			}
		}

		if(iC.getWorld().getName() != newWorld) {
			if(playerQueue.get(p) == null) {
				playerQueue.put(p, newWorld);
			}
			if(worldMap.get(newWorld).get(p) == null) {
				worldMap.get(newWorld).put(p, teleportDelay);
			}
			else {
				worldMap.get(newWorld).put(p, worldMap.get(newWorld).get(p)-1);
			}
			if(worldMap.get(playerQueue.get(p)).get(p) == 0) {
				iC.setWorld(Bukkit.getWorld(newWorld));
				teleportPlayer(p, iC);
			}
			else {
				displayTitles(p);
			}
		}
		else {
			//Check if player is in any current queue, and remove from queue
			if(!playerQueue.containsKey(p)) {
				return;
			}
			worldMap.get(playerQueue.get(p)).remove(p);
			playerQueue.remove(p);
		}
	}
	
	private void displayTitles(Player p) {
		String titleCommand = "/title " + p.getName() + " times 0 20 0";
		String title = "/title " + p.getName() + " title {text:\"Approaching " + playerQueue.get(p) + "\",color:\"" + Region.getRegion(playerQueue.get(p)).getColor().getName() + "\"}";
		String subtitle = "/title " + p.getName() + " subtitle {text:\"Continue " + Direction.getFacingDirection(p).name() + " to transfer in " + worldMap.get(playerQueue.get(p)).get(p) + " seconds\"}";
		
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), titleCommand);
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), title);
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), subtitle);
	}
	
	private void teleportPlayer(Player p, Location iC) {
		Location target = iC.clone();
		
		switch(iC.getWorld().getName()) {
		case "LOWAS":
			target.setX(iC.getX()+planetaryRadius+planetaryOffset);
			target.setZ(iC.getZ()-planetaryRadius-planetaryOffset);
			break;
		case "LOLAR":
			target.setX(iC.getX()+planetaryRadius+planetaryOffset);
			target.setZ(iC.getZ()+planetaryRadius+planetaryOffset);
			break;
		case "LOHAC":
			target.setX(iC.getX()-planetaryRadius-planetaryOffset);
			target.setZ(iC.getZ()+planetaryRadius+planetaryOffset);
			break;
		case "LOFAF":
			target.setX(iC.getX()-planetaryRadius-planetaryOffset);
			target.setZ(iC.getZ()-planetaryRadius-planetaryOffset);
			break;
		}
		
		p.teleport(target);
		p.setVelocity(p.getVelocity().setY(p.getVelocity().getY()+3));
	}
}

