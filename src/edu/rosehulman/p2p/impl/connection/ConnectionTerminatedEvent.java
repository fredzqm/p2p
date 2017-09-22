package edu.rosehulman.p2p.impl.connection;

import edu.rosehulman.p2p.protocol.IHost;



public class ConnectionTerminatedEvent {
	private IHost host;

	public ConnectionTerminatedEvent(IHost remoteHost) {
		host = remoteHost;
	}

	public IHost getHost() {
		// TODO Auto-generated method stub
		return host;
	}
}
