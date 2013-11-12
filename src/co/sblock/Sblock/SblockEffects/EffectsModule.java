package co.sblock.Sblock.SblockEffects;

import org.bukkit.scheduler.BukkitTask;

import co.sblock.Sblock.Module;
import co.sblock.Sblock.Sblock;
import co.sblock.Sblock.Utilities.Sblogger;

public class EffectsModule extends Module {
	/*
	 * Keiko TODO:
	 * low:
	 * + stacking effects (potentially also for godtier)
	 * + 
	 * godtier:
	 * + reduce tick time
	 * + 
	 * 
	 */
	private static EffectsModule instance;
	private EffectListener eL;
	private EffectManager eM;
	@SuppressWarnings("unused")
	private BukkitTask task;
	protected static boolean verbose = false;
	private EffectsCommandListener eCL = new EffectsCommandListener();
	private EffectListener eListener = new EffectListener();

	@Override
	public void onEnable() {
		Sblogger.info("SburbEffects", "Enabling Effects");
		instance = this;
		this.registerCommands(eCL);
		this.registerEvents(eListener);
		eL = new EffectListener();
		eM = new EffectManager();
		Sblogger.info("SburbEffects", "Effects check task started");
		task = new EffectScheduler().runTaskTimer(Sblock.getInstance(), 0, 1180);
		Sblogger.info("SburbEffects", "Effects enabled");
	}

	@Override
	public void onDisable() {
		
	}
	public EffectManager getEffectManager() {
		return eM;
	}
	public EffectListener getEffectListener() {
		return eL;
	}
	public static EffectsModule getInstance() {
		return instance;
	}
}
