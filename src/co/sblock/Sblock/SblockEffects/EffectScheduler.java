package co.sblock.Sblock.SblockEffects;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import co.sblock.Sblock.Chat.ChatUser;
import co.sblock.Sblock.Chat.ChatUserManager;
import co.sblock.Sblock.Chat.Channel.ChannelManager;
import co.sblock.Sblock.Database.SblockData;
import co.sblock.Sblock.UserData.SblockUser;
import co.sblock.Sblock.UserData.UserManager;

public class EffectScheduler extends BukkitRunnable {

	@Override
	public void run() {
		for (Player p : Bukkit.getServer().getOnlinePlayers()) {
			SblockUser user = UserManager.getUserManager().addUser(p.getName());
			EffectManager.applyPassiveEffects(user);
			ChatUser u = ChatUserManager.getUserManager().getUser(p.getName());
			if (u == null) {
				u = SblockData.getDB().loadUserData(p.getName());
			}
			u.setComputerAccess();
			if (u.getComputerAccess() && !u.getListening().contains("#")) {
				u.addListening(ChannelManager.getChannelManager().getChannel("#"));
			}
			if (!u.getComputerAccess() && u.getListening().contains("#")) {
				u.removeListening("#");
			}
		}
	}
}
