package com.MCXCC.JNBTExplorer.nbt.ui;

import com.MCXCC.JNBTExplorer.nbt.util.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class About extends JDialog {
private final Logger logger;

public About(MainFrame parent) {
	super(parent, "About JNBTExplorer", true);
	this.logger = parent.logger;
	initComponents();
	setLocationRelativeTo(parent);
}

private void initComponents() {
	setSize(400, 300);
	setLayout(new BorderLayout());

	JPanel contentPanel = new JPanel();
	contentPanel.setLayout(new GridBagLayout());
	contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

	GridBagConstraints gbc = new GridBagConstraints();
	gbc.insets = new Insets(10, 10, 10, 10);
	gbc.fill = GridBagConstraints.HORIZONTAL;

	JLabel titleLabel = new JLabel("JNBTExplorer");
	titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
	titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
	gbc.gridx = 0;
	gbc.gridy = 0;
	gbc.gridwidth = 2;
	contentPanel.add(titleLabel, gbc);

	String version = "1.0.0";
	JLabel versionLabel = new JLabel("Version: " + version);
	versionLabel.setHorizontalAlignment(SwingConstants.CENTER);
	gbc.gridy = 1;
	contentPanel.add(versionLabel, gbc);

	JLabel descLabel = new JLabel("NBT Explorer Java Edition, A tool for exploring and editing NBT files.");
	descLabel.setHorizontalAlignment(SwingConstants.CENTER);
	gbc.gridy = 2;
	contentPanel.add(descLabel, gbc);

	JLabel linkLabel = new JLabel("GitHub: MCXCC303/JNBTExplorer");
	linkLabel.setHorizontalAlignment(SwingConstants.CENTER);
	linkLabel.setForeground(Color.BLUE);
	linkLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
	linkLabel.addMouseListener(new MouseAdapter() {
		@Override
		public void mouseClicked(MouseEvent e) {
			try {
				Desktop.getDesktop().browse(new URI("https://github.com/MCXCC303/JNBTExplorer"));
				logger.info("Opened GitHub link");
			} catch (IOException | URISyntaxException ex) {
				logger.error("Error opening GitHub link", ex);
				JOptionPane.showMessageDialog(About.this, "Error opening link", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	});
	gbc.gridy = 3;
	contentPanel.add(linkLabel, gbc);

	JLabel copyrightLabel = new JLabel("Copyright © 2026 MCXCC");
	copyrightLabel.setHorizontalAlignment(SwingConstants.CENTER);
	gbc.gridy = 4;
	contentPanel.add(copyrightLabel, gbc);

	add(contentPanel, BorderLayout.CENTER);

	JPanel buttonPanel = new JPanel();
	buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

	JButton okButton = new JButton("OK");
	okButton.addActionListener(e -> dispose());
	buttonPanel.add(okButton);

	add(buttonPanel, BorderLayout.SOUTH);

	getRootPane().setDefaultButton(okButton);
}
}
