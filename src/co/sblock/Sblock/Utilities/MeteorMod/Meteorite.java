package co.sblock.Sblock.Utilities.MeteorMod;

import java.util.ArrayList;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.plugin.Plugin;

import co.sblock.Sblock.Sblock;

public class Meteorite implements Listener	{
	
	private MeteorMod module;
	private int countdown;
	private int radius;
	private final int DEFAULT_RADIUS = 3;
	private final int DEFAULT_COUNTDOWN = 0;
	private Location target;
	private Location skyTarget;
	private Player pTarget;
	private boolean explosionBlockDamage;
	private Material mat;
	private final Material DEFAULT_MAT = Material.NETHERRACK;
	private ArrayList<Location> sphereCoords = new ArrayList<Location>();
	private ArrayList<UUID> blockID = new ArrayList<UUID>();
	private int initialLevel;
	
	public Meteorite(MeteorMod pl, Player pT, int c)	{
		pTarget = pT;
		countdown = c;
		module = pl;
		target = pTarget.getLocation();
		defaultMeteorite();
		Bukkit.getPluginManager().registerEvents(this, Sblock.getInstance());
		//see wall o' text below
		dropMeteorite();
	}
	public Meteorite(MeteorMod instance, Player pT, Location xyz, String m, int r, int c, boolean explode)	{
		if (pT != null)	{
			pTarget = pT;
		}
		target = xyz;
		
		this.defaultMeteorite();
		
		if (r > 0)	{
			radius = r;
		}
		if (!(m.equalsIgnoreCase("")))	{
			Material.matchMaterial(m);
		}
		if (c > 0 && pTarget != null)	{
			countdown = c;
		}
		explosionBlockDamage = explode;
		module = instance;
		Bukkit.getPluginManager().registerEvents(this, Sblock.getInstance());
		//Ok, I know this is pretty much the most awful practice ever, but I need the list of meteorites in case
		//the UUID list doesn't end up fully empty for some reason - it would suck to have like 43824578245 meteors
		//checking events. This is why I suggested exploding all UUIDs on contact of 1. Would look less cool, yes,
		//but would prevent this sort of issue. Here we are instead, you're welcome. ;-;
		dropMeteorite();
	}
	
	public void defaultMeteorite()	{
		//target handled by constructor
		radius = DEFAULT_RADIUS;
		skyTarget = target.clone();
		target.setY(target.getWorld().getHighestBlockAt(target).getY());
		skyTarget.setY(255 - radius);
		mat = DEFAULT_MAT;
		countdown = DEFAULT_COUNTDOWN;
		if (pTarget != null) initialLevel = pTarget.getLevel();
	}
	
	@EventHandler
	public void onEntityChangeBlockEvent(EntityChangeBlockEvent event){
		//Logger.getLogger("Minecraft").info("Meteor in position! (EntityChangeBlockEvent)");
		try {
			for (UUID u : blockID)	{
				if (u.equals(event.getEntity().getUniqueId()))	{
					//Logger.getLogger("Minecraft").info("Meteor in position!");
					explode(event.getBlock().getLocation());
					event.getBlock().setType(Material.AIR);
					blockID.remove(u);
					if (blockID.isEmpty())
						doHandlerUnregister();
					return;
				}
				else	{
					event.setCancelled(true);
				}
			}
		} catch (NullPointerException e) {
		}

	}
		
	public void dropMeteorite() {
		if (countdown >= 0) {
			if (pTarget != null) {
				pTarget.setLevel(countdown);
			}
			doCounterTick();
			return;
		} else {
			if (pTarget != null) {
				pTarget.setLevel(initialLevel);
			}
			genMeteorite();
			if (sphereCoords.size() >= 1) { //Ensures that genSphereCoords has been run (well)
				for (Location a : sphereCoords)	{			
					a.getBlock().setType(Material.AIR);
					blockID.add(skyTarget.getWorld().spawnFallingBlock(a, mat, (byte) 0).getUniqueId());
				}			
				Bukkit.getLogger().info("Meteorificationalizing " + target.getBlockX() + ", " + target.getBlockZ());
			} else Bukkit.getLogger().info("What kind of a sphere did you just generate? Also, how?");
		}
	}
	
	public void genMeteorite()	{	//Creates a floating ball. Calling this in dropMeteorite() to make life easier.
		sphereCoords = this.genSphereCoords(radius);
		sphereCoords.removeAll(this.genSphereCoords(radius - 1));
		for (Location a : sphereCoords)	{			
				a.getBlock().setType(mat);
		}
	}
	
	private ArrayList<Location> genSphereCoords(int r)	{
		ArrayList<Location> coords = new ArrayList<Location>();
		double bpow = Math.pow(r, 2);
		double bx = skyTarget.getX();
		double by = skyTarget.getY();
		double bz = skyTarget.getZ();
		
		 for (int z = 0; z <= r; z++) {
	            double zpow = Math.pow(z, 2);
	            for (int x = 0; x <= r; x++) {
	                double xpow = Math.pow(x, 2);
	                for (int y = 0; y <= r; y++) {
	                    if ((xpow + Math.pow(y, 2) + zpow) <= bpow) {
	                    	coords.add(new Location(skyTarget.getWorld(), bx + x, by + y, bz + z));
	                    	coords.add(new Location(skyTarget.getWorld(), bx + x, by + y, bz - z));
	                    	coords.add(new Location(skyTarget.getWorld(), bx - x, by + y, bz + z));
	                    	coords.add(new Location(skyTarget.getWorld(), bx - x, by + y, bz - z));
	                    	coords.add(new Location(skyTarget.getWorld(), bx + x, by - y, bz + z));
	                    	coords.add(new Location(skyTarget.getWorld(), bx + x, by - y, bz - z));
	                    	coords.add(new Location(skyTarget.getWorld(), bx - x, by - y, bz + z));
	                    	coords.add(new Location(skyTarget.getWorld(), bx - x, by - y, bz - z));
	                    }
	                }
	            }
		 }
		 return coords;
	}
	public void explode(Location loc)	{
		target.getWorld().createExplosion(loc.getX(), loc.getY(), loc.getZ(), 4F, false, explosionBlockDamage);
	}
	

	public void doCounterTick() {
		Bukkit.getScheduler().scheduleSyncDelayedTask(Sblock.getInstance(), new scheduledCounter(), 20);
	}
	public class scheduledCounter implements Runnable	{

		@Override
		public void run() {
			countdown--;
			dropMeteorite();
		}
		
	}
	
	public void doHandlerUnregister() {
		Bukkit.getScheduler().scheduleSyncDelayedTask(Sblock.getInstance(), new handlerUnregister(this));
	}
	public class handlerUnregister implements Runnable {
		Meteorite m;
		public handlerUnregister(Meteorite m) {
			this.m = m;
		}
		public void run() {
			//I don't know if it's safe to do this inside an event handler, so synchronous task.
			if (!blockID.isEmpty()) {
				//there is no easier way to get entity by UUID, sadly. Loop here we come!
				for (Entity e : target.getWorld().getEntities()) {
					if (blockID.contains(e.getUniqueId())) { 
						blockID.remove(e.getUniqueId());
						e.remove();
					}
				}
			}
			HandlerList.unregisterAll(m);
		}
	}
}

