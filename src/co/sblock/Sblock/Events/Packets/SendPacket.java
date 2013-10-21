/**
 * 
 */
package co.sblock.Sblock.Events.Packets;

import java.lang.reflect.InvocationTargetException;

import org.bukkit.entity.Player;

import co.sblock.Sblock.Utilities.Sblogger;

import com.comphenix.protocol.ProtocolLibrary;

/**
 * Send a <code>Player</code> a packet.
 * <p>
 * Primarily designed for multi-packet scenarios where receive order is
 * critical.
 * 
 * @author Jikoo
 */
public class SendPacket implements Runnable {
	/** The <code>Player</code> to send a packet to. */
	private Player p;
	/** The <code>AbstractPacket</code> to send. */
	private AbstractPacket packet;
	
	/**
	 * @param p
	 *            the <code>Player</code> to send a packet to
	 * @param packet
	 *            the <code>AbstractPacket</code> to send
	 */
	public SendPacket(Player p, AbstractPacket packet) {
		this.p = p;
		this.packet = packet;
	}
	/**
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		try {
			ProtocolLibrary.getProtocolManager().sendServerPacket(p, packet.getHandle());
		} catch (InvocationTargetException e) {
			Sblogger.err(e);
		}
	}
}
