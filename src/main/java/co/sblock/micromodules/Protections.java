package co.sblock.micromodules;

import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.reflections.Reflections;

import co.sblock.Sblock;
import co.sblock.micromodules.protectionhooks.ProtectionHook;
import co.sblock.module.Module;

/**
 * Module for managing hooks for protection plugins.
 * 
 * @author Jikoo
 */
public class Protections extends Module {

	private final Map<String, ProtectionHook> hooks;

	public Protections(Sblock plugin) {
		super(plugin);
		this.hooks = new HashMap<>();
	}

	@Override
	protected void onEnable() {
		Reflections reflections = new Reflections("co.sblock.micromodules.protectionhooks");
		for (Class<? extends ProtectionHook> hookClazz : reflections.getSubTypesOf(ProtectionHook.class)) {
			if (Modifier.isAbstract(hookClazz.getModifiers())) {
				continue;
			}
			ProtectionHook hook;
			try {
				hook = hookClazz.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
				continue;
			}
			if (hook.isHookUsable()) {
				hooks.put(hook.getPluginName(), hook);
			}
		}
	}

	public void enableHook(String plugin) {
		// TODO
		throw new UnsupportedOperationException("unimplemented");
	}

	public void disableHook(String plugin) {
		// TODO
		throw new UnsupportedOperationException("unimplemented");
	}

	public Collection<ProtectionHook> getHooks() {
		return hooks.values();
	}

	@Override
	protected void onDisable() {
		hooks.clear();
	}

	@Override
	public boolean isRequired() {
		return true;
	}

	@Override
	public String getName() {
		return "Protections";
	}

}
