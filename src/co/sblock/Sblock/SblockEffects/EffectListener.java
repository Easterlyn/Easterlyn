package co.sblock.Sblock.SblockEffects;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

import co.sblock.Sblock.Sblock;

public class EffectListener implements Listener	{
	
	private EffectManager eM;
	
	public EffectListener()	{
		Bukkit.getServer().getPluginManager().registerEvents(this, Sblock.getInstance());
		eM = new EffectManager();
	}

	@EventHandler
	public void onPlayerLogin(PlayerLoginEvent event)	{
		Player p = event.getPlayer();
		  eM.applyPassiveEffects(eM.scan(p), p);
	}	
	@EventHandler
	public void onPlayerPickupItem(PlayerPickupItemEvent event)	{
		Player p = event.getPlayer();
		eM.applyPassiveEffects(eM.scan(p), p);
	}
	@EventHandler
	public void onPlayerRightClick(PlayerInteractEvent event)	{
		Player p = event.getPlayer();
		eM.applyActiveRightClickEffects(eM.activeScan(p), p);
	}
	@EventHandler
	public void onPlayerDamagePlayer(EntityDamageByEntityEvent event)	{
		if(event.getEntity() instanceof Player && event.getDamager() instanceof Player)	{
			Player p = (Player) event.getDamager();
			Player target = (Player) event.getEntity();
			eM.applyActiveDamageEffects(eM.activeScan(p), p, target);

		}
	}
}
