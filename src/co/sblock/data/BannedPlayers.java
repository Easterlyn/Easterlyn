package co.sblock.data;

import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.BanList.Type;

import co.sblock.users.User;

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
	protected static void addBan(User target, String reason) {
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
		BanList bans = Bukkit.getBanList(Type.IP);
		BanList pbans = Bukkit.getBanList(Type.NAME);
		if (bans.isBanned(target)) {
			pbans.pardon(bans.getBanEntry(target).getReason()
					.replaceAll(".*<name=(\\w{1,16}+)>.*", "$1"));
			bans.pardon(target);
		} else if (pbans.isBanned(target)) {
			bans.pardon(pbans.getBanEntry(target).getReason()
					.replaceAll(".*<ip=([0-9]{1,3}+\\.[0-9]{1,3}+\\.[0-9]{1,3}+\\.[0-9]{1,3}+)>.*", "$1"));
			pbans.pardon(target);
		} else	{
			return;
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
		if (Bukkit.getBanList(Type.NAME).isBanned(name)) {
			return Bukkit.getBanList(Type.NAME).getBanEntry(name).getReason()
					.replaceAll("<ip=[0-9]{1,3}+\\.[0-9]{1,3}+\\.[0-9]{1,3}+\\.[0-9]{1,3}+>", "");
		} else {
			return Bukkit.getBanList(Type.IP).getBanEntry(ip).getReason()
					.replaceAll("<uuid=[\\w-]+>", "").replaceAll("<name=[\\w-]{1,16}+>", "");
		}
	}
}
