package co.sblock.Sblock.SblockEffects;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import co.sblock.Sblock.Chat2.ChatUser;
import co.sblock.Sblock.Chat2.ChatUserManager;
import co.sblock.Sblock.Chat2.Channel.ChannelManager;

public class EffectScheduler extends BukkitRunnable {
	
	private EffectManager eM;

	public EffectScheduler()	{
		eM = new EffectManager();
	}
	
	@Override
	public void run() {
	//	plugin.getLogger().info("Effect Tick");
		for(Player p : Bukkit.getServer().getOnlinePlayers())	{
			eM.applyPassiveEffects(eM.scan(p), p);
			ChatUserManager.getUserManager().getUser(p.getName()).setComputerAccess();
			for(ChatUser u : ChatUserManager.getUserManager().getUserlist())	{
				if(u.getComputerAccess() && !u.getListening().contains("#"))	{
					u.addListening(ChannelManager.getChannelManager().getChannel("#"));
				}
				if(!u.getComputerAccess() && u.getListening().contains("#"))	{
					u.removeListening("#");
				}
			}
		}		
	}
}
