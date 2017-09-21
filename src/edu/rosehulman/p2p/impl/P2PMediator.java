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

import edu.rosehulman.p2p.impl.notification.ConnectionEstablishedEvent;
import edu.rosehulman.p2p.impl.notification.RequestLogEvent;
import edu.rosehulman.p2p.protocol.IHost;
import edu.rosehulman.p2p.protocol.IP2PMediator;
import edu.rosehulman.p2p.protocol.IPacket;
import edu.rosehulman.p2p.protocol.IStreamMonitor;

import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class P2PMediator implements IP2PMediator {
    private Host localhost;
    private Map<IHost, IStreamMonitor> hostToInStreamMonitor;
    private Map<Integer, IPacket> requestLog;
    private String rootDirectory;
    private int sequence;
    private Map<Class<?>, Collection<Object>> eventHandlerRegistry;

    public P2PMediator(int port, String rootDirectory) throws UnknownHostException {
        this.rootDirectory = rootDirectory;
        this.localhost = new Host(InetAddress.getLocalHost().getHostAddress(), port);
        this.hostToInStreamMonitor = Collections.synchronizedMap(new HashMap<IHost, IStreamMonitor>());
        this.requestLog = Collections.synchronizedMap(new HashMap<Integer, IPacket>());
        this.sequence = 0;

        this.eventHandlerRegistry = new HashMap<>();
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
        this.fireEvent(new ConnectionEstablishedEvent(host));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T> void registerEventHandler(Class<T> eventType, IEventHandler<T> hanlder) {
        if (!this.eventHandlerRegistry.containsKey(eventType))
            this.eventHandlerRegistry.put(eventType, new HashSet());
        this.eventHandlerRegistry.get(eventType).add(hanlder);
    }

    @SuppressWarnings("unchecked")
    public <T> void fireEvent(T event) {
        Class<?> eventType = event.getClass();
        if (this.eventHandlerRegistry.containsKey(eventType)) {
            for (Object obj : this.eventHandlerRegistry.get(eventType)) {
                IEventHandler<T> eventHandler = (IEventHandler<T>) obj;
                eventHandler.handleEvent(this, event);
            }
        }
        if (event instanceof IEventHandler) {
            IEventHandler<T> handler = (IEventHandler<T>) event;
            handler.handleEvent(this, event);
        }
    }

    @Override
    public IPacket getRequest(int number) {
        return this.requestLog.get(number);
    }

    @Override
    public void logRequest(int number, IPacket p) {
        this.requestLog.put(number, p);
        fireRequestLogChanged();
    }

    @Override
    public void completeRequest(int number) {
        IPacket p = this.requestLog.remove(number);

        if (p != null)
            this.fireRequestLogChanged();
    }

    private void fireRequestLogChanged() {
        this.fireEvent(new RequestLogEvent(Collections.unmodifiableCollection(this.requestLog.values())));
    }
}
