package co.sblock.Sblock.Utilities.RawMessages;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import co.sblock.Sblock.CommandListener;
import co.sblock.Sblock.Module;
import co.sblock.Sblock.Sblock;
import co.sblock.Sblock.SblockCommand;
import co.sblock.Sblock.Utilities.RawMessages.MessageClickEffect.ClickEffect;
import co.sblock.Sblock.Utilities.RawMessages.MessageHoverEffect.HoverEffect;

/**
 * @author Jikoo
 */
public class RawAnnouncer extends Module implements CommandListener {

	private int taskId;
	private List<Message> announcements;

	/**
	 * @see co.sblock.Sblock.Module#onEnable()
	 */
	@Override
	protected void onEnable() {
		announcements = this.constructAnnouncements();
		

		taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(Sblock.getInstance(), new Runnable() {

			@Override
			public void run() {
				String announcement = announcements.get(
						(int) (Math.random() * announcements.size())).toString();
				for (Player p : Bukkit.getServer().getOnlinePlayers()) {
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
							"tellraw " + p.getName() + " " + announcement);
				}
			}
		}, 0, 1200 * 30); // 30 minutes in between rawnouncments

		this.registerCommands(this);
	}

	/**
	 * Creates a List of all announcements.
	 * 
	 * @return the List created
	 */
	private List<Message> constructAnnouncements() {
		List<Message> msgs = new ArrayList<Message>();

		msgs.add(new Message(new MessageHalement("Join us on our subreddit, "),
				new MessageElement("/r/Sblock", ChatColor.AQUA)
						.addClickEffect(new MessageClickEffect(ClickEffect.OPEN_URL,
								"http://www.reddit.com/r/sblock"))
						.addHoverEffect(new MessageHoverEffect(HoverEffect.SHOW_TEXT,
								ChatColor.GOLD + "Click here to go!")),
				new MessageElement("!", ChatColor.RED)));

		msgs.add(new Message(new MessageHalement("If you're having difficulty with chat, "),
				new MessageElement("/sc ?", ChatColor.AQUA)
						.addClickEffect(new MessageClickEffect(ClickEffect.RUN_COMMAND, "/sc ?"))
						.addHoverEffect(new MessageHoverEffect(HoverEffect.SHOW_TEXT,
								ChatColor.GOLD + "Click to run!")),
				new MessageElement(" is your friend!", ChatColor.RED)));

		msgs.add(new Message(new MessageHalement("Remember, we are in "),
				new MessageElement("ALPHA", ChatColor.GOLD, ChatColor.BOLD)
						.addHoverEffect(new MessageHoverEffect(HoverEffect.SHOW_TEXT,
								ChatColor.DARK_RED + "We reserve the right to fuck up badly.")),
				new MessageElement("!", ChatColor.RED)));

		msgs.add(new Message(
				new MessageHalement("Join us on "),
				new MessageElement("Mumble", ChatColor.AQUA)
						.addClickEffect(new MessageClickEffect(ClickEffect.OPEN_URL,
								"http://mumble.sourceforge.net/"))
						.addHoverEffect(new MessageHoverEffect(HoverEffect.SHOW_TEXT,
								ChatColor.GOLD + "Click here to download!")),
				new MessageElement(" for voice chat! The server is at ", ChatColor.RED),
				new MessageElement("   mumble.sblock.co", ChatColor.AQUA),
				new MessageElement("!", ChatColor.RED)));

		msgs.add(new Message(new MessageHalement("It appears that enchanting furnaces is very "
				+ "beneficial. You might consider giving it a try.")));

		return msgs;
	}

	/**
	 * @see co.sblock.Sblock.Module#onDisable()
	 */
	@Override
	protected void onDisable() {
		Bukkit.getScheduler().cancelTask(taskId);
	}

	// TEMPORARY - TESTING ONLY
	@SblockCommand(consoleFriendly = true, description = "Force a raw message announcement.",
			usage = "/rawnounce", permission = "group.horrorterror")
	public boolean rawnounce(CommandSender s, String[] args) {
		for (Player p : Bukkit.getServer().getOnlinePlayers()) {
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + p.getName() + " "
					+ announcements.get((int) (Math.random() * announcements.size())).toString());
		}
		return true;
	}
}
