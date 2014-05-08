package co.sblock.Sblock.Events.Listeners;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

import com.dsh105.holoapi.HoloAPI;

/**
 * Listener for EntityDamageEvents.
 * 
 * @author Jikoo
 */
public class EntityDamageListener implements Listener {

	/**
	 * EventHandler for EntityDamageEvents. Checks post-event completion to create holograms.
	 * 
	 * @param event the EntityDamageEvent
	 */
	@EventHandler(priority = EventPriority.MONITOR)
	public void onEntityDamageComplete(EntityDamageEvent event) {
		if (!(event.getEntity() instanceof LivingEntity)) {
			return;
		}
		ChatColor damageColor;
		switch(event.getCause()) {
		case FIRE:
		case LAVA:
		case SUFFOCATION:
		case VOID:
			// These damage types happen every tick while applicable.
			// Utter resource hog, ignore.
			return;
		case BLOCK_EXPLOSION:
		case ENTITY_EXPLOSION:
		case LIGHTNING:
			damageColor = ChatColor.GOLD;
			break;
		case DROWNING:
			damageColor = ChatColor.AQUA;
			break;
		case FALL:
			damageColor = ChatColor.YELLOW;
			break;
		case FIRE_TICK:
		case MELTING:
			damageColor = ChatColor.DARK_RED;
			break;
		case MAGIC:
		case THORNS:
			damageColor = ChatColor.DARK_PURPLE;
			break;
		case POISON:
		case STARVATION:
			damageColor = ChatColor.DARK_GREEN;
			break;
		case SUICIDE:
		case WITHER:
			damageColor = ChatColor.BLACK;
			break;
		case CONTACT:
		case CUSTOM:
		case ENTITY_ATTACK:
		case FALLING_BLOCK:
		case PROJECTILE:
		default:
			damageColor = ChatColor.RED;
			break;
		}
		Location l = event.getEntity().getLocation().clone();
		l.setY(l.getY() + 2);
		HoloAPI.getManager().createSimpleHologram(l, 2, true,
				damageColor.toString() + (int) event.getDamage() + '\u2764');
	}
}
