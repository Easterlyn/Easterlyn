package co.sblock.utilities.rawmessages;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import co.sblock.Sblock;
import co.sblock.chat.ColorDef;
import co.sblock.module.Module;
import co.sblock.utilities.Log;

/**
 * @author Jikoo
 */
public class RawAnnouncer extends Module {

	private List<AnnouncementMessage> announcements;
	private static RawAnnouncer instance;
	private static final String HALEMENT = "{\"text\":\"\",\"extra\":["
			+ "{\"color\":\"white\",\"text\":\"[\"},"
			+ "{\"color\":\"red\",\"text\":\"#\"},"
			+ "{\"color\":\"white\",\"text\":\"] <\"},"
			+ "{\"color\":\"dark_red\",\"text\":\"Lil Hal\",\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"Automated Announcement\",\"color\":\"red\"}}},"
			+ "{\"color\":\"white\",\"text\":\"> \"},"
			+ "{\"color\":\"red\",\"text\":\"";

	/**
	 * @see co.sblock.Module#onEnable()
	 */
	@Override
	protected void onEnable() {
		instance = this;

		announcements = this.constructAnnouncements();

		Bukkit.getScheduler().scheduleSyncRepeatingTask(Sblock.getInstance(), new Runnable() {
			@Override
			public void run() {
				AnnouncementMessage msg = announcements.get((int) (Math.random() * announcements.size()));
				Log.anonymousInfo(msg.getConsole());
				for (Player p : Bukkit.getOnlinePlayers()) {
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + p.getName() + ' ' + msg.getJSON());
				}
			}
		}, 0, 1200 * 15); // 15 minutes in between rawnouncments
	}

	/**
	 * Creates a List of all announcements.
	 * 
	 * @return the List created
	 */
	private List<AnnouncementMessage> constructAnnouncements() {
		List<AnnouncementMessage> msgs = new ArrayList<>();

		msgs.add(new AnnouncementMessage(HALEMENT + "Join us on our subreddit, \"},"
				+ "{\"color\":\"aqua\",\"text\":\"/r/Sblock\","
					+ "\"clickEvent\":{\"action\":\"open_url\",\"value\":\"http://www.reddit.com/r/sblock\"},"
					+ "\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"Click here to go!\",\"color\":\"gold\"}}},"
				+ "{\"color\":\"red\",\"text\":\"!\"}]}",
				ColorDef.HAL + "Join us on our subreddit, http://www.reddit.com/r/sblock"));

		msgs.add(new AnnouncementMessage(HALEMENT + "If you're having trouble with chat, \"},"
				+ "{\"color\":\"aqua\",\"text\":\"/sc ?\","
					+ "\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/sc ?\"},"
					+ "\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"Click here to run!\",\"color\":\"gold\"}}},"
				+ "{\"color\":\"red\",\"text\":\" is your friend!\"}]}",
				ColorDef.HAL + "If you're having trouble with chat, /sc ? is your friend"));

		msgs.add(new AnnouncementMessage(HALEMENT + "Remember, we are in \"},"
				+ "{\"color\":\"gold\",\"bold\":\"true\",\"text\":\"ALPHA\","
					+ "\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"We reserve the right to fuck up badly.\",\"color\":\"dark_red\"}}},"
				+ "{\"color\":\"red\",\"text\":\"!\"}]}",
				ColorDef.HAL + "Remember, we are in ALPHA! We reserve the right to fuck up badly."));

		msgs.add(new AnnouncementMessage(HALEMENT + "Join us on \"},"
				+ "{\"color\":\"aqua\",\"text\":\"Mumble\","
				+ "\"clickEvent\":{\"action\":\"open_url\",\"value\":\"http://mumble.sourceforge.net/\"},"
					+ "\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"Click here to download!\",\"color\":\"gold\"}}},"
				+ "{\"color\":\"red\",\"text\":\" for voice chat! The server is \"},"
				+ "{\"color\":\"aqua\",\"text\":\"   sblock.co\"},"
				+ "{\"color\":\"red\",\"text\":\", port \"},"
				+ "{\"color\":\"aqua\",\"text\":\"25560\"},"
				+ "{\"color\":\"red\",\"text\":\"!\"}]}",
				ColorDef.HAL + "Join us on Mumble for voice chat! The server is sblock.co, port 25560!"));

		msgs.add(new AnnouncementMessage(HALEMENT + "It appears that enchanting furnaces is very beneficial. You might consider giving it a try.\"}]}",
				ColorDef.HAL + "It appears that enchanting furnaces is very beneficial. You might consider giving it a try."));

		msgs.add(new AnnouncementMessage(HALEMENT + "It is your generosity that keeps Sblock alive. Please consider \"},"
				+ "{\"color\":\"aqua\",\"text\":\"donating\","
					+ "\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=Z327Z7E2SBVV2&lc=US&item_name=Sblock&currency_code=USD&bn=PP%2dDonationsBF%3abtn_donate_LG%2egif%3aNonHosted\"},"
					+ "\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"Click here to go!\",\"color\":\"gold\"}}},"
				+ "{\"color\":\"red\",\"text\":\" to help!\"}]}",
				ColorDef.HAL + "It is your generosity that keeps Sblock alive. Please consider donating to help."));

		msgs.add(new AnnouncementMessage(HALEMENT + "To sleep without dreaming, sneak while right clicking your bed!\"}]}",
				ColorDef.HAL + "To sleep without dreaming, sneak while right clicking your bed!"));

		msgs.add(new AnnouncementMessage(HALEMENT + "If you're using our resource pack, we suggest you \"},"
				+ "{\"color\":\"aqua\",\"text\":\"download\","
				+ "\"clickEvent\":{\"action\":\"open_url\",\"value\":\"http://sblock.co/rpack/\"},"
					+ "\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"Click here to see all Sblock rpacks!\",\"color\":\"gold\"}}},"
				+ "{\"color\":\"red\",\"text\":\" the sound pack as well.\"}]}",
				ColorDef.HAL + "If you're using our resource pack, we suggest you download the sound pack as well."));

		msgs.add(new AnnouncementMessage(HALEMENT + "Interested in jamming with your fellow Sblock players? Join our \"},"
				+ "{\"color\":\"aqua\",\"text\":\"plug.dj room\","
					+ "\"clickEvent\":{\"action\":\"open_url\",\"value\":\"http://plug.dj/sblock/\"},"
					+ "\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"Click here to go!\",\"color\":\"gold\"}}},"
				+ "{\"color\":\"red\",\"text\":\" to listen and play!\"}]}",
				ColorDef.HAL + "Interested in jamming with your fellow Sblock players? Join http://plug.dj/sblock/ to listen and play!"));

		return msgs;
	}

	public List<AnnouncementMessage> getMessages() {
		return announcements;
	}

	@Override
	protected void onDisable() {
		instance = null;
	}

	@Override
	protected String getModuleName() {
		return "RawAnnouncer";
	}

	public static RawAnnouncer getInstance() {
		return instance;
	}

	public class AnnouncementMessage {
		private String json, console;
		public AnnouncementMessage(String json, String console) {
			this.json = json;
			this.console = console;
		}
		public String getJSON() {
			return this.json;
		}
		public String getConsole() {
			return this.console;
		}
	}
}
