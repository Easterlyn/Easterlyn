/*
 * PacketWrapper - Contains wrappers for each packet in Minecraft.
 * Copyright (C) 2012 Kristian S. Stangeland
 * Modified 12/24/13 by A. Gunn aka Jikoo - updated to remove deprecation.
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version 2 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 */

package co.sblock.Sblock.Events.Packets;

import com.comphenix.protocol.PacketType;
// import com.comphenix.protocol.Packets; // Jikoo
import com.comphenix.protocol.events.PacketContainer;

public abstract class AbstractPacket {
	/** The packet to modify. */
	protected PacketContainer handle;

	/**
	 * Constructs a new strongly typed wrapper for the given packet.
	 * 
	 * @param handle
	 *            handle to the raw packet data.
	 * @param packetID int
	 */
	protected AbstractPacket(PacketContainer handle, PacketType p) { // Jikoo - int packetID -> PacketType p
		// Make sure we're given a valid packet
		if (handle == null)
			throw new IllegalArgumentException("Packet handle cannot be NULL.");
		// Jikoo start
		if (!handle.getType().equals(p))
			throw new IllegalArgumentException(handle.getHandle()
					+ " is not a packet " + p.getPacketClass().getName());
		// Jikoo end

		this.handle = handle;
	}

	/**
	 * Retrieve a handle to the raw packet data.
	 * 
	 * @return Raw packet data.
	 */
	public PacketContainer getHandle() {
		return handle;
	}
}
