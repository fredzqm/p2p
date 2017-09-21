package edu.rosehulman.p2p.app.panel;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import edu.rosehulman.p2p.impl.Host;
import edu.rosehulman.p2p.impl.notification.RequestAttachEvent;
import edu.rosehulman.p2p.impl.notification.RequestDetachEvent;
import edu.rosehulman.p2p.impl.notification.RequestGetEvent;
import edu.rosehulman.p2p.impl.notification.RequestListEvent;
import edu.rosehulman.p2p.protocol.IHost;
import edu.rosehulman.p2p.protocol.IP2PMediator;

public class RemoteConnectionPanel extends JPanel {
	JPanel newConnectionPanel;
	JTextField hostNameField;
	JTextField portField;
	JButton connectButton;

	JScrollPane peerListScrollPane;
	JList<IHost> peerList;
	private DefaultListModel<IHost> peerListModel;
	JButton disconnectButton;
	JButton listFileButton;
	JScrollPane fileListingPane;
	JList<String> fileList;
	private DefaultListModel<String> fileListModel;
	JButton downloadDirect;

	public RemoteConnectionPanel(JFrame frame, IP2PMediator mediator, StatusPanel statusPanel) {
		super(new BorderLayout());
		this.setBorder(BorderFactory.createTitledBorder("Remote Connections"));

		this.newConnectionPanel = new JPanel();

		this.hostNameField = new JTextField("");
		this.hostNameField.setColumns(25);

		this.portField = new JTextField("");
		this.portField.setColumns(8);

		this.connectButton = new JButton("Connect");
		this.newConnectionPanel.add(new JLabel("Host: "));
		this.newConnectionPanel.add(this.hostNameField);
		this.newConnectionPanel.add(new JLabel("Port: "));
		this.newConnectionPanel.add(this.portField);
		this.newConnectionPanel.add(this.connectButton);

		this.connectButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					String host = hostNameField.getText();
					int port = Integer.parseInt(portField.getText());
					final IHost remoteHost = new Host(host, port);

					Thread runner = new Thread() {
						public void run() {
							statusPanel.postStatus("Trying to connect to " + remoteHost + " ...");
							try {
								RequestAttachEvent requestAttachEvent = new RequestAttachEvent(remoteHost, false);
								mediator.fireEvent(requestAttachEvent);
								if (requestAttachEvent.isSucessfully()) {
									statusPanel.postStatus("Connected to " + remoteHost);
								} else {
									statusPanel
											.postStatus("Could not connect to " + remoteHost + ". Please try again!");
								}
							} catch (Exception exp) {
								statusPanel.postStatus("An error occured while connecting: " + exp.getMessage());
							}
						}
					};
					runner.start();
				} catch (Exception ex) {
					statusPanel.postStatus("Connection could not be established: " + ex.getMessage());
				}
			}
		});

		this.add(this.newConnectionPanel, BorderLayout.NORTH);

		JPanel peerListPanel = new JPanel(new BorderLayout());
		peerListPanel.add(new JLabel("List of Peers", JLabel.CENTER), BorderLayout.NORTH);
		this.setPeerListModel(new DefaultListModel<>());
		this.peerList = new JList<>(this.getPeerListModel());
		this.peerList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.peerListScrollPane = new JScrollPane(this.peerList);
		this.listFileButton = new JButton("List Files");
		this.disconnectButton = new JButton("Disconnect");
		peerListPanel.add(this.peerListScrollPane, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel(new GridLayout());
		buttonPanel.add(disconnectButton);
		buttonPanel.add(listFileButton);
		peerListPanel.add(buttonPanel, BorderLayout.SOUTH);

		this.disconnectButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				IHost remoteHost = peerList.getSelectedValue();
				if (remoteHost == null) {
					JOptionPane.showMessageDialog(frame, "You must first select a peer from the list above!",
							"P2P Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				try {
					RequestDetachEvent requestDetachEvent = new RequestDetachEvent(remoteHost);
					mediator.fireEvent(requestDetachEvent);
					statusPanel.postStatus("Disconnected from " + remoteHost + "!");
				} catch (Exception ex) {
					statusPanel.postStatus("Error disconnecting to " + remoteHost + "!");
				}
			}
		});

		this.listFileButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final IHost remoteHost = peerList.getSelectedValue();
				if (remoteHost == null) {
					JOptionPane.showMessageDialog(frame, "You must first select a peer from the list above!",
							"P2P Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				Thread thread = new Thread() {
					public void run() {
						try {
							mediator.fireEvent(new RequestListEvent(remoteHost));
							statusPanel.postStatus("File listing request sent to " + remoteHost + "!");
						} catch (Exception e) {
							statusPanel.postStatus("Error sending list request to " + remoteHost + "!");
						}
					}
				};
				thread.start();
			}
		});

		JPanel fileListPanel = new JPanel(new BorderLayout());
		fileListPanel.add(new JLabel("List of files in the selected peer", JLabel.CENTER), BorderLayout.NORTH);
		this.setFileListModel(new DefaultListModel<>());
		this.fileList = new JList<>(this.getFileListModel());
		this.fileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.fileListingPane = new JScrollPane(this.fileList);
		this.downloadDirect = new JButton("Download the selected file");
		fileListPanel.add(this.fileListingPane, BorderLayout.CENTER);
		fileListPanel.add(this.downloadDirect, BorderLayout.SOUTH);

		this.downloadDirect.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final IHost remoteHost = peerList.getSelectedValue();
				final String fileName = fileList.getSelectedValue();
				if (remoteHost == null || fileName == null) {
					JOptionPane.showMessageDialog(frame,
							"You must have a peer and a file selected from the lists above!", "P2P Error",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				Thread thread = new Thread() {
					public void run() {
						try {
							mediator.fireEvent(new RequestGetEvent(remoteHost, fileName));
							statusPanel.postStatus("Getting file " + fileName + " from " + remoteHost + "...");
						} catch (Exception e) {
							statusPanel.postStatus("Error sending the get file request to " + remoteHost + "!");
						}
					}
				};
				thread.start();
			}
		});

		this.add(peerListPanel, BorderLayout.WEST);
		this.add(fileListPanel, BorderLayout.CENTER);
	}

	public DefaultListModel<String> getFileListModel() {
		return fileListModel;
	}

	void setFileListModel(DefaultListModel<String> fileListModel) {
		this.fileListModel = fileListModel;
	}

	public DefaultListModel<IHost> getPeerListModel() {
		return peerListModel;
	}

	void setPeerListModel(DefaultListModel<IHost> peerListModel) {
		this.peerListModel = peerListModel;
	}

}
