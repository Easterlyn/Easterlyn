package co.sblock.Sblock.Events.Session;

import co.sblock.Sblock.Events.SblockEvents;

/**
 * Changes Status synchronously to prevent concurrent file modification.
 * 
 * @author Jikoo
 */
public class StatusSync implements Runnable {

	private Status s;
	protected StatusSync(Status s) {
		this.s = s;
	}
	/**
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		SblockEvents.getEvents().changeStatus(s);
	}

}
