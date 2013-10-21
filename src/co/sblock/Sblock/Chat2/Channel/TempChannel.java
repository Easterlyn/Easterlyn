package co.sblock.Sblock.Chat2.Channel;

import co.sblock.Sblock.Chat2.ChatUser;

public class TempChannel extends Channel {

	/**
	 * @param name
	 * @param a
	 * @param creator
	 */
	public TempChannel(String name, AccessLevel a, String creator) {
		super(name, a, creator);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see co.sblock.Sblock.Chat2.Channel.Channel#setNick(co.sblock.Sblock.Chat2.ChatUser, java.lang.String)
	 */
	@Override
	public void setNick(ChatUser sender, String nick) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see co.sblock.Sblock.Chat2.Channel.Channel#removeNick(co.sblock.Sblock.Chat2.ChatUser)
	 */
	@Override
	public void removeNick(ChatUser sender) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see co.sblock.Sblock.Chat2.Channel.Channel#getNick(co.sblock.Sblock.Chat2.ChatUser)
	 */
	@Override
	public String getNick(ChatUser sender) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see co.sblock.Sblock.Chat2.Channel.Channel#hasNick(co.sblock.Sblock.Chat2.ChatUser)
	 */
	@Override
	public boolean hasNick(ChatUser sender) {
		// TODO Auto-generated method stub
		return false;
	}

}
