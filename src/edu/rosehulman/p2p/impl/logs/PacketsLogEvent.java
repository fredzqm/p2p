package edu.rosehulman.p2p.impl.logs;

import java.util.Collection;

import edu.rosehulman.p2p.protocol.IPacket;



public class PacketsLogEvent {
	private Collection<IPacket> packates;

	public PacketsLogEvent(Collection<IPacket> unmodifiableCollection) {
		packates= unmodifiableCollection;
	}

	public Collection<IPacket> getPackates() {
		// TODO Auto-generated method stub
		return packates;
	}
}
