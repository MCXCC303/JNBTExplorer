package com.MCXCC.JNBTExplorer.nbt.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {
private static final String APP_NAME = "JNBTExplorer";
private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
private static final SimpleDateFormat FILE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
private PrintWriter writer;
private boolean initialized;

public Logger(Date windowCreationTime) {
	try {

		String configDir = getConfigDirectory();
		File logDir = new File(configDir, "log");
		if (!logDir.exists()) {
			logDir.mkdirs();
		}

		String fileName = FILE_DATE_FORMAT.format(windowCreationTime) + ".log";
		String logFilePath = new File(logDir, fileName).getAbsolutePath();
		writer = new PrintWriter(new FileWriter(logFilePath, true));
		initialized = true;
		info("Logger initialized, log file: " + logFilePath);
	} catch (IOException e) {
		System.err.println("Failed to initialize logger: " + e.getMessage());
		initialized = false;
	}
}

private String getConfigDirectory() {
	String os = System.getProperty("os.name").toLowerCase();
	String userHome = System.getProperty("user.home");

	if (os.contains("linux")) {

		String xdgConfig = System.getenv("XDG_CONFIG_HOME");
		if (xdgConfig != null && !xdgConfig.isEmpty()) {
			return xdgConfig + File.separator + APP_NAME;
		} else {
			return userHome + File.separator + ".config" + File.separator + APP_NAME;
		}
	} else if (os.contains("mac") || os.contains("darwin")) {

		return userHome + File.separator + "Library" + File.separator + "Application Support" + File.separator + APP_NAME;
	} else {

		String appData = System.getenv("APPDATA");
		if (appData != null && !appData.isEmpty()) {
			return appData + File.separator + APP_NAME;
		} else {
			return userHome + File.separator + "AppData" + File.separator + "Roaming" + File.separator + APP_NAME;
		}
	}
}

public void info(String message) {
	log("INFO", message);
}

public void warning(String message) {
	log("WARNING", message);
}

public void error(String message) {
	log("ERROR", message);
}

public void error(String message, Exception e) {
	log("ERROR", message + ": " + e.getMessage());
	e.printStackTrace(writer);
	writer.flush();
}

private void log(String level, String message) {
	if (!initialized) {
		return;
	}

	String timestamp = DATE_FORMAT.format(new Date());
	String logMessage = String.format("[%s] [%s] %s", timestamp, level, message);

	System.out.println(logMessage);
	writer.println(logMessage);
	writer.flush();
}

public void close() {
	if (initialized) {
		info("Logger closed");
		writer.close();
		initialized = false;
	}
}
}
