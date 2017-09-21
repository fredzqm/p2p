package edu.rosehulman.p2p.app.panel;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import edu.rosehulman.p2p.protocol.IProtocol;

public class StatusPanel extends JPanel {
	JScrollPane statusScrollPane;
	JTextArea statusTextArea;
	JScrollPane requestLogScrollPane;
	private DefaultListModel<String> requestLogListModel;
	JList<String> requestLogList;
	public StatusPanel() {
		super(new BorderLayout());
		this.setBorder(BorderFactory.createTitledBorder("Activity"));

		JPanel panel = new JPanel(new BorderLayout());
		this.statusTextArea = new JTextArea("");
		this.statusTextArea.setRows(10);
		this.statusScrollPane = new JScrollPane(this.statusTextArea);
		panel.add(new JLabel("Activity Log", JLabel.CENTER), BorderLayout.NORTH);
		panel.add(this.statusScrollPane, BorderLayout.CENTER);
		this.add(panel, BorderLayout.CENTER);

		panel = new JPanel(new BorderLayout());
		this.setRequestLogListModel(new DefaultListModel<>());
		this.requestLogList = new JList<>(this.getRequestLogListModel());
		this.requestLogScrollPane = new JScrollPane(this.requestLogList);

		panel.add(new JLabel("Request Log", JLabel.CENTER), BorderLayout.NORTH);
		panel.add(this.requestLogScrollPane, BorderLayout.CENTER);
		this.add(panel, BorderLayout.EAST);
	}
	
	public void postStatus(String msg) {
		this.statusTextArea.append(msg + IProtocol.LF);
		this.statusTextArea.setCaretPosition(this.statusTextArea.getDocument().getLength());
	}

	public DefaultListModel<String> getRequestLogListModel() {
		return requestLogListModel;
	}

	void setRequestLogListModel(DefaultListModel<String> requestLogListModel) {
		this.requestLogListModel = requestLogListModel;
	}

}
