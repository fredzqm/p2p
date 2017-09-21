package edu.rosehulman.p2p.impl;

import edu.rosehulman.p2p.protocol.IP2PMediator;

public interface IEventHandler<T> {
	
	void handleEvent(IP2PMediator mediator, T event);
}
