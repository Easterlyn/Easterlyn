package co.sblock.effects;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;

import co.sblock.utilities.meteors.MeteorMod;

public enum ActiveEffect {

	PSHOOOOT("PSHOOOOT", ActiveEffectType.RIGHT_CLICK, 350), // Vector chucking
	BACKPACK("Backpack", ActiveEffectType.RIGHT_CLICK, 800), // mobile enderchest access
	HATGIVER("Hatgiver", ActiveEffectType.DAMAGE, 150), // Pop-o-matic Vrillyhoo effect: random /hat
														// from inventory item
	STRENGTH("STRONG", ActiveEffectType.DAMAGE, 500), // Extra damage applied by item
	BLINK("Blink", ActiveEffectType.RIGHT_CLICK, 800), // teleport to crosshairs, may require
														// cooldown
	CROTCHROCKET("RocketJump", ActiveEffectType.RIGHT_CLICK, 500); // Ride a rocket!

	private String loreText;
	private ActiveEffectType type;
	private int cost;

	private ActiveEffect(String s, ActiveEffectType AET, int cost) {
		loreText = s;
		type = AET;
		this.cost = cost;
	}

	public String getLoreText() {
		return this.loreText;
	}

	public ActiveEffectType getActiveEffectType() {
		return this.type;
	}

	public int getCost() {
		return this.cost;
	}

	/**
	 * Gets if a String is a valid ActiveEffect
	 * 
	 * @param s the String to test
	 * 
	 * @return true if the String is valid
	 */
	public static boolean isValidEffect(String s) {
		for (ActiveEffect a : ActiveEffect.values()) {
			if (a.getLoreText().equalsIgnoreCase(s)) {
				return true;
			}
		}
		try {
			ActiveEffect.valueOf(s);
		} catch (IllegalArgumentException e) {
			return false;
		}
		return true;
	}

	/**
	 * Gets the specified effect
	 * 
	 * @param s the String to return the ActiveEffect for
	 * 
	 * @return the ActiveEffect. Null if Effect does not exist
	 */
	public static ActiveEffect getEffect(String s) {
		ActiveEffect aE = null;
		for (ActiveEffect a : ActiveEffect.values()) {
			if (a.getLoreText().equalsIgnoreCase(s)) {
				return a;
			}
		}
		try {
			aE = ActiveEffect.valueOf(s);
		} catch (IllegalArgumentException e) {}
		return aE;
	}

	public static void applyRightClickEffect(Player p, ActiveEffect aE, Integer strength) {
		switch (aE) {
		case BLINK:
			@SuppressWarnings("deprecation")
			Location target = p.getTargetBlock(null, 128).getLocation();
			p.teleport(target);
			break;
		case BACKPACK:
			Inventory ec = p.getEnderChest();
			p.openInventory(ec);
			break;
		case PSHOOOOT:
			Vector v = p.getLocation().getDirection();
			p.setVelocity(v.multiply(strength + 2));
			break;
		case CROTCHROCKET:
			MeteorMod.getInstance().getCommandListener().crotchrocket(p, null);
			break;
		default:
			break;
		}
	}

	public static void applyDamageEffect(Player p, Player target, ActiveEffect aE, Integer strength) {
		switch (aE) {
		case HATGIVER:
			PlayerInventory inv = target.getInventory();
			ItemStack oldHat = inv.getHelmet();
			ArrayList<ItemStack> hatOptions = new ArrayList<ItemStack>();
			for (ItemStack iS : inv.getContents()) {
				if (iS != null) {
					hatOptions.add(iS);
				}
			}
			ItemStack newHat = hatOptions.get((int) Math.random() * hatOptions.size());
			inv.setHelmet(newHat);
			inv.addItem(oldHat);
			target.sendMessage("RIDICULOUS HAT");
			break;
		default:
			break;
		}
	}
}
