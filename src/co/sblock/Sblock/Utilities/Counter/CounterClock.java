package co.sblock.Sblock.Utilities.Counter;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class CounterClock extends BukkitRunnable {

	private Set<Player> deletionSet = new HashSet<Player>();
	
	public CounterClock() {
		
	}

	@Override
	public void run() {
		for(Counter c : CounterModule.counterMap.values())	{
			
			if(c.getCurrent() > 0){
				c.getPlayer().sendMessage("Tick " + c.getCurrent() + c.getCooldown());
				c.tick();
			}
			else if(c.getCurrent() == 0 && c.getCooldown() > 0)	{
				c.getPlayer().sendMessage("Cooldown Tick");
				c.tickCooldown();
			}
			else if(c.getCurrent() == 0 && c.getCooldown() == 0)	{
				c.getPlayer().sendMessage("Fin");
				c.stopCounter();
				deletionSet.add(c.getPlayer());
			}
		}
		for(Player p : deletionSet)	{
			CounterModule.deleteCounter(p);
		}

	}

}
