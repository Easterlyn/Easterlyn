package co.sblock.Sblock.SblockEffects;

import org.bukkit.scheduler.BukkitTask;

import co.sblock.Sblock.Module;
import co.sblock.Sblock.Sblock;

public class EffectsModule extends Module	{
	
	private static EffectsModule instance;
	private EffectListener eL;
	private EffectManager eM;
	private BukkitTask task;
	protected static boolean verbose = false;
	private EffectsCommandListener eCL = new EffectsCommandListener();
	private EffectListener eListener = new EffectListener();

	@Override
	public void onEnable()	{
		instance = this;
		this.registerCommands(eCL);
		this.registerEvents(eListener);
		eL = new EffectListener();
		eM = new EffectManager();
		task = new EffectScheduler().runTaskTimer(Sblock.getInstance(), 0, 1180);
	}

	@Override
	public void onDisable()	{
		
	}
	public EffectManager getEffectManager()	{
		return eM;
	}
	public EffectListener getEffectListener()	{
		return eL;
	}
	public static EffectsModule getInstance()	{
		return instance;
	}
}
