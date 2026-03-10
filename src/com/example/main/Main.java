package com.example.main;

import com.example.nbt.ui.MainFrame;
import com.example.nbt.util.Logger;

import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;
import java.io.File;

public class Main {
public static void main(String[] args) {
	Logger logger = Logger.getInstance();
	logger.info("NBT Editor starting...");

	try {
		for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
			if ("Nimbus".equals(info.getName())) {
				UIManager.setLookAndFeel(info.getClassName());
				logger.info("Set look and feel: Nimbus");
				break;
			}
		}
	} catch (Exception e) {
		logger.warning("Failed to set look and feel: " + e.getMessage());
	}

	SwingUtilities.invokeLater(() -> {
		try {
			if (args.length > 0) {
				for (String arg : args) {
					File file = new File(arg);
					if (file.exists()) {
						MainFrame frame = new MainFrame(file.getAbsolutePath());
						frame.setVisible(true);
						logger.info("Opened file from command line: " + file.getAbsolutePath());
					} else {
						logger.warning("File not found: " + arg);
						// Create window with empty file
						MainFrame frame = new MainFrame();
						frame.setVisible(true);
					}
				}
			} else {
				MainFrame frame = new MainFrame();
				frame.setVisible(true);
				logger.info("Main frame created and visible");
			}
		} catch (Exception e) {
			logger.error("Failed to create main frame", e);
			System.exit(1);
		}
	});
}
}
