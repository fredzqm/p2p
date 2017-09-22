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

package edu.rosehulman.p2p.app;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collection;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

import edu.rosehulman.p2p.app.panel.NetworkPanel;
import edu.rosehulman.p2p.app.panel.RemoteConnectionPanel;
import edu.rosehulman.p2p.app.panel.SearchPanel;
import edu.rosehulman.p2p.app.panel.StatusPanel;
import edu.rosehulman.p2p.protocol.IConnectionMonitor;
import edu.rosehulman.p2p.protocol.IHost;
import edu.rosehulman.p2p.protocol.IP2PMediator;
import edu.rosehulman.p2p.protocol.IPacket;
import edu.rosehulman.p2p.protocol.IProtocol;

/**
 * @author rupakhet
 *
 */
public class P2PGUI {
	JFrame frame;
	JPanel contentPane;

	RemoteConnectionPanel remoteConnectionPanel;
	StatusPanel statusPanel;
	SearchPanel searchPanel;
	JPanel networkMapPanel;

	IP2PMediator mediator;
	IConnectionMonitor connectionMonitor;

	public P2PGUI(JFrame mainFrame, IP2PMediator mediator, IConnectionMonitor connectionMonitor) {
		this.frame = mainFrame;
		this.mediator = mediator;
		this.connectionMonitor = connectionMonitor;
		this.initGUI();
	}

	public void show() {
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				connectionMonitor.stop();
			}
		});

		// Position the window to the center of the screen
		frame.pack();
		final Toolkit toolkit = Toolkit.getDefaultToolkit();
		final Dimension screenSize = toolkit.getScreenSize();
		final int x = (screenSize.width - frame.getWidth()) / 2;
		final int y = (screenSize.height - frame.getHeight()) / 2;
		frame.setLocation(x, y);
	}

	private void initGUI() {
		frame.setTitle("Rose P2P App (" + IProtocol.PROTOCOL + ") - Localhost [" + mediator.getLocalhost() + "]");
		this.contentPane = (JPanel) frame.getContentPane();

		this.statusPanel = new StatusPanel();
		this.remoteConnectionPanel = new RemoteConnectionPanel(frame, mediator, statusPanel);
		this.searchPanel = new SearchPanel(frame, mediator, statusPanel);
		this.networkMapPanel = new NetworkPanel();

		this.contentPane.add(this.remoteConnectionPanel, BorderLayout.WEST);
		this.contentPane.add(this.networkMapPanel, BorderLayout.CENTER);
		this.contentPane.add(this.searchPanel, BorderLayout.EAST);
		this.contentPane.add(this.statusPanel, BorderLayout.SOUTH);
	}

	

	public void listingReceived(IHost host, List<String> listing) {
		this.statusPanel.postStatus("File listing received from " + host + "!");
		this.remoteConnectionPanel.getFileListModel().clear();
		for (String f : listing) {
			this.remoteConnectionPanel.getFileListModel().addElement(f);
		}
	}



	

}
