package com.MCXCC.JNBTExplorer.nbt.util;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.*;

public class Logger {
	private static final String APP_NAME = "JNBTExplorer";
	private static final SimpleDateFormat FILE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
	private final java.util.logging.Logger julLogger;
	private FileHandler fileHandler;
	private ConsoleHandler consoleHandler;
	private boolean initialized;

	public Logger(Date windowCreationTime, ConfigManager configManager) {
	julLogger = java.util.logging.Logger.getLogger(APP_NAME);
	try {
		String configDir = getConfigDirectory();
		File logDir = new File(configDir, "log");
		if (!logDir.exists()) {
			logDir.mkdirs();
		}

		String fileName = FILE_DATE_FORMAT.format(windowCreationTime) + ".log";
		String logFilePath = new File(logDir, fileName).getAbsolutePath();

		julLogger.setUseParentHandlers(false);

		Level logLevel = parseLogLevel(configManager.getLogLevel());
		julLogger.setLevel(logLevel);

		SimpleFormatter formatter = new SimpleFormatter() {
			private static final String FORMAT = "[%1$tF %1$tT] [%2$s] %3$s%n";

			@Override
			public String format(LogRecord record) {
				return String.format(FORMAT,
					new Date(record.getMillis()),
					record.getLevel().getName(),
					formatMessage(record));
			}
		};

		fileHandler = new FileHandler(logFilePath, true);
		fileHandler.setFormatter(formatter);
		fileHandler.setLevel(logLevel);
		julLogger.addHandler(fileHandler);

		if (configManager.isDebugMode()) {
			consoleHandler = new ConsoleHandler();
			consoleHandler.setFormatter(formatter);
			consoleHandler.setLevel(logLevel);
			julLogger.addHandler(consoleHandler);
		}

		initialized = true;
		info("Logger initialized, log file: " + logFilePath + ", log level: " + logLevel + ", debug mode: " + configManager.isDebugMode());
	} catch (IOException e) {
		System.err.println("Failed to initialize logger: " + e.getMessage());
		initialized = false;
	}
}

private Level parseLogLevel(String levelStr) {
	if (levelStr == null) {
		return Level.INFO;
	}
	switch (levelStr.toUpperCase()) {
		case "SEVERE":
			return Level.SEVERE;
		case "WARNING":
			return Level.WARNING;
		case "INFO":
			return Level.INFO;
		case "CONFIG":
			return Level.CONFIG;
		case "FINE":
			return Level.FINE;
		case "FINER":
			return Level.FINER;
		case "FINEST":
			return Level.FINEST;
		case "ALL":
			return Level.ALL;
		case "OFF":
			return Level.OFF;
		default:
			return Level.INFO;
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

	public void severe(String message) {
		julLogger.severe(message);
	}

	public void warning(String message) {
		julLogger.warning(message);
	}

	public void info(String message) {
		julLogger.info(message);
	}

	public void config(String message) {
		julLogger.config(message);
	}

	public void fine(String message) {
		julLogger.fine(message);
	}

	public void finer(String message) {
		julLogger.finer(message);
	}

	public void finest(String message) {
		julLogger.finest(message);
	}

	public void log(Level level, String message) {
		julLogger.log(level, message);
	}

	public void log(Level level, String message, Throwable thrown) {
		julLogger.log(level, message, thrown);
	}

	public void close() {
		if (initialized) {
			if (fileHandler != null) {
				julLogger.removeHandler(fileHandler);
				fileHandler.close();
				fileHandler = null;
			}
			if (consoleHandler != null) {
				julLogger.removeHandler(consoleHandler);
				consoleHandler.close();
				consoleHandler = null;
			}
			initialized = false;
		}
	}
}
