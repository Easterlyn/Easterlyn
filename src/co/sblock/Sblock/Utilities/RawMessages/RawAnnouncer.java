package co.sblock.Sblock.Utilities.RawMessages;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.util.org.apache.commons.lang3.StringUtils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import co.sblock.Sblock.CommandListener;
import co.sblock.Sblock.Module;
import co.sblock.Sblock.Sblock;
import co.sblock.Sblock.SblockCommand;
import co.sblock.Sblock.Utilities.RawMessages.MessageClick.ClickEffect;
import co.sblock.Sblock.Utilities.RawMessages.MessageHover.HoverEffect;

/**
 * @author Jikoo
 */
public class RawAnnouncer extends Module implements CommandListener {

	private int taskId;
	private List<MessageElement> announcements;

	/**
	 * @see co.sblock.Sblock.Module#onEnable()
	 */
	@Override
	protected void onEnable() {
		announcements = this.constructAnnouncements();
		

		taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(Sblock.getInstance(), new Runnable() {

			@Override
			public void run() {
				MessageElement msg = announcements.get((int) (Math.random() * announcements.size()));
				getLogger().info(msg.getConsoleFriendly());
				String announcement = msg.toString();
				for (Player p : Bukkit.getServer().getOnlinePlayers()) {
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
							"tellraw " + p.getName() + " " + announcement);
				}
			}
		}, 0, 1200 * 15); // 15 minutes in between rawnouncments

		this.registerCommands(this);
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
						.addClickEffect(new MessageClick(ClickEffect.OPEN_URL,
								"http://www.reddit.com/r/sblock"))
						.addHoverEffect(new MessageHover(HoverEffect.SHOW_TEXT,
								ChatColor.GOLD + "Click here to go!")),
				new MessageElement("!", ChatColor.RED)));

		msgs.add(new MessageHalement("If you're having difficulty with chat, ").addExtra(
				new MessageElement("/sc ?", ChatColor.AQUA)
						.addClickEffect(new MessageClick(ClickEffect.RUN_COMMAND, "/sc ?"))
						.addHoverEffect(new MessageHover(HoverEffect.SHOW_TEXT,
								ChatColor.GOLD + "Click to run!")),
				new MessageElement(" is your friend!", ChatColor.RED)));

		msgs.add(new MessageHalement("Remember, we are in ").addExtra(
				new MessageElement("ALPHA", ChatColor.GOLD, ChatColor.BOLD)
						.addHoverEffect(new MessageHover(HoverEffect.SHOW_TEXT,
								ChatColor.DARK_RED + "We reserve the right to fuck up badly.")),
				new MessageElement("!", ChatColor.RED)));

		msgs.add(new MessageHalement("Join us on ").addExtra(
				new MessageElement("Mumble", ChatColor.AQUA)
						.addClickEffect(new MessageClick(ClickEffect.OPEN_URL,
								"http://mumble.sourceforge.net/"))
						.addHoverEffect(new MessageHover(HoverEffect.SHOW_TEXT,
								ChatColor.GOLD + "Click here to download!")),
				new MessageElement(" for voice chat! The server is ", ChatColor.RED),
				new MessageElement("   sblock.co", ChatColor.AQUA),
				new MessageElement(", port ", ChatColor.RED),
				new MessageElement("25560", ChatColor.AQUA),
				new MessageElement("!", ChatColor.RED)));

		msgs.add(new MessageHalement("It appears that enchanting furnaces is very beneficial."
				+ " You might consider giving it a try."));

		msgs.add(new MessageHalement("Curious about your fellow players' classpects? Have a look at their ")
				.addExtra(new MessageElement("/profile", ChatColor.AQUA)
								.addClickEffect(new MessageClick(ClickEffect.RUN_COMMAND, "/profile"))
								.addHoverEffect(new MessageHover(HoverEffect.SHOW_TEXT,
										ChatColor.GOLD + "Click to run!")),
						new MessageElement("!", ChatColor.RED)));

		msgs.add(new MessageHalement("It is your generosity that keeps Sblock alive. Please consider ").addExtra(
				new MessageElement("donating", ChatColor.AQUA)
						.addClickEffect(new MessageClick(ClickEffect.OPEN_URL, "http://adf.ly/NThbj"))
						.addHoverEffect(new MessageHover(HoverEffect.SHOW_TEXT, ChatColor.GOLD + "Click here to go!")),
				new MessageElement(" to help.", ChatColor.RED)));

		msgs.add(new MessageHalement("Sleeping now is more beneficial. Try holding shift while right clicking your bed!"));

		return msgs;
	}

	/**
	 * @see co.sblock.Sblock.Module#onDisable()
	 */
	@Override
	protected void onDisable() {
		Bukkit.getScheduler().cancelTask(taskId);
	}

	@SblockCommand(consoleFriendly = true, description = "Force a raw message announcement or talk as Hal.",
			usage = "/hal", permission = "group.horrorterror")
	public boolean hal(CommandSender s, String[] args) {
		MessageElement msg;
		if (args.length == 1) {
			try {
				int msgNum = Integer.valueOf(args[0]);
				if (msgNum > announcements.size()) {
					s.sendMessage(ChatColor.RED.toString() + announcements.size() + " announcements exist currently.");
					msgNum = announcements.size();
				}
				msg = announcements.get(msgNum - 1);
			} catch (NumberFormatException e) {
				msg = new MessageHalement(args[0]);
			}
		} else if (args.length > 0) {
			msg = new MessageHalement(StringUtils.join(args, ' '));
		} else {
			msg = announcements.get((int) (Math.random() * announcements.size()));
		}
		getLogger().info(msg.getConsoleFriendly());
		String announcement = msg.toString();
		for (Player p : Bukkit.getServer().getOnlinePlayers()) {
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + p.getName() + " " + announcement);
		}
		return true;
	}
}
