package edu.rosehulman.p2p.impl.notification;

import java.net.Socket;

import edu.rosehulman.p2p.protocol.IHost;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RequestAttachOKEvent {
	private IHost remote;
	private Socket socket;
	private int sequenceNum;
}
