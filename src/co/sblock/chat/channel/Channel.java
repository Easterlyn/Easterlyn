package co.sblock.chat.channel;

import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import co.sblock.chat.ChatMsgs;
import co.sblock.chat.ColorDef;
import co.sblock.chat.SblockChat;
import co.sblock.data.SblockData;
import co.sblock.users.Region;
import co.sblock.users.User;
import co.sblock.users.UserManager;
import co.sblock.utilities.Log;
import co.sblock.utilities.rawmessages.MessageClick;
import co.sblock.utilities.rawmessages.MessageElement;
import co.sblock.utilities.rawmessages.MessageHover;
import co.sblock.utilities.threadsafe.SetGenerator;

/**
 * Defines default channel behavior
 *
 * @author Dublek, Jikoo, tmathmeyer
 */
public abstract class Channel {

	/**
	 * 
	 * @author ted
	 *
	 * A wrapper for the Channels because frankly, they are managed in a rediculous, absurd way
	 * this is just a temporary solution for serializing them until I get around to refactoring
	 * that too.
	 *
	 */
	public static class ChannelSerialiser {

		private final ChannelType ct;
		private final String name;
		private final AccessLevel access;

		private final Set<UUID> approvedPlayers;
		private final Set<UUID> moderators;
		private final Set<UUID> mutedPlayers;
		private final Set<UUID> bannedPlayers;
		private final Set<UUID> listeningPlayers;

		private final UUID owner;

		/**
		 * oh boy, there are alot of params...
		 * 
		 * @param ct
		 * @param name
		 * @param access
		 * @param owner
		 * @param approvedPlayers
		 * @param moderators
		 * @param mutedPlayers
		 * @param bannedPlayers
		 * @param listeningPlayers
		 */
		public ChannelSerialiser(ChannelType ct, String name, AccessLevel access, UUID owner,
				Set<UUID> approvedPlayers,
				Set<UUID> moderators,
				Set<UUID> mutedPlayers,
				Set<UUID> bannedPlayers,
				Set<UUID> listeningPlayers) {
			this.approvedPlayers = approvedPlayers;
			this.moderators = moderators;
			this.mutedPlayers = mutedPlayers;
			this.bannedPlayers = bannedPlayers;
			this.listeningPlayers = listeningPlayers;
			this.ct = ct;
			this.name = name;
			this.access = access;
			this.owner = owner;
		}

		public ChannelSerialiser(ChannelType ct, String name, AccessLevel access, UUID owner) {
			this.approvedPlayers = SetGenerator.generate();
			this.moderators = SetGenerator.generate();
			this.mutedPlayers = SetGenerator.generate();
			this.bannedPlayers = SetGenerator.generate();
			this.listeningPlayers = SetGenerator.generate();
			this.ct = ct;
			this.name = name;
			this.access = access;
			this.owner = owner;
		}

		public Channel build() {
			Channel chan;
			switch(ct) {
				case NICK:
					chan = new NickChannel(name, access, owner);
					break;
				case NORMAL:
					chan = new NormalChannel(name, access, owner);
					break;
				case REGION:
					chan = new RegionChannel(name, access, owner);
					break;
				case RP:
					chan = new RPChannel(name, access, owner);
					break;
				default:
					throw new RuntimeException("if this gets called you have some SERIOUS issues");
			}
			chan.approvedList = approvedPlayers;
			chan.modList = moderators;
			chan.muteList = mutedPlayers;
			chan.banList = bannedPlayers;
			chan.listening = listeningPlayers;
			return chan;
		}

	}

	/*
	 * Immutable Data regarding the channel
	 */
	protected String name;
	protected AccessLevel access;
	protected Set<UUID> approvedList;
	protected Set<UUID> modList;
	protected Set<UUID> muteList;
	protected Set<UUID> banList;
	protected Set<UUID> listening;

	/*
	 * Ownership is supposed to be transferrable, Keiko just gave up too early D:
	 */
	protected UUID owner;

