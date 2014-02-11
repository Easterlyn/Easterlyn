package co.sblock.Sblock.SblockEffects;

import co.sblock.Sblock.Module;
import co.sblock.Sblock.Sblock;

public class SblockEffects extends Module {
	
	private static SblockEffects instance;
	private EffectListener eL;
	private EffectManager eM;
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
		new EffectScheduler().runTaskTimer(Sblock.getInstance(), 0, 1180);
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
