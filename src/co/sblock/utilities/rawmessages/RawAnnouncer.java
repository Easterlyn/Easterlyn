package co.sblock.utilities.rawmessages;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import co.sblock.Sblock;
import co.sblock.module.Module;
import co.sblock.utilities.Log;

/**
 * @author Jikoo
 */
public class RawAnnouncer extends Module {

	private List<MessageElement> announcements;
	private static RawAnnouncer instance;

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
				MessageElement msg = announcements.get((int) (Math.random() * announcements.size()));
				Log.anonymousInfo(msg.getConsoleFriendly());
				String announcement = msg.toString();
				for (Player p : Bukkit.getOnlinePlayers()) {
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
							"tellraw " + p.getName() + " " + announcement);
				}
			}
		}, 0, 1200 * 15); // 15 minutes in between rawnouncments
	}

	/**
	 * Creates a List of all announcements.
	 * 
	 * @return the List created
	 */
	private List<MessageElement> constructAnnouncements() {
		List<MessageElement> msgs = new ArrayList<MessageElement>();

		msgs.add(new MessageHalement("Join us on our subreddit, ").addExtra(
				new MessageElement("/r/Sblock", ChatColor.AQUA)
						.addClickEffect(new MessageClick(MessageClick.ClickEffect.OPEN_URL,
								"http://www.reddit.com/r/sblock"))
						.addHoverEffect(new MessageHover(MessageHover.HoverEffect.SHOW_TEXT,
								ChatColor.GOLD + "Click here to go!")),
				new MessageElement("!", ChatColor.RED)));

		msgs.add(new MessageHalement("If you're having difficulty with chat, ").addExtra(
				new MessageElement("/sc ?", ChatColor.AQUA)
						.addClickEffect(new MessageClick(MessageClick.ClickEffect.RUN_COMMAND, "/sc ?"))
						.addHoverEffect(new MessageHover(MessageHover.HoverEffect.SHOW_TEXT,
								ChatColor.GOLD + "Click to run!")),
				new MessageElement(" is your friend!", ChatColor.RED)));

		msgs.add(new MessageHalement("Remember, we are in ").addExtra(
				new MessageElement("ALPHA", ChatColor.GOLD, ChatColor.BOLD)
						.addHoverEffect(new MessageHover(MessageHover.HoverEffect.SHOW_TEXT,
								ChatColor.DARK_RED + "We reserve the right to fuck up badly.")),
				new MessageElement("!", ChatColor.RED)));

		msgs.add(new MessageHalement("Join us on ").addExtra(
				new MessageElement("Mumble", ChatColor.AQUA)
						.addClickEffect(new MessageClick(MessageClick.ClickEffect.OPEN_URL,
								"http://mumble.sourceforge.net/"))
						.addHoverEffect(new MessageHover(MessageHover.HoverEffect.SHOW_TEXT,
								ChatColor.GOLD + "Click here to download!")),
				new MessageElement(" for voice chat! The server is ", ChatColor.RED),
				new MessageElement("   sblock.co", ChatColor.AQUA),
				new MessageElement(", port ", ChatColor.RED),
				new MessageElement("25560", ChatColor.AQUA),
				new MessageElement("!", ChatColor.RED)));

		msgs.add(new MessageHalement("It appears that enchanting furnaces is very beneficial."
				+ " You might consider giving it a try."));

		msgs.add(new MessageHalement("It is your generosity that keeps Sblock alive. Please consider ").addExtra(
				new MessageElement("donating", ChatColor.AQUA)
						.addClickEffect(new MessageClick(MessageClick.ClickEffect.OPEN_URL, "https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=Z327Z7E2SBVV2&lc=US&item_name=Sblock&currency_code=USD&bn=PP%2dDonationsBF%3abtn_donate_LG%2egif%3aNonHosted"))
						.addHoverEffect(new MessageHover(MessageHover.HoverEffect.SHOW_TEXT, ChatColor.GOLD + "Click here to go!")),
				new MessageElement(" to help.", ChatColor.RED)));

		msgs.add(new MessageHalement("To sleep without dreaming, sneak while right clicking your bed!"));

		msgs.add(new MessageHalement("If you're using our resource pack, we suggest you ").addExtra(
				new MessageElement("download", ChatColor.AQUA)
				.addClickEffect(new MessageClick(MessageClick.ClickEffect.OPEN_URL, "http://sblock.co/rpack/"))
				.addHoverEffect(new MessageHover(MessageHover.HoverEffect.SHOW_TEXT, ChatColor.GOLD + "Click to see all Sblock rpacks!")),
				new MessageElement(" the sound pack as well.", ChatColor.RED)));

		msgs.add(new MessageHalement("Interested in jamming with your fellow Sblock players? Join our ").addExtra(
				new MessageElement("plug.dj room", ChatColor.AQUA)
				.addClickEffect(new MessageClick(MessageClick.ClickEffect.OPEN_URL, "http://plug.dj/sblock/"))
				.addHoverEffect(new MessageHover(MessageHover.HoverEffect.SHOW_TEXT, ChatColor.GOLD + "Click join!")),
				new MessageElement(" to listen and play!", ChatColor.RED)));

		return msgs;
	}

	public List<MessageElement> getMessages() {
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
}
