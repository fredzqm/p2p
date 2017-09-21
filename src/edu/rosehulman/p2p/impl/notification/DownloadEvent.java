package edu.rosehulman.p2p.impl.notification;

import edu.rosehulman.p2p.protocol.IHost;
import lombok.Data;

@Data
public class DownloadEvent {
	private IHost host;
	private String file;
}
