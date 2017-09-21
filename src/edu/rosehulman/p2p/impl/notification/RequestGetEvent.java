package edu.rosehulman.p2p.impl.notification;

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
public class RequestGetEvent implements IEventHandler<RequestGetEvent> {
    private IHost remoteHost;
    private String fileName;

    @Override
    public void handleEvent(IP2PMediator mediator, RequestGetEvent requestGetEvent) {
        IHost remoteHost = requestGetEvent.getRemoteHost();
        String file = requestGetEvent.getFileName();
        IStreamMonitor monitor = mediator.getIStreamMonitor(remoteHost);

        if (monitor == null) {
            throw new P2PException("No connection exists to " + remoteHost);
        }

        int seqNum = mediator.newSequenceNumber();
        IPacket packet = new Packet(IProtocol.PROTOCOL, IProtocol.GET, remoteHost.toString());
        packet.setHeader(IProtocol.HOST, mediator.getLocalhost().getHostAddress());
        packet.setHeader(IProtocol.PORT, mediator.getLocalhost().getPort() + "");
        packet.setHeader(IProtocol.SEQ_NUM, seqNum + "");
        packet.setHeader(IProtocol.FILE_NAME, file);

        mediator.logRequest(seqNum, packet);
        packet.toStream(monitor.getOutputStream());
    }
}
