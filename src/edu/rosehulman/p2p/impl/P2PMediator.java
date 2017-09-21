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

package edu.rosehulman.p2p.impl;

import java.io.File;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.rosehulman.p2p.impl.notification.IActivityListener;
import edu.rosehulman.p2p.impl.notification.IConnectionListener;
import edu.rosehulman.p2p.impl.notification.IDownloadListener;
import edu.rosehulman.p2p.impl.notification.IFoundListener;
import edu.rosehulman.p2p.impl.notification.IListingListener;
import edu.rosehulman.p2p.impl.notification.IRequestLogListener;
import edu.rosehulman.p2p.protocol.IHost;
import edu.rosehulman.p2p.protocol.IP2PMediator;
import edu.rosehulman.p2p.protocol.IPacket;
import edu.rosehulman.p2p.protocol.IProtocol;
import edu.rosehulman.p2p.protocol.IStreamMonitor;
import edu.rosehulman.p2p.protocol.P2PException;

public class P2PMediator implements IP2PMediator {
	private Host localhost;
	private Map<IHost, IStreamMonitor> hostToInStreamMonitor;
	private Map<Integer, IPacket> requestLog;
	private String rootDirectory;
	private int sequence;

	List<IDownloadListener> downloadListeners;
	List<IListingListener> listingListeners;
	List<IRequestLogListener> requestLogListeners;
	List<IConnectionListener> connectionListeners;
	List<IActivityListener> activityListeners;
	List<IFoundListener> fireFoundListeners;


	public P2PMediator(int port, String rootDirectory) throws UnknownHostException {
		this.rootDirectory = rootDirectory;

		this.localhost = new Host(InetAddress.getLocalHost().getHostAddress(), port);
		this.hostToInStreamMonitor = Collections.synchronizedMap(new HashMap<IHost, IStreamMonitor>());

		this.requestLog = Collections.synchronizedMap(new HashMap<Integer, IPacket>());

		this.downloadListeners = Collections.synchronizedList(new ArrayList<IDownloadListener>());
		this.listingListeners = Collections.synchronizedList(new ArrayList<IListingListener>());
		this.requestLogListeners = Collections.synchronizedList(new ArrayList<IRequestLogListener>());
		this.connectionListeners = Collections.synchronizedList(new ArrayList<IConnectionListener>());
		this.activityListeners = Collections.synchronizedList(new ArrayList<IActivityListener>());
		this.fireFoundListeners = Collections.synchronizedList(new ArrayList<IFoundListener>());

		this.sequence = 0;
	}

	@Override
	public synchronized int newSequenceNumber() {
		return ++this.sequence;
	}

	@Override
	public IHost getLocalhost() {
		return localhost;
	}

	@Override
	public String getRootDirectory() {
		return this.rootDirectory;
	}

	@Override
	public IStreamMonitor getIStreamMonitor(IHost remoteHost) {
		return this.hostToInStreamMonitor.get(remoteHost);
	}

	@Override
	public Set<IHost> getPeerHosts() {
		return this.hostToInStreamMonitor.keySet();
	}

	@Override
	public void setDisConnectedHost(IHost remoteHost) {
		IStreamMonitor monitor = this.hostToInStreamMonitor.remove(remoteHost);
		Socket socket = monitor.getSocket();

		try {
			socket.close();
		} catch (Exception e) {
			Logger.getGlobal().log(Level.WARNING, "Error closing socket!", e);
		}
	}

	@Override
	public void setConnected(IHost host, IStreamMonitor monitor) {
		this.hostToInStreamMonitor.put(host, monitor);
		this.fireConnected(host);
	}