	/**
	 * @param name the name of the channel
	 * @param a the access level of the channel
	 * @param creator the owner of the channel
	 */
	public Channel(String name, AccessLevel a, UUID creator) {
		this.name = name;
		this.access = a;
		this.owner = creator;
		approvedList = SetGenerator.generate();
		modList = SetGenerator.generate();
		muteList = SetGenerator.generate();
		banList = SetGenerator.generate();
		listening = SetGenerator.generate();
		if (creator != null) {
			modList.add(creator);
			SblockData.getDB().saveChannelData(this);
		}
	}



	/* GETTERS */
	/**
	 * @return the channel's name
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * @return this channel's access level
	 */
	public AccessLevel getAccess() {
		return this.access;
	}

	/**
	 * @return all UUID's of users listening to this channel
	 */
	public Set<UUID> getListening() {
		return this.listening;
	}

	/**
	 * @return all UUID's of the mods of this channel
	 */
	public Set<UUID> getModList() {
		return this.modList;
	}

	/**
	 * @return all UUID's of the players banned from this channel
	 */
	public Set<UUID> getBanList() {
		return banList;
	}

	/**
	 * @return the UUID of the channel owner
	 */
	public UUID getOwner() {
		return this.owner;
	}




	/* TESTERS */
	/**
	 * @param user a user
	 * @return if this user is an owner (created channel / set by previous owner, or is a 'denizen')
	 */
	public boolean isOwner(User user) {
		return user.getUUID().equals(owner) || user.getPlayer().hasPermission("group.denizen");
	}

	public abstract ChannelSerialiser toSerialiser();

	/**
	 * @param user a user
	 * @return whether this user has permission to moderate the channel
	 */
	public boolean isModerator(User user) {
		return isOwner(user) || user.getPlayer().hasPermission("group.felt") || modList.contains(user.getUUID());
	}

	/**
	 * the user must be in the banlist AND not an op (aka 'denizen')
	 *
	 * @param user a user
	 * @return whether this user is banned
	 */
	public boolean isBanned(User user) {
		return banList.contains(user.getUUID()) && !user.getPlayer().hasPermission("group.denizen");
	}





	/* ADDERS / REMOVERS */
	/**
	 * TODO: enforce this
	 * ONLY CALL FROM USER
	 *
	 * @param userID the user UUID to add listening
	 */
	public void addListening(UUID userID) {
		this.listening.add(userID);
	}

	/**
	 * @param userID the user to add to the approval list
	 */
	public void addApproved(UUID userID) {
		this.approvedList.add(userID);
	}

	/**
	 * Method used by database to load a ban silently.
	 *
	 * @param user the UUID to add as a ban
	 */
	public void addBan(UUID userID) {
		this.banList.add(userID);
	}

	/**
	 * Change the owner of a channel. This should only be useable by admins/the current owner.
	 *
	 * @param newOwner the new owner
	 */
	public void setOwner(UUID newOwner) {
		this.owner = newOwner;
	}

	/**
	 * Method used by database to load a moderator silently.
	 *
	 * @param user the name to add as a moderator
	 */
	public void addModerator(UUID id) {
		modList.add(id);
	}

	/**
	 * @param sender the person attempting to apply moderator status to another
	 * @param userID the ID of the person who may become a mod
	 */
	public void addMod(User sender, UUID userID) {
		User user = UserManager.getUser(userID);
		if (user == null) {
			sender.sendMessage(ChatMsgs.errorInvalidUser(userID.toString()));
			return;
		}
		if (!isModerator(sender)) {
			sender.sendMessage(ChatMsgs.onChannelCommandFail(this.name));
			return;
		}
		String message = ChatMsgs.onChannelModAdd(user.getPlayerName(), this.name);
		if (!this.isModerator(UserManager.getUser(userID))) {
			this.modList.add(userID);
			this.sendMessage(message);
			if (!this.listening.contains(userID)) {
				user.sendMessage(message);
			}
		} else {
			sender.sendMessage(message);
		}
	}

