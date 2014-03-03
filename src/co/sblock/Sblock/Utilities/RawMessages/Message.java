package co.sblock.Sblock.Utilities.RawMessages;

import java.util.LinkedList;

/**
 * Used to assemble a series of MessageElements into a valid JSON String for /tellraw.
 * 
 * @author Jikoo
 */
public class Message {

	private LinkedList<MessageElement> messageElements;

	public Message(MessageElement... elements) {
		messageElements = new LinkedList<MessageElement>();
		for (int i = 0; i < elements.length; i++) {
			messageElements.add(elements[i]);
		}
	}

	public void addElements(MessageElement... elements) {
		for (int i = 0; i < elements.length; i++) {
			messageElements.add(elements[i]);
		}
	}

	public String toString() {
		StringBuilder sb = new StringBuilder("{\"text\":\"\",\"extra\":[");
		for (int i = 0; i < messageElements.size(); i++) {
			sb.append(messageElements.get(i)).append(',');
		}
		if (messageElements.size() > 0) {
			sb.deleteCharAt(sb.length() - 1);
		}
		sb.append("]}");
		return sb.toString();
	}
}
