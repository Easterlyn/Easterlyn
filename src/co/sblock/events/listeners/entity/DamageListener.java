package co.sblock.events.listeners.entity;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;

import co.sblock.Sblock;
import co.sblock.effects.Effects;
import co.sblock.events.listeners.SblockListener;

/**
 * Listener for EntityDamageEvents.
 * 
 * @author Jikoo
 */
public class DamageListener extends SblockListener {

	private final Effects effects;

	public DamageListener(Sblock plugin) {
		super(plugin);
		this.effects = plugin.getModule(Effects.class);
	}

	/**
	 * EventHandler for EntityDamageEvents.
	 * 
	 * @param event the EntityDamageEvent
	 */
	@EventHandler(ignoreCancelled = true)
	public void onEntityDamage(EntityDamageEvent event) {
		if (!(event.getEntity() instanceof Player)) {
			return;
		}

		effects.handleEvent(event, (Player) event.getEntity(), true);
	}
}
