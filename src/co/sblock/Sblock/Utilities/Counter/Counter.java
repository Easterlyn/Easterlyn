package co.sblock.Sblock.Utilities.Counter;

import org.bukkit.entity.Player;

public class Counter {

	private Player p;
	private int playerXP;
	private float xp;
	private int duration;
	private int current;
	private int cooldown;

	public Counter(Player player, int length)	{
		p = player;
		duration = length;
		current = length;
		playerXP = p.getLevel();
		xp = p.getExp();
		cooldown = 1;
		startCounter();
	}	
	public Player getPlayer()	{
		return p;
	}
	public int getPlayerXP()	{
		return playerXP;
	}
	public int getCurrent()	{
		return current;
	}
	public int getCooldown()	{
		return cooldown;
	}

	public void startCounter()	{
		p.sendMessage(current + " " + cooldown);
		p.setLevel(0);
		p.setExp(1);
		p.setLevel(current);
	}
	public void tick()	{
		current -= 1;
		p.setLevel(current);
		p.setExp((float) current / duration);
	}
	public void tickCooldown()	{
		cooldown -= 1;
	}
	public void stopCounter()	{
		p.setLevel(playerXP);
		p.setExp(xp);
	}

}
