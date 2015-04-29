package co.sblock.events.listeners.player;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import co.sblock.events.Events;
import co.sblock.users.Users;
import co.sblock.utilities.experience.Experience;
import co.sblock.utilities.minecarts.FreeCart;

/**
 * Listener for PlayerDeathEvents.
 * 
 * @author Jikoo
 */
public class DeathListener implements Listener {

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
		String location = new StringBuilder(" Death point: ")
				.append(event.getEntity().getLocation().getBlockX()).append("x ")
				.append(event.getEntity().getLocation().getBlockY()).append("y ")
				.append(event.getEntity().getLocation().getBlockZ()).append('z').toString();
		event.getEntity().sendMessage(ChatColor.RED + message + location);

		if (Events.getInstance().getPVPTasks().containsKey(event.getEntity().getUniqueId())) {
			event.setDroppedExp(Experience.getExp(event.getEntity()));
			event.setKeepInventory(true);
			Events.getInstance().getPVPTasks().remove(event.getEntity().getUniqueId()).cancel();
			if (event.getEntity().getKiller() != null) {
				Bukkit.getConsoleSender().sendMessage(event.getEntity().getName() + " died to "
						+ event.getEntity().getKiller().getName() + ". " + location);
			}
		} else {
			Bukkit.getConsoleSender().sendMessage(event.getEntity().getName() + " died." + location);
		}

		Users.getGuaranteedUser(event.getEntity().getUniqueId()).getOnlineUser().removeAllEffects();
	}
}
