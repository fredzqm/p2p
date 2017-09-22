package edu.rosehulman.p2p.impl.logs;

import edu.rosehulman.p2p.protocol.IPacket;


public class ActivityEvent {
	private String message;
	private IPacket packet;
	public String getMessage() {
		// TODO Auto-generated method stub
		return message;
	}
	public IPacket getPacket() {
		// TODO Auto-generated method stub
		return packet;
	}
}