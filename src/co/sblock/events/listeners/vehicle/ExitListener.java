package co.sblock.events.listeners.vehicle;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.vehicle.VehicleExitEvent;

import co.sblock.Sblock;
import co.sblock.events.listeners.SblockListener;
import co.sblock.micromodules.FreeCart;
import co.sblock.micromodules.ParticleUtils;

/**
 * Listener for VehicleExitEvents.
 * 
 * @author Jikoo
 */
public class ExitListener extends SblockListener {

	private final ParticleUtils particles;
	private final FreeCart carts;

	public ExitListener(Sblock plugin) {
		super(plugin);
		carts = plugin.getModule(FreeCart.class);
		this.particles = plugin.getModule(ParticleUtils.class);
	}

	/**
	 * EventHandler for VehicleExitEvents.
	 * 
	 * @param event the VehicleExitEvent
	 */
	@EventHandler(ignoreCancelled = true)
	public void onVehicleExit(VehicleExitEvent event) {
		Entity entity = event.getVehicle();
		if (entity.getType() == EntityType.HORSE) {
			particles.removeAllEffects(entity);
			return;
		}
		if (entity.getType() == EntityType.MINECART) {
			carts.remove((Minecart) event.getVehicle());
			return;
		}
	}
}
