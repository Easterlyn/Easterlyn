package co.sblock.Sblock.Database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import co.sblock.Sblock.Machines.MachineManager;
import co.sblock.Sblock.Machines.MachineModule;
import co.sblock.Sblock.Machines.Type.Machine;
import co.sblock.Sblock.Utilities.Sblogger;

/**
 * A small helper class containing all methods that access the Machines table.
 * <p>
 * The Machines table is created by the following call:
 * CREATE TABLE Machines (location varchar(34) UNIQUE KEY, type varchar(3), data varchar(16), face tinyint);
 * 
 * @author Jikoo
 *
 */
public class Machines {

	/**
	 * Save <code>Machine</code> data to database.
	 * 
	 * @param m
	 *            the <code>Machine</code> to save data for
	 */
	public static void saveMachine(Machine m) {
		PreparedStatement pst = null;
		try {
			pst = DBManager.getDBM().connection().prepareStatement(Call.MACHINE_SAVE.toString());

			pst.setString(1, m.getLocationString());
			pst.setString(2, m.getType().getAbbreviation());
			pst.setString(3, m.getData());
			pst.setByte(4, m.getFacingDirection().getDirByte());

			pst.executeUpdate();
		} catch (SQLException e) {
			Sblogger.err(e);
		} finally {
			if (pst != null) {
				try {
					pst.close();
				} catch (SQLException e) {
					Sblogger.err(e);
				}
			}
		}
	}

	/**
	 * Create a <code>PreparedStatement</code> with which to query the SQL database.
	 * Delete a specified <code>Machine</code>'s data from database.
	 * 
	 * @param m
	 *            the <code>Machine</code> to delete data of
	 */
	public static void deleteMachine(Machine m) {
		try {
			PreparedStatement pst = DBManager.getDBM().connection().prepareStatement(Call.MACHINE_DELETE.toString());
			pst.setString(1, m.getLocationString());

			new AsyncCall(pst).schedule();
		} catch (SQLException e) {
			Sblogger.err(e);
		}
	}

	/**
	 * Creates and loads all <code>Machine</code>s from saved data.
	 */
	public static void loadAllMachines() {
		PreparedStatement pst = null;
		try {
			pst = DBManager.getDBM().connection().prepareStatement(Call.MACHINE_LOADALL.toString());

			ResultSet rs = pst.executeQuery();
			MachineManager mm = MachineModule.getInstance().getManager();

			while (rs.next()) {
				mm.loadMachine(rs.getString("location"), rs.getString("type"), rs.getString("data"), rs.getByte("face"));
			}
		} catch (SQLException e) {
			Sblogger.err(e);
		} finally {
			if (pst != null) {
				try {
					pst.close();
				} catch (SQLException e) {
					Sblogger.err(e);
				}
			}
		}
	}
}
