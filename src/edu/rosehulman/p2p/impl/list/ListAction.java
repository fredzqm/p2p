package edu.rosehulman.p2p.impl.list;

import edu.rosehulman.p2p.impl.IEventHandler;
import edu.rosehulman.p2p.impl.Packet;
import edu.rosehulman.p2p.protocol.IHost;
import edu.rosehulman.p2p.protocol.IP2PMediator;
import edu.rosehulman.p2p.protocol.IPacket;
import edu.rosehulman.p2p.protocol.IProtocol;
import edu.rosehulman.p2p.protocol.IStreamMonitor;
import edu.rosehulman.p2p.protocol.P2PException;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ListAction implements IEventHandler<ListAction> {
	private IHost host;

    @Override
    public void handleEvent(IP2PMediator mediator, ListAction requestListEvent) {
        IHost remoteHost = requestListEvent.getHost();
        IStreamMonitor monitor = mediator.getIStreamMonitor(remoteHost);

        if (monitor == null) {
            throw new P2PException("No connection exists to " + remoteHost);
        }

        int seqNum = mediator.newSequenceNumber();
        IPacket packet = new Packet(IProtocol.PROTOCOL, IProtocol.LIST, remoteHost.toString());
        packet.setHeader(IProtocol.HOST, mediator.getLocalhost().getHostAddress());
        packet.setHeader(IProtocol.PORT, mediator.getLocalhost().getPort() + "");
        packet.setHeader(IProtocol.SEQ_NUM, seqNum + "");

        mediator.logRequest(seqNum, packet);
        monitor.send(packet);
    }
}
