package co.sblock.utilities.hologram;

import java.text.SimpleDateFormat;
import java.util.Date;
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
	}
	/* (non-Javadoc)
	 * @see com.dsh105.holoapi.api.DynamicTagFormat#match(Matcher, String, Hologram, Player)
	 */
	@Override
	public String match(Matcher matcher, String content, Hologram hologram, Player observer) {
		int last = 0;
		long current = System.currentTimeMillis();
		StringBuilder sb = new StringBuilder();
		while (matcher.find()) {
			sb.append(content.subSequence(last, matcher.start()));
			long time = Long.getLong(matcher.group().replaceAll("%entry:\\w{3,16}:(\\d+)%", ""));
			sb.append(format.format(new Date(time > current ? time - current : 0)));
			last = matcher.end();
		}
		sb.append(content.subSequence(last, content.length() - 1));
		return sb.toString();
	}

}
