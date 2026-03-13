package com.MCXCC.JNBTExplorer;

import com.MCXCC.JNBTExplorer.nbt.ui.MainFrame;
import com.MCXCC.JNBTExplorer.nbt.util.ConfigManager;
import com.MCXCC.JNBTExplorer.nbt.util.Logger;

import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;
import java.io.File;
import java.util.Date;

public class JNBTExplorerMain {
private static Logger logger;

public static void main(String[] args) {
	ConfigManager configManager = new ConfigManager();
	logger = new Logger(new Date(), configManager);

	Runtime.getRuntime().addShutdownHook(new Thread(() -> {
		if (logger != null) {
			logger.close();
		}
	}));

	logger.info("NBT Editor starting...");

	try {
		for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
			if ("Nimbus".equals(info.getName())) {
				UIManager.setLookAndFeel(info.getClassName());
				logger.config("Set look and feel: Nimbus");
				break;
			}
		}
	} catch (Exception e) {
		logger.severe("Failed to set look and feel: " + e.getMessage());
	}

	SwingUtilities.invokeLater(() -> {
		try {
			if (args.length > 0) {
				for (String arg : args) {
					File file = new File(arg);
					if (file.exists()) {
						MainFrame frame = new MainFrame(file.getAbsolutePath(), configManager, logger);
						frame.setVisible(true);
						logger.info("Opened file from command line: " + file.getAbsolutePath());
					} else {
						logger.severe("File not found: " + arg);

						MainFrame frame = new MainFrame(null, configManager, logger);
						frame.setVisible(true);
					}
				}
			} else {
				MainFrame frame = new MainFrame(null, configManager, logger);
				frame.setVisible(true);
				logger.info("Main frame created and visible");
			}
		} catch (Exception e) {
			logger.severe("Failed to create main frame: " + e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
	});
}
}
