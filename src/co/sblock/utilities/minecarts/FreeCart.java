package co.sblock.utilities.minecarts;

import java.util.HashSet;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import co.sblock.utilities.general.Cooldowns;

/**
 * 
 * 
 * @author Jikoo
 */
public class FreeCart {

	private static FreeCart instance;

	private final HashSet<Minecart> carts;

	public FreeCart() {
		carts = new HashSet<>();
	}

	public void spawnCart(Player p, Location location, Vector startspeed) {
		Cooldowns cooldowns = Cooldowns.getInstance();
		if (cooldowns.getRemainder(p.getUniqueId(), "freecart") > 0) {
			return;
		}
		cooldowns.addCooldown(p.getUniqueId(), "freecart", 2000);
		Minecart m = (Minecart) location.getWorld().spawnEntity(location, EntityType.MINECART);
		m.setPassenger(p);
		m.setVelocity(startspeed);
		carts.add(m);
	}

	public boolean isFreeCart(Minecart cart) {
		return carts.contains(cart);
	}

	public boolean isOnFreeCart(Player p) {
		if (p.getVehicle() == null) {
			return false;
		}
		if (p.getVehicle().getType() != EntityType.MINECART) {
			return false;
		}
		return isFreeCart((Minecart) p.getVehicle());
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
