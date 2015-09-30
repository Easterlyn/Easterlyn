package co.sblock.micromodules;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;

import co.sblock.Sblock;
import co.sblock.chat.ChannelManager;
import co.sblock.chat.message.Message;
import co.sblock.chat.message.MessageBuilder;
import co.sblock.module.Module;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

/**
 * @author Jikoo
 */
public class RawAnnouncer extends Module {

	private List<Message> announcements;
	private static RawAnnouncer instance;

	@Override
	protected void onEnable() {
		instance = this;

		announcements = this.constructAnnouncements();

		Bukkit.getScheduler().scheduleSyncRepeatingTask(Sblock.getInstance(), new Runnable() {
			@Override
			public void run() {
				if (Bukkit.getOnlinePlayers().size() == 0) {
					return;
				}
				announcements.get((int) (Math.random() * announcements.size())).send(Bukkit.getOnlinePlayers());
			}
		}, 0, 1200 * 15); // 15 minutes in between rawnouncments
	}

	/**
	 * Creates a List of all announcements.
	 * 
	 * @return the List created
	 */
	private List<Message> constructAnnouncements() {
		List<Message> msgs = new ArrayList<>();
		TextComponent nameHover = new TextComponent("Automated Announcement");
		nameHover.setColor(ChatColor.RED);
		MessageBuilder builder = new MessageBuilder().setChannel(ChannelManager.getChannelManager().getChannel("#"))
				.setSender(ChatColor.DARK_RED + "Lil Hal").setNameHover(nameHover).setNameClick("/report ");

		List<TextComponent> components = new ArrayList<>();

		TextComponent hover = new TextComponent("Click here to go!");
		hover.setColor(ChatColor.GOLD);

		// Announcement: Join us on our subreddit
		TextComponent component = new TextComponent("Join us on our subreddit,");
		component.setColor(ChatColor.RED);
		components.add(component);

		component = new TextComponent(" /r/Sblock");
		component.setColor(ChatColor.BLUE);
		component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{hover.duplicate()}));
		component.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "http://www.reddit.com/r/sblock"));
		components.add(component);

		component = new TextComponent("!");
		component.setColor(ChatColor.RED);
		components.add(component);

		builder.setMessage("Join us on our subreddit, http://www.reddit.com/r/sblock",
				components.toArray(new TextComponent[components.size()]));
		msgs.add(builder.toMessage());

		// Announcement: Pls gib monie
		components.clear();
		component = new TextComponent("It is your generosity that keeps Sblock alive. Please consider");
		component.setColor(ChatColor.RED);
		components.add(component);

		component = new TextComponent(" donating ");
		component.setColor(ChatColor.DARK_GREEN);
		hover.setText("Click here for more information!");
		component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{hover.duplicate()}));
		component.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "http://sblock.co/wiki/Donating"));
		components.add(component);

		component = new TextComponent("to help.");
		component.setColor(ChatColor.RED);
		components.add(component);

		builder.setMessage("It is your generosity that keeps Sblock alive. Please consider http://sblock.co/wiki/Donating to help.",
				components.toArray(new TextComponent[components.size()]));
		msgs.add(builder.toMessage());

		// Announcement: Try EnchantedFurnace
		components.clear();
		component = new TextComponent("Smelting wasting your time?");
		component.setColor(ChatColor.RED);
		components.add(component);

		component = new TextComponent(" Enchant a furnace");
		component.setColor(ChatColor.BLUE);
		component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{hover.duplicate()}));
		component.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "http://sblock.co/wiki/EnchantedFurnace"));
		components.add(component);

		component = new TextComponent("!");
		component.setColor(ChatColor.RED);
		components.add(component);

		builder.setMessage("Smelting wasting your time? http://sblock.co/wiki/EnchantedFurnace",
				components.toArray(new TextComponent[components.size()]));
		msgs.add(builder.toMessage());

		// Announcement: Protect your stuff
		components.clear();
		component = new TextComponent("Unprotected and unvisited areas gradually regenerate. Please");
		component.setColor(ChatColor.RED);
		components.add(component);

		component = new TextComponent(" claim your builds");
		component.setColor(ChatColor.BLUE);
		component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{hover.duplicate()}));
		component.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL,
				"https://www.youtube.com/watch?v=VDsjXB-BaE0&list=PL8YpI023Cthye5jUr-KGHGfczlNwgkdHM&index=1"));
		components.add(component);

		component = new TextComponent("!");
		component.setColor(ChatColor.RED);
		components.add(component);

		builder.setMessage("Unprotected and unvisited areas gradually regenerate. Please claim your builds!",
				components.toArray(new TextComponent[components.size()]));
		msgs.add(builder.toMessage());

		// Announcement: Try /chat
		components.clear();
		component = new TextComponent("Having trouble with chat? Check out ");
		component.setColor(ChatColor.RED);
		components.add(component);

		component = new TextComponent("/chat");
		component.setColor(ChatColor.AQUA);
		hover.setText("Click here to run!");
		component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{hover.duplicate()}));
		component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/chat"));
		components.add(component);

		component = new TextComponent("!");
		component.setColor(ChatColor.RED);
		components.add(component);

		builder.setMessage("Having trouble with chat? Check out /chat!",
				components.toArray(new TextComponent[components.size()]));
		msgs.add(builder.toMessage());

		// Announcement: How to sleep
		components.clear();
		component = new TextComponent("To");
		component.setColor(ChatColor.RED);
		components.add(component);

		component = new TextComponent(" sleep ");
		component.setColor(ChatColor.AQUA);
		component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{hover.duplicate()}));
		component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/sleep"));
		components.add(component);

		component = new TextComponent("without dreaming, sneak while right clicking your bed!");
		component.setColor(ChatColor.RED);
		components.add(component);

		builder.setMessage("To sleep without dreaming, sneak while right clicking your bed!",
				components.toArray(new TextComponent[components.size()]));
		msgs.add(builder.toMessage());

		// Announcement: /convert your shit
		components.clear();
		component = new TextComponent("Captchas have changed! Be sure to");
		component.setColor(ChatColor.RED);
		components.add(component);

		component = new TextComponent(" convert ");
		component.setColor(ChatColor.AQUA);
		component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{hover.duplicate()}));
		component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/convert"));
		components.add(component);

		component = new TextComponent("all your captchas before the old format is dropped!");
		component.setColor(ChatColor.RED);
		components.add(component);

		builder.setMessage("Captchas have changed! Be sure to /convert all your captchas before the old format is dropped!",
				components.toArray(new TextComponent[components.size()]));
		msgs.add(builder.toMessage());

		// Announcement: Use /report ffs
		components.clear();
		component = new TextComponent("Found grief or a bug? Please");
		component.setColor(ChatColor.RED);
		components.add(component);

		component = new TextComponent(" /report ");
		component.setColor(ChatColor.AQUA);
		hover.setText("Click to autofill!");
		component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{hover.duplicate()}));
		component.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/report "));
		components.add(component);

		component = new TextComponent("issues so we can help!");
		component.setColor(ChatColor.RED);
		components.add(component);

		builder.setMessage("Found grief or a bug? Please /report issues so we can help!",
				components.toArray(new TextComponent[components.size()]));
		msgs.add(builder.toMessage());

		// Announcement: Mamblo No. 5
		components.clear();
		component = new TextComponent("We use");
		component.setColor(ChatColor.RED);
		components.add(component);

		component = new TextComponent(" Discord ");
		component.setColor(ChatColor.BLUE);
		hover.setText("Click here to join Sblock's server!");
		component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{hover.duplicate()}));
		component.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "http://sblock.co/discord/"));
		components.add(component);

		component = new TextComponent("for voice chat. Join today!");
		component.setColor(ChatColor.RED);
		components.add(component);

		builder.setMessage("We use http://sblock.co/discord/ for voice chat. Join today!",
				components.toArray(new TextComponent[components.size()]));
		msgs.add(builder.toMessage());

		// Announcement: ALPHA
		components.clear();
		component = new TextComponent("Remember, we are in ");
		component.setColor(ChatColor.RED);
		components.add(component);

		component = new TextComponent("ALPHA");
		component.setColor(ChatColor.GOLD);
		component.setBold(true);
		hover.setText("We reserve the right to fuck up badly.");
		hover.setColor(ChatColor.DARK_RED);
		component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{hover.duplicate()}));
		component.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/report "));
		components.add(component);

		component = new TextComponent("!");
		component.setColor(ChatColor.RED);
		components.add(component);

		builder.setMessage("Remember, we are in ALPHA! We reserve the right to fuck up badly.",
				components.toArray(new TextComponent[components.size()]));
		msgs.add(builder.toMessage());

		return msgs;
	}

	public List<Message> getMessages() {
		return announcements;
	}

	@Override
	protected void onDisable() {
		instance = null;
	}

	public static RawAnnouncer getInstance() {
		return instance;
	}

	@Override
	protected String getModuleName() {
		return "RawAnnouncer";
	}
}
