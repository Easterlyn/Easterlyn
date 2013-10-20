package co.sblock.Sblock.SblockEffects;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public enum PassiveEffect {
	
	COMPUTER("Computer"),
	PSHOOOES("PSHOOOES"),
	JUMP("Boing"),
	SPEED("Speed"),
	FLOWERS("Flowers");
	
	private String loreText;
	private PassiveEffect(String s)	{
		loreText = s;
	}
	
	public String getLoreText()	{
		return this.loreText;
	}
	public static boolean isValidEffect(String s){
		for(PassiveEffect p : PassiveEffect.values())	{
			if(p.getLoreText().equalsIgnoreCase(s))	{
				return true;
			}
		}
		try {
			PassiveEffect.valueOf(s);
		} catch (IllegalArgumentException e) {
			return false;
		}
		return true;
	}
	
	public void getEffect(Player p)	{
		PotionEffect potEffect;
		switch (this)	{
		
		case JUMP:
			potEffect = new PotionEffect(PotionEffectType.JUMP, 1180, 5);
			p.addPotionEffect(potEffect, true);
			break;
		case SPEED:
			potEffect = new PotionEffect(PotionEffectType.SPEED, 1180, 3);
			p.addPotionEffect(potEffect, true);
			break;
		default:
			break;
			
		}
	}
/*
Jump boost
Sanic Lightspeed
Armor Particles (Particle Armor?)
Snow Trail (Snow Golem-style) (Freezes water underneath you)
Item Vacuum (Long-distance Pickup)
SGA (enchantment text) Particle Aura
Mob Disguise (also able to emit relevant sfx)
Dirt Accumulator (occasionally adds blocks of dirt to inventory)
Love Aura (Emit heart particles; animals inclined to breed when you are near)
Footprints
Steady Hand (Decreases mining speed, silk touch for all blocks)
Batman (attract bats to you)
Stay Away from the AIDS Bakesale (automatically eats nearby cake when low on hunger)
Something with beacon beams.
Fill buckets with milk without needing a cow (Aurthor is your spirit animal)
Pogo (jumps automatically on contact with the ground)
Clonevision (other players appear to be wearing your head)
Sodavison (other players appear to be wearing Sodapop’s head)
LEADERSHIP SKILLS: All chat messages are allcaps
Grimdark: All chat messages are scrambled and black. give off nether portal particles.
Wither
Saladturtle’s Blessing: Guaranteed chicken spawn from thrown eggs
Johnny Appleseed: 25% chance to get apples from oak leaves. apples restore a little more hunger.
Fred flintstone: always get flint from gravel. Flint and Steel is not damaged when used.
Ninja: Nametag can't be seen through walls, even when not crouching.
HeartStone: either short crappy regen every effect tick or just add 1/2 heart (1 hp)
 */
}
