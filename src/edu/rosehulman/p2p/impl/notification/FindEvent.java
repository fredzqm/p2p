package edu.rosehulman.p2p.impl.notification;

import edu.rosehulman.p2p.impl.IEventHandler;
import edu.rosehulman.p2p.impl.Packet;
import edu.rosehulman.p2p.protocol.IHost;
import edu.rosehulman.p2p.protocol.IP2PMediator;
import edu.rosehulman.p2p.protocol.IPacket;
import edu.rosehulman.p2p.protocol.IProtocol;
import edu.rosehulman.p2p.protocol.IStreamMonitor;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FindEvent implements IEventHandler<FindEvent> {
	private String fileName;
	private int depth;
	private String traceList;

	@Override
	public void handleEvent(IP2PMediator mediator, FindEvent findEvent) {
		String traceList = findEvent.getTraceList();
		String fileName = findEvent.getFileName();
		int depth = findEvent.getDepth();

		for (IHost remoteHost : mediator.getPeerHosts()) {
			IStreamMonitor monitor = mediator.getIStreamMonitor(remoteHost);
			IPacket packet = new Packet(IProtocol.PROTOCOL, IProtocol.FIND, remoteHost.toString());
			packet.setHeader(IProtocol.DEPTH, "" + depth);
			packet.setHeader(IProtocol.TRACElIST, traceList + "|" + mediator.getLocalhost().toString());
			packet.setHeader(IProtocol.FILE_NAME, fileName);
			packet.toStream(monitor.getOutputStream());
		}
	}
}
