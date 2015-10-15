package co.sblock.micromodules;

import java.util.HashMap;
import java.util.Map;

import co.sblock.micromodules.godules.AspectEffect;
import co.sblock.module.Module;
import co.sblock.users.UserAspect;

/**
 * Module for managing "god" behaviors.
 * 
 * @author Jikoo
 */
public class Godule extends Module {

	private static Godule instance;

	private Map<UserAspect, AspectEffect> aspeffects;

	@Override
	protected void onEnable() {
		instance = this;
		aspeffects = new HashMap<>();
	}

	@Override
	protected void onDisable() {
		instance = null;
		for (AspectEffect aspeffect : aspeffects.values()) {
			aspeffect.onDisable();
		}
	}

	public void enable(UserAspect aspect) {
		if (aspeffects.containsKey(aspect)) {
			aspeffects.get(aspect).onEnable();
			return;
		}
		try {
			Class<?> clazz = Class.forName(getClass().getPackage() + ".godules." + aspect.getDisplayName() + "Effect");
			AspectEffect aspeffect = (AspectEffect) clazz.newInstance();
			aspeffect.onEnable();
			aspeffects.put(aspect, aspeffect);
			return;
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			// For now, catch silently - Not all aspects have plans
			return;
		}
	}

	public void disable(UserAspect aspect) {
		if (aspeffects.containsKey(aspect)) {
			aspeffects.get(aspect).onDisable();
		}
	}

	public boolean isEnabled(UserAspect aspect) {
		return aspeffects.containsKey(aspect) && aspeffects.get(aspect).isEnabled();
	}

	public AspectEffect getAspectEffect(UserAspect aspect) {
		return aspeffects.get(aspect);
	}

	@Override
	protected String getModuleName() {
		return "GodManager";
	}

	public static Godule getInstance() {
		return instance;
	}

}