	/**
	 * @param sender the person attempting to remove moderator status from another
	 * @param userID the ID of the person who may lose mod status
	 */
	public void removeMod(User sender, UUID userID) {
		User user = UserManager.getUser(userID);
		if (user == null) {
			sender.sendMessage(ChatMsgs.errorInvalidUser(userID.toString()));
			return;
		}
		if (!this.isModerator(sender)) {
			sender.sendMessage(ChatMsgs.onChannelCommandFail(this.name));
			return;
		}
		String message = ChatMsgs.onChannelModRm(user.getPlayerName(), this.name);
		if (this.modList.contains(userID) && !this.isOwner(user)) {
			this.modList.remove(userID);
			this.sendMessage(message);
			if (!this.listening.contains(userID)) {
				user.sendMessage(message);
			}
		} else {
			sender.sendMessage(message);
		}
	}

	/**
	 * ONLY CALL FROM CHATUSER
	 *
	 * @param user the UUID to remove from listening.
	 */
	public void removeListening(UUID userID) {
		this.listening.remove(userID);
	}

	/**
	 *
	 * @param sender the sender
	 * @param nick the nick
	 */
	public abstract void setNick(User sender, String nick);

	/**
	 *
	 * @param sender the sender
	 * @param warn whether to warn the user
	 */
	public abstract void removeNick(User sender, boolean warn);

	/**
	 *
	 * @param sender the sender
	 * @return the nick of the sender
	 */
	public abstract String getNick(User sender);

	/**
	 *
	 * @param sender the sender
	 * @return whether the sender has had a nick set
	 */
	public abstract boolean hasNick(User sender);

	/**
	 *
	 * @param nick the nickname to reverse lookup
	 * @return the owner of the provided nickname
	 */
	public abstract User getNickOwner(String nick);

	/**
	 * @return the type of this channel
	 */
	public abstract ChannelType getType();













	/**
	 * TODO: implement a permission level system to say who is ranked above who (probably an enum)
	 *
	 * @param sender the user attempting to kick
	 * @param userID the user who might be kicked
	 */
	public void kickUser(User sender, UUID userID) {
		User user = UserManager.getUser(userID);
		if (!this.isModerator(sender)) {
			sender.sendMessage(ChatMsgs.onChannelCommandFail(this.name));
			return;
		}
		if (user == null) {
			sender.sendMessage(ChatMsgs.errorInvalidUser(userID.toString()));
			return;
		}
		String message = ChatMsgs.onUserKickAnnounce(user.getPlayerName(), this.name);
		if (this.isOwner(user)) {
			sender.sendMessage(ChatMsgs.onChannelCommandFail(this.name));
		} else if (listening.contains(user.getPlayerName())) {
			this.sendMessage(message);
			this.listening.remove(user.getUUID());
			user.removeListening(this.getName());
		} else {
			sender.sendMessage(message);
		}

	}



	public void banUser(User sender, UUID userID) {
		if (!this.isModerator(sender)) {
			sender.sendMessage(ChatMsgs.onChannelCommandFail(this.name));
			return;
		}
		if (UserManager.getUser(userID) == null) {
			sender.sendMessage(ChatMsgs.errorInvalidUser(userID.toString()));
			return;
		}
		User user = UserManager.getUser(userID);
		String message = ChatMsgs.onUserBanAnnounce(Bukkit.getPlayer(userID).getName(), this.name);
		if (this.isOwner(user)) {
			sender.sendMessage(ChatMsgs.onChannelCommandFail(this.name));
		} else if (!this.isBanned(user)) {
			if (modList.contains(userID)) {
				modList.remove(userID);
			}
			this.approvedList.remove(userID);
			this.banList.add(userID);
			this.sendMessage(message);
			if (listening.contains(userID)) {
				user.removeListening(this.getName());
			}
		} else {
			sender.sendMessage(message);
		}
	}

	public void unbanUser(User sender, UUID userID) {
		if (!this.isOwner(sender)) {
			sender.sendMessage(ChatMsgs.onChannelCommandFail(this.name));
			return;
		}
		if (UserManager.getUser(userID) == null) {
			sender.sendMessage(ChatMsgs.errorInvalidUser(userID.toString()));
			return;
		}
		User user = UserManager.getUser(userID);
		String message = ChatMsgs.onUserUnbanAnnounce(user.getPlayerName(), this.name);
		if (banList.contains(userID)) {
			this.banList.remove(userID);
			user.sendMessage(message);
			this.sendMessage(message);
		} else {
			sender.sendMessage(message);
		}
	}

