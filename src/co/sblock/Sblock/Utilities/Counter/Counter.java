package co.sblock.Sblock.Utilities.Counter;

import org.bukkit.entity.Player;

public class Counter {

	private Player p;
	private int duration;
	private int playerXP;
	private int current;
	private int cooldown;

	public Counter(Player player, int length)	{
		p = player;
		duration = length;
		playerXP = p.getTotalExperience();
		current = length;
		cooldown = 2;
		startCounter();
	}
	
	public Player getPlayer()	{
		return p;
	}
	public int getDuration()	{
		return duration;
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
		p.setTotalExperience(0);
		p.setLevel(duration);
	}
	public void tick()	{
		current -= 1;
		p.setLevel(current);
	}
	public void tickCooldown()	{
		cooldown -= 1;
	}
	public void stopCounter()	{
		p.setTotalExperience(playerXP);
	}

}
