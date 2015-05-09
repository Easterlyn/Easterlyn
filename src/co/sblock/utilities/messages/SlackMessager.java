package co.sblock.utilities.messages;

import java.io.BufferedOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.UUID;

import org.bukkit.scheduler.BukkitRunnable;

import org.json.simple.JSONObject;

import co.sblock.Sblock;

import net.md_5.bungee.api.ChatColor;

/**
 * 
 * 
 * @author Jikoo
 */
public class SlackMessager {
	private static final String URL_STRING = "https://cravatar.eu/helmhead/%s/128.png";

	public static void post(String username, String message) {
		post(username, null, message);
	}

	@SuppressWarnings("unchecked")
	public static void post(String username, UUID uuid, String message) {
		if (Sblock.getInstance().getConfig().getString("slack.webhook") == null) {
			return;
		}

		JSONObject json = new JSONObject();
		json.put("text", ChatColor.stripColor(message));
		json.put("username", username);
		json.put("icon_url", String.format(URL_STRING, uuid == null ? username : uuid.toString()));
		json.put("mrkdwn", false);
		final String payload = "payload=" + json.toJSONString();

		new BukkitRunnable() {
			@Override
			public void run() {
				try {
					HttpURLConnection urlConnection = (HttpURLConnection) new URL(Sblock.getInstance().getConfig().getString("slack.webhook")).openConnection();
					urlConnection.setDoOutput(true);
					urlConnection.setRequestMethod("POST");
					try (BufferedOutputStream out = new BufferedOutputStream(urlConnection.getOutputStream())) {
						out.write(payload.getBytes(Charset.forName("UTF-8")));
						out.flush();
						out.close();
					}
					urlConnection.getResponseCode();
					urlConnection.disconnect();
				} catch (Exception e) {
					// Ignore exceptions, Slack is a very non-critical service
				}
			}
		}.runTaskAsynchronously(Sblock.getInstance());
	}
}