	public void approveUser(User sender, UUID target) {
		if (this.getAccess().equals(AccessLevel.PUBLIC)) {
			sender.sendMessage(ChatMsgs.unsupportedOperation(this.name));
			return;
		} else {
			User targ = UserManager.getUser(target);
			String message = ChatMsgs.onUserApproved(targ.getPlayerName(), this.name);
			if (this.isApproved(targ)) {
				sender.sendMessage(message);
				return;
			}
			approvedList.add(target);
			this.sendMessage(message);
			targ.sendMessage(message);
		}
	}

	public void disapproveUser(User sender, UUID target) {
		if (this.getAccess().equals(AccessLevel.PUBLIC)) {
			sender.sendMessage(ChatMsgs.unsupportedOperation(this.name));
			return;
		} else {
			User targ = UserManager.getUser(target);
			String message = ChatMsgs.onUserDeapproved(targ.getPlayerName(), this.name);
			if (!this.isApproved(targ)) {
				sender.sendMessage(message);
				return;
			}
			approvedList.remove(target);
			this.sendMessage(message);
			targ.removeListeningSilent(this);
		}
	}

	public Set<UUID> getApprovedUsers() {
		return approvedList;
	}

	public boolean isApproved(User user) {
		return approvedList.contains(user.getUUID()) || isModerator(user);
	}

	public void disband(User sender) {
		if (this.owner == null) {
			sender.sendMessage(ChatMsgs.errorDisbandDefault());
			return;
		}
		if (!this.isOwner(sender)) {
			sender.sendMessage(ChatMsgs.onChannelCommandFail(this.name));
			return;
		}
		this.sendMessage(ChatMsgs.onChannelDisband(this.getName()));
		for (UUID userID : this.listening.toArray(new UUID[0])) {
			UserManager.getUser(userID).removeListeningSilent(this);
		}
		SblockChat.getChat().getChannelManager().dropChannel(this.name);
	}

	/**
	 * For sending a channel message, not for chat! Chat should be handled by getting a Message from
	 * ChannelManager.
	 *
	 * @param message the message to send the channel.
	 */
	public void sendMessage(String message) {
		for (UUID userID : this.listening.toArray(new UUID[0])) {
			User u = UserManager.getUser(userID);
			if (u == null) {
				listening.remove(userID);
				continue;
			}
			u.sendMessage(message);
		}
		Log.anonymousInfo(message);
	}

