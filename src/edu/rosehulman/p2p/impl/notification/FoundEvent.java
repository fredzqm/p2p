package edu.rosehulman.p2p.impl.notification;

import edu.rosehulman.p2p.protocol.IHost;
import lombok.Data;

@Data
public class FoundEvent {
	private String fileName;
	private IHost foundAt;
}
