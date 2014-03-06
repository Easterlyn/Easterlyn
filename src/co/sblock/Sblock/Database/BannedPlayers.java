package co.sblock.Sblock.Database;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.BanEntry;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.BanList.Type;

import co.sblock.Sblock.UserData.SblockUser;

/**
 * A small helper class containing all methods that access Minecraft's ban system.
 * 
 * @author Jikoo
 */
public class BannedPlayers {

	/**
	 * Create a PreparedStatement with which to query the SQL database. Adds a
	 * ban for the specified SblockUser.
	 * 
	 * @param target the SblockUser to add a ban for
	 * @param reason the reason the SblockUser was banned
	 */
	protected static void addBan(SblockUser target, String reason) {
		Bukkit.getBanList(Type.NAME).addBan(target.getPlayerName(),
				"<ip=" + target.getUserIP() + ">" + reason, null, "sban");
		Bukkit.getBanList(Type.IP).addBan(target.getUserIP(),
				"<name=" + target.getPlayerName() + ">" + reason, null, "sban");
	}

	/**
	 * Delete all bans (name or IP) matching the specified String.
	 * 
	 * @param target the name or IP to match
	 */
	protected static void deleteBans(String target) {
		String relatedBan = null;
		Type loop = Type.IP;
		BanList bans = Bukkit.getBanList(Type.IP);
		BanList pbans = Bukkit.getBanList(Type.NAME);
		if (bans.isBanned(target)) {
			relatedBan = bans.getBanEntry(target).getReason().replaceAll(".*<name=(\\w{1,16}+)>.*", "$1");
			bans.pardon(target);
			bans = pbans;
			loop = Type.NAME;
		} else if (pbans.isBanned(target)) {
			relatedBan = pbans.getBanEntry(target).getReason()
					.replaceAll(".*<ip=([0-9]{1,3}+\\.[0-9]{1,3}+\\.[0-9]{1,3}+\\.[0-9]{1,3}+)>.*", "$1");
			pbans.pardon(target);
		} else  {
			return;
		}

		Pattern p = Pattern.compile(loop == Type.IP 
				? "<ip=([0-9]{1,3}+\\.[0-9]{1,3}+\\.[0-9]{1,3}+\\.[0-9]{1,3}+)>" : "<name=(\\w{1,16}+)>");
		for (BanEntry b : bans.getBanEntries()) {
			Matcher m = p.matcher(b.getReason());
			while (m.find()) {
				String s = m.group().replaceAll("<\\w++=", "");
				s = s.substring(0,  s.length() - 1);
				if (s.equals(relatedBan)) {
					bans.pardon(relatedBan);
				}
			}
		}
	}

	/**
	 * Get the reason a SblockUser was banned.
	 * 
	 * @param name the name of the banned SblockUser
	 * @param ip the IP of the banned SblockUser
	 * 
	 * @return the ban reason
	 */
	protected static String getBanReason(String name, String ip) {
		if (!Bukkit.getBanList(Type.IP).isBanned(ip) && !Bukkit.getBanList(Type.NAME).isBanned(name)) {
			return null;
		}
		if (Bukkit.getOfflinePlayer(name).isBanned()) {
			return Bukkit.getBanList(Type.NAME).getBanEntry(name).getReason()
					.replaceAll("<ip=[0-9]{1,3}+\\.[0-9]{1,3}+\\.[0-9]{1,3}+\\.[0-9]{1,3}+>", "");
		} else {
			return Bukkit.getBanList(Type.IP).getBanEntry(ip).getReason()
					.replaceAll("<name=\\w{1,16}+>", "");
		}
	}
}
