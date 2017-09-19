package edu.rosehulman.p2p.app;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class P2PConfigurationWindow {
	private JFrame mainFrame;
	private JDialog frame;
	
	private JTextField portField;
	private JLabel uriLbl;
	private JPanel pane;
	
	private String rootDir;

	public P2PConfigurationWindow(JFrame mainFrame) {
		this.mainFrame = mainFrame;
		frame = new JDialog(mainFrame, "P2P Configuration Window", true);
		pane = new JPanel(new GridLayout(0,2));
		frame.setContentPane(pane);
	}
	
	public void show() {
		JLabel portLbl = new JLabel("Port:");
		pane.add(portLbl);
		
		this.portField = new JTextField("9001");
		pane.add(portField);
		
		JLabel fileLbl = new JLabel("Root Directory:");
		pane.add(fileLbl);
		
		File f = new File(".");
		this.rootDir = f.getName();
		this.uriLbl = new JLabel("   " + this.rootDir + "/");
		pane.add(uriLbl);

		JButton fileButton = new JButton("Browse");
		pane.add(fileButton);
		
		fileButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser chooser = new JFileChooser();
				chooser.setCurrentDirectory(new File("."));
				chooser.setDialogTitle("Select the root directory ");
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				chooser.setAcceptAllFileFilterUsed(false);

				if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
					File currentDir = chooser.getSelectedFile();
					uriLbl.setText("... /" + currentDir.getName());
					rootDir = currentDir.getAbsolutePath();
				} 
			}
		});

		
//		JLabel tempLbl = new JLabel();
//		pane.add(tempLbl);

		JButton doneBtn = new JButton("Done");
		pane.add(doneBtn);
		doneBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				frame.setVisible(false);
				frame.dispose();
			}
		});
		
		frame.pack();
		frame.setLocationRelativeTo(mainFrame);
		frame.setVisible(true);
	}

	public String getRootDirectory() {
		return this.rootDir;
	}
	
	public int getPort() {
		return Integer.parseInt(this.portField.getText());
	}
}
