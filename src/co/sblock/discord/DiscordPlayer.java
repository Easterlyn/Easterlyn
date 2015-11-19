package co.sblock.discord;

import org.bukkit.entity.Player;

import co.sblock.utilities.PermissiblePlayer;

import me.itsghost.jdiscord.talkable.GroupUser;

/**
 * Wrapper for a Player for replying to Discord.
 * 
 * @author Jikoo
 */
public class DiscordPlayer extends PermissiblePlayer {

	private final GroupUser user;

	public DiscordPlayer(GroupUser user, Player player) {
		super(player);
		this.user = user;
	}

	@Override
	public String getDisplayName() {
		return Discord.getInstance().getGroupColor(user) + getName();
	}

	@Override
	public void sendMessage(String arg0) {
		// FIXME REPLY TO DISCORD - command response, etc.
	}

	@Override
	public void sendMessage(String[] arg0) {
		// FIXME see above
	}

	@Override
	public boolean isOnline() {
		return true;
	}

}
