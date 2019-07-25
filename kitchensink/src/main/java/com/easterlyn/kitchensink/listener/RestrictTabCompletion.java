package com.easterlyn.kitchensink.listener;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.easterlyn.EasterlynCore;
import com.easterlyn.user.UserRank;
import com.easterlyn.util.PermissionUtil;
import com.mojang.brigadier.suggestion.Suggestions;
import java.util.stream.Collectors;
import org.bukkit.command.Command;

public class RestrictTabCompletion extends PacketAdapter {

	private final EasterlynCore core;

	public RestrictTabCompletion(EasterlynCore plugin) {
		super(plugin, PacketType.Play.Server.TAB_COMPLETE, PacketType.Play.Client.TAB_COMPLETE);

		this.core = plugin;
		PermissionUtil.addParent("easterlyn.commands.unfiltered", UserRank.MODERATOR.getPermission());
	}

	/**
	 * Edit packets outgoing to the client.
	 *
	 * @see com.comphenix.protocol.events.PacketAdapter#onPacketSending(com.comphenix.protocol.events.PacketEvent)
	 *
	 * @param event the PacketEvent
	 */
	@Override
	public void onPacketSending(PacketEvent event) {
		if (event.getPacketType() == PacketType.Play.Server.TAB_COMPLETE) {
			if (event.getPlayer().hasPermission("easterlyn.commands.unfiltered")) {
				return;
			}
			Suggestions suggestions = event.getPacket().getSpecificModifier(Suggestions.class).read(0);
			event.getPacket().getSpecificModifier(Suggestions.class).write(0,
					new Suggestions(suggestions.getRange(), suggestions.getList().stream().filter(suggestion -> {
						String completion = suggestion.getText();
						if (completion.length() < 1 || completion.indexOf('/') != 0 || completion.indexOf(':') < 0) {
							return true;
						}
						if (core.getSimpleCommandMap() == null) {
							return false;
						}
						// Certain commands injected by plugins can still be completed for players lacking permissions.
						// Don't complete any commands that don't have permissions set up.
						Command command = core.getSimpleCommandMap().getCommand(completion.substring(1));
						return command == null || command.getPermission() == null || command.getPermission().isEmpty()
								|| event.getPlayer().hasPermission(command.getPermission());
					}).collect(Collectors.toList())));
		}
	}

	/**
	 * Check a packet from the client.
	 *
	 * @see com.comphenix.protocol.events.PacketAdapter#onPacketReceiving(PacketEvent)
	 *
	 * @param event the PacketEvent
	 */
	@Override
	public void onPacketReceiving(PacketEvent event) {
		if (event.getPacketType() == PacketType.Play.Client.TAB_COMPLETE) {
			if (event.getPlayer().hasPermission("easterlyn.commands.unfiltered")) {
				return;
			}
			String completing = event.getPacket().getStrings().read(0);
			int colon = completing.indexOf(':'), space = completing.indexOf(' ');
			if (colon > 0 && (space < 0 || space > colon)) {
				event.setCancelled(true);
			}
		}
	}

}
