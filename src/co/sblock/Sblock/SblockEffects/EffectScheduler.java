package co.sblock.Sblock.SblockEffects;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class EffectScheduler extends BukkitRunnable {
	
	private EffectManager eM;

	public EffectScheduler()	{
		eM = new EffectManager();
	}
	
	@Override
	public void run() {
	//	plugin.getLogger().info("Effect Tick");
		for(Player p : Bukkit.getServer().getOnlinePlayers())	{
			eM.applyPassiveEffects(eM.scan(p), p);
			
		}		
	}
}
