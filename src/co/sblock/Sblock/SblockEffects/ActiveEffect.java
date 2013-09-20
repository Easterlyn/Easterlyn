package co.sblock.Sblock.SblockEffects;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public enum ActiveEffect {
	
	PSHOOOOT("PSHOOOOT"),	//teleport to crosshairs, may require cooldown
	BACKPACK("Backpack"), 	//mobile enderchest access
	HATGIVER("Hatgiver"),	//Pop-o-matic Vrillyhoo effect: random /hat from inventory item
	STRENGTH("STRONG");		//Extra damage applied by item
	
	
	private String loreText;
	private ActiveEffect(String s)	{
		loreText = s;
	}
	
	public String getLoreText()	{
		return this.loreText;
	}
	
	public static boolean isValidEffect(String s){
		for(ActiveEffect a : ActiveEffect.values())	{
			if(a.getLoreText().equalsIgnoreCase(s))	{
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
	
	public void getEffect(Player p)	{
		switch (this)	{
		case PSHOOOOT:
			Location target = p.getTargetBlock(null, 128).getLocation();
			p.teleport(target);
			break;
		case BACKPACK:
			Inventory ec = p.getEnderChest();
			p.openInventory(ec);
			break;
		case HATGIVER:
			
			break;
		case STRENGTH:
			break;
		default:
			break;		
		}
	}
/*Fireworks Sword
Warp
Enderpack
Slimeball Projectiles (Slowdown) (With magma cream, also causes damage)
Portable Music Player (Play random music disc from your inventory)
Lightning Smite
Weather Summoning
Disease Hit (Poison and possibly Hunger)
Explosions / Ghast Projectiles (consume gunpowder)
Cupid’s Arrows: 
Lava Immunity (Consumes Experience)
Ultimate Efficiency (items degrade 3x faster but dig 5x faster) 
Seismic Toss (Iron Golem-style)
Blaze Projectiles (Consumes Fire Charges)
Bezerker Rage (pops up that little angry villager particle and boosts hand to hand combat, the ultimate equius technique)
Frostbite (2-second immobility + causes damage, consumes snowballs)
The Horrorterror (Summons squids, only activatable by uttering Cthulhu r’yleh phtagn in grimdark speak) (fucking useless ability, but amazing)
Block out the sun (summons a torrent of flaming arrows in exchange for all of your levels, say... 3 arrows per level?) (arrows not able to be picked up of course)
Sandgun
Soporifics (Causes nausea but increases hand to hand)
The batterbitch (cake that applies random potion effects on nearby players)
Slob (eat entire cake, straight from the inventory)
Convert redstone to EXP
Slimemancer: summon slimes of various sizes; each slime consumes an appropriate quantity of slimeballs. Can spawn magma cubes with magma cream.
Monstrositifier: turn zombies into giants (consumes a lot of rotten flesh...16-32?)
Rainicorn: sheep can be randomly recolored without dye. Dye can be converted to EXP.
Convert an iron ingot to two gold nuggets. Costs EXP.
*/
}
