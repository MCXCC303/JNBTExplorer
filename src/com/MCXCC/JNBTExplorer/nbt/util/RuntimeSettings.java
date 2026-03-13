package com.MCXCC.JNBTExplorer.nbt.util;

public class RuntimeSettings {
private boolean enableHiDPISupport;
private double uiScale;
private boolean enableFontAntialiasing;
private boolean enableXRender;

public RuntimeSettings() {
	setDefaultValues();
}

private void setDefaultValues() {
	enableHiDPISupport = true;
	uiScale = 2.0;
	enableFontAntialiasing = true;
	enableXRender = true;
}

public void applySettings() {
	if (enableHiDPISupport) {
		if (enableFontAntialiasing) {
			System.setProperty("awt.useSystemAAFontSettings", "lcd");
		}
		if (enableXRender) {
			System.setProperty("sun.java2d.xrender", "true");
		}
		System.setProperty("sun.java2d.uiScale", String.valueOf(uiScale));
	}
}

public boolean isEnableHiDPISupport() {
	return enableHiDPISupport;
}

public void setEnableHiDPISupport(boolean enable) {
	this.enableHiDPISupport = enable;
}

public double getUiScale() {
	return uiScale;
}

public void setUiScale(double scale) {
	this.uiScale = scale;
}

public boolean isEnableFontAntialiasing() {
	return enableFontAntialiasing;
}

public void setEnableFontAntialiasing(boolean enable) {
	this.enableFontAntialiasing = enable;
}

public boolean isEnableXRender() {
	return enableXRender;
}

public void setEnableXRender(boolean enable) {
	this.enableXRender = enable;
}

public void loadFromConfig(ConfigManager configManager) {
	enableHiDPISupport = configManager.isEnableHiDPISupport();
	uiScale = configManager.getUiScale();
	enableFontAntialiasing = configManager.isEnableFontAntialiasing();
	enableXRender = configManager.isEnableXRender();
}

public void saveToConfig(ConfigManager configManager) {
	configManager.setEnableHiDPISupport(enableHiDPISupport);
	configManager.setUiScale(uiScale);
	configManager.setEnableFontAntialiasing(enableFontAntialiasing);
	configManager.setEnableXRender(enableXRender);
}
}
