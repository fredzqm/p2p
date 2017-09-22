package edu.rosehulman.p2p.impl.connection;

import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.rosehulman.p2p.impl.IEventHandler;
import edu.rosehulman.p2p.impl.Packet;
import edu.rosehulman.p2p.impl.StreamMonitor;
import edu.rosehulman.p2p.protocol.IHost;
import edu.rosehulman.p2p.protocol.IP2PMediator;
import edu.rosehulman.p2p.protocol.IPacket;
import edu.rosehulman.p2p.protocol.IProtocol;
import edu.rosehulman.p2p.protocol.IStreamMonitor;



public class RequestAttachAction implements IEventHandler<RequestAttachAction> {
	private IHost host;
	private boolean sucessfully;

	public RequestAttachAction(IHost remoteHost, boolean b) {
		host= remoteHost;
		sucessfully = b;
	}

	@Override
	public void handleEvent(IP2PMediator mediator, RequestAttachAction requestAttachEvent) {
		synchronized (mediator) {
			IHost remoteHost = requestAttachEvent.getHost();
			if (mediator.getIStreamMonitor(remoteHost) != null)
				return;

			IPacket sPacket = new Packet(IProtocol.PROTOCOL, IProtocol.ATTACH, remoteHost.toString());
			sPacket.setHeader(IProtocol.HOST, mediator.getLocalhost().getHostAddress());
			sPacket.setHeader(IProtocol.PORT, mediator.getLocalhost().getPort() + "");
			int seqNum = mediator.newSequenceNumber();
			sPacket.setHeader(IProtocol.SEQ_NUM, seqNum + "");

			try {
				mediator.logRequest(seqNum, sPacket);

				Socket socket = new Socket(remoteHost.getHostAddress(), remoteHost.getPort());
				sPacket.toStream(socket.getOutputStream());

				IPacket rPacket = new Packet();
				rPacket.fromStream(socket.getInputStream());
				if (rPacket.getCommand().equals(IProtocol.ATTACH_OK)) {
					// Connection accepted
					IStreamMonitor monitor = new StreamMonitor(mediator, remoteHost, socket);
					mediator.setConnected(remoteHost, monitor);

					// Let's start a thread for monitoring the input stream of this socket
					Thread runner = new Thread(monitor);
					runner.start();
				} else {
					// Connection rejected
					socket.close();
				}
			} catch (Exception e) {
				Logger.getGlobal().log(Level.SEVERE, "Could not establish connection!", e);
				mediator.completeRequest(seqNum);
				return;
			}
			mediator.completeRequest(seqNum);
			this.sucessfully = true;
		}
	}

	private IHost getHost() {
		// TODO Auto-generated method stub
		return this.host;
	}

	public boolean isSucessfully() {
		// TODO Auto-generated method stub
		return sucessfully;
	}
}
