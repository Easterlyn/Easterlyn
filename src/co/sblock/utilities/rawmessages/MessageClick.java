package co.sblock.utilities.rawmessages;

/**
 * Adds an effect when a Player clicks a MessageElement in chat.
 * 
 * @author Jikoo
 */
public class MessageClick {

	public enum ClickEffect {
		OPEN_URL,
		RUN_COMMAND,
		SUGGEST_COMMAND;
	}

	private ClickEffect effect;
	private String data;

	public MessageClick(ClickEffect effect, String data) {
		this.effect = effect;
		this.data = data;
	}

	public String getData() {
		return this.data;
	}

	public String toString() {
		return new StringBuilder(",\"clickEvent\":{\"action\":\"")
				.append(effect.name().toLowerCase()).append("\",\"value\":\"").append(data)
				.append("\"}").toString();
	}
}
