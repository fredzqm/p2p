package edu.rosehulman.p2p.impl.find;

import edu.rosehulman.p2p.protocol.IHost;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FoundUpdate {
	private String fileName;
	private IHost foundAt;
}
