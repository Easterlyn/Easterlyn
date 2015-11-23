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

	private final Discord discord;
	private final GroupUser user;
	private StringBuilder messages;

	public DiscordPlayer(Discord discord, GroupUser user, Player player) {
		super(player);
		this.discord = discord;
		this.user = user;
	}

	@Override
	public String getDisplayName() {
		return discord.getGroupColor(user) + getName();
	}

	public synchronized boolean hasPendingCommand() {
		return messages != null;
	}

	public synchronized void startMessages() {
		if (messages == null) {
			messages = new StringBuilder();
		} else {
			messages.delete(0, messages.length());
		}
	}

	public synchronized String stopMessages() {
		if (messages == null) {
			return null;
		}
		String message = messages.toString();
		messages = null;
		return message;
	}

	@Override
	public void sendMessage(String arg0) {
		if (messages == null) {
			return;
		}
		if (messages.length() > 0) {
			messages.append('\n');
		}
		messages.append(arg0);
	}

	@Override
	public void sendMessage(String[] arg0) {
		if (messages == null) {
			return;
		}
		for (String s : arg0) {
			sendMessage(s);
		}
	}

	@Override
	public boolean isOnline() {
		return true;
	}

}
