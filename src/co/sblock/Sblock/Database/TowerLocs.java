/**
 * 
 */
package co.sblock.Sblock.Database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import co.sblock.Sblock.Events.EventModule;
import co.sblock.Sblock.UserData.TowerData;
import co.sblock.Sblock.Utilities.Log;

/**
 * A small helper class containing all methods that access the TowerLocs table.
 * <p>
 * The TowerLocs table is created by the following call:
 * CREATE TABLE TowerLocs (towerID varchar(8) UNIQUE KEY, location varchar(15) UNIQUE KEY);
 * @author Jikoo
 *
 */
public class TowerLocs {
	/**
	 * Fills out <code>TowerData</code> from saved data.
	 */
	protected static void loadTowerData() {
		PreparedStatement pst = null;
		try {
			pst = DBManager.getDBM().connection().prepareStatement(Call.TOWER_LOAD.toString());

			ResultSet rs = pst.executeQuery();

			while (rs.next()) {
				String towerID = rs.getString("towerID");
				String location = rs.getString("location");
				if (towerID != null && location != null) {
					EventModule.getEventModule().getTowerData().add(towerID, location);
				}
			}
		} catch (SQLException e) {
			Log.err(e);
		} finally {
			if (pst != null) {
				try {
					pst.close();
				} catch (SQLException e) {
					Log.err(e);
				}
			}
		}
	}

	/**
	 * Save all <code>TowerData</code>.
	 * @param towers the <code>TowerData</code> to save
	 */
	protected static void saveTowerData(TowerData towers) {
		PreparedStatement pst = null;
		for (byte i = 0; i < 8; i++) {
			try {
				pst = DBManager.getDBM().connection().prepareStatement(Call.TOWER_SAVE.toString());

				pst.setString(1, "Derse" + i);
				pst.setString(2, towers.getLocString("Derse", i));

				pst.executeUpdate();
			} catch (SQLException e) {
				Log.err(e);
			} finally {
				if (pst != null) {
					try {
						pst.close();
					} catch (SQLException e) {
						Log.err(e);
					}
				}
			}
			try {
				pst = DBManager.getDBM().connection().prepareStatement(Call.TOWER_SAVE.toString());

				pst.setString(1, "Prospit" + i);
				pst.setString(2, towers.getLocString("Prospit", i));

				pst.executeUpdate();
			} catch (SQLException e) {
				Log.err(e);
			} finally {
				if (pst != null) {
					try {
						pst.close();
					} catch (SQLException e) {
						Log.err(e);
					}
				}
			}
		}
	}
}
