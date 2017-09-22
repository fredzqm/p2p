package edu.rosehulman.p2p.impl.list;

import java.util.List;

import edu.rosehulman.p2p.protocol.IHost;


public class ListUpdate {
	private IHost host;
	private List<String> listings;
	public ListUpdate(IHost remoteHost, List<String> listing) {
		// TODO Auto-generated constructor stub
		host = remoteHost;
		listings = listing;
	}
	public List<String> getListings() {
		// TODO Auto-generated method stub
		return listings;
	}
	public IHost getHost() {
		// TODO Auto-generated method stub
		return host;
	}
}
