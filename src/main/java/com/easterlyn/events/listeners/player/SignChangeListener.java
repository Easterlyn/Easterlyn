package com.easterlyn.events.listeners.player;

import com.easterlyn.Easterlyn;
import com.easterlyn.chat.Chat;
import com.easterlyn.events.listeners.EasterlynListener;
import com.easterlyn.users.UserRank;
import com.easterlyn.utilities.PermissionUtils;
import com.easterlyn.utilities.TextUtils;

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.SignChangeEvent;

import net.md_5.bungee.api.ChatColor;

/**
 * Listener for SignChangeEvents.
 * 
 * @author Jikoo
 */
public class SignChangeListener extends EasterlynListener {

	private final Chat chat;

	public SignChangeListener(Easterlyn plugin) {
		super(plugin);
		this.chat = plugin.getModule(Chat.class);

		PermissionUtils.addParent("easterlyn.sign.unlogged", "easterlyn.spam");
		PermissionUtils.addParent("easterlyn.sign.unlogged", UserRank.FELT.getPermission());
	}

	/**
	 * The event handler for SignChangeEvents.
	 * <p>
	 * Allows signs to be colored using &codes.
	 * 
	 * @param event the SignChangeEvent
	 */
	@EventHandler(ignoreCancelled = true)
	public void onSignChange(SignChangeEvent event) {

		// Automatically flag players with bypass as posting non-empty signs to skip empty checks
		boolean empty = !event.getPlayer().hasPermission("easterlyn.sign.unlogged");

		for (int i = 0; i < event.getLines().length; i++) {
			event.setLine(i, ChatColor.translateAlternateColorCodes('&', event.getLine(i)));
			if (empty && !TextUtils.appearsEmpty(event.getLine(i))) {
				empty = false;
			}
		}

		if (empty || event.getPlayer().hasPermission("easterlyn.sign.unlogged")) {
			return;
		}

		Block block = event.getBlock();

		StringBuilder msg = new StringBuilder().append(ChatColor.GRAY)
				.append(block.getWorld().getName()).append(' ').append(block.getX()).append("x, ")
				.append(block.getY()).append("y, ").append(block.getZ()).append("z\n");
		for (String line : event.getLines()) {
			if (!TextUtils.appearsEmpty(line)) {
				msg.append(line).append(ChatColor.GRAY).append('\n');
			}
		}
		msg.delete(msg.length() - 3, msg.length());

		if (chat.testForMute(event.getPlayer(), msg.toString(), "#sign")) {
			event.setCancelled(true);
		}
	}

}
