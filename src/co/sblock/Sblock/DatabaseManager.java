package co.sblock.Sblock;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Collection of all database-related functions
 * 
 * @author Jikoo, FireNG
 * 
 */
public class DatabaseManager {

    private static final String
    	LOAD_PLAYER = "SELECT * FROM \"PlayerData\" WHERE \"playerName\" = ?;";
    private static DatabaseManager dbm;

    public static DatabaseManager getDatabaseManager() {
	if (dbm == null)
	    dbm = new DatabaseManager();
	return dbm;
    }

    private Connection database;

    public boolean enable() {
	Sblock plugin = Sblock.getInstance();
	try {
	    Class.forName("org.postgresql.Driver");
	    database = DriverManager.getConnection("jdbc:postgresql://"
		    + plugin.getConfig().getString("host") + ":"
		    + plugin.getConfig().getString("port") + "/"
		    + plugin.getConfig().getString("database"), plugin
		    .getConfig().getString("username"), plugin.getConfig()
		    .getString("password"));
	} catch (ClassNotFoundException e) {
	    // if we can't connect to the database, we're pretty much done here.
	    plugin.getLogger().severe("The database driver was not found. Plugin functionality will be limited.");
	    return false;
	} catch (SQLException e) {
	    plugin.getLogger().severe("An error occurred while connecting to the database. Plugin functionality will be limited.");
	    e.printStackTrace();
	    return false;
	}
	return true;
    }

    /**
     * Queries the database for an entry for the given player
     * @param playerName The player name to look for
     * @return a Map containing the player's data, or null if no player was found.
     */
    public Map<String, Object> loadPlayer(String playerName) {
	PreparedStatement statement = null;
	try {
            statement = database.prepareStatement(LOAD_PLAYER);
            statement.setString(1, playerName);
            Sblock.getInstance().getLogger().info("Statement executed: " + statement);
            ResultSet results = statement.executeQuery();
            if(results.next())
            {
        	Map<String, Object> returnMap = new HashMap<String, Object>();
        	for(int i = 1; i < results.getMetaData().getColumnCount() + 1; i++)
        	{
        	    Sblock.getInstance().getLogger().info(results.getMetaData().getColumnName(i));
        	    returnMap.put(results.getMetaData().getColumnName(i), results.getObject(i));
        	}
        	statement.close();
        	return returnMap;
            }
            else
        	return null;
	}
	catch(SQLException e) {
	    e.printStackTrace();
	    return null;
	}
    }
    

    public void disable() {
	try {
	    database.close();
	} catch (SQLException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	dbm = null;
	database = null;
    }
    
    
}
