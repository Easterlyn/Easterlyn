package co.sblock.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import co.sblock.events.SblockEvents;
import co.sblock.users.TowerData;

/**
 * A small helper class containing all methods that access the TowerLocs table.
 * <p>
 * The TowerLocs table is created by the following call:
 * CREATE TABLE TowerLocs (towerID varchar(8) UNIQUE KEY, location varchar(255) UNIQUE KEY);
 * 
 * @author Jikoo
 */
public class TowerLocs {

	/**
	 * Fills out TowerData from saved data.
	 */
	protected static void loadTowerData() {
		PreparedStatement pst = null;
		try {
			pst = SblockData.getDB().connection().prepareStatement(Call.TOWER_LOAD.toString());

			ResultSet rs = pst.executeQuery();

			while (rs.next()) {
				String towerID = rs.getString("towerID");
				String location = rs.getString("location");
				if (towerID != null && location != null) {
					SblockEvents.getEvents().getTowerData().add(towerID, location);
				}
			}
		} catch (SQLException e) {
			SblockData.getLogger().err(e);
		} finally {
			if (pst != null) {
				try {
					pst.close();
				} catch (SQLException e) {
					SblockData.getLogger().err(e);
				}
			}
		}
	}

	/**
	 * Save all TowerData.
	 * 
	 * @param towers the TowerData to save
	 */
	protected static void saveTowerData(TowerData towers) {
		PreparedStatement pst = null;
		for (byte i = 0; i < 8; i++) {
			try {
				pst = SblockData.getDB().connection().prepareStatement(Call.TOWER_SAVE.toString());

				pst.setString(1, "Derse" + i);
				pst.setString(2, towers.getLocString("Derse", i));

				pst.executeUpdate();
			} catch (SQLException e) {
				SblockData.getLogger().err(e);
			} finally {
				if (pst != null) {
					try {
						pst.close();
					} catch (SQLException e) {
						SblockData.getLogger().err(e);
					}
				}
			}
			try {
				pst = SblockData.getDB().connection().prepareStatement(Call.TOWER_SAVE.toString());

				pst.setString(1, "Prospit" + i);
				pst.setString(2, towers.getLocString("Prospit", i));

				pst.executeUpdate();
			} catch (SQLException e) {
				SblockData.getLogger().err(e);
			} finally {
				if (pst != null) {
					try {
						pst.close();
					} catch (SQLException e) {
						SblockData.getLogger().err(e);
					}
				}
			}
		}
	}
}
