package co.sblock.users;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import co.sblock.Sblock;
import co.sblock.chat.ChannelManager;
import co.sblock.chat.ChatMsgs;
import co.sblock.chat.Chat;
import co.sblock.chat.channel.AccessLevel;
import co.sblock.chat.channel.Channel;
import co.sblock.fx.SblockFX;
import co.sblock.machines.Machines;
import co.sblock.machines.type.Machine;
import co.sblock.machines.utilities.Icon;
import co.sblock.machines.utilities.MachineType;
import co.sblock.utilities.captcha.Captcha;
import co.sblock.utilities.inventory.InventoryManager;
import co.sblock.utilities.progression.ServerMode;
import co.sblock.utilities.regex.RegexUtils;
import co.sblock.utilities.spectator.Spectators;

/**
 * Represents a Player currently logged into the game.
 * 
 * @author Jikoo
 */
public class OnlineUser extends OfflineUser {

	private Location serverDisableTeleport;
	private final Map<String, SblockFX> effectsList;

	protected OnlineUser(UUID userID, String displayName, Region currentRegion, UserClass userClass,
			UserAspect aspect, Region mplanet, Region dplanet, ProgressionState progstate,
			boolean allowFlight, String IP, Location previousLocation,
			String currentChannel, Set<Integer> programs, Set<String> listening,
			AtomicBoolean globalMute, AtomicBoolean supress, UUID server, UUID client) {
		super(userID, displayName, currentRegion, userClass, aspect, mplanet, dplanet, progstate,
				allowFlight, IP, previousLocation, currentChannel, programs, listening, globalMute,
				supress, server, client);
		effectsList = new HashMap<>();
		this.delayedJoin(displayName, listening);
	}

	private void delayedJoin(final String displayName, final Set<String> listening) {
		new BukkitRunnable() {
			@Override
			public void run() {
				Player player = getPlayer();
				allowFlight = getProgression().value() > ProgressionState.GODTIER.value()
						|| player != null && (getCurrentRegion().isDream()
								|| player.getGameMode() == GameMode.CREATIVE
								|| player.getGameMode() == GameMode.SPECTATOR || isServer
								|| Spectators.getInstance().isSpectator(getUUID()));
				if (player != null) {
					player.setAllowFlight(allowFlight);
					player.setFlying(allowFlight);
					player.setDisplayName(displayName);
					loginAddListening(listening);
					Region region = getCurrentRegion();
					updateCurrentRegion(region);
					// On login, conditions for setting rpack are not met, must be done here
					player.setResourcePack(region.getResourcePackURL());
				}
			}
		}.runTask(Sblock.getInstance());
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
				allowFlight = getPlayer() != null && (getPlayer().getWorld().getName().contains("Circle")
						|| getPlayer().getGameMode() == GameMode.CREATIVE
						|| getPlayer().getGameMode() == GameMode.SPECTATOR
						|| isServer || Spectators.getInstance().isSpectator(getUUID()));
				if (getPlayer() != null) {
					getPlayer().setAllowFlight(allowFlight);
					getPlayer().setFlying(allowFlight);
				}
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
		return days + " days, " + DECIMAL_FORMATTER.format(hours) + ':' + DECIMAL_FORMATTER.format(time);
	}

	@Override
	public Region getCurrentRegion() {
		Region region = super.getCurrentRegion();
		if (region != Region.UNKNOWN) {
			return region;
		}
		region = Region.getRegion(getPlayer().getWorld().getName());
		if (region.isDream()) {
			// TODO fix for godtier
			return getDreamPlanet();
		}
		return region;
	}

	@Override
	public void updateCurrentRegion(Region newR) {
		if (getCurrentRegion() != null && newR == getCurrentRegion()) {
			if (!getListening().contains(getCurrentRegion().getChannelName())) {
				Channel c = ChannelManager.getChannelManager().getChannel(getCurrentRegion().getChannelName());
				addListening(c);
			}
			return;
		}
		if (currentChannel == null || getCurrentRegion() != null && currentChannel.equals(getCurrentRegion().getChannelName())) {
			currentChannel = newR.getChannelName();
		}
		if (getCurrentRegion() != null && !getCurrentRegion().getChannelName().equals(newR.getChannelName())) {
			removeListening(getCurrentRegion().getChannelName());
		}
		if (!getListening().contains(newR.getChannelName())) {
			addListening(ChannelManager.getChannelManager().getChannel(newR.getChannelName()));
		}
		if (newR.isDream()) {
			getPlayer().setPlayerTime(newR == Region.DERSE ? 18000L : 6000L, false);
		} else {
			getPlayer().resetPlayerTime();
		}
		if (getCurrentRegion() == null || !getCurrentRegion().getResourcePackURL().equals(newR.getResourcePackURL())) {
			getPlayer().setResourcePack(newR.getResourcePackURL());
		}
		setCurrentRegion(newR);
	}

