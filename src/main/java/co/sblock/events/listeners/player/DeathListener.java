package co.sblock.events.listeners.player;

import java.util.concurrent.ThreadLocalRandom;

import co.sblock.Sblock;
import co.sblock.chat.Language;
import co.sblock.events.Events;
import co.sblock.events.listeners.SblockListener;
import co.sblock.micromodules.FreeCart;
import co.sblock.micromodules.Godule;
import co.sblock.users.UserAspect;
import co.sblock.utilities.Experience;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Listener for PlayerDeathEvents.
 * 
 * @author Jikoo
 */
public class DeathListener extends SblockListener {

	private final Events events;
	private final FreeCart carts;
	private final Godule godule;
	private final Language lang;
	private final ItemStack facts;
	private final String[] messages;

	public DeathListener(Sblock plugin) {
		super(plugin);
		this.events = plugin.getModule(Events.class);
		this.carts = plugin.getModule(FreeCart.class);
		this.godule = plugin.getModule(Godule.class);
		this.lang = plugin.getModule(Language.class);
		this.messages = lang.getValue("events.death.random").split("\n");
		this.facts = new ItemStack(Material.WRITTEN_BOOK);
		BookMeta meta = (BookMeta) facts.getItemMeta();
		meta.setTitle("Wither Facts");
		meta.setAuthor(Language.getColor("rank.denizen") + "Pete");
		meta.addPage("Withers are awesome.");
		this.facts.setItemMeta(meta);
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
		this.carts.remove(player);

		Location location = player.getLocation();
		String randomMessage = messages[ThreadLocalRandom.current().nextInt(messages.length)];
		String locString = this.lang.getValue("events.death.message")
				.replace("{X}", String.valueOf(location.getBlockX()))
				.replace("{Y}", String.valueOf(location.getBlockY()))
				.replace("{Z}", String.valueOf(location.getBlockZ()));
		player.sendMessage(Language.getColor("bad") + locString.replaceAll("\\{WORLD\\}\\s?", "").replace("{OPTION}", randomMessage));
		locString = locString.replace("{WORLD}", location.getWorld().getName()).replaceAll("\\{OPTION\\}\\s?", "");

		EntityDamageEvent lastDamage = player.getLastDamageCause();
		if (lastDamage != null && (lastDamage.getCause() == DamageCause.WITHER
				|| (lastDamage instanceof EntityDamageByEntityEvent
						&& ((EntityDamageByEntityEvent) lastDamage).getDamager().getType() == EntityType.WITHER))) {
			new BukkitRunnable() {
				@Override
				public void run() {
					if (player != null) {
						player.getInventory().addItem(facts);
					}
				}
			}.runTask(getPlugin());
		}

		// TODO post deaths (sans coordinates) to global chat
		if (this.events.getPVPTasks().containsKey(player.getUniqueId())) {
			event.setDroppedExp(Experience.getExp(player));
			int dropped = Experience.getExp(player) / 10;
			if (dropped > 30) {
				dropped = 30;
			}
			event.setDroppedExp(dropped);
			Experience.changeExp(player, -dropped);
			event.setKeepLevel(true);
			event.setKeepInventory(true);
			this.events.getPVPTasks().remove(player.getUniqueId()).cancel();
			Player killer = player.getKiller();
			if (killer == null) {
				return;
			}
			Bukkit.getConsoleSender().sendMessage(String.format("%s died to %s. %s",
					player.getName(), killer.getName(), locString));
			if (godule.isEnabled(UserAspect.BREATH)) {
				ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
				SkullMeta meta = (SkullMeta) skull.getItemMeta();
				meta.setOwner(player.getName());
				skull.setItemMeta(meta);
				player.getWorld().dropItem(player.getLocation(), skull);
			}
		} else {
			Bukkit.getConsoleSender().sendMessage(String.format("%s died to %s. %s",
					player.getName(), lastDamage != null ? lastDamage.getCause().name() : "null", locString));
		}
	}
}
