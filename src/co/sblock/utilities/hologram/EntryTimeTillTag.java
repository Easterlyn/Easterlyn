package co.sblock.utilities.hologram;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;

import org.bukkit.entity.Player;

import com.dsh105.holoapi.api.DynamicTagFormat;
import com.dsh105.holoapi.api.Hologram;

/**
 * A dynamically updating tag displaying the time remaining until Entry.
 * 
 * @author Jikoo
 */
public class EntryTimeTillTag extends DynamicTagFormat {

	private SimpleDateFormat format;

	public EntryTimeTillTag() {
		format = new SimpleDateFormat("m:ss");
		format.setTimeZone(TimeZone.getTimeZone("UTC"));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dsh105.holoapi.api.DynamicTagFormat#match(Matcher, String, Hologram, Player)
	 */
	@Override
	public String match(Matcher matcher, String content, Hologram hologram, Player observer) {
		long current = System.currentTimeMillis();
		long time = Long.parseLong(matcher.group(1));
		return format.format(new Date(time > current ? time - current : 0));
	}

}
