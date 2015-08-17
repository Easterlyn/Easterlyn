package co.sblock.events.session;

import co.sblock.events.Events;

/**
 * Changes Status synchronously to prevent concurrent modification.
 * 
 * @author Jikoo
 */
public class StatusSync implements Runnable {

	private final Status s;
	protected StatusSync(Status s) {
		this.s = s;
	}

	@Override
	public void run() {
		Events.getInstance().changeStatus(s);
	}

}
