package edu.rosehulman.p2p.impl.logs;

import edu.rosehulman.p2p.protocol.IPacket;
import lombok.Data;

@Data
public class ActivityEvent {
	private String message;
	private IPacket packet;
}