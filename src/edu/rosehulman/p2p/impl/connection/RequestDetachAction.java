package edu.rosehulman.p2p.impl.connection;

import edu.rosehulman.p2p.impl.IEventHandler;
import edu.rosehulman.p2p.impl.Packet;
import edu.rosehulman.p2p.protocol.IHost;
import edu.rosehulman.p2p.protocol.IP2PMediator;
import edu.rosehulman.p2p.protocol.IPacket;
import edu.rosehulman.p2p.protocol.IProtocol;


public class RequestDetachAction implements IEventHandler<RequestDetachAction> {
    private IHost host;

    public RequestDetachAction(IHost remoteHost) {
		host = remoteHost;
	}

	@Override
    public void handleEvent(IP2PMediator mediator, RequestDetachAction event) {
        synchronized (mediator) {
            IHost remoteHost = event.getHost();
            if (mediator.getIStreamMonitor(remoteHost) == null)
                return;

            IPacket sPacket = new Packet(IProtocol.PROTOCOL, IProtocol.DETACH, remoteHost.toString());
            sPacket.setHeader(IProtocol.HOST, mediator.getLocalhost().getHostAddress());
            sPacket.setHeader(IProtocol.PORT, mediator.getLocalhost().getPort() + "");

            mediator.getIStreamMonitor(remoteHost).send(sPacket);

            mediator.setDisConnectedHost(remoteHost);
            mediator.fireEvent(new ConnectionTerminatedEvent(remoteHost));
        }
    }





	private IHost getHost() {
		// TODO Auto-generated method stub
		return host;
	}
}