	@Override
	public boolean requestAttach(IHost remoteHost) throws P2PException {
		synchronized (this) {
			if (getIStreamMonitor(remoteHost) != null)
				return false;

			IPacket sPacket = new Packet(IProtocol.PROTOCOL, IProtocol.ATTACH, remoteHost.toString());
			sPacket.setHeader(IProtocol.HOST, this.getLocalhost().getHostAddress());
			sPacket.setHeader(IProtocol.PORT, this.getLocalhost().getPort() + "");
			int seqNum = this.newSequenceNumber();
			sPacket.setHeader(IProtocol.SEQ_NUM, seqNum + "");

			try {
				this.logRequest(seqNum, sPacket);

				Socket socket = new Socket(remoteHost.getHostAddress(), remoteHost.getPort());
				sPacket.toStream(socket.getOutputStream());

				IPacket rPacket = new Packet();
				rPacket.fromStream(socket.getInputStream());
				if (rPacket.getCommand().equals(IProtocol.ATTACH_OK)) {
					// Connection accepted
					IStreamMonitor monitor = new StreamMonitor(this, remoteHost, socket);
					this.setConnected(remoteHost, monitor);

					// Let's start a thread for monitoring the input stream of this socket
					Thread runner = new Thread(monitor);
					runner.start();
				} else {
					// Connection rejected
					socket.close();
				}
			} catch (Exception e) {
				Logger.getGlobal().log(Level.SEVERE, "Could not establish connection!", e);
				this.completeRequest(seqNum);
				return false;
			}
			this.completeRequest(seqNum);
			return true;
		}
	}

	public void requestAttachOK(IHost remoteHost, Socket socket, int seqNum) throws P2PException {
		IPacket sPacket = new Packet(IProtocol.PROTOCOL, IProtocol.ATTACH_OK, remoteHost.toString());
		sPacket.setHeader(IProtocol.HOST, this.getLocalhost().getHostAddress());
		sPacket.setHeader(IProtocol.PORT, this.getLocalhost().getPort() + "");
		sPacket.setHeader(IProtocol.SEQ_NUM, seqNum + "");

		try {
			sPacket.toStream(socket.getOutputStream());

			IStreamMonitor monitor = new StreamMonitor(this, remoteHost, socket);
			this.setConnected(remoteHost, monitor);

			// Let's start a thread for monitoring the input stream of this socket
			Thread runner = new Thread(monitor);
			runner.start();
		} catch (Exception e) {
			Logger.getGlobal().log(Level.SEVERE, "Could not send attach ok message to remote peer", e);
		}
	}

	public void requestAttachNOK(IHost remoteHost, Socket socket, int seqNum) throws P2PException {
		IPacket sPacket = new Packet(IProtocol.PROTOCOL, IProtocol.ATTACH_NOK, remoteHost.toString());
		sPacket.setHeader(IProtocol.HOST, this.getLocalhost().getHostAddress());
		sPacket.setHeader(IProtocol.PORT, this.getLocalhost().getPort() + "");
		sPacket.setHeader(IProtocol.SEQ_NUM, seqNum + "");

		try {
			sPacket.toStream(socket.getOutputStream());
			socket.close();
			Logger.getGlobal().log(Level.INFO, "Connection rejected to " + remoteHost);
		} catch (Exception e) {
			Logger.getGlobal().log(Level.SEVERE, "Could not send attach ok message to remote peer", e);
		}
	}

	@Override
	public void requestDetach(IHost remoteHost) throws P2PException {
		synchronized (this) {
			if (getIStreamMonitor(remoteHost) == null)
				return;

			IPacket sPacket = new Packet(IProtocol.PROTOCOL, IProtocol.DETACH, remoteHost.toString());
			sPacket.setHeader(IProtocol.HOST, this.getLocalhost().getHostAddress());
			sPacket.setHeader(IProtocol.PORT, this.getLocalhost().getPort() + "");

			sPacket.toStream(getIStreamMonitor(remoteHost).getOutputStream());
			setDisConnectedHost(remoteHost);
			this.fireDisconnected(remoteHost);
		}
	}
	
	@Override
	public void requestList(IHost remoteHost) throws P2PException {
		IStreamMonitor monitor = getIStreamMonitor(remoteHost);

		if (monitor == null) {
			throw new P2PException("No connection exists to " + remoteHost);
		}

		int seqNum = this.newSequenceNumber();
		IPacket packet = new Packet(IProtocol.PROTOCOL, IProtocol.LIST, remoteHost.toString());
		packet.setHeader(IProtocol.HOST, this.getLocalhost().getHostAddress());
		packet.setHeader(IProtocol.PORT, this.getLocalhost().getPort() + "");
		packet.setHeader(IProtocol.SEQ_NUM, seqNum + "");

		this.logRequest(seqNum, packet);
		packet.toStream(monitor.getOutputStream());
	}

