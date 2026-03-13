package com.MCXCC.JNBTExplorer.nbt.util;

import java.io.*;
import java.util.Properties;

public class ConfigManager {
private static final String CONFIG_FILE_NAME = "config.properties";
private static final String APP_NAME = "JNBTExplorer";
private final Properties properties;
private String configFilePath;

public ConfigManager() {
	properties = new Properties();
	loadConfig();
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

public void loadConfig() {
	try {
		String configDir = getConfigDirectory();
		File configDirFile = new File(configDir);
		if (!configDirFile.exists()) {
			configDirFile.mkdirs();
		}

		configFilePath = new File(configDir, CONFIG_FILE_NAME).getAbsolutePath();
		File configFile = new File(configFilePath);
		if (configFile.exists()) {
			try (InputStream input = new FileInputStream(configFile)) {
				properties.load(input);
			}
		} else {
			setDefaultValues();
			saveConfig();
		}
	} catch (IOException e) {
		System.err.println("Failed to load config: " + e.getMessage());
		setDefaultValues();
	}
}

private void setDefaultValues() {
	properties.setProperty("defaultTypeSortMode", "TYPE_DESC");
	properties.setProperty("defaultNameSortMode", "NAME_ASC");
	properties.setProperty("enableDragAndDrop", "true");
	properties.setProperty("showTypeIcons", "true");
	properties.setProperty("alwaysShowNames", "true");
	properties.setProperty("showArrayRawValues", "false");
	properties.setProperty("debugMode", "false");
	properties.setProperty("logLevel", "INFO");
}

public void saveConfig() {
	try {
		String configDir = getConfigDirectory();
		File configDirFile = new File(configDir);
		if (!configDirFile.exists()) {
			configDirFile.mkdirs();
		}

		try (OutputStream output = new FileOutputStream(configFilePath)) {
			properties.store(output, "JNBTExplorer Configuration");
		}
	} catch (IOException e) {
		System.err.println("Failed to save config: " + e.getMessage());
	}
}

public String getDefaultTypeSortMode() {
	return properties.getProperty("defaultTypeSortMode", "TYPE_DESC");
}

public void setDefaultTypeSortMode(String mode) {
	properties.setProperty("defaultTypeSortMode", mode);
}

public String getDefaultNameSortMode() {
	return properties.getProperty("defaultNameSortMode", "NAME_ASC");
}

public void setDefaultNameSortMode(String mode) {
	properties.setProperty("defaultNameSortMode", mode);
}

public boolean isDragAndDropEnabled() {
	return Boolean.parseBoolean(properties.getProperty("enableDragAndDrop", "true"));
}

public void setDragAndDropEnabled(boolean enabled) {
	properties.setProperty("enableDragAndDrop", String.valueOf(enabled));
}

public boolean isShowTypeIcons() {
	return Boolean.parseBoolean(properties.getProperty("showTypeIcons", "true"));
}

public void setShowTypeIcons(boolean show) {
	properties.setProperty("showTypeIcons", String.valueOf(show));
}

public boolean isAlwaysShowNames() {
	return Boolean.parseBoolean(properties.getProperty("alwaysShowNames", "true"));
}

public void setAlwaysShowNames(boolean show) {
	properties.setProperty("alwaysShowNames", String.valueOf(show));
}

public boolean isShowArrayRawValues() {
	return Boolean.parseBoolean(properties.getProperty("showArrayRawValues", "false"));
}

public void setShowArrayRawValues(boolean show) {
	properties.setProperty("showArrayRawValues", String.valueOf(show));
}

public String getIconStyle() {
	return properties.getProperty("iconStyle", "classic");
}

public void setIconStyle(String style) {
	properties.setProperty("iconStyle", style);
}

public boolean isDebugMode() {
	return Boolean.parseBoolean(properties.getProperty("debugMode", "false"));
}

public void setDebugMode(boolean enabled) {
	properties.setProperty("debugMode", String.valueOf(enabled));
}

public String getLogLevel() {
	return properties.getProperty("logLevel", "INFO");
}

public void setLogLevel(String level) {
	properties.setProperty("logLevel", level);
}
}
