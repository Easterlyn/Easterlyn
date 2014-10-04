package co.sblock.events.listeners;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;

import co.sblock.Sblock;
import co.sblock.utilities.Log;

public class HorseMountListener implements Listener
{
	@EventHandler
    public void onVehicleEnter(VehicleEnterEvent event) {
        Log.getLog().info("Vehicle entered!");
        Entity type = event.getEntered();
        if (type != null && type.getType() == EntityType.HORSE)
        {
        	FireTracker.registerAndStartFire((Horse)type);
        }
    }
	
	@EventHandler
    public void onVehicleExit(VehicleExitEvent event) {
		Entity type = event.getExited();
        if (type != null && type.getType() == EntityType.HORSE)
        {
        	FireTracker.stopFireEffect((Horse)type);
        }
		Log.getLog().info("Vehicle exited!");
    }
	
	static class HorseFireAnimator implements Runnable
	{
		private final Horse horse;
		private boolean running = true;
		private int threadID;
		
		private HorseFireAnimator(Horse horse)
		{
			this.horse = horse;
		}
		
		public void stop()
		{
			running = false;
		}
		
		@Override
		public void run()
		{
			if (running)
			{
				horse.getWorld().playEffect(horse.getLocation(), Effect.MOBSPAWNER_FLAMES, 10);
				Log.getLog().info("flamessss");
			}
			else
			{
				Bukkit.getScheduler().cancelTask(threadID);
			}
		}

		public void setID(int threadID2) {
			threadID = threadID2;
		}
		
	}
	
	static class FireTracker
	{
		private static Map<UUID, HorseFireAnimator> blazingSaddles = new HashMap<>();
		
		public static void registerAndStartFire(final Horse horse)
		{
			HorseFireAnimator thread = new HorseFireAnimator(horse);
			int threadID = Bukkit.getScheduler().scheduleSyncRepeatingTask(Sblock.getInstance(), thread, 0l, 1l);
			thread.setID(threadID);
			blazingSaddles.put(horse.getUniqueId(), thread);
		}
		
		public static void stopFireEffect(Horse horse)
		{
			blazingSaddles.get(horse.getUniqueId()).stop();
		}
	}
}