	/**
	 * Gets chat channel name prefix.
	 *
	 * @param sender the User sending the message
	 *
	 * @return the channel prefix
	 */
	public String[] getChannelPrefixing(User sender, boolean isThirdPerson) {

		ChatColor guildRank;
		ChatColor channelRank;
		String globalRank = null;
		ChatColor region;
		String nick;
		String displayName;
		String prepend = new String();

		String[] prefixes = new String[2];

		if (sender != null) {
			Player player = sender.getPlayer();

			// Used for /m and profile displaying, may vary from channel nick
			displayName = player.getDisplayName();

			// Guild leader color
			if (player.hasPermission("sblock.guildleader")) {
				guildRank = sender.getAspect().getColor();
			} else {
				guildRank = ColorDef.RANK_HERO;
			}

			// Chat rank color
			if (this.isOwner(sender)) {
				channelRank = ColorDef.CHATRANK_OWNER;
			} else if (this.isModerator(sender)) {
				channelRank = ColorDef.CHATRANK_MOD;
			} else {
				channelRank = ColorDef.CHATRANK_MEMBER;
			}

			// Message coloring provided by additional perms
			for (ChatColor c : ChatColor.values()) {
				if (player.hasPermission("sblockchat.color")
						&& player.hasPermission("sblockchat." + c.name().toLowerCase())) {
					prepend += c;
					break;
				}
			}

			// Name color fetched from scoreboard, if team invalid perm-based instead.
			try {
				globalRank = Bukkit.getScoreboardManager().getMainScoreboard().getPlayerTeam(player).getPrefix();
			} catch (IllegalStateException | IllegalArgumentException | NullPointerException e) {
				if (sender.getPlayer().hasPermission("group.horrorterror"))
					globalRank = ColorDef.RANK_HORRORTERROR.toString();
				else if (sender.getPlayer().hasPermission("group.denizen"))
					globalRank = ColorDef.RANK_DENIZEN.toString();
				else if (sender.getPlayer().hasPermission("group.felt"))
					globalRank = ColorDef.RANK_FELT.toString();
				else if (sender.getPlayer().hasPermission("group.helper"))
					globalRank = ColorDef.RANK_HELPER.toString();
				else if (sender.getPlayer().hasPermission("group.godtier"))
					globalRank = ColorDef.RANK_GODTIER.toString();
				else if (sender.getPlayer().hasPermission("group.donator"))
					globalRank = ColorDef.RANK_DONATOR.toString();
				else {
					globalRank = ColorDef.RANK_HERO.toString();
				}
			}

			nick = this.getNick(sender);

			Region sRegion = sender.getCurrentRegion();
			if (sRegion == null) {
				region = ChatColor.GOLD;
			} else {
				region = sRegion.getRegionColor();
			}
			prefixes[1] = new MessageElement("[", guildRank) + ","
					+ new MessageElement(this.name, channelRank).addClickEffect(
							new MessageClick(MessageClick.ClickEffect.SUGGEST_COMMAND, "@" + this.name + ' ')) + ","
					+ new MessageElement("]", guildRank) + "," + new MessageElement(isThirdPerson ? "> " : " <", region) + ","
					+ new MessageElement(globalRank + nick).addClickEffect(
							new MessageClick(MessageClick.ClickEffect.SUGGEST_COMMAND, "/m " + player.getName() + ' '))
							.addHoverEffect(new MessageHover(MessageHover.HoverEffect.SHOW_ITEM,
									"{id:minecraft:diamond,tag:{display:{Name:\\\"" + ChatColor.YELLOW + ChatColor.STRIKETHROUGH
									+ "+--" + ChatColor.RESET + " " + globalRank + displayName + " " + ChatColor.YELLOW + ChatColor.STRIKETHROUGH
									+ "--+\\\",Lore:[\\\"" + ChatColor.DARK_AQUA + sender.getPlayerClass().getDisplayName()
									+ ChatColor.YELLOW + " of " + ChatColor.DARK_AQUA + sender.getAspect().getDisplayName()
									+ "\\\",\\\"" + ChatColor.YELLOW + "Dream: " + ChatColor.DARK_AQUA + sender.getDreamPlanet().getDisplayName()
									+ "\\\",\\\"" + ChatColor.YELLOW + "Medium: " + ChatColor.DARK_AQUA + sender.getMediumPlanet().getDisplayName()
									+ "\\\"]}}}")) + ","
					+ new MessageElement(isThirdPerson ? " " : "> ", region) + "," + new MessageElement(ChatColor.WHITE + prepend);
		} else {
			guildRank = ColorDef.RANK_HERO;
			channelRank = ColorDef.CHATRANK_OWNER;
			globalRank = ColorDef.RANK_HORRORTERROR.toString();
			region = ColorDef.WORLD_AETHER;
			nick = "<nonhuman>";
		}
		prefixes[0] = guildRank + "[" + channelRank + this.name + guildRank + "]" + region
				+ (isThirdPerson ? "> " : " <") + globalRank + nick
				+ (isThirdPerson ? "" : region + ">") + ChatColor.WHITE + ' ' + prepend;
		if (prefixes[1] == null) {
			prefixes[1] = prefixes[0];
		}

		return prefixes;
	}

	public String toString() {
		return ChatColor.GOLD + this.getName() + ChatColor.GREEN + ": Access: " + ChatColor.GOLD
				+ this.getAccess() + ChatColor.GREEN + " Type: " + ChatColor.GOLD + this.getType()
				+ "\n" + ChatColor.GREEN + "Owner: " + ChatColor.GOLD
				+ Bukkit.getOfflinePlayer(this.getOwner()).getName();
	}

	// this would probably get me fired
	public boolean equals(Object o) {
		if (o instanceof String) {
			return this.name.equals(o);
		}
		return super.equals(o);
	}

	
}
