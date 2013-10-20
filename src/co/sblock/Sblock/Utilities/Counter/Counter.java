package co.sblock.Sblock.Utilities.Counter;

import org.bukkit.entity.Player;

import co.sblock.Sblock.Utilities.Sblogger;

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
		cooldown = 2;
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
		Sblogger.info("Counter", "Counter started for " + p.getName() + " with duration " + current);
		Sblogger.info("Counter", "Player had " + p.getLevel() + " levels");
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
		Sblogger.info("Counter", "" + playerXP);
		p.setLevel(playerXP);
		p.setExp(xp);
	}

}
