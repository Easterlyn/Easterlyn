package co.sblock.users;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import co.sblock.Sblock;
import co.sblock.captcha.Captcha;
import co.sblock.chat.ChannelManager;
import co.sblock.chat.Chat;
import co.sblock.chat.ChatMsgs;
import co.sblock.chat.Color;
import co.sblock.chat.channel.AccessLevel;
import co.sblock.chat.channel.Channel;
import co.sblock.chat.channel.NickChannel;
import co.sblock.effects.Effects;
import co.sblock.machines.Machines;
import co.sblock.machines.type.Machine;
import co.sblock.micromodules.Spectators;
import co.sblock.progression.ServerMode;
import co.sblock.utilities.InventoryManager;

/**
 * Represents a Player currently logged into the game.
 * 
 * @author Jikoo
 */
public class OnlineUser extends OfflineUser {

	private Location serverDisableTeleport;
	private boolean isServer;
	private String lastChat;
	private final AtomicInteger violationLevel;
	private final AtomicBoolean spamWarned;

	protected OnlineUser(UUID userID, String ip, YamlConfiguration yaml, Location previousLocation,
			Set<String> programs, String currentChannel, Set<String> listening) {
		super(userID, ip, yaml, previousLocation, programs, currentChannel, listening);
		isServer = false;
		lastChat = new String();
		violationLevel = new AtomicInteger();
		spamWarned = new AtomicBoolean();
	}

	@Override
	public Player getPlayer() {
		return Bukkit.getPlayer(getUUID());
	}

	@Override
	public String getDisplayName() {
		return getPlayer().getDisplayName();
	}

	@Override
	public void updateFlight() {
		new BukkitRunnable() {
			@Override
			public void run() {
				boolean allowFlight = getPlayer() != null
						&& (getPlayer().getWorld().getName().equals("Derspit")
								|| getPlayer().getGameMode() == GameMode.CREATIVE
								|| getPlayer().getGameMode() == GameMode.SPECTATOR || isServer);
				if (getPlayer() != null) {
					getPlayer().setAllowFlight(allowFlight);
					getPlayer().setFlying(allowFlight);
				}
				getYamlConfiguration().set("flying", allowFlight);
			}
		}.runTask(Sblock.getInstance());
	}

	@Override
	public String getTimePlayed() {
		long time = getPlayer().getStatistic(org.bukkit.Statistic.PLAY_ONE_TICK);
		long days = time / (24 * 60 * 60 * 20);
		time -= days * 24 * 60 * 60 * 20;
		long hours = time / (60 * 60 * 20);
		time -= hours * 60 * 60 * 20;
		time = time / (60 * 20);
		DecimalFormat decimalFormat = new DecimalFormat("00");
		return days + " days, " + decimalFormat.format(hours) + ':' + decimalFormat.format(time);
	}

	@Override
	public Location getCurrentLocation() {
		return getPlayer().getLocation();
	}

	@Override
	public Region getCurrentRegion() {
		Region region = Region.getRegion(getPlayer().getWorld().getName());
		if (region.isDream()) {
			return getDreamPlanet();
		}
		return region;
	}

	@Override
	public void updateCurrentRegion(Region newRegion) {
		Region oldRegion = super.getCurrentRegion();
		if (newRegion.isDream()) {
			getPlayer().setPlayerTime(newRegion == Region.DERSE ? 18000L : 6000L, false);
		} else {
			getPlayer().resetPlayerTime();
		}
		if (oldRegion != null && newRegion == oldRegion) {
			if (canJoinDefaultChats() && !getListening().contains(oldRegion.getChannelName())) {
				Channel channel = ChannelManager.getChannelManager().getChannel(oldRegion.getChannelName());
				addListening(channel);
			}
			return;
		}
		if (currentChannel == null || oldRegion != null && currentChannel.equals(oldRegion.getChannelName())) {
			currentChannel = newRegion.getChannelName();
		}
		if (oldRegion != null && !oldRegion.getChannelName().equals(newRegion.getChannelName())) {
			removeListening(oldRegion.getChannelName());
		}
		if (!getListening().contains(newRegion.getChannelName()) && canJoinDefaultChats()) {
			addListening(ChannelManager.getChannelManager().getChannel(newRegion.getChannelName()));
		}
		if (oldRegion == null || !oldRegion.getResourcePackURL().equals(newRegion.getResourcePackURL())) {
			getPlayer().setResourcePack(newRegion.getResourcePackURL());
		}
		setCurrentRegion(newRegion);
	}

	/**
	 * Check if the User is in server mode.
	 * 
	 * @return true if the User is in server mode
	 */
	public boolean isServer() {
		return this.isServer;
	}

