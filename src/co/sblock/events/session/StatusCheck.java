package co.sblock.events.session;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.logging.Logger;

import org.bukkit.scheduler.BukkitRunnable;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import co.sblock.Sblock;
import co.sblock.events.Events;

/**
 * Checks and updates Status from Minecraft's servers.
 * 
 * @author Jikoo
 */
public class StatusCheck extends BukkitRunnable {

	@Override
	public void run() {
		boolean session = false;
		boolean login = false;
		JSONParser parser = new JSONParser();

		try {
			JSONObject data = (JSONObject) parser.parse(new BufferedReader(new InputStreamReader(
					new URL("http://status.mojang.com/check?service=session.minecraft.net").openStream())));
			session = data.get("session.minecraft.net").equals("red");
			data = (JSONObject) parser.parse(new BufferedReader(new InputStreamReader(
					new URL("http://status.mojang.com/check?service=auth.mojang.com").openStream())));
			login = data.get("auth.mojang.com").equals("red");
		} catch (IOException | ParseException | ClassCastException | NullPointerException e) {
			// ClassCast/NPE happens occasionally when JSON appears to be parsed incorrectly.
			// This check is run every minute, and 99.9% of the time we are casting correctly. I blame Mojang.
			Logger.getLogger("Session").warning("Unable to check http://status.mojang.com/check - status unavailable.");
			return;
		}

		Status status;
		if (login) {
			if (session) {
				status = Status.BOTH;
			} else {
				status = Status.LOGIN;
			}
		} else {
			if (session) {
				status = Status.SESSION;
			} else {
				status = Status.NEITHER;
			}
		}

		Sblock sblock = Sblock.getInstance();
		if (sblock.isEnabled()) {
			new BukkitRunnable() {
				@Override
				public void run() {
					Events.getInstance().changeStatus(status);
				}
			}.runTask(sblock);
		}
	}

}
