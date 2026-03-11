package com.MCXCC.JNBTExplorer;

import com.MCXCC.JNBTExplorer.nbt.ui.MainFrame;

import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;
import java.io.File;

public class JNBTExplorerMain {
public static void main(String[] args) {
	System.out.println("NBT Editor starting...");

	try {
		for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
			if ("Nimbus".equals(info.getName())) {
				UIManager.setLookAndFeel(info.getClassName());
				System.out.println("Set look and feel: Nimbus");
				break;
			}
		}
	} catch (Exception e) {
		System.err.println("Failed to set look and feel: " + e.getMessage());
	}

	SwingUtilities.invokeLater(() -> {
		try {
			if (args.length > 0) {
				for (String arg : args) {
					File file = new File(arg);
					if (file.exists()) {
						MainFrame frame = new MainFrame(file.getAbsolutePath());
						frame.setVisible(true);
						System.out.println("Opened file from command line: " + file.getAbsolutePath());
					} else {
						System.err.println("File not found: " + arg);

						MainFrame frame = new MainFrame();
						frame.setVisible(true);
					}
				}
			} else {
				MainFrame frame = new MainFrame();
				frame.setVisible(true);
				System.out.println("Main frame created and visible");
			}
		} catch (Exception e) {
			System.err.println("Failed to create main frame: " + e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
	});
}
}