	public void startServerMode() {
		if (this.isServer) {
			return;
		}
		Player p = this.getPlayer();
		if (getClient() == null) {
			p.sendMessage(Color.BAD + "You must have a client to enter server mode!"
					+ "+\nAsk someone with " + Color.COMMAND + "/requestclient <player>");
			return;
		}
		OnlineUser u = Users.getOnlineUser(getClient());
		if (u == null) {
			p.sendMessage(Color.BAD + "You should wait for your client before progressing!");
			return;
		}
		if (!u.getPrograms().contains("SburbClient")) {
			p.sendMessage(Color.BAD + u.getPlayerName() + " does not have the Sburb Client installed!");
			return;
		}
		Pair<Machine, ConfigurationSection> pair = Machines.getInstance().getComputer(getClient());
		if (pair == null) {
			p.sendMessage(Color.BAD + u.getPlayerName() + " has not placed their computer in their house!");
			return;
		}
		if (Spectators.getInstance().isSpectator(getUUID())) {
			Spectators.getInstance().removeSpectator(p);
		}
		this.serverDisableTeleport = p.getLocation();
		if (!Machines.getInstance().isByComputer(u.getPlayer(), 25)) {
			p.teleport(pair.getLeft().getKey(pair.getRight()));
		} else {
			p.teleport(u.getPlayer());
		}
		this.isServer = true;
		this.updateFlight();
		p.setNoDamageTicks(Integer.MAX_VALUE);
		InventoryManager.storeAndClearInventory(p);
		p.getInventory().addItem(Machines.getMachineByName("Computer").getUniqueDrop());
		p.getInventory().addItem(Machines.getMachineByName("Cruxtruder").getUniqueDrop());
		p.getInventory().addItem(Machines.getMachineByName("PunchDesignix").getUniqueDrop());
		p.getInventory().addItem(Machines.getMachineByName("TotemLathe").getUniqueDrop());
		p.getInventory().addItem(Machines.getMachineByName("Alchemiter").getUniqueDrop());
		for (Material mat : ServerMode.getInstance().getApprovedSet()) {
			p.getInventory().addItem(new ItemStack(mat));
		}
		p.sendMessage(Color.GOOD + "Server mode enabled!");
	}

	public void stopServerMode() {
		if (!this.isServer) {
			return;
		}
		if (Bukkit.getOfflinePlayer(getClient()).isOnline()) {
			Player clientPlayer = Bukkit.getPlayer(getClient());
			for (ItemStack is : getPlayer().getInventory()) {
				if (Captcha.isPunch(is)) {
					clientPlayer.getWorld().dropItem(clientPlayer.getLocation(), is).setPickupDelay(0);
					break;
				}
			}
		}
		this.isServer = false;
		this.updateFlight();
		Player p = this.getPlayer();
		p.teleport(serverDisableTeleport);
		p.setFallDistance(0);
		p.setNoDamageTicks(0);
		InventoryManager.restoreInventory(p);
		p.sendMessage(Color.GOOD + "Server program closed!");
	}

	@Override
	public void sendMessage(String message) {
		Player player = this.getPlayer();
		if (player == null) {
			return;
		}
		player.sendMessage(message);
	}

	@Override
	public void setCurrentChannel(Channel c) {
		if (c == null) {
			this.sendMessage(ChatMsgs.errorInvalidChannel("null"));
			return;
		}
		if (c.isBanned(this)) {
			this.sendMessage(ChatMsgs.onUserBanAnnounce(this.getPlayerName(), c.getName()));
			return;
		}
		if (!c.isApproved(this)) {
			this.sendMessage(ChatMsgs.onUserDeniedPrivateAccess(c.getName()));
			return;
		}
		currentChannel = c.getName();
		if (!this.getListening().contains(c.getName())) {
			this.addListening(c);
		} else {
			this.sendMessage(ChatMsgs.onChannelSetCurrent(c.getName()));
		}
	}

	@Override
	public boolean addListening(Channel channel) {
		if (channel == null) {
			return false;
		}
		if (channel.isBanned(this) || !canJoinDefaultChats() && channel.getOwner() == null) {
			this.sendMessage(ChatMsgs.onUserBanAnnounce(this.getPlayerName(), channel.getName()));
			return false;
		}
		if (!channel.isApproved(this)) {
			this.sendMessage(ChatMsgs.onUserDeniedPrivateAccess(channel.getName()));
			return false;
		}
		if (!this.getListening().contains(channel)) {
			this.getListening().add(channel.getName());
		}
		if (!channel.getListening().contains(this.getUUID())) {
			channel.getListening().add(this.getUUID());
			this.getListening().add(channel.getName());
			channel.sendMessage(ChatMsgs.onChannelJoin(this, channel));
			return true;
		} else {
			this.sendMessage(ChatMsgs.errorAlreadyListening(channel.getName()));
			return false;
		}
	}

