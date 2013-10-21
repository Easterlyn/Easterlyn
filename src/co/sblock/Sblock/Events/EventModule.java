/**
 * 
 */
package co.sblock.Sblock.Events;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;

import com.bergerkiller.bukkit.common.protocol.PacketType;
import com.bergerkiller.bukkit.common.utils.PacketUtil;

import co.sblock.Sblock.Module;
import co.sblock.Sblock.Sblock;
import co.sblock.Sblock.UserData.TowerData;

/**
 * @author Jikoo
 *
 */
public class EventModule extends Module {

	private static EventModule instance;
	private TowerData towers;
	private EventListener listener;
	private int regionTask;

	@Override
	protected void onEnable() {
		instance = this;
		towers = new TowerData();
		towers.load();
		listener = new EventListener();
		this.registerEvents(listener);
		PacketUtil.addPacketListener(Sblock.getInstance(), listener, PacketType.ENTITY_ACTION);
		PacketUtil.addPacketListener(Sblock.getInstance(), listener, PacketType.MOB_SPAWN);
		PacketUtil.addPacketListener(Sblock.getInstance(), listener, PacketType.DESTROY_ENTITY);
		this.registerCommands(new PacketCommands());
		regionTask = listener.initiateRegionChecks();
	}

	@Override
	protected void onDisable() {
		Bukkit.getScheduler().cancelTask(regionTask);
		HandlerList.unregisterAll(listener);
		towers.save(towers);
		towers = null;
		instance = null;
	}

	public TowerData getTowerData() {
		return towers;
	}

	public EventListener getListener() {
		return this.listener;
	}

	public static EventModule getEventModule() {
		return instance;
	}
}
