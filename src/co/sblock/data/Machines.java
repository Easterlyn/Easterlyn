package co.sblock.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import co.sblock.machines.MachineManager;
import co.sblock.machines.SblockMachines;
import co.sblock.machines.type.Machine;

/**
 * A small helper class containing all methods that access the Machines table.
 * <p>
 * The Machines table is created by the following call:
 * CREATE TABLE Machines (location varchar(255) UNIQUE KEY, type varchar(3), owner varchar(255), face tinyint), data varchar(255);
 * 
 * @author Jikoo
 */
public class Machines {

	/**
	 * Save Machine data to database.
	 * 
	 * @param m the Machine to save data for
	 */
	public static void saveMachine(Machine m) {
		PreparedStatement pst = null;
		try {
			pst = SblockData.getDB().connection().prepareStatement(Call.MACHINE_SAVE.toString());

			try {
				pst.setString(1, m.getLocationString());
				pst.setString(2, m.getType().getAbbreviation());
			} catch (NullPointerException e) {
				SblockData.getDB().getLogger().warning("A Machine appears to have invalid data, skipping save.");
				return;
			}
			pst.setString(3, m.getOwner());
			pst.setByte(4, m.getFacingDirection().getDirByte());
			pst.setString(5, m.getData());

			pst.executeUpdate();
		} catch (SQLException e) {
			SblockMachines.getMachines().getLogger().err(e);
		} finally {
			if (pst != null) {
				try {
					pst.close();
				} catch (Exception e) {
					SblockMachines.getMachines().getLogger().err(e);
				}
			}
		}
	}

	/**
	 * Create a PreparedStatement with which to query the SQL database. Delete a
	 * specified Machine's data from database.
	 * 
	 * @param m the Machine to delete data of
	 */
	public static void deleteMachine(Machine m) {
		try {
			PreparedStatement pst = SblockData.getDB().connection().prepareStatement(Call.MACHINE_DELETE.toString());
			pst.setString(1, m.getLocationString());

			new AsyncCall(pst).schedule();
		} catch (SQLException e) {
			SblockMachines.getMachines().getLogger().err(e);
		}
	}

	/**
	 * Creates and loads all Machines from saved data.
	 */
	public static void loadAllMachines() {
		PreparedStatement pst = null;
		try {
			pst = SblockData.getDB().connection().prepareStatement(Call.MACHINE_LOADALL.toString());

			ResultSet rs = pst.executeQuery();
			MachineManager mm = SblockMachines.getMachines().getManager();

			while (rs.next()) {
				mm.loadMachine(rs.getString("location"), rs.getString("type"),
						rs.getString("owner"), rs.getByte("face"), rs.getString("data"));
			}
		} catch (SQLException e) {
			SblockMachines.getMachines().getLogger().err(e);
		} finally {
			if (pst != null) {
				try {
					pst.close();
				} catch (SQLException e) {
					SblockMachines.getMachines().getLogger().err(e);
				}
			}
		}
	}
}
