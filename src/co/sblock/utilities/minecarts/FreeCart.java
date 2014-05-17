package co.sblock.utilities.minecarts;

import java.util.HashSet;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 * 
 * 
 * @author Jikoo
 */
public class FreeCart {

	private static FreeCart instance;

	private HashSet<Minecart> carts;

	public FreeCart() {
		carts = new HashSet<>();
	}

	public void spawnCart(Player p, Location location, Vector startspeed) {
		Minecart m = (Minecart) location.getWorld().spawnEntity(location, EntityType.MINECART);
		m.setPassenger(p);
		m.setVelocity(m.getLocation().getDirection().multiply(startspeed));
		carts.add(m);
	}

	public void remove(Player p) {
		if (p.getVehicle() == null) {
			return;
		}
		if (p.getVehicle().getType() != EntityType.MINECART) {
			return;
		}
		remove((Minecart) p.getVehicle());
	}

	public void remove(Minecart minecart) {
		if (!carts.remove(minecart)) {
			return;
		}
		minecart.eject();
		minecart.remove();
	}

	public void cleanUp() {
		for (Minecart cart : this.carts) {
			cart.eject();
			cart.remove();
		}
	}

	public static FreeCart getInstance() {
		if (instance == null) {
			instance = new FreeCart();
		}
		return instance;
	}
}
