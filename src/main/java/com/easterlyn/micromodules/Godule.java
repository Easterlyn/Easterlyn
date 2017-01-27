package com.easterlyn.micromodules;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import com.easterlyn.Easterlyn;
import com.easterlyn.micromodules.godules.AspectEffect;
import com.easterlyn.module.Module;
import com.easterlyn.users.UserAspect;

/**
 * Module for managing "god" behaviors.
 * 
 * @author Jikoo
 */
public class Godule extends Module {

	private final Map<UserAspect, AspectEffect> aspeffects;

	public Godule(Easterlyn plugin) {
		super(plugin);
		aspeffects = new HashMap<>();
	}

	@Override
	protected void onEnable() {}

	@Override
	protected void onDisable() {
		for (AspectEffect aspeffect : aspeffects.values()) {
			aspeffect.onDisable();
		}
	}

	public void enable(UserAspect aspect) {
		if (!this.isEnabled()) {
			return;
		}
		if (aspeffects.containsKey(aspect)) {
			aspeffects.get(aspect).onEnable();
			return;
		}
		try {
			Class<?> clazz = Class.forName(getClass().getPackage() + ".godules." + aspect.getDisplayName() + "Effect");
			Constructor<?> constructor = clazz.getConstructor(this.getClass());
			AspectEffect aspeffect = (AspectEffect) constructor.newInstance(this);
			aspeffect.onEnable();
			aspeffects.put(aspect, aspeffect);
			return;
		} catch (ClassCastException | ClassNotFoundException | InstantiationException
				| IllegalAccessException | NoSuchMethodException | SecurityException
				| IllegalArgumentException | InvocationTargetException e) {
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
	public boolean isRequired() {
		return false;
	}

	@Override
	public String getName() {
		return "GodManager";
	}

}
