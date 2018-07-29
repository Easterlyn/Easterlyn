package com.easterlyn.commands.info;

import com.easterlyn.Easterlyn;
import com.easterlyn.commands.EasterlynAsynchronousCommand;
import com.easterlyn.users.UserAffinity;
import com.easterlyn.users.UserClass;
import com.easterlyn.users.UserRank;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Get data about player classpect percentages!
 *
 * @author Jikoo
 */
public class NumberCrunchCommand extends EasterlynAsynchronousCommand {

	public NumberCrunchCommand(Easterlyn plugin) {
		super(plugin, "numbercrunch");
		setPermissionLevel(UserRank.DANGER_DANGER_HIGH_VOLTAGE);
		setPermissionMessage("Number crunching is very server-intensive. Ask an admin!");
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		HashMap<String, AtomicInteger> counts =  new HashMap<>();
		for (UserClass userClass : UserClass.values()) {
			counts.put(userClass.getDisplayName(), new AtomicInteger(0));
		}
		for (UserAffinity userAffinity : UserAffinity.values()) {
			counts.put(userAffinity.getDisplayName(), new AtomicInteger(0));
		}
		File folder = new File(getPlugin().getDataFolder(), "users");
		if (!folder.exists()) {
			return true;
		}
		String[] files = folder.list();
		for (String fileName : files) {
			YamlConfiguration player = YamlConfiguration.loadConfiguration(new File(folder, fileName));
			counts.get(player.getString("classpect.class", "Heir")).incrementAndGet();
			counts.get(player.getString("classpect.aspect", "Breath")).incrementAndGet();
			counts.get(player.getString("classpect.medium", "LOWAS")).incrementAndGet();
			counts.get(player.getString("classpect.dream", "Prospit")).incrementAndGet();
		}
		sender.sendMessage("Total files: " + files.length);
		sender.sendMessage("\nCLASS:");
		for (UserClass userClass : UserClass.values()) {
			int count = counts.get(userClass.getDisplayName()).get();
			sender.sendMessage(userClass.getDisplayName() + ": " + count + " (" + (count * 100.0 / files.length) + "%)");
		}
		sender.sendMessage("\nASPECT:");
		for (UserAffinity userAffinity : UserAffinity.values()) {
			int count = counts.get(userAffinity.getDisplayName()).get();
			sender.sendMessage(userAffinity.getDisplayName() + ": " + count + " (" + (count * 100.0 / files.length) + "%)");
		}
		return true;
	}
}
