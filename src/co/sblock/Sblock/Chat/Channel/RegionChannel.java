package co.sblock.Sblock.Chat.Channel;

import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import co.sblock.Sblock.Chat.ChatMsgs;
import co.sblock.Sblock.UserData.SblockUser;

public class RegionChannel extends NormalChannel {

	/**
	 * @param name
	 * @param a
	 * @param creator
	 */
	public RegionChannel(String name, AccessLevel a, String creator) {
		super(name, a, creator);
	}

	@Override
	public void kickUser(SblockUser user, SblockUser sender) {
		sender.sendMessage(ChatMsgs.errorRegionChannel());
	}
	
	@Override
	public void banUser(String username, SblockUser sender) {
		sender.sendMessage(ChatMsgs.errorRegionChannel());
	}

	@Override
	public void unbanUser(String username, SblockUser sender) {
		sender.sendMessage(ChatMsgs.errorRegionChannel());
	}
}