	@Override
	public void requestListing(IHost remoteHost, int seqNum) throws P2PException {
		IStreamMonitor monitor = getIStreamMonitor(remoteHost);

		if (monitor == null) {
			throw new P2PException("No connection exists to " + remoteHost);
		}

		StringBuilder builder = new StringBuilder();
		File dir = new File(this.getRootDirectory());
		for (File f : dir.listFiles()) {
			if (f.isFile()) {
				builder.append(f.getName());
				builder.append(IProtocol.CRLF);
			}
		}

		try {
			byte[] payload = builder.toString().getBytes(IProtocol.CHAR_SET);

			IPacket packet = new Packet(IProtocol.PROTOCOL, IProtocol.LISTING, remoteHost.toString());
			packet.setHeader(IProtocol.HOST, this.getLocalhost().getHostAddress());
			packet.setHeader(IProtocol.PORT, this.getLocalhost().getPort() + "");
			packet.setHeader(IProtocol.SEQ_NUM, seqNum + "");
			packet.setHeader(IProtocol.PAYLOAD_SIZE, payload.length + "");

			OutputStream out = monitor.getOutputStream();
			packet.toStream(out);
			out.write(payload);
		} catch (Exception e) {
			throw new P2PException(e);
		}
	}

	@Override
	public void requestGet(IHost remoteHost, String file) throws P2PException {
		IStreamMonitor monitor = getIStreamMonitor(remoteHost);

		if (monitor == null) {
			throw new P2PException("No connection exists to " + remoteHost);
		}

		int seqNum = this.newSequenceNumber();
		IPacket packet = new Packet(IProtocol.PROTOCOL, IProtocol.GET, remoteHost.toString());
		packet.setHeader(IProtocol.HOST, this.getLocalhost().getHostAddress());
		packet.setHeader(IProtocol.PORT, this.getLocalhost().getPort() + "");
		packet.setHeader(IProtocol.SEQ_NUM, seqNum + "");
		packet.setHeader(IProtocol.FILE_NAME, file);

		this.logRequest(seqNum, packet);
		packet.toStream(monitor.getOutputStream());
	}

	@Override
	public void requestPut(IHost remoteHost, String file, int seqNum) throws P2PException {
		IStreamMonitor monitor = getIStreamMonitor(remoteHost);

		if (monitor == null) {
			throw new P2PException("No connection exists to " + remoteHost);
		}

		File fileObj = new File(this.getRootDirectory() + IProtocol.FILE_SEPERATOR + file);

		IPacket packet = null;
		if (fileObj.exists() && fileObj.isFile()) {
			packet = new Packet(IProtocol.PROTOCOL, IProtocol.PUT, remoteHost.toString());
			packet.setHeader(IProtocol.HOST, this.getLocalhost().getHostAddress());
			packet.setHeader(IProtocol.PORT, this.getLocalhost().getPort() + "");
			packet.setHeader(IProtocol.SEQ_NUM, seqNum + "");
			packet.setHeader(IProtocol.FILE_NAME, file);
			packet.setHeader(IProtocol.PAYLOAD_SIZE, fileObj.length() + "");
		} else {
			packet = new Packet(IProtocol.PROTOCOL, IProtocol.GET_NOK, remoteHost.toString());
			packet.setHeader(IProtocol.HOST, this.getLocalhost().getHostAddress());
			packet.setHeader(IProtocol.PORT, this.getLocalhost().getPort() + "");
			packet.setHeader(IProtocol.SEQ_NUM, seqNum + "");
			packet.setHeader(IProtocol.FILE_NAME, file);
		}

		packet.toStream(monitor.getOutputStream());
	}

	@Override
	public void discover(int depth) throws P2PException {

	}

