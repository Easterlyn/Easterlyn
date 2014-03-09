package co.sblock.Sblock.SblockEffects;

import org.bukkit.Bukkit;

import org.bukkit.event.Listener;

import co.sblock.Sblock.Sblock;

public class EffectListener implements Listener	{
	
	private EffectManager eM;
	
	public EffectListener()	{
		Bukkit.getServer().getPluginManager().registerEvents(this, Sblock.getInstance());
		eM = new EffectManager();
	}

//	@EventHandler
//	public void onFly(PlayerToggleFlightEvent event) {
//		if (!event.isFlying()) {
//			return;
//		}
//		Player p = event.getPlayer();
//		if (p.getGameMode() != GameMode.CREATIVE && !SblockUser.getUser(p.getName()).isSleeping()
//				&& eM.passiveScan(p).containsKey("PSHOOOES")) {
//			int jumpMult = 1;
//			if (p.hasPotionEffect(PotionEffectType.JUMP)) {
//				for (PotionEffect pe : p.getActivePotionEffects()) {
//					if (pe.getType() == PotionEffectType.JUMP) {
//						jumpMult += pe.getAmplifier();
//					}
//				}
//			}
//			p.setVelocity(p.getVelocity().setY(0.5 * jumpMult));
//				event.setCancelled(true);
//				p.setFlying(false);
//			if (!p.hasPermission("group.horrorterror")) {
//				// Hax admin powaaaaa
//				p.setAllowFlight(false);
//			}
//		}
//	}
//	@EventHandler
//	public void onMove(PlayerMoveEvent event) {
//		if (event.getPlayer().getGameMode() != GameMode.CREATIVE
//				&& eM.scan(event.getPlayer()).contains("PSHOOOES")
//				&& event.getTo().getBlock().getRelative(BlockFace.DOWN).getType() != Material.AIR) {
//			event.getPlayer().setAllowFlight(true);
//		}
//	}
}
