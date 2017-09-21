package edu.rosehulman.p2p.impl.download;

import edu.rosehulman.p2p.protocol.IHost;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DownloadUpdate {
	private IHost host;
	private String file;
}
