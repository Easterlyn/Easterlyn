package co.sblock.Sblock.Events.Session;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import org.bukkit.Bukkit;

import co.sblock.Sblock.Sblock;
import co.sblock.Sblock.Utilities.Sblogger;

/**
 * Checks and updates <code>Status</code> from
 * <a href="http://craftstats.com/mcstatus">craftstats</a>.
 * 
 * @author Jikoo
 */
public class SessionCheck implements Runnable {
	/**
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		String s = null;
		try {
			URLConnection uc = new URL("http://craftstats.com/mcstatus").openConnection();
			uc.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Linux x86_64; rv:2.6.32) Gecko/20100101 Firefox/25.0");
			BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream()));
			s = in.readLine();
		} catch (IOException | NumberFormatException e) {
			Sblogger.err(e);
			return;
		}

		int login = 0;
		int session = 0;
		try {
			login = Integer.valueOf(s.replaceAll(".*\"login\":\"(\\d).*", "$1"));
			session = Integer.valueOf(s.replaceAll(".*\"session\":\"(\\d).*", "$1"));
		} catch (NumberFormatException e) {
			Sblogger.warning("Session Check", "Fix regex: \"" + s + "\"");
			return;
		}

		Status status = Status.NEITHER;
		if (login == 0) {
			if (session != 0) {
				status = Status.SESSION;
			}
		} else {
			if (session != 0) {
				status = Status.BOTH;
			} else {
				status = Status.LOGIN;
			}
		}
		Bukkit.getScheduler().scheduleSyncDelayedTask(Sblock.getInstance(), new StatusSync(status));
	}
}
