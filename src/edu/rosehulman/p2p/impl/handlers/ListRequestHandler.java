/*
 * The MIT License (MIT)
 * 
 * Copyright (c) 2015 Chandan R. Rupakheti (chandan.rupakheti@rose-hulman.edu)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package edu.rosehulman.p2p.impl.handlers;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

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

/**
 * @author rupakhet
 *
 */
public class ListRequestHandler extends AbstractHandler implements IRequestHandler {

	public ListRequestHandler(IP2PMediator mediator) {
		super(mediator);
	}

	@Override
	public void handle(IPacket packet, InputStream in) throws P2PException {
		int seqNum = Integer.parseInt(packet.getHeader(IProtocol.SEQ_NUM));
		String host = packet.getHeader(IProtocol.HOST);
		int port = Integer.parseInt(packet.getHeader(IProtocol.PORT));
		IHost remoteHost = new Host(host, port);
		
		IStreamMonitor monitor = mediator.getIStreamMonitor(remoteHost);

		if (monitor == null) {
			throw new P2PException("No connection exists to " + remoteHost);
		}

		StringBuilder builder = new StringBuilder();
		File dir = new File(mediator.getRootDirectory());
		for (File f : dir.listFiles()) {
			if (f.isFile()) {
				builder.append(f.getName());
				builder.append(IProtocol.CRLF);
			}
		}

		try {
			byte[] payload = builder.toString().getBytes(IProtocol.CHAR_SET);

			IPacket reqPacket = new Packet(IProtocol.PROTOCOL, IProtocol.LISTING, remoteHost.toString());
			reqPacket.setHeader(IProtocol.HOST, mediator.getLocalhost().getHostAddress());
			reqPacket.setHeader(IProtocol.PORT, mediator.getLocalhost().getPort() + "");
			reqPacket.setHeader(IProtocol.SEQ_NUM, seqNum + "");
			reqPacket.setHeader(IProtocol.PAYLOAD_SIZE, payload.length + "");

			OutputStream out = monitor.getOutputStream();
			reqPacket.toStream(out);
			out.write(payload);
		} catch (Exception e) {
			throw new P2PException(e);
		}
	}
}
