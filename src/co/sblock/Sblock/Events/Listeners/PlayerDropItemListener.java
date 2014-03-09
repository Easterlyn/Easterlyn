package co.sblock.Sblock.Events.Listeners;

import java.util.HashMap;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;

import co.sblock.Sblock.SblockEffects.EffectManager;
import co.sblock.Sblock.SblockEffects.PassiveEffect;
import co.sblock.Sblock.UserData.SblockUser;

public class PlayerDropItemListener implements Listener {

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
}