	@Override
	public void handleLoginChannelJoins() {
		for (Iterator<String> iterator = this.getListening().iterator(); iterator.hasNext();) {
			Channel channel = ChannelManager.getChannelManager().getChannel(iterator.next());
			if (channel != null && !channel.isBanned(this) && (channel.getAccess() != AccessLevel.PRIVATE || channel.isApproved(this))
					&& (canJoinDefaultChats() || channel.getOwner() != null)) {
				this.getListening().add(channel.getName());
				channel.getListening().add(this.getUUID());
			} else {
				iterator.remove();
			}
		}
		if (this.getPlayer().hasPermission("sblock.felt") && !this.getListening().contains("@")) {
			this.getListening().add("@");
			ChannelManager.getChannelManager().getChannel("@").getListening().add(this.getUUID());
		}
		String base = new StringBuilder(Color.GOOD_PLAYER.toString()).append(this.getDisplayName())
				.append(Color.GOOD).append(" began pestering <>").append(Color.GOOD)
				.append(" at ").append(new SimpleDateFormat("HH:mm").format(new Date())).toString();
		// Heavy loopage ensues
		for (OfflineUser u : Users.getUsers()) {
			if (!u.isOnline() || !(u instanceof OnlineUser)) {
				continue;
			}
			StringBuilder matches = new StringBuilder();
			for (String s : this.getListening()) {
				if (u.getListening().contains(s)) {
					matches.append(Color.GOOD_EMPHASIS).append(s).append(Color.GOOD).append(", ");
				}
			}
			String message;
			if (matches.length() > 0) {
				matches.replace(matches.length() - 3, matches.length() - 1, "");
				StringBuilder msg = new StringBuilder(base.replace("<>", matches.toString()));
				int comma = msg.lastIndexOf(",");
				if (comma != -1) {
					if (comma == msg.indexOf(",")) {
						msg.replace(comma, comma + 1, " and");
					} else {
						msg.insert(comma + 1, " and");
					}
				}
				message = msg.toString();
			} else {
				message = base.replace(" <>", "");
			}
			u.sendMessage(message);
		}

		Logger.getLogger("Minecraft").info(base.toString().replace("<>", StringUtils.join(getListening(), ", ")));
	}

	@Override
	public void removeListening(String channelName) {
		Channel channel = ChannelManager.getChannelManager().getChannel(channelName);
		if (channel == null) {
			this.sendMessage(ChatMsgs.errorInvalidChannel(channelName));
			this.getListening().remove(channelName);
			return;
		}
		if (this.getListening().remove(channelName)) {
			if (channel instanceof NickChannel) {
				((NickChannel) channel).removeNick(this);
			}
			channel.sendMessage(ChatMsgs.onChannelLeave(this, channel));
			channel.getListening().remove(this.getUUID());
			if (this.currentChannel != null && channelName.equals(this.getCurrentChannel().getName())) {
				this.currentChannel = null;
			}
		} else {
			this.sendMessage(Color.BAD + "You are not listening to " + Color.BAD_EMPHASIS + channelName);
		}
	}

	@Override
	public synchronized String getLastChat() {
		return this.lastChat;
	}

	@Override
	public synchronized void setLastChat(String message) {
		this.lastChat = message;
	}

	@Override
	public int getChatViolationLevel() {
		return violationLevel.get();
	}

	@Override
	public void setChatViolationLevel(int violationLevel) {
		this.violationLevel.set(violationLevel);
	}

	@Override
	public boolean getChatWarnStatus() {
		return spamWarned.get();
	}

	@Override
	public void setChatWarnStatus(boolean warned) {
		spamWarned.set(warned);
	}

	@Override
	public boolean getComputerAccess() {
		if (!Chat.getComputerRequired()) {
			// Overrides the computer limitation for pre-Entry shenanigans
			return true;
		}
		Effects effects = Effects.getInstance();
		return effects.getAllEffects(getPlayer()).containsKey(effects.getEffect("Computer"))
				|| Machines.getInstance().isByComputer(this.getPlayer(), 10);
	}

	@Override
	public OnlineUser getOnlineUser() {
		return this;
	}

	@Override
	public void save() {
		if (this.isOnline()) {
			super.save();
		} else {
			Users.getInstance().getLogger().warning("Online user did not unload for " + getUUID());
			OfflineUser.fromOnline(this).save();
		}
	}
}
