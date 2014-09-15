package co.sblock.data;

import co.sblock.chat.ColorDef;
import co.sblock.utilities.Broadcast;

public class MockBroadcast extends Broadcast
{
	protected MockBroadcast() {
		INSTANCE = this;
	}
	
	/**
	 * Broadcast as Lil Hal to all users.
	 */
	public void lilHalImpl(String msg) {
		System.out.println(ColorDef.HAL + msg);
	}

	/**
	 * General broadcast to all users.
	 */
	public void generalImpl(String msg) {
		System.out.println(msg);
	}
}
