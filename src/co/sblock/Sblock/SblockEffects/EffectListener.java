package co.sblock.Sblock.SblockEffects;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import co.sblock.Sblock.Sblock;
import co.sblock.Sblock.UserData.SblockUser;

public class EffectListener implements Listener	{
	
	private EffectManager eM;
	
	public EffectListener()	{
		Bukkit.getServer().getPluginManager().registerEvents(this, Sblock.getInstance());
		eM = new EffectManager();
	}

	@EventHandler
	public void onPlayerLogin(PlayerLoginEvent event)	{
		Player p = event.getPlayer();
		SblockUser user = SblockUser.getUser(p.getName());
		user.setAllPassiveEffects(EffectManager.passiveScan(p));
		EffectManager.applyPassiveEffects(user);
	}	
	
	@EventHandler
	public void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
		Player p = event.getPlayer();
		SblockUser user = SblockUser.getUser(p.getName());
		user.setAllPassiveEffects(EffectManager.passiveScan(p));
		EffectManager.applyPassiveEffects(user);
	}
	
	@EventHandler
	public void onPlayerPickupItem(PlayerPickupItemEvent event)	{
		Player p = event.getPlayer();
		SblockUser user = SblockUser.getUser(p.getName());
		HashMap<PassiveEffect, Integer> effects = EffectManager.itemScan(event.getItem());
		for (PassiveEffect e : effects.keySet()) {
			user.addPassiveEffect(e);
		}
		EffectManager.applyPassiveEffects(user);
	}
	
	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		Player p = event.getPlayer();
		SblockUser user = SblockUser.getUser(p.getName());
		HashMap<PassiveEffect, Integer> effects = EffectManager.itemScan(event.getItemDrop());
		for (PassiveEffect e : effects.keySet()) {
			user.removePassiveEffect(e);
		}
		EffectManager.applyPassiveEffects(user);
	}
	
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) {
		Player p = (Player) event.getPlayer();
		SblockUser user = SblockUser.getUser(p.getName());
		user.setAllPassiveEffects(EffectManager.passiveScan(p));
		EffectManager.applyPassiveEffects(user);
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
	@EventHandler
	public void onFly(PlayerToggleFlightEvent event) {
		if (!event.isFlying()) {
			return;
		}
		Player p = event.getPlayer();
		if (p.getGameMode() != GameMode.CREATIVE && !SblockUser.getUser(p.getName()).isSleeping()
				&& eM.scan(p).contains("PSHOOOES")) {
			int jumpMult = 1;
			if (p.hasPotionEffect(PotionEffectType.JUMP)) {
				for (PotionEffect pe : p.getActivePotionEffects()) {
					if (pe.getType() == PotionEffectType.JUMP) {
						jumpMult += pe.getAmplifier();
					}
				}
			}
			p.setVelocity(p.getVelocity().setY(0.5 * jumpMult));
				event.setCancelled(true);
				p.setFlying(false);
			if (!p.hasPermission("group.horrorterror")) {
				// Hax admin powaaaaa
				p.setAllowFlight(false);
			}
		}
	}
	@EventHandler
	public void onMove(PlayerMoveEvent event) {
		if (event.getPlayer().getGameMode() != GameMode.CREATIVE
				&& eM.scan(event.getPlayer()).contains("PSHOOOES")
				&& event.getTo().getBlock().getRelative(BlockFace.DOWN).getType() != Material.AIR) {
			event.getPlayer().setAllowFlight(true);
		}
	}
}
