package edu.rosehulman.p2p.impl.connection;

import edu.rosehulman.p2p.protocol.IHost;



public class ConnectionEstablishedEvent {
	private IHost host;

	public ConnectionEstablishedEvent(IHost host2) {
		host = host2;
	}

	public IHost getHost() {
		return host;
	}
}
