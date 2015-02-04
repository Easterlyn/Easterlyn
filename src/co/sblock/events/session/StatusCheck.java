package co.sblock.events.session;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import co.sblock.Sblock;
import co.sblock.utilities.Log;

/**
 * Checks and updates Status from Minecraft's servers.
 * 
 * @author Jikoo
 */
public class StatusCheck extends BukkitRunnable {
	/**
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		boolean session = false;
		boolean login = false;
		JSONParser parser = new JSONParser();

		try {
			JSONObject data = (JSONObject) parser.parse(new BufferedReader(new InputStreamReader(
					new URL("http://status.mojang.com/check?service=session.minecraft.net").openStream())));
			session = ((String) data.get("session.minecraft.net")).equals("red");
			data = (JSONObject) parser.parse(new BufferedReader(new InputStreamReader(
					new URL("http://status.mojang.com/check?service=session.minecraft.net").openStream())));
			login = ((String) data.get("session.minecraft.net")).equals("red");
		} catch (IOException | ParseException | ClassCastException | NullPointerException e) {
			// ClassCast/NPE happens occasionally when JSON appears to be parsed incorrectly.
			// This check is run every minute, and 99.9% of the time we are casting correctly. I blame Mojang.
			Log.getLogger("Session").warning("Unable to check http://status.mojang.com/check - status unavailable.");
			return;
		}

		Status status = Status.NEITHER;
		if (login) {
			if (session) {
				status = Status.BOTH;
			} else {
				status = Status.LOGIN;
			}
		} else {
			if (session) {
				status = Status.SESSION;
			}
		}
		if (Sblock.getInstance().isEnabled()) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(Sblock.getInstance(), new StatusSync(status));
		}
	}
}
