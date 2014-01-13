package co.sblock.Sblock.Database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Bukkit;

import co.sblock.Sblock.Sblock;
import co.sblock.Sblock.Utilities.Log;

/**
 * @author Jikoo
 *
 */
public class AsyncCall {
	private PreparedStatement pst;
	private Call c;
	private boolean callback = false;
	protected AsyncCall(PreparedStatement pst, Call c) {
		this.pst = pst;
		this.c = c;
		callback = true;
	}
	protected AsyncCall(PreparedStatement pst) {
		this.pst = pst;
	}

	@SuppressWarnings("deprecation")
	protected void schedule() {
		Bukkit.getScheduler().scheduleAsyncDelayedTask(Sblock.getInstance(), new Task());
	}

	private class Task implements Runnable {
		public void run() {
			try {
				if (callback) {
					Bukkit.getScheduler().scheduleSyncDelayedTask(Sblock.getInstance(),
							new CallBack(pst.executeQuery()));
				} else {
					pst.executeUpdate();
				}
			} catch (SQLException e) {
				Log.err(e);
			} finally {
				if (!callback) {
					try {
						pst.close();
					} catch (SQLException e) {
						Log.err(e);
					}
				}
			}
		}
	}

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
