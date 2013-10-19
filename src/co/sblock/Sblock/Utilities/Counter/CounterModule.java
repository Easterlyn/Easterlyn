package co.sblock.Sblock.Utilities.Counter;

import java.util.HashMap;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import co.sblock.Sblock.Module;
import co.sblock.Sblock.Sblock;

public class CounterModule extends Module{

	public static HashMap<Player, Counter> counterMap;
	private BukkitTask task;
	private CounterCommandListener cCL = new CounterCommandListener();
	
	@Override
	protected void onEnable() {
		counterMap = new HashMap<Player, Counter>();
		task = new CounterClock().runTaskTimer(Sblock.getInstance(), 20, 20);
		this.registerCommands(cCL);
	}

	@Override
	protected void onDisable() {
		counterMap.clear();
		task.cancel();
	}
	
	public static void createCounter(Player player, int length)	{
		Counter counter = new Counter(player, length);
		if (counterMap.containsKey(player))	{
			counterMap.remove(player);
		}
		counterMap.put(player, counter);
	}
	public static void deleteCounter(Player p)	{
		counterMap.remove(p);
	}

}
