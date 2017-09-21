package edu.rosehulman.p2p.impl.list;

import java.util.List;

import edu.rosehulman.p2p.protocol.IHost;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ListUpdate {
	private IHost host;
	private List<String> listings;
}
