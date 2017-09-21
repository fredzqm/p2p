package edu.rosehulman.p2p.impl.handlers;

import java.io.File;
import java.io.InputStream;

import edu.rosehulman.p2p.impl.Host;
import edu.rosehulman.p2p.impl.Packet;
import edu.rosehulman.p2p.protocol.AbstractHandler;
import edu.rosehulman.p2p.protocol.IHost;
import edu.rosehulman.p2p.protocol.IP2PMediator;
import edu.rosehulman.p2p.protocol.IPacket;
import edu.rosehulman.p2p.protocol.IProtocol;
import edu.rosehulman.p2p.protocol.IRequestHandler;
import edu.rosehulman.p2p.protocol.IStreamMonitor;
import edu.rosehulman.p2p.protocol.P2PException;

public class GetRequestHandler extends AbstractHandler implements IRequestHandler {

	public GetRequestHandler(IP2PMediator mediator) {
		super(mediator);
	}

	@Override
	public void handle(IPacket packet, InputStream in) throws P2PException {
		try {
			int seqNum = Integer.parseInt(packet.getHeader(IProtocol.SEQ_NUM));

			String host = packet.getHeader(IProtocol.HOST);
			int port = Integer.parseInt(packet.getHeader(IProtocol.PORT));
			IHost remoteHost = new Host(host, port);

			String fileName = packet.getHeader(IProtocol.FILE_NAME);

			IStreamMonitor monitor = mediator.getIStreamMonitor(remoteHost);

			if (monitor == null) {
				throw new P2PException("No connection exists to " + remoteHost);
			}

			File fileObj = new File(mediator.getRootDirectory() + IProtocol.FILE_SEPERATOR + fileName);

			IPacket reqPacket = null;
			if (fileObj.exists() && fileObj.isFile()) {
				reqPacket = new Packet(IProtocol.PROTOCOL, IProtocol.PUT, remoteHost.toString());
				reqPacket.setHeader(IProtocol.HOST, mediator.getLocalhost().getHostAddress());
				reqPacket.setHeader(IProtocol.PORT, mediator.getLocalhost().getPort() + "");
				reqPacket.setHeader(IProtocol.SEQ_NUM, seqNum + "");
				reqPacket.setHeader(IProtocol.FILE_NAME, fileName);
				reqPacket.setHeader(IProtocol.PAYLOAD_SIZE, fileObj.length() + "");
			} else {
				reqPacket = new Packet(IProtocol.PROTOCOL, IProtocol.GET_NOK, remoteHost.toString());
				reqPacket.setHeader(IProtocol.HOST, mediator.getLocalhost().getHostAddress());
				reqPacket.setHeader(IProtocol.PORT, mediator.getLocalhost().getPort() + "");
				reqPacket.setHeader(IProtocol.SEQ_NUM, seqNum + "");
				reqPacket.setHeader(IProtocol.FILE_NAME, fileName);
			}

			reqPacket.toStream(monitor.getOutputStream());
			
		} catch (Exception e) {
			throw new P2PException(e);
		}
	}
}