	@Override
	public void startServerMode() {
		Player p = this.getPlayer();
		if (Spectators.getInstance().isSpectator(getUUID())) {
			Spectators.getInstance().removeSpectator(p);
		}
		if (getClient() == null) {
			p.sendMessage(ChatColor.RED + "You must have a client to enter server mode!"
					+ "+\nAsk someone with " + ChatColor.AQUA + "/requestclient <player>");
			return;
		}
		OnlineUser u = Users.getOnlineUser(getClient());
		if (u == null) {
			p.sendMessage(ChatColor.RED + "You should wait for your client before progressing!");
			return;
		}
		if (!u.getPrograms().contains(Icon.SBURBCLIENT.getProgramID())) {
			p.sendMessage(ChatColor.RED + u.getPlayerName() + " does not have the Sburb Client installed!");
			return;
		}
		Machine m = Machines.getInstance().getComputer(getClient());
		if (m == null) {
			p.sendMessage(ChatColor.RED + u.getPlayerName() + " has not placed their computer in their house!");
			return;
		}
		this.serverDisableTeleport = p.getLocation();
		if (!Machines.getInstance().isByComputer(u.getPlayer(), 25)) {
			p.teleport(m.getKey());
		} else {
			p.teleport(u.getPlayer());
		}
		this.isServer = true;
		this.updateFlight();
		p.setNoDamageTicks(Integer.MAX_VALUE);
		InventoryManager.storeAndClearInventory(p);
		p.getInventory().addItem(MachineType.COMPUTER.getUniqueDrop());
		p.getInventory().addItem(MachineType.CRUXTRUDER.getUniqueDrop());
		p.getInventory().addItem(MachineType.PUNCH_DESIGNIX.getUniqueDrop());
		p.getInventory().addItem(MachineType.TOTEM_LATHE.getUniqueDrop());
		p.getInventory().addItem(MachineType.ALCHEMITER.getUniqueDrop());
		for (Material mat : ServerMode.getInstance().getApprovedSet()) {
			p.getInventory().addItem(new ItemStack(mat));
		}
		p.sendMessage(ChatColor.GREEN + "Server mode enabled!");
	}

	@Override
	public void stopServerMode() {
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
		p.sendMessage(ChatColor.GREEN + "Server program closed!");
	}

	@Override
	public void sendMessage(String message) {
		this.getPlayer().sendMessage(message);
	}

	@Override
	public void rawHighlight(String message, String... additionalMatches) {
		Player p = this.getPlayer();

		String[] matches = new String[additionalMatches.length + 2];
		matches[0] = p.getName();
		matches[1] = ChatColor.stripColor(p.getDisplayName());
		if (additionalMatches.length > 0) {
			System.arraycopy(additionalMatches, 0, matches, 2, additionalMatches.length);
		}
		StringBuilder msg = new StringBuilder();
		Matcher match = Pattern.compile(RegexUtils.ignoreCaseRegex(matches)).matcher(message);
		int lastEnd = 0;
		// For every match, prepend aqua chat color and append previous color
		while (match.find()) {
			msg.append(message.substring(lastEnd, match.start()));
			String last = ChatColor.getLastColors(msg.toString());
			msg.append(ChatColor.AQUA).append(match.group()).append(last);
			lastEnd = match.end();
		}
		if (lastEnd < message.length()) {
		msg.append(message.substring(lastEnd));
		}
		message = msg.toString();

		if (lastEnd > 0) {
			// Matches were found, commence highlight format changes.
			// This is stupid and unsafe - a json element prior to the channel names may have extra, which is now broken.
			// TODO fix
			//message = message.replaceFirst("\\[(" + ChatColor.COLOR_CHAR + ".*?)\\]", ChatColor.AQUA + "!!$1" + ChatColor.AQUA +"!!");
			// Funtimes sound effects here
			switch ((int) (Math.random() * 20)) {
			case 0:
				p.playSound(p.getLocation(), Sound.ENDERMAN_STARE, 1, 2);
				break;
			case 1:
				p.playSound(p.getLocation(), Sound.WITHER_SPAWN, 1, 2);
				break;
			case 2:
			case 3:
				p.playSound(p.getLocation(), Sound.ANVIL_LAND, 1, 1);
				break;
			default:
				p.playSound(p.getLocation(), Sound.LEVEL_UP, 1, 2);
			}
		}

		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + p.getName() + " " + message);
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
		if (channel.isBanned(this)) {
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
		if (!channel.getListening().contains(getUUID())) {
			channel.addListening(getUUID());
			this.getListening().add(channel.getName());
			channel.sendMessage(ChatMsgs.onChannelJoin(this, channel));
			return true;
		} else {
			this.sendMessage(ChatMsgs.errorAlreadyListening(channel.getName()));
			return false;
		}
	}

