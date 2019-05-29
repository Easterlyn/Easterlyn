package com.easterlyn.micromodules;

import com.easterlyn.Easterlyn;
import com.easterlyn.chat.ChannelManager;
import com.easterlyn.chat.Chat;
import com.easterlyn.chat.Language;
import com.easterlyn.chat.message.Message;
import com.easterlyn.chat.message.MessageBuilder;
import com.easterlyn.module.Module;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

/**
 * A Module for periodic announcements with hover text and click functionality.
 *
 * @author Jikoo
 */
public class RawAnnouncer extends Module {

	private Language lang;
	private List<Message> announcements;

	public RawAnnouncer(Easterlyn plugin) {
		super(plugin);
	}

	@Override
	protected void onEnable() {

		this.lang = this.getPlugin().getModule(Language.class);

		this.announcements = this.constructAnnouncements();

		new BukkitRunnable() {
			@Override
			public void run() {
				if (Bukkit.getOnlinePlayers().size() == 0) {
					return;
				}
				announcements.get((int) (Math.random() * announcements.size())).send(Bukkit.getOnlinePlayers());
			}
		}.runTaskTimer(getPlugin(), 1200, 1200 * 15); // 15 minutes in between rawnouncments
	}

	/**
	 * Creates a List of all announcements.
	 *
	 * @return the List created
	 */
	private List<Message> constructAnnouncements() {
		// TODO: Parse from language file
		// Ex: We use[ Discord ]{CLICK.OPEN_URL:http://discord.easterlyn.com}{HOVER.SHOW_TEXT:Click here to go!}{PLAINTEXT:http://discord.easterlyn.com}for voice chat. Join today!

		List<Message> msgs = new ArrayList<>();
		ChannelManager manager = getPlugin().getModule(Chat.class).getChannelManager();
		MessageBuilder builder = new MessageBuilder(getPlugin()).setChannel(manager.getChannel("#"))
				.setSender(lang.getValue("rawannouncer.name"))
				.setNameHover(TextComponent.fromLegacyText(lang.getValue("rawannouncer.hover")))
				.setNameClick("/report ");

		List<TextComponent> components = new ArrayList<>();

		TextComponent component;
		TextComponent hover = new TextComponent("Click here to go!");
		hover.setColor(Language.getColor("link_color"));

		// Announcement: Try EnchantedFurnace
		component = new TextComponent("Wasting time smelting?");
		component.setColor(Language.getColor("bot_text"));
		components.add(component);

		component = new TextComponent(" Enchant a furnace");
		component.setColor(Language.getColor("link_color"));
		component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{hover.duplicate()}));
		component.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://dev.bukkit.org/projects/enchantedfurnace"));
		components.add(component);

		component = new TextComponent("!");
		component.setColor(Language.getColor("bot_text"));
		components.add(component);

		builder.setMessage("Wasting time smelting? https://dev.bukkit.org/projects/enchantedfurnace",
				components.toArray(new TextComponent[0]));
		msgs.add(builder.toMessage());

		// Announcement: Discord > all
		components.clear();
		component = new TextComponent("We use");
		component.setColor(Language.getColor("bot_text"));
		components.add(component);

		component = new TextComponent(" Discord ");
		component.setColor(Language.getColor("link_color"));
		hover.setText("Click to join Easterlyn's server!");
		component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{hover.duplicate()}));
		component.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "http://discord.easterlyn.com"));
		components.add(component);

		component = new TextComponent("for voice chat. Join today!");
		component.setColor(Language.getColor("bot_text"));
		components.add(component);

		builder.setMessage("We use http://discord.easterlyn.com for voice chat. Join today!",
				components.toArray(new TextComponent[0]));
		msgs.add(builder.toMessage());

		// Announcement: Protect your stuff
		components.clear();
		component = new TextComponent("Please");
		component.setColor(Language.getColor("bot_text"));
		components.add(component);

		component = new TextComponent(" claim your builds");
		component.setColor(Language.getColor("link_color"));
		hover.setText("Click to watch a video!");
		component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{hover.duplicate()}));
		component.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL,
				"https://www.youtube.com/watch?v=VDsjXB-BaE0&list=PL8YpI023Cthye5jUr-KGHGfczlNwgkdHM&index=1"));
		components.add(component);

		component = new TextComponent("! Proper protections greatly reduce staff strain.");
		component.setColor(Language.getColor("bot_text"));
		components.add(component);

		builder.setMessage("Please claim your builds! Proper protections greatly reduce staff strain.",
				components.toArray(new TextComponent[0]));
		msgs.add(builder.toMessage());

		// Announcement: Use /report ffs
		components.clear();
		component = new TextComponent("Found grief or a bug? Please");
		component.setColor(Language.getColor("bot_text"));
		components.add(component);

		component = new TextComponent(" /report ");
		component.setColor(Language.getColor("command"));
		hover.setText("Click to autofill!");
		component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{hover.duplicate()}));
		component.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/report "));
		components.add(component);

		component = new TextComponent("issues so we can help!");
		component.setColor(Language.getColor("bot_text"));
		components.add(component);

		builder.setMessage("Found grief or a bug? Please /report issues so we can help!",
				components.toArray(new TextComponent[0]));
		msgs.add(builder.toMessage());

		// Announcement: Protect your stuff redux
		components.clear();
		component = new TextComponent("Always use protection! If you can't");
		component.setColor(Language.getColor("bot_text"));
		components.add(component);

		component = new TextComponent(" claim ");
		component.setColor(Language.getColor("link_color"));
		hover.setText("Click to watch a video!");
		component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{hover.duplicate()}));
		component.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL,
				"https://www.youtube.com/watch?v=VDsjXB-BaE0&list=PL8YpI023Cthye5jUr-KGHGfczlNwgkdHM&index=1"));
		components.add(component);

		component = new TextComponent("an area, at least");
		component.setColor(Language.getColor("bot_text"));
		components.add(component);

		component = new TextComponent(" lock ");
		component.setColor(Language.getColor("command"));
		hover.setText("Click for info!");
		component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{hover.duplicate()}));
		component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/lwc"));
		components.add(component);

		component = new TextComponent("your chests!");
		component.setColor(Language.getColor("bot_text"));
		components.add(component);

		builder.setMessage("Always use protection! If you can't claim an area, at lease use /lwc!",
				components.toArray(new TextComponent[0]));
		msgs.add(builder.toMessage());

		return msgs;
	}

	public List<Message> getMessages() {
		return announcements;
	}

	@Override
	protected void onDisable() { }

	@Override
	public boolean isRequired() {
		return false;
	}

	@Override
	public String getName() {
		return "RawAnnouncer";
	}

}
