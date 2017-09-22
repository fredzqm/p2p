package edu.rosehulman.p2p.impl.find;

import edu.rosehulman.p2p.impl.IEventHandler;
import edu.rosehulman.p2p.impl.Packet;
import edu.rosehulman.p2p.protocol.IHost;
import edu.rosehulman.p2p.protocol.IP2PMediator;
import edu.rosehulman.p2p.protocol.IPacket;
import edu.rosehulman.p2p.protocol.IProtocol;
import edu.rosehulman.p2p.protocol.IStreamMonitor;



public class FindAction implements IEventHandler<FindAction> {
	private String fileName;
	private int depth;
	private String traceList;

	public FindAction(String fileName2, Integer depth2, String string) {
		fileName=fileName2;
		depth = depth2;
		traceList = string;
	}

	@Override
	public void handleEvent(IP2PMediator mediator, FindAction findAction) {
		String traceList = findAction.getTraceList();
		String fileName = findAction.getFileName();
		int depth = findAction.getDepth();

		for (IHost remoteHost : mediator.getPeerHosts()) {
			IStreamMonitor monitor = mediator.getIStreamMonitor(remoteHost);
			IPacket packet = new Packet(IProtocol.PROTOCOL, IProtocol.FIND, remoteHost.toString());
			packet.setHeader(IProtocol.DEPTH, "" + depth);
			packet.setHeader(IProtocol.TRACElIST, traceList + "|" + mediator.getLocalhost().toString());
			packet.setHeader(IProtocol.FILE_NAME, fileName);
			monitor.send(packet);
		}
	}

	private int getDepth() {
		// TODO Auto-generated method stub
		return depth;
	}

	private String getFileName() {
		// TODO Auto-generated method stub
		return fileName;
	}

	private String getTraceList() {
		// TODO Auto-generated method stub
		return traceList;
	}
}
