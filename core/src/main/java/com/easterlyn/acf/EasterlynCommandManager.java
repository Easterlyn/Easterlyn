package com.easterlyn.acf;

import co.aikar.commands.BukkitCommandIssuer;
import co.aikar.commands.BukkitCommandManager;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.MessageType;
import co.aikar.commands.PaperCommandManager;
import co.aikar.locales.MessageKeyProvider;
import com.easterlyn.EasterlynCore;
import java.lang.reflect.Field;
import java.util.Arrays;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.bukkit.scheduler.BukkitTask;

public class EasterlynCommandManager extends PaperCommandManager {

	private final EasterlynCore plugin;

	public EasterlynCommandManager(EasterlynCore plugin) {
		super(plugin);
		this.plugin = plugin;

		// Unregister unnecessary locale stuff
		try {
			Field bukkitLocale = BukkitCommandManager.class.getDeclaredField("localeTask");
			bukkitLocale.setAccessible(true);
			Object o = bukkitLocale.get(this);
			if (o instanceof BukkitTask) {
				((BukkitTask) o).cancel();
			}
		} catch (ReflectiveOperationException e) {
			e.printStackTrace();
		}

		HandlerList.getHandlerLists().stream().flatMap(handlerList -> Arrays.stream(handlerList.getRegisteredListeners()))
				.filter(registeredListener -> registeredListener.getListener().getClass().getSimpleName().equals("ACFBukkitListener"))
				.findAny().ifPresent(registeredListener -> HandlerList.unregisterAll(registeredListener.getListener()));

		this.locales = new EasterlynLocales(plugin, this);
	}

	@Override
	public void sendMessage(CommandIssuer issuer, MessageType type, MessageKeyProvider key, String... replacements) {
		plugin.getLocaleManager().sendMessage(((BukkitCommandIssuer) issuer).getIssuer(), key.getMessageKey().getKey(), replacements);
	}

	@Override
	public void sendMessage(CommandSender issuer, MessageType type, MessageKeyProvider key, String... replacements) {
		plugin.getLocaleManager().sendMessage(((BukkitCommandIssuer) issuer).getIssuer(), key.getMessageKey().getKey(), replacements);
	}

}
