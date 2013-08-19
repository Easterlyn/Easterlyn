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
import co.sblock.Sblock.Utilities.TowerData;

/**
 * @author Jikoo
 *
 */
public class EventModule extends Module {

	private static EventModule instance;
	private TowerData towers;
	private EventListener listener;

	@Override
	protected void onEnable() {
		instance = this;
		towers = new TowerData();
		towers.load();
		listener = new EventListener();
		// While Fire's code is cool, it doesn't seem to be registering our event priority.
		// TODO fix. For now, this workaround.
		Bukkit.getPluginManager().registerEvents(listener, Sblock.getInstance());
		PacketUtil.addPacketListener(Sblock.getInstance(), listener, PacketType.ENTITY_ACTION);
		PacketUtil.addPacketListener(Sblock.getInstance(), listener, PacketType.MOB_SPAWN);
		PacketUtil.addPacketListener(Sblock.getInstance(), listener, PacketType.DESTROY_ENTITY);
		this.registerCommands(new PacketCommands());
	}

	@Override
	protected void onDisable() {
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
