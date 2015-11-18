package co.sblock.events.listeners.player;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;

import co.sblock.Sblock;
import co.sblock.chat.Color;
import co.sblock.events.Events;
import co.sblock.micromodules.FreeCart;
import co.sblock.micromodules.Godule;
import co.sblock.users.UserAspect;
import co.sblock.utilities.Experience;

/**
 * Listener for PlayerDeathEvents.
 * 
 * @author Jikoo
 */
public class DeathListener implements Listener {

	private final ItemStack facts;

	public DeathListener() {
		facts = new ItemStack(Material.WRITTEN_BOOK);
		BookMeta meta = (BookMeta) facts.getItemMeta();
		meta.setTitle("Wither Facts");
		meta.setAuthor(Color.RANK_DENIZEN + "Pete");
		meta.addPage("Withers are awesome.");
		facts.setItemMeta(meta);
	}

	/**
	 * EventHandler for PlayerDeathEvents.
	 * 
	 * @param event the PlayerDeathEvent
	 */
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		final Player player = event.getEntity();

		// Remove free minecart if riding one
		FreeCart.getInstance().remove(player);

		String message = new String[] {"Oh dear, you are dead.", "Crikey, that was a big 'un!",
				"I say, my dear chap, you appear to have died a little there.", "Git rekt.",
				"That was a fatal miscalculation."}[(int) (Math.random() * 5)];
		String location = new StringBuilder(" Death point: ")
				.append(player.getLocation().getBlockX()).append("x ")
				.append(player.getLocation().getBlockY()).append("y ")
				.append(player.getLocation().getBlockZ()).append('z').toString();
		player.sendMessage(Color.BAD + message + location);

		EntityDamageEvent lastDamage = player.getLastDamageCause();
		if (lastDamage.getCause() == DamageCause.WITHER
				|| (lastDamage instanceof EntityDamageByEntityEvent 
						&& ((EntityDamageByEntityEvent) lastDamage).getDamager().getType() == EntityType.WITHER)) {
			new BukkitRunnable() {
				@Override
				public void run() {
					if (player != null) {
						player.getInventory().addItem(facts);
					}
				}
			}.runTask(Sblock.getInstance());
		}

		// TODO post deaths (sans coordinates) to global chat
		if (Events.getInstance().getPVPTasks().containsKey(player.getUniqueId())) {
			event.setDroppedExp(Experience.getExp(player));
			event.setKeepInventory(true);
			Events.getInstance().getPVPTasks().remove(player.getUniqueId()).cancel();
			Player killer = player.getKiller();
			if (killer == null) {
				return;
			}
			Bukkit.getConsoleSender().sendMessage(player.getName() + " died to "
					+ killer.getName() + "." + location);
			if (Godule.getInstance().isEnabled(UserAspect.BREATH)) {
				ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
				SkullMeta meta = (SkullMeta) skull.getItemMeta();
				meta.setOwner(player.getName());
				skull.setItemMeta(meta);
				player.getWorld().dropItem(player.getLocation(), skull);
			}
		} else {
			Bukkit.getConsoleSender().sendMessage(player.getName() + " died to "
					+ player.getLastDamageCause().getCause().name() + "." + location);
		}
	}
}
