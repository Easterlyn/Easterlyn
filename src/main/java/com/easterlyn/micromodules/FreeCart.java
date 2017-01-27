package com.easterlyn.micromodules;

import java.util.HashSet;

import com.easterlyn.Easterlyn;
import com.easterlyn.module.Module;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 * A Module for tracking minecarts which despawn on collisions or when the rider exits.
 * 
 * @author Jikoo
 */
public class FreeCart extends Module {

	private final HashSet<Minecart> carts = new HashSet<>();

	private Cooldowns cooldowns;

	public FreeCart(Easterlyn plugin) {
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
		//nocheatplus.checks.moving.vehicle.envelope
		if (cooldowns.getRemainder(p, "freecart") > 0) {
			return;
		}
		cooldowns.addCooldown(p, "freecart", 2000);
		Minecart minecart = (Minecart) location.getWorld().spawnEntity(location, EntityType.MINECART);
		minecart.setPassenger(p);
		minecart.setVelocity(startspeed);
		minecart.setMaxSpeed(10);
		carts.add(minecart);
	}

	public boolean isFreeCart(Minecart cart) {
		return carts.contains(cart);
	}

	public boolean isOnFreeCart(Player player) {
		if (player.getVehicle() == null) {
			return false;
		}
		if (player.getVehicle().getType() != EntityType.MINECART) {
			return false;
		}
		return isFreeCart((Minecart) player.getVehicle());
	}

	public void remove(Player player) {
		if (player.getVehicle() == null) {
			return;
		}
		if (player.getVehicle().getType() != EntityType.MINECART) {
			return;
		}
		remove((Minecart) player.getVehicle());
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
