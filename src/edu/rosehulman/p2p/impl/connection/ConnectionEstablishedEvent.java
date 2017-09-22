package edu.rosehulman.p2p.impl.connection;

import edu.rosehulman.p2p.protocol.IHost;



public class ConnectionEstablishedEvent {
	private IHost host;

	public ConnectionEstablishedEvent(IHost host2) {
		// TODO Auto-generated constructor stub
		host = host2;
	}

	public IHost getHost() {
		// TODO Auto-generated method stub
		return host;
	}
}
