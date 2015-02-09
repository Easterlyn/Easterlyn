package co.sblock.events.listeners;

import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;

import co.sblock.events.packets.ParticleUtils;
import co.sblock.events.packets.ParticleEffectWrapper;
import co.sblock.users.Users;

public class VehicleEnterListener implements Listener {

	public VehicleEnterListener() {
		new Permission("sblock.blaze").addParent("sblock.donator", true).recalculatePermissibles();
	}

	@EventHandler
	public void onVehicleEnter(VehicleEnterEvent event) {
		if (event.getVehicle() instanceof Player && !event.getEntered().hasPermission("sblock.helper")
				&& !Users.getGuaranteedUser(event.getVehicle().getUniqueId()).getSpectatable()) {
			event.getEntered().sendMessage(ChatColor.RED + "That player has spectating toggled off.");
			event.setCancelled(true);
		}
		if (event.getVehicle().getType() != EntityType.HORSE || event.getEntered().getType() != EntityType.PLAYER
				|| !((Player) event.getEntered()).hasPermission("sblock.blaze")) {
			return;
		}
		Horse horse = (Horse) event.getVehicle();
		ItemStack saddle = horse.getInventory().getSaddle();
		if (saddle != null && saddle.containsEnchantment(Enchantment.ARROW_FIRE)) {
			ParticleUtils.getInstance().addEntity(horse, new ParticleEffectWrapper(Effect.MOBSPAWNER_FLAMES, 1));
		}
	}
}
