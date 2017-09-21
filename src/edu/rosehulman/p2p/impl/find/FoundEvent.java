package edu.rosehulman.p2p.impl.find;

import edu.rosehulman.p2p.impl.Host;
import edu.rosehulman.p2p.impl.IEventHandler;
import edu.rosehulman.p2p.impl.Packet;
import edu.rosehulman.p2p.protocol.IHost;
import edu.rosehulman.p2p.protocol.IP2PMediator;
import edu.rosehulman.p2p.protocol.IProtocol;
import edu.rosehulman.p2p.protocol.IStreamMonitor;
import edu.rosehulman.p2p.protocol.P2PException;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FoundEvent implements IEventHandler<FoundEvent> {
    private String fileName;
    private IHost foundAt;
    private String traceList;

    @Override
    public void handleEvent(IP2PMediator mediator, FoundEvent event) {
        assert this == event;

        int index = traceList.lastIndexOf('|');
        String prevTracePath = traceList.substring(0, index);
        IHost remoteHost = new Host(traceList.substring(index + 1));

        IStreamMonitor monitor = mediator.getIStreamMonitor(remoteHost);
        if (monitor == null) {
            throw new P2PException("No connection exists to " + remoteHost);
        }

        Packet packet = new Packet(IProtocol.PROTOCOL, IProtocol.FOUND, remoteHost.toString());
        packet.setHeader(IProtocol.TRACElIST, prevTracePath);
        packet.setHeader(IProtocol.FOUNDAT, foundAt.toString());
        packet.setHeader(IProtocol.FILE_NAME, fileName);
        packet.toStream(monitor.getOutputStream());
    }
}
