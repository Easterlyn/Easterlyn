package co.sblock.Sblock.Utilities.RawMessages;

/**
 * Adds an effect when a Player clicks a MessageElement in chat.
 * 
 * @author Jikoo
 */
public class MessageClickEffect {
	public enum ClickEffect {
		OPEN_URL, RUN_COMMAND, SUGGEST_COMMAND;
	}

	private ClickEffect effect;
	private String data;

	public MessageClickEffect(ClickEffect effect, String data) {
		this.effect = effect;
		this.data = data;
	}

	public String toString() {
		return new StringBuilder(",\"clickEvent\":{\"action\":\"").append(effect.name().toLowerCase())
				.append("\",\"value\":\"").append(data).append("\"}").toString();
	}
}
