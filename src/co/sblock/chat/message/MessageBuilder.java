package co.sblock.chat.message;

import java.text.Normalizer;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import co.sblock.chat.ChannelManager;
import co.sblock.chat.ChatMsgs;
import co.sblock.chat.channel.Channel;
import co.sblock.chat.channel.ChannelType;
import co.sblock.users.OfflineUser;
import co.sblock.utilities.regex.RegexUtils;

/**
 * 
 * 
 * @author Jikoo
 */
public class MessageBuilder {

	private OfflineUser sender = null;
	private String senderName = null;
	private Channel channel = null;
	private String message = null;
	private boolean thirdPerson = false;
	private String atChannel = null;

	public MessageBuilder setSender(OfflineUser sender) {
		this.sender = sender;
		if (this.channel == null) {
			this.channel = sender.getCurrentChannel();
		}
		return this;
	}

	public MessageBuilder setSender(String name) {
		this.senderName = name;
		return this;
	}

	public MessageBuilder setChannel(Channel channel) {
		this.channel = channel;
		return this;
	}

	public MessageBuilder setMessage(String message) {
		// Set @<channel> destination
		this.atChannel = null;
		int space = message.indexOf(' ');
		if (space > 1 && message.charAt(0) == '@') {
			this.atChannel = message.substring(1, space);
			message = message.substring(space);
			this.channel = ChannelManager.getChannelManager().getChannel(this.atChannel);
		}

		// Anyone can use color codes in nick channels. Channel mods can use color codes in non-rp channels
		if (channel.getType() != ChannelType.RP 
				&& (channel.getType() == ChannelType.NICK || sender != null && channel.isModerator(sender))) {
			message = ChatColor.translateAlternateColorCodes('&', message);
		}

		Player player = sender != null ? sender.getPlayer() : null;

		if (channel.getOwner() == null && (player == null || !player.hasPermission("sblock.felt"))) {
			// TODO perhaps allow non-ASCII in non-global channels
			StringBuilder sb = new StringBuilder();
			for (char character : Normalizer.normalize(message, Normalizer.Form.NFD).toCharArray()) {
				if (character > '\u001F' && character < '\u007E' || character == ChatColor.COLOR_CHAR) {
					sb.append(character);
				}
			}
			// Fuck you.
			message = sb.toString().replaceAll("tilde?s?", "");
		}

		// Trim whitespace created by formatting codes, etc.
		message = RegexUtils.trimExtraWhitespace(message);

		// Greentext must be at least 4 letters long and the second character must be a letter.
		// E.G. >mfw people do it wrong
		// instead of > lol le edgy meme
		if (message.length() > 3 && message.charAt(0) == '>' && Character.isLetter(message.charAt(1))) {
			if (player == null || player.hasPermission("sblockchat.greentext"))
			message = ChatColor.GREEN + message;
		}

		this.message = message;
		return this;
	}

	public MessageBuilder setThirdPerson(boolean thirdPerson) {
		this.thirdPerson = thirdPerson;
		return this;
	}

	public boolean canBuild(boolean informSender) {
		informSender = this.sender != null ? informSender : false;

		// Channel must exist
		if (this.channel == null) {
			if (informSender && this.atChannel != null) {
				this.sender.sendMessage(ChatMsgs.errorInvalidChannel(atChannel));
			} else if (informSender) {
				this.sender.sendMessage(ChatMsgs.errorNoCurrent());
			}
			return false;
		}

		// No sending of blank messages.
		if (RegexUtils.appearsEmpty(this.message)) {
			if (informSender) {
				this.sender.sendMessage(ChatMsgs.errorEmptyMessage());
			}
			return false;
		}

		// No sender and no name, invalid message.
		if (this.sender == null) {
			return this.senderName != null;
		}

		// Shockingly, muted users are not allowed to talk.
		if (this.sender.getMute()) {
			if (informSender) {
				this.sender.sendMessage(ChatMsgs.isMute());
			}
			return false;
		}

		// No sending messages to global chats while ignoring them.
		if (this.channel.getType() == ChannelType.REGION && this.sender.getSuppression()) {
			if (informSender) {
				this.sender.sendMessage(ChatMsgs.errorSuppressingGlobal());
			}
			return false;
		}

		// Nicks required in RP channels.
		if (this.channel.getType() == ChannelType.RP && !this.channel.hasNick(sender)) {
			if (informSender) {
				this.sender.sendMessage(ChatMsgs.errorNickRequired(channel.getName()));
			}
			return false;
		}

		return true;
	}

	public boolean isSenderInChannel(boolean informSender) {
		// Must be in target channel to send messages.
		if (sender == null || this.channel.getListening().contains(this.sender.getUUID())) {
			return true;
		}
		if (informSender) {
			this.sender.sendMessage(ChatMsgs.errorNotListening(channel.getName()));
		}
		return false;
	}

	public Message toMessage() {
		if (!canBuild(false)) {
			throw new RuntimeException("Someone did something stupid with chat!");
		}
		return new Message(this.sender, this.senderName, this.channel, this.message, this.thirdPerson);
	}
}
