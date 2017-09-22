package edu.rosehulman.p2p.app.panel;

import java.awt.BorderLayout;
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

import edu.rosehulman.p2p.impl.connection.ConnectionEstablishedEvent;
import edu.rosehulman.p2p.impl.connection.RequestAttachAction;
import edu.rosehulman.p2p.impl.connection.RequestDetachAction;
import edu.rosehulman.p2p.impl.download.DownloadUpdate;
import edu.rosehulman.p2p.impl.download.GetAction;
import edu.rosehulman.p2p.impl.find.FindAction;
import edu.rosehulman.p2p.protocol.IHost;
import edu.rosehulman.p2p.protocol.IP2PMediator;

public class SearchPanel extends JPanel {
	JTextField searchTermField, depthField;
	JButton searchButton;
	JList<IHost> searchResultList;
	private DefaultListModel<IHost> searchResultListModel;
	JScrollPane searchResultScrollPane;
	JButton downloadAfterSearch;

	public SearchPanel(JFrame frame, IP2PMediator mediator, StatusPanel statusPanel) {
		super(new BorderLayout());
		this.setBorder(BorderFactory.createTitledBorder("Network File Searching"));

		JPanel top = new JPanel();
		top.add(new JLabel("Search Term: "));
		this.searchTermField = new JTextField("");
		this.searchTermField.setColumns(10);
		this.searchButton = new JButton("Search Network");
		top.add(this.searchTermField);
		top.add(new JLabel("Depth: "));
		this.depthField = new JTextField("");
		this.depthField.setColumns(7);
		top.add(this.depthField);
		top.add(this.searchButton);

		this.searchButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final String fileName = searchTermField.getText();
				final Integer depth = Integer.parseInt(depthField.getText());
				if (fileName == null || fileName == null) {
					JOptionPane.showMessageDialog(frame,
							"You must have a peer and a file selected from the lists above!", "P2P Error",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				getSearchResultListModel().clear();
				Thread thread = new Thread() {
					public void run() {
						try {
							mediator.fireEvent(new FindAction(fileName, depth - 1, ""));
							statusPanel.postStatus("Getting file " + fileName + " from " + fileName + "...");
						} catch (Exception e) {
							e.printStackTrace();
							statusPanel.postStatus("Error sending the get file request to " + fileName + "! ");
						}
					}
				};
				thread.start();
			}
		});

		this.setSearchResultListModel(new DefaultListModel<>());
		this.searchResultList = new JList<>(this.getSearchResultListModel());
		this.searchResultScrollPane = new JScrollPane(this.searchResultList);

		this.downloadAfterSearch = new JButton("Download the selected file");
		this.downloadAfterSearch.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Thread thread = new Thread() {
					public void run() {
						String file = searchTermField.getText();
						IHost host = searchResultList.getSelectedValue();
						try {
							mediator.registerOneTimeEventHandler(ConnectionEstablishedEvent.class, (med1, connectEvent) -> {
								mediator.registerOneTimeEventHandler(DownloadUpdate.class, (med2, downLoad) -> {
									mediator.fireEvent(new RequestDetachAction(host));
									statusPanel.postStatus("Getting file " + file + " from " + host.toString());
								});
								mediator.fireEvent(new GetAction(host, file));
							});
							mediator.fireEvent(new RequestAttachAction(host, false));
						} catch (Exception e) {
							e.printStackTrace();
							statusPanel.postStatus("Error sending the get file request to " + host.toString() + "! ");
						}

					}
				};
				thread.start();

			}
		});

		this.add(top, BorderLayout.NORTH);
		this.add(this.searchResultScrollPane, BorderLayout.CENTER);
		this.add(this.downloadAfterSearch, BorderLayout.SOUTH);
	}

	public DefaultListModel<IHost> getSearchResultListModel() {
		return searchResultListModel;
	}

	void setSearchResultListModel(DefaultListModel<IHost> searchResultListModel) {
		this.searchResultListModel = searchResultListModel;
	}
	
	public void foundFile(String fileName, IHost foundAt) {
		if (!this.getSearchResultListModel().contains(foundAt))
			this.getSearchResultListModel().addElement(foundAt);
	}

}
