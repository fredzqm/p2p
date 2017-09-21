package edu.rosehulman.p2p.impl.logs;

import java.util.Collection;

import edu.rosehulman.p2p.protocol.IPacket;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PacketsLogEvent {
	private Collection<IPacket> packates;
}
