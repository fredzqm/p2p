package edu.rosehulman.p2p.impl.notification;

import edu.rosehulman.p2p.protocol.IHost;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ConnectionTerminatedEvent {
	private IHost host;
}
