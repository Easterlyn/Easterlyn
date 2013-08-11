/**
 * 
 */
package co.sblock.Sblock.Events;

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

	@Override
	protected void onEnable() {
		instance = this;
		towers = new TowerData();
		EventListener listener = new EventListener();
		this.registerEvents(listener);
		PacketUtil.addPacketListener(Sblock.getInstance(), listener, PacketType.ENTITY_ACTION);
	}

	@Override
	protected void onDisable() {
		towers.save(towers);
		towers = null;
		instance = null;
	}

	public TowerData getTowerData() {
		return towers;
	}

	public static EventModule getEventModule() {
		return instance;
	}
}
