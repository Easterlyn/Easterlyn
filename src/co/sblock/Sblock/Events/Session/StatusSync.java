/**
 * 
 */
package co.sblock.Sblock.Events.Session;

import co.sblock.Sblock.Events.EventModule;

/**
 * Changes <code>Status</code> synchronously to prevent concurrent file
 * modification.
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
		EventModule.getEventModule().changeStatus(s);
	}

}
