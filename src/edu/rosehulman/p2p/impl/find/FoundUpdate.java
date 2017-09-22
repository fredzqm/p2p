package edu.rosehulman.p2p.impl.find;

import edu.rosehulman.p2p.protocol.IHost;



public class FoundUpdate {
	private String fileName;
	private IHost foundAt;
	public FoundUpdate(String fileName2, IHost foundAt2) {
		fileName= fileName2;
		foundAt= foundAt2;
	}
	public String getFileName() {
		// TODO Auto-generated method stub
		return fileName;
	}
	public IHost getFoundAt() {
		// TODO Auto-generated method stub
		return foundAt;
	}
}
