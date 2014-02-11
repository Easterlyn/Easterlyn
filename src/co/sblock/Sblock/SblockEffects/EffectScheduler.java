package co.sblock.Sblock.SblockEffects;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import co.sblock.Sblock.Chat.ChatUser;
import co.sblock.Sblock.Chat.ChatUserManager;
import co.sblock.Sblock.Chat.Channel.ChannelManager;
import co.sblock.Sblock.Database.SblockData;
import co.sblock.Sblock.UserData.SblockUser;

public class EffectScheduler extends BukkitRunnable {
	
	private EffectManager eM;

	public EffectScheduler()	{
		eM = new EffectManager();
	}
	
	@Override
	public void run() {
		for(Player p : Bukkit.getServer().getOnlinePlayers()) {
			SblockUser user = SblockUser.getUser(p.getName());
			EffectManager.applyPassiveEffects(user);
			ChatUserManager.getUserManager().getUser(p.getName()).setComputerAccess();
			for (ChatUser u : ChatUserManager.getUserManager().getUserlist()) {
				if (u == null) {
					u = SblockData.getDB().loadUserData(p.getName());
				}
				if (u.getComputerAccess() && !u.getListening().contains("#")) {
					u.addListening(ChannelManager.getChannelManager().getChannel("#"));
				}
				if (!u.getComputerAccess() && u.getListening().contains("#")) {
					u.removeListening("#");
				}
			}
		}
	}
}
