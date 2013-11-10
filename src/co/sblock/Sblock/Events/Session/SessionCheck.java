package co.sblock.Sblock.Events.Session;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import org.bukkit.Bukkit;

import co.sblock.Sblock.Sblock;

/**
 * Checks and updates <code>Status</code> from Minecraft's servers.
 * 
 * @author Jikoo
 */
public class SessionCheck implements Runnable {
	/**
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		boolean session = false;
		boolean login = false;
		int response = -1;

		// Session check
		try {
			HttpURLConnection uc = (HttpURLConnection) new URL("http://session.minecraft.net").openConnection();

			response = uc.getResponseCode();
		} catch (IOException e) {
			response = 503;
		}
		if (response == 503) {
			session = true;
			response = -1;
		}

		// Login check
		try {
			HttpURLConnection uc = (HttpURLConnection) new URL("https://login.minecraft.net").openConnection();

			response = uc.getResponseCode();
		} catch (IOException e) {
			response = 503;
		}
		if (response == 503) {
			login = true;
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
		Bukkit.getScheduler().scheduleSyncDelayedTask(Sblock.getInstance(), new StatusSync(status));
	}
}
