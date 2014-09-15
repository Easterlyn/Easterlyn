package co.sblock.data.sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Bukkit;

import co.sblock.Sblock;
import co.sblock.data.SblockData;

/**
 * A wrapper for making asynchronous SQL calls.
 * 
 * @author Jikoo
 */
public class AsyncCall {

	/** The PreparedStatement to query the database with. */
	private PreparedStatement pst;

	/** The Call being made. */
	private Call c;

	/** Boolean, true if Call has a callback to provide data from ResultSet. */
	private boolean callback = false;

	/**
	 * Constructor for AsyncCall.
	 * 
	 * @param pst the PreparedStatement to send
	 * @param c the Call being made
	 */
	protected AsyncCall(PreparedStatement pst, Call c) {
		this.pst = pst;
		this.c = c;
		if (c != null) {
			callback = true;
		}
	}

	/**
	 * Constructor for AsyncCall.
	 * 
	 * @param pst the PreparedStatement to send
	 */
	protected AsyncCall(PreparedStatement pst) {
		this.pst = pst;
	}

	/**
	 * Execute the PreparedStatement off the main thread.
	 * <p>
	 * if there is a ResultSet expected, trigger a CallBack for it.
	 */
	@SuppressWarnings("deprecation")
	protected void schedule() {
		Bukkit.getScheduler().scheduleAsyncDelayedTask(Sblock.getInstance(), new Runnable() {
			public void run() {
				try {
					if (callback) {
						if (Sblock.getInstance().isEnabled()) {
							Bukkit.getScheduler().scheduleSyncDelayedTask(Sblock.getInstance(),
									new CallBack(pst.executeQuery()));
						}
					} else {
						pst.executeUpdate();
					}
				} catch (SQLException e) {
					SblockData.getDB().getLogger().err(e);
				} finally {
					if (!callback) {
						try {
							pst.close();
						} catch (SQLException e) {
							SblockData.getDB().getLogger().err(e);
						}
					}
				}
			}
		});
	}

	/**
	 * Class used to trigger a ResultSet being returned to its caller.
	 */
	private class CallBack implements Runnable {
		private ResultSet rs;
		private CallBack(ResultSet rs) {
			this.rs = rs;
		}

		public void run() {
			c.result(rs);
		}
	}
}
