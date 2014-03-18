package co.sblock.Sblock.Events.Session;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import org.bukkit.Bukkit;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import co.sblock.Sblock.Sblock;
import co.sblock.Sblock.Utilities.Log;

/**
 * Checks and updates Status from Minecraft's servers.
 * 
 * @author Jikoo
 */
public class StatusCheck implements Runnable {
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
			session = !((String) data.get("session.minecraft.net")).equals("green");
			data = (JSONObject) parser.parse(new BufferedReader(new InputStreamReader(
					new URL("http://status.mojang.com/check?service=login.minecraft.net").openStream())));
			login = !((String) data.get("login.minecraft.net")).equals("green");
		} catch (IOException | ParseException e) {
			Log.getLogger("Session").warning("Unable to connect to http://status.mojang.com/check - status unavailable.");
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
