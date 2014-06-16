package co.sblock.events.listeners;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import co.sblock.utilities.minecarts.FreeCart;

/**
 * Listener for PlayerDeathEvents.
 * 
 * @author Jikoo
 */
public class PlayerDeathListener implements Listener {

	/**
	 * EventHandler for PlayerDeathEvents.
	 * 
	 * @param event the PlayerDeathEvent
	 */
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {

		// Remove free minecart if riding one
		FreeCart.getInstance().remove(event.getEntity());

		String message = new String[] {"Oh dear, you are dead.", "Crikey, that was a big 'un!",
				"I say, my dear chap, you appear to have died a little there.", "Git rekt.",
				"That was a fatal miscalculation."}[(int) (Math.random() * 5)];
		event.getEntity().sendMessage(ChatColor.RED + message + " Death point: "
				+ ChatColor.AQUA + event.getEntity().getLocation().getBlockX() +"x, "
				+ event.getEntity().getLocation().getBlockY() +"y, "
				+ event.getEntity().getLocation().getBlockZ() +"z");

		// Fun future feature for when I get bored
//		Player killer = event.getEntity().getKiller();
//		String message = null;
//		switch (event.getEntity().getLastDamageCause().getCause()) {
//		case BLOCK_EXPLOSION:
//			if (killer == null) {
//				message = new String[]{"", "", ""}[0];
//				event.setDeathMessage(message);
//			}
//			break;
//		case CONTACT:
//			break;
//		case DROWNING:
//			break;
//		case ENTITY_ATTACK:
//		case ENTITY_EXPLOSION:
//			break;
//		case FALL:
//			break;
//		case FALLING_BLOCK:
//			break;
//		case FIRE:
//		case FIRE_TICK:
//			break;
//		case LAVA:
//			break;
//		case LIGHTNING:
//			break;
//		case MAGIC:
//			break;
//		case POISON:
//			break;
//		case PROJECTILE:
//			break;
//		case STARVATION:
//			break;
//		case SUFFOCATION:
//			break;
//		case THORNS:
//			break;
//		case VOID:
//			break;
//		case WITHER:
//			break;
//		case SUICIDE:
//		case CUSTOM:
//		default:
//			break;
//		
//		}
	}
}
