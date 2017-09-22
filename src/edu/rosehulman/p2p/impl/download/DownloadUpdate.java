package edu.rosehulman.p2p.impl.download;

import edu.rosehulman.p2p.protocol.IHost;


public class DownloadUpdate {
	private IHost host;
	private String file;
	public DownloadUpdate(IHost remoteHost, String fileName) {
		host= remoteHost;
		file= fileName;
	}
	public String getFile() {
		// TODO Auto-generated method stub
		return file;
	}
	public IHost getHost() {
		// TODO Auto-generated method stub
		return host;
	}
}
