package edu.rosehulman.p2p.impl.notification;

import java.util.Collection;

import edu.rosehulman.p2p.protocol.IPacket;
import lombok.Data;

@Data
public class RequestLogEvent {
	private Collection<IPacket> packates;
}
