package co.sblock.Sblock.Utilities.Counter;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class CounterClock extends BukkitRunnable	{

	private JavaPlugin plugin;
	private Player p;
	public BukkitTask task;
	private int pLevel;
	private boolean cleanup;
	
	public CounterClock(JavaPlugin pl, Player play, int level, boolean end)	{
		plugin = pl;
		p = play;
		pLevel = level;
		cleanup = end;
	}
	
	@Override
	public void run() {
		if (p.getLevel() >= 1 && cleanup == false) {
			p.setLevel(p.getLevel() - 1);
			task = new CounterClock(plugin, p, pLevel, false).runTaskLater(plugin, 20);
		}
		else if (p.getLevel() == 0 && cleanup == false)	{
			task = new CounterClock(plugin, p, pLevel, true).runTaskLater(plugin, 100);			
		}
		else if (cleanup == true)	{
			p.setLevel(pLevel);
		}
			
		
	}

}
