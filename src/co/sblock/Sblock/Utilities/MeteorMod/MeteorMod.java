package co.sblock.Sblock.Utilities.MeteorMod;

import java.util.ArrayList;

import org.bukkit.event.Listener;
import co.sblock.Sblock.Module;
import co.sblock.Sblock.Sblock;

public class MeteorMod extends Module implements Listener {
	private static MeteorMod instance;
	//private BukkitTask task;
	private MeteorCommandListener mcl = new MeteorCommandListener();

	
	private static ArrayList<Meteorite> meteorites = new ArrayList<Meteorite>();

	//TODO Move commands into Fire's command handler
	
	@Override
	public void onEnable()	{
		this.registerCommands(mcl);
		Sblock.getInstance().getServer().getPluginManager().registerEvents(this, Sblock.getInstance());
		//startReckoning(20*20);
	}
	
	@Override
	public void onDisable()	{
		meteorites = null;
	//	stopReckoning();
	}
	
	public static MeteorMod getInstance()	{
		return instance;
	}
	
	public static ArrayList<Meteorite> getMeteorites()	{
		return meteorites;
	}
	
/*	public void startReckoning(long rLong)	{
		task = new scheduledReckoning().runTaskTimer(this, 20*300, rLong);
		  }
	
	public void stopReckoning()	{
		task.cancel();
	}
*/


/*	public class scheduledReckoning	extends BukkitRunnable	{
		@Override
		public void run() {
			if (getServer().getOnlinePlayers().length >= 1) {
				Player pTarget = getServer().getOnlinePlayers()[(int) (getServer()
						.getOnlinePlayers().length * Math.random())];
				Location target = pTarget.getLocation();
				target.setX((int) ((160 * Math.random()) - 80));
				target.setZ((int) ((160 * Math.random()) - 80));
				int radius = -1;
				int countdown = -1;
				String material = "";
				boolean blockDamage = false;
				getLogger().info(
						pTarget.getName()
								+ "has been randomly selected for termination");
				meteorites.add(new Meteorite(plugin, pTarget, target, material,
						radius, countdown, blockDamage));
			}
		}
	}*/
}