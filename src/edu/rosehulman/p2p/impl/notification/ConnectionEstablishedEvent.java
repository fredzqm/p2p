package edu.rosehulman.p2p.impl.notification;

import edu.rosehulman.p2p.protocol.IHost;
import lombok.Data;

@Data
public class ConnectionEstablishedEvent {
	private IHost host;
}
