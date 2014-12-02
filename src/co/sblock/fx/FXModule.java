package co.sblock.fx;

import co.sblock.module.Module;

public class FXModule extends Module {

	@Override
	protected void onEnable() {
		new FXManager();
	}

	@Override
	protected void onDisable() {

	}

	@Override
	protected String getModuleName() {

		return "FXModule";
	}

}