	@Override
	public void loginAddListening(Set<String> channels) {
		for (Iterator<String> iterator = channels.iterator(); iterator.hasNext();) {
			Channel c = ChannelManager.getChannelManager().getChannel(iterator.next());
			if (c != null && !c.isBanned(this) && (c.getAccess() != AccessLevel.PRIVATE || c.isApproved(this))) {
				this.getListening().add(c.getName());
				c.addListening(this.getUUID());
			} else {
				iterator.remove();
			}
		}
		if (this.getPlayer().hasPermission("group.felt") && !this.getListening().contains("@")) {
			this.getListening().add("@");
			ChannelManager.getChannelManager().getChannel("@").addListening(this.getUUID());
		}

		StringBuilder base = new StringBuilder(ChatColor.GREEN.toString())
				.append(this.getPlayer().getDisplayName()).append(ChatColor.YELLOW)
				.append(" logs the fuck in and begins pestering <>").append(ChatColor.YELLOW)
				.append(" at ").append(new SimpleDateFormat("HH:mm").format(new Date()));
		// Heavy loopage ensues
		for (OfflineUser u : Users.getUsers()) {
			StringBuilder matches = new StringBuilder();
			for (String s : this.getListening()) {
				if (u.getListening().contains(s)) {
					matches.append(ChatColor.GOLD).append(s).append(ChatColor.YELLOW).append(", ");
				}
			}
			if (matches.length() > 0) {
				matches.replace(matches.length() - 3, matches.length() - 1, "");
				StringBuilder msg = new StringBuilder(base.toString().replace("<>", matches.toString()));
				int comma = msg.toString().lastIndexOf(',');
				if (comma != -1) {
					u.sendMessage(msg.replace(comma, comma + 1, " and").toString());
				} else {
					u.sendMessage(msg.toString());
				}
			} else {
				u.sendMessage(base.toString().replace(" and begins pestering <>", ""));
			}
		}

		Bukkit.getConsoleSender().sendMessage(this.getPlayerName() + " began pestering " + StringUtils.join(channels, ' '));
	}

	@Override
	public void removeListening(String channelName) {
		Channel c = ChannelManager.getChannelManager().getChannel(channelName);
		if (c == null) {
			this.sendMessage(ChatMsgs.errorInvalidChannel(channelName));
			this.getListening().remove(channelName);
			return;
		}
		if (this.getListening().remove(channelName)) {
			c.removeNick(this, false);
			c.sendMessage(ChatMsgs.onChannelLeave(this, c));
			c.removeListening(this.getUUID());
			if (this.currentChannel != null && channelName.equals(this.getCurrentChannel().getName())) {
				this.currentChannel = null;
			}
		} else {
			this.sendMessage(ChatMsgs.errorNotListening(channelName));
		}
	}

	@Override
	public boolean getComputerAccess() {
		if (!Chat.getComputerRequired()) {
			// Overrides the computer limitation for pre-Entry shenanigans
			return true;
		}
		return Machines.getInstance().isByComputer(this.getPlayer(), 10);
	}

	/**
	 * Gets the user's current Effects
	 * 
	 * @return the Map of effects
	 */
	public Map<String, SblockFX> getEffects() {
		return this.effectsList;
	}

	/**
	 * Set the user's current Effects. Will overlay existing map.
	 * 
	 * @param effects the map of all Effects to add
	 */
	public void setAllEffects(HashMap<String, SblockFX> effects) {
		removeAllEffects();
		for (SblockFX entry : effects.values()) {
			this.effectsList.put(entry.getCanonicalName(), entry);
			if (entry.isPassive()) {
				entry.applyEffect(this, null);
			}
		}

	}

	/**
	 * Removes all Effects from the user and cancels the Effect
	 */
	public void removeAllEffects() {
		for (SblockFX effect : effectsList.values()) {
			effect.removeEffect(this);
		}
		this.effectsList.clear();
	}

	/**
	 * Add a new effect to the user's current Effects. If the effect is already present, increases
	 * the strength by 1.
	 * 
	 * @param effect the Effect to add
	 */
	public void addEffect(SblockFX effect) {
		if (this.effectsList.containsKey(effect.getCanonicalName())) {
			effect.setMultiplier(this.effectsList.get(effect.getCanonicalName()).getMultiplier()
					+ effect.getMultiplier());
			this.effectsList.put(effect.getCanonicalName(), effect);
		} else {
			this.effectsList.put(effect.getCanonicalName(), effect);
		}
		if (effect.isPassive()) {
			effect.applyEffect(this, null);
		}
	}

	/**
	 * Reduces the multiplier on an effect
	 * 
	 * @param effect The effect to change
	 * @param reduction The amount to reduce the multiplier by
	 */
	public void reduceEffect(SblockFX effect, Integer reduction) {
		if (this.effectsList.containsKey(effect.getCanonicalName())) {
			if (this.effectsList.get(effect.getCanonicalName()).getMultiplier() - reduction > 0) {
				effect.setMultiplier(effect.getMultiplier() - reduction);
				if (effect.isPassive()) {
					effect.applyEffect(this, null);
				}
			} else {
				this.effectsList.remove(effect.getCanonicalName());
				effect.removeEffect(this);
			}
		}
	}

	@Override
	public OnlineUser getOnlineUser() {
		return this;
	}
}