	@Override
	public void find(String searchFile, int depth, String pathList) throws P2PException {
		for (IHost remoteHost : getPeerHosts()) {
			IStreamMonitor monitor = getIStreamMonitor(remoteHost);
			IPacket packet = new Packet(IProtocol.PROTOCOL, IProtocol.FIND, remoteHost.toString());
			packet.setHeader(IProtocol.DEPTH, "" + depth);
			packet.setHeader(IProtocol.TRACElIST, pathList + "|" + this.getLocalhost().toString());
			packet.setHeader(IProtocol.FILE_NAME, searchFile);
			packet.toStream(monitor.getOutputStream());
		}
	}

	@Override
	public void found(String fileName, IHost foundAt, String tracePath) throws P2PException {
		int index = tracePath.lastIndexOf('|');
		String prevTracePath = tracePath.substring(0, index);
		IHost remoteHost = new Host(tracePath.substring(index + 1));

		IStreamMonitor monitor = getIStreamMonitor(remoteHost);
		if (monitor == null) {
			throw new P2PException("No connection exists to " + remoteHost);
		}

		Packet packet = new Packet(IProtocol.PROTOCOL, IProtocol.FOUND, remoteHost.toString());
		packet.setHeader(IProtocol.TRACElIST, prevTracePath);
		packet.setHeader(IProtocol.FOUNDAT, foundAt.toString());
		packet.setHeader(IProtocol.FILE_NAME, fileName);
		packet.toStream(monitor.getOutputStream());
	}

	@Override
	public IPacket getRequest(int number) {
		return this.requestLog.get(number);
	}

	@Override
	public void logRequest(int number, IPacket p) {
		this.requestLog.put(number, p);
		this.fireRequestLogChanged();
	}

	@Override
	public void completeRequest(int number) {
		IPacket p = this.requestLog.remove(number);

		if (p != null)
			this.fireRequestLogChanged();
	}

	/**************************
	 * Listeners Related Code
	 *********************************/

	@Override
	public void addDownloadListener(IDownloadListener l) {
		this.downloadListeners.add(l);
	}

	@Override
	public void addListingListener(IListingListener l) {
		this.listingListeners.add(l);
	}

	@Override
	public void addRequestLogListener(IRequestLogListener l) {
		this.requestLogListeners.add(l);
	}

	@Override
	public void addConnectionListener(IConnectionListener l) {
		this.connectionListeners.add(l);
	}

	@Override
	public void addActivityListener(IActivityListener l) {
		this.activityListeners.add(l);
	}

	@Override
	public void addFoundListener(IFoundListener l) {
		this.fireFoundListeners.add(l);
	}

	@Override
	public void fireConnected(IHost host) {
		synchronized (this.connectionListeners) {
			for (IConnectionListener c : this.connectionListeners) {
				c.connectionEstablished(host);
			}
		}
	}

	@Override
	public void fireDisconnected(IHost host) {
		synchronized (this.connectionListeners) {
			for (IConnectionListener c : this.connectionListeners) {
				c.connectionTerminated(host);
			}
		}
	}

	@Override
	public void fireDownloadComplete(IHost host, String file) {
		synchronized (this.downloadListeners) {
			for (IDownloadListener d : this.downloadListeners) {
				d.downloadComplete(host, file);
			}
		}
	}

	@Override
	public void fireListingReceived(IHost host, List<String> listing) {
		synchronized (this.listingListeners) {
			for (IListingListener l : this.listingListeners) {
				l.listingReceived(host, listing);
			}
		}
	}

	@Override
	public void fireRequestLogChanged() {
		synchronized (this.requestLogListeners) {
			for (IRequestLogListener l : this.requestLogListeners) {
				l.requestLogChanged(Collections.unmodifiableCollection(this.requestLog.values()));
			}
		}
	}

	@Override
	public void fireActivityPerformed(String message, IPacket p) {
		synchronized (this.activityListeners) {
			for (IActivityListener l : this.activityListeners) {
				l.activityPerformed(message, p);
			}
		}
	}

	@Override
	public void fireFoundFile(String fileName, IHost foundAt) {
		synchronized (this.fireFoundListeners) {
			for (IFoundListener l : this.fireFoundListeners) {
				l.activityPerformed(fileName, foundAt);
			}
		}
	}
}
