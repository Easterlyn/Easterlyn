package co.sblock.chat.channel;

/**
 * 
 * @author tmathmeyer
 */
public class ChannelCreationException extends Exception{
	private static final long serialVersionUID = -542162503482441390L;
	private final String message;
	
	/**
	 * basic constructor
	 * @param msg the message to go along with the exception
	 */
	public ChannelCreationException(String msg) {
		message = msg;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}
}
