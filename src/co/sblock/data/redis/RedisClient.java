package co.sblock.data.redis;

import java.sql.Connection;
import java.util.UUID;

import org.bukkit.command.CommandSender;

import com.tmathmeyer.jadis.Jadis;
import com.tmathmeyer.jadis.async.Promise;

import co.sblock.chat.channel.Channel;
import co.sblock.data.SblockData;
import co.sblock.data.redis.promises.PlayerDataPromise;
import co.sblock.machines.type.Machine;
import co.sblock.users.User;
import co.sblock.users.UserManager;
import co.sblock.utilities.Log;

/**
 * Database Implementation for redis
 * 
 * @author tmathmeyer
 */
public class RedisClient extends SblockData{

	private final Log logger = Log.getLog("SblockData - Redis");
	private Jadis connection;
	private final Promise<User> playerDataPromise = PlayerDataPromise.getPDP();
	private final ExceptionLogger exceptionLogger = ExceptionLogger.getEL();
	
	@Override
	public Log getLogger() {
		return logger;
	}

	@Override
	public boolean enable() {
		try {
			connection = Jadis.getJadis("localhost");
		} catch (Exception e) {
			return false;
		}
		return connection != null;
	}

	@Override
	public void disable() {
		connection = null;
	}

	@Override
	protected Connection connection() {
		// TODO this needs to go, but that requires centralising all database code (ie, out of those classes)
		return null;
	}

	@Override
	public void saveUserData(UUID userID) {
		User user = UserManager.getUser(userID);
		saveUserData(user);
	}
	
	public void saveUserData(User u) {
		connection.putMap("USERS", u.getUUID().toString(), u, exceptionLogger);
	}

	@Override
	public void loadUserData(UUID userID) {
		connection.getFromMap("USERS", userID.toString(), playerDataPromise, User.class);
	}

	@Override
	public void startOfflineLookup(CommandSender sender, String name) {
		// TODO Auto-generated method stub
	}

	@Override
	public void deleteUser(UUID userID) {
		String[] uuids = {userID.toString()};
		connection.delMap("Users", uuids);
	}

	@Override
	public void saveChannelData(Channel c) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void loadAllChannelData() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteChannel(String channelName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void saveMachine(Machine m) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteMachine(Machine m) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void loadAllMachines() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getUserFromIP(String hostAddress) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getBanReason(String user, String ip) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addBan(User target, String reason) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeBan(String target) {
		// TODO Auto-generated method stub
		
	}
	
}
