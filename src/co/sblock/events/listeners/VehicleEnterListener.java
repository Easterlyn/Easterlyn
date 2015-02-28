package co.sblock.events.listeners;

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

import co.sblock.events.packets.ParticleEffectWrapper;
import co.sblock.events.packets.ParticleUtils;

public class VehicleEnterListener implements Listener {

	public VehicleEnterListener() {
		new Permission("sblock.blaze").addParent("sblock.donator", true).recalculatePermissibles();
	}

	@EventHandler(ignoreCancelled = true)
	public void onVehicleEnter(VehicleEnterEvent event) {
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
