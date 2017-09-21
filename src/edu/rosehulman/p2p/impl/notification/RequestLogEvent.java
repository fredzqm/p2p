package edu.rosehulman.p2p.impl.notification;

import java.util.Collection;

import edu.rosehulman.p2p.protocol.IPacket;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RequestLogEvent {
	private Collection<IPacket> packates;
}
