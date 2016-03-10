package co.sblock.micromodules;

import java.util.HashSet;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import co.sblock.Sblock;
import co.sblock.module.Module;

/**
 * A Module for tracking minecarts which despawn on collisions or when the rider exits.
 * 
 * @author Jikoo
 */
public class FreeCart extends Module {

	private final HashSet<Minecart> carts = new HashSet<>();

	private Cooldowns cooldowns;

	public FreeCart(Sblock plugin) {
		super(plugin);
	}

	@Override
	protected void onEnable() {
		 cooldowns = getPlugin().getModule(Cooldowns.class);
	}

	@Override
	protected void onDisable() {
		for (Minecart cart : this.carts) {
			cart.eject();
			cart.remove();
		}
	}

	public void spawnCart(Player p, Location location, Vector startspeed) {
		if (cooldowns.getRemainder(p, "freecart") > 0) {
			return;
		}
		cooldowns.addCooldown(p, "freecart", 2000);
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

	@Override
	public boolean isRequired() {
		return false;
	}

	@Override
	public String getName() {
		return "FreeCart";
	}
}
