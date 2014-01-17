package co.sblock.Sblock.SblockEffects;

import org.bukkit.scheduler.BukkitTask;

import co.sblock.Sblock.Module;
import co.sblock.Sblock.Sblock;

public class SblockEffects extends Module {
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
	private static SblockEffects instance;
	private EffectListener eL;
	private EffectManager eM;
	@SuppressWarnings("unused")
	private BukkitTask task;
	protected static boolean verbose = false;
	private EffectsCommandListener eCL = new EffectsCommandListener();
	private EffectListener eListener = new EffectListener();

	@Override
	public void onEnable() {
		getLogger().fine("Enabling Effects");
		instance = this;
		this.registerCommands(eCL);
		this.registerEvents(eListener);
		eL = new EffectListener();
		eM = new EffectManager();
		getLogger().fine("Effects check task started");
		task = new EffectScheduler().runTaskTimer(Sblock.getInstance(), 0, 1180);
		getLogger().fine("Effects enabled");
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
	public static SblockEffects getEffects() {
		return instance;
	}
}
