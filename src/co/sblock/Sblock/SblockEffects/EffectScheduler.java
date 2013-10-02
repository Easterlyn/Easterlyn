package co.sblock.Sblock.SblockEffects;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import co.sblock.Sblock.Chat.Channel.ChannelManager;
import co.sblock.Sblock.UserData.SblockUser;
import co.sblock.Sblock.UserData.UserManager;

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
			SblockUser.getUser(p.getName()).setComputerAccess();
			for(SblockUser u : UserManager.getUserManager().getUserlist())	{
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
