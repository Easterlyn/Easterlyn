package co.sblock.Sblock;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import co.sblock.Sblock.Utilities.Log;

/**
 * The base class for all plugin modules, allowing separate components of the
 * plugin to be managed separately.
 * 
 * @author FireNG
 */
public abstract class Module {
	/** The <code>Set</code> of <code>Listener</code>s registered by this <code>Module</code>. */
	private Set<Listener> listeners = new HashSet<Listener>();

	/**
	 * Called when the <code>Module</code> is enabled.
	 */
	protected abstract void onEnable();

	/**
	 * Called when the <code>Module</code> is disabled before handlers are unassigned.
	 */
	protected abstract void onDisable();

	/**
	 * Register events for one or more <code>Listener</code>s.
	 * 
	 * @param listeners
	 *            <code>Listener[]</code>
	 */
	protected final void registerEvents(Listener... listeners) {
		for (Listener listener : listeners) {
			Bukkit.getPluginManager().registerEvents(listener, Sblock.getInstance());
			this.listeners.add(listener);
		}
	}

	/**
	 * Registers all of the <code>Command</code>s handled in the given <code>CommandListener</code>.
	 * 
	 * @param listener
	 *            <code>Listener</code> to register.
	 */
	protected final void registerCommands(CommandListener listener) {
		Sblock.getInstance().registerCommands(listener);
	}

	/**
	 * Enables the <code>Module</code>.
	 * 
	 * @return the <code>Module</code> enabled
	 */
	public final Module enable() {
		try {
			this.onEnable();
		} catch (Exception e) {
			throw new RuntimeException("[SblockSuite] Unhandled exception in module "
					+ this.getClass().getSimpleName() + ". Plugin failed to load.", e);
		}
		return this;
	}

	/**
	 * Disables the <code>Module</code>.
	 * 
	 * @return the <code>Module</code> disabled
	 */
	public final Module disable() {

		try {
			this.onDisable();
			for (Listener listener : listeners) {
				HandlerList.unregisterAll(listener);
			}
		} catch (Exception e) {
			throw new RuntimeException("Unhandled exception in module "
					+ this.getClass().getSimpleName() + ". Plugin failed to properly disable.", e);
		}
		this.getLogger().info("Disabled module " + this.getClass().getSimpleName());
		return this;
	}

	/**
	 * 
	 * @return a <code>Logger</code> that the plugin may use whose name is the same
	 *         as this <code>Module</code>'s class name.
	 */
	public final Log getLogger() {
		return new Log(this.getClass().getSimpleName(), null);
	}
}
