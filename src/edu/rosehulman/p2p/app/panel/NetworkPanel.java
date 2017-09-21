package edu.rosehulman.p2p.app.panel;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class NetworkPanel extends JPanel {
		public NetworkPanel() {
			super(new BorderLayout());
			this.setBorder(BorderFactory.createTitledBorder("Network Graph"));

			this.add(new JLabel("Shown the network graph (Bonus) ..."));
		}
}
