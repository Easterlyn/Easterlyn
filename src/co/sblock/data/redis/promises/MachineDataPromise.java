package co.sblock.data.redis.promises;

import java.util.List;
import java.util.Map;
import java.util.Set;

import co.sblock.machines.type.Machine.MachineSerialiser;

import com.tmathmeyer.jadis.async.Promise;

public class MachineDataPromise implements Promise<MachineSerialiser> {

	private final static MachineDataPromise instance = new MachineDataPromise();

	private MachineDataPromise() { }

	public static MachineDataPromise getMDP() {
		return instance;
	}

	@Override
	public void getList(List<MachineSerialiser> listOfMachines) { }

	@Override
	public void getMap(Map<String, MachineSerialiser> mapOfMachines) { }

	@Override
	public void getObject(MachineSerialiser mach, String key) { }

	@Override
	public void getSet(Set<MachineSerialiser> arg0) {
		// TODO Auto-generated method stub
		
	}

}
