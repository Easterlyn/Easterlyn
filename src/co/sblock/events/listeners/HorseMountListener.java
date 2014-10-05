package co.sblock.events.listeners;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.inventory.ItemStack;

import co.sblock.Sblock;

public class HorseMountListener implements Listener
{
	@EventHandler
    public void onVehicleEnter(VehicleEnterEvent event) {
        Entity type = event.getVehicle();
        if (type != null && type.getType() == EntityType.HORSE)
        {
        	Horse horse = (Horse)type;
        	ItemStack saddle = horse.getInventory().getSaddle();
        	if (saddle != null && saddle.containsEnchantment(Enchantment.ARROW_FIRE))
        	{
        		FireTracker.registerAndStartFire(horse);
        	}
        }
    }
	
	@EventHandler
    public void onVehicleExit(VehicleExitEvent event) {
		Entity type = event.getVehicle();
        if (type != null && type.getType() == EntityType.HORSE)
        {
        	FireTracker.stopFireEffect((Horse)type);
        }
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
			if (horse.isDead())
			{
				FireTracker.stopFireEffect(horse);
			}
			if (running)
			{
				horse.getWorld().playEffect(horse.getLocation(), Effect.MOBSPAWNER_FLAMES, 10);
			}
			else
			{
				Bukkit.getScheduler().cancelTask(threadID);
			}
		}

		public void setID(int threadID2)
		{
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
			stop(blazingSaddles.get(horse.getUniqueId()));
		}
		
		private static void stop(HorseFireAnimator hfm)
		{
			if (hfm != null)
			{
				hfm.stop();
			}
		}
	}
}
