package co.sblock.data.redis.promises;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.NotImplementedException;

import co.sblock.chat.ChannelManager;
import co.sblock.chat.channel.Channel;
import co.sblock.chat.channel.Channel.ChannelSerialiser;

import com.tmathmeyer.jadis.async.Promise;

public class ChannelDataPromise implements Promise<ChannelSerialiser> {

	private final static ChannelDataPromise instance = new ChannelDataPromise();

	private ChannelDataPromise() { }

	public static ChannelDataPromise getCDP() {
		return instance;
	}

	@Override
	public void getList(List<ChannelSerialiser> listOfUsers) {
		throw new NotImplementedException();
	}

	@Override
	public void getMap(Map<String, ChannelSerialiser> mapOfChannels) {
		ChannelManager cm = ChannelManager.getChannelManager();
		for(Entry<String, ChannelSerialiser> es : mapOfChannels.entrySet()) {
			Channel c = es.getValue().build();
			cm.loadChannel(c.getName(), c);
		}
	}

	@Override
	public void getObject(ChannelSerialiser cs, String key) {
		Channel c = cs.build();
		ChannelManager.getChannelManager().loadChannel(c.getName(), c);
	}

}
