package co.sblock.effects;

import co.sblock.module.Module;

public class SblockEffects extends Module {
    
    private static SblockEffects instance;
    private EffectManager eM;
    protected static boolean verbose = false;
    private EffectsCommandListener eCL = new EffectsCommandListener();

    @Override
    public void onEnable() {
        instance = this;
        this.registerCommands(eCL);
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
}
