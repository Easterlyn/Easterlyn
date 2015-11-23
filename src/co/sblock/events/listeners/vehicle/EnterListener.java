package co.sblock.events.listeners.vehicle;

import org.bukkit.Effect;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;

import co.sblock.Sblock;
import co.sblock.events.listeners.SblockListener;
import co.sblock.events.packets.ParticleEffectWrapper;
import co.sblock.micromodules.ParticleUtils;

/**
 * Listener for VehicleEnterEvents.
 * 
 * @author Jikoo
 */
public class EnterListener extends SblockListener {

	private final ParticleUtils particles;

	public EnterListener(Sblock plugin) {
		super(plugin);
		new Permission("sblock.blaze").addParent("sblock.donator", true).recalculatePermissibles();
		this.particles = plugin.getModule(ParticleUtils.class);
	}

	/**
	 * EventHandler for VehicleEnterEvents.
	 * 
	 * @param event the VehicleEnterEvent
	 */
	@EventHandler(ignoreCancelled = true)
	public void onVehicleEnter(VehicleEnterEvent event) {
		if (event.getVehicle().getType() != EntityType.HORSE || event.getEntered().getType() != EntityType.PLAYER
				|| !((Player) event.getEntered()).hasPermission("sblock.blaze")) {
			return;
		}
		Horse horse = (Horse) event.getVehicle();
		ItemStack saddle = horse.getInventory().getSaddle();
		if (saddle != null && saddle.containsEnchantment(Enchantment.ARROW_FIRE)) {
			particles.addEntity(horse, new ParticleEffectWrapper(Effect.MOBSPAWNER_FLAMES, 1));
		}
	}
}
