package co.sblock.effects;

import co.sblock.module.Module;

public class SblockEffects extends Module {

	private static SblockEffects instance;
	private EffectManager eM;
	protected static boolean verbose = false;

	@Override
	public void onEnable() {
		instance = this;
		eM = new EffectManager();
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

	@Override
	protected String getModuleName() {
		return "EffectsModule";
	}
}
