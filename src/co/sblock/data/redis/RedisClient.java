package co.sblock.data.redis;

import java.sql.Connection;
import java.util.UUID;

import org.bukkit.command.CommandSender;

import com.tmathmeyer.jadis.Jadis;
import com.tmathmeyer.jadis.async.Promise;

import co.sblock.chat.channel.Channel;
import co.sblock.chat.channel.Channel.ChannelSerialiser;
import co.sblock.data.BannedPlayers;
import co.sblock.data.SblockData;
import co.sblock.data.redis.promises.ChannelDataPromise;
import co.sblock.data.redis.promises.MachineDataPromise;
import co.sblock.data.redis.promises.PlayerDataPromise;
import co.sblock.machines.type.Machine;
import co.sblock.machines.type.Machine.MachineSerialiser;
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
	private final Promise<ChannelSerialiser> channelDataPromise = ChannelDataPromise.getCDP();
	private final Promise<MachineSerialiser> machineDataPromise = MachineDataPromise.getMDP();
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
	/**
	 * Helper method for SaveUserData(UUID userID)
	 * @param u the user to save
	 */
	public void saveUserData(User u) {
		connection.putMap("USERS", u.getUUID().toString(), u, exceptionLogger);
		connection.putMap("IPTABLE", u.getUserIP(), u, exceptionLogger);
	}

	@Override
	public void loadUserData(UUID userID) {
		connection.getFromMap("USERS", userID.toString(), playerDataPromise, User.class, exceptionLogger);
	}

	@Override
	public void startOfflineLookup(CommandSender sender, String name) {
		// TODO Auto-generated method stub
	}

	@Override
	public void deleteUser(UUID userID) {
		String[] uuids = {userID.toString()};
		connection.delMap("Users", exceptionLogger, uuids);
	}

	@Override
	public void saveChannelData(Channel c) {
		connection.putMap("CHANNELS", c.getName(), c.toSerialiser(), exceptionLogger);
	}

	@Override
	public void loadAllChannelData() {
		connection.getMap("CHANNELS", channelDataPromise, ChannelSerialiser.class, exceptionLogger);
	}

	@Override
	public void deleteChannel(String channelName) {
		connection.delMap("CHANNELS", exceptionLogger, channelName);
	}

	@Override
	public void saveMachine(Machine m) {
		connection.addSet("MACHINES", m.getSerialiser(), MachineSerialiser.class);
	}

	@Override
	public void deleteMachine(Machine m) {
		connection.remSet("MACHINES", exceptionLogger, m.getSerialiser());
	}

	@Override
	public void loadAllMachines() {
		connection.getSet("MACHINES", machineDataPromise, MachineSerialiser.class, exceptionLogger);
	}

	@Override
	public void getUserFromIP(String hostAddress, Promise<String> executor) {
		connection.getMap("IPTABLE", executor, String.class, exceptionLogger);
	}

	@Override
	public String getBanReason(String user, String ip) {
		return BannedPlayers.getBanReason(user, ip);
	}

	@Override
	public void addBan(User target, String reason) {
		BannedPlayers.addBan(target, reason);
	}

	@Override
	public void removeBan(String target) {
		BannedPlayers.deleteBans(target);
	}

	@Override
	public void enterFinalizeMode() {
		connection.setNonAsync();
	}

}
