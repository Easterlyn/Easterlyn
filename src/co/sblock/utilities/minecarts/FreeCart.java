package co.sblock.utilities.minecarts;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

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
	private HashMap<UUID, Long> cooldowns;

	public FreeCart() {
		carts = new HashSet<>();
		cooldowns = new HashMap<>();
	}

	public void spawnCart(Player p, Location location, Vector startspeed) {
		if (cooldowns.containsKey(p.getUniqueId())) {
			if (cooldowns.get(p.getUniqueId()) >= System.currentTimeMillis())  {
				return;
			}
		}
		cooldowns.put(p.getUniqueId(), System.currentTimeMillis() + 200);
		Minecart m = (Minecart) location.getWorld().spawnEntity(location, EntityType.MINECART);
		m.setPassenger(p);
		m.setVelocity(startspeed);
		carts.add(m);
	}

	public boolean isCart(Minecart cart) {
		return carts.contains(cart);
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
		cooldowns.clear();
	}

	public static FreeCart getInstance() {
		if (instance == null) {
			instance = new FreeCart();
		}
		return instance;
	}
}
