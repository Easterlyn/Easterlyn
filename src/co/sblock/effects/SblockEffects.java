package co.sblock.effects;

import co.sblock.Module;
import co.sblock.Sblock;

public class SblockEffects extends Module {
	
	private static SblockEffects instance;
	private EffectManager eM;
	protected static boolean verbose = false;
	private EffectsCommandListener eCL = new EffectsCommandListener();

	@Override
	public void onEnable() {
		getLogger().fine("Enabling Effects");
		instance = this;
		this.registerCommands(eCL);
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
	public static SblockEffects getEffects() {
		return instance;
	}
}
