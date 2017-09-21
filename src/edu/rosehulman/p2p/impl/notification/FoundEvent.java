package edu.rosehulman.p2p.impl.notification;

import edu.rosehulman.p2p.protocol.IHost;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FoundEvent {
	private String fileName;
	private IHost foundAt;
}
