package edu.rosehulman.p2p.impl.notification;

import java.util.List;

import edu.rosehulman.p2p.protocol.IHost;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ListingEvent {
	private IHost host;
	private List<String> listings;
}
