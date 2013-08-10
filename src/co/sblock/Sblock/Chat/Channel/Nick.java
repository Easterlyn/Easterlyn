/**
 * 
 */
package co.sblock.Sblock.Chat.Channel;

import org.bukkit.ChatColor;

/**
 * @author Jikoo
 *
 */
public class Nick {

	private boolean canon;
	private String name;
	private CanonNicks nick;

	public Nick (String name) {
		this.name = name;
		canon = CanonNicks.getNick(name) != null;
		if (canon) {
			nick = CanonNicks.getNick(name);
		}
	}

	public boolean isCanon() {
		return canon;
	}

	public String getName() {
		return canon ? nick.getName() : name;
	}

	public ChatColor getColor() {
		return canon ? nick.getColor() : ChatColor.DARK_GREEN;
	}

	public String getPester() {
		return canon ? nick.getPester() : "pestering";
	}

	public String getHandle() {
		return canon ? nick.getHandle() : null;
	}

	public CanonNicks getCanon() {
		return canon ? nick : null;
	}

	public static boolean isCanon(String nick) {
		return CanonNicks.getNick(nick) != null;
	}

	public String toString() {
		return this.name;
	}
}
