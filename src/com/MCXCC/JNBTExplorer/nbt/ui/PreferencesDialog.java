package com.MCXCC.JNBTExplorer.nbt.ui;

import com.MCXCC.JNBTExplorer.nbt.model.SortMode;
import com.MCXCC.JNBTExplorer.nbt.util.ConfigManager;
import com.MCXCC.JNBTExplorer.nbt.util.Logger;

import javax.swing.*;
import java.awt.*;

public class PreferencesDialog extends JDialog {
private final ConfigManager configManager;
private final Logger logger;
private JComboBox<String> typeSortModeComboBox;
private JComboBox<String> nameSortModeComboBox;
private JComboBox<String> iconStyleComboBox;
private JCheckBox dragAndDropCheckBox;
private JCheckBox showTypeIconsCheckBox;
private JCheckBox alwaysShowNamesCheckBox;
private JCheckBox showArrayRawValuesCheckBox;
private JCheckBox debugModeCheckBox;
private JComboBox<String> logLevelComboBox;
private JCheckBox enableHiDPICheckBox;
private JComboBox<String> uiScaleComboBox;
private JCheckBox enableFontAntialiasingCheckBox;
private JCheckBox enableXRenderCheckBox;
private boolean confirmed = false;

public PreferencesDialog(JFrame parent, ConfigManager configManager, Logger logger) {
	super(parent, "Preferences", true);
	this.configManager = configManager;
	this.logger = logger;
	initComponents();
	setLocationRelativeTo(parent);
}

private void initComponents() {
	setSize(500, 500);
	setLayout(new BorderLayout());

	JPanel mainPanel = new JPanel();
	mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
	mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

	JPanel generalPanel = new JPanel(new GridLayout(9, 2, 10, 8));
	generalPanel.setBorder(BorderFactory.createTitledBorder("General"));
	JLabel typeSortModeLabel = new JLabel("Default Type Sort Mode:");
	typeSortModeComboBox = new JComboBox<>(new String[]{
		SortMode.TYPE_ASC.getDisplayName(),
		SortMode.TYPE_DESC.getDisplayName(),
		"None"
	});
	String currentTypeSortMode = configManager.getDefaultTypeSortMode();
	if (currentTypeSortMode.equals("TYPE_ASC")) {
		typeSortModeComboBox.setSelectedIndex(0);
	} else if (currentTypeSortMode.equals("TYPE_DESC")) {
		typeSortModeComboBox.setSelectedIndex(1);
	} else {
		typeSortModeComboBox.setSelectedIndex(2);
	}
	generalPanel.add(typeSortModeLabel);
	generalPanel.add(typeSortModeComboBox);

	JLabel nameSortModeLabel = new JLabel("Default Name Sort Mode:");
	nameSortModeComboBox = new JComboBox<>(new String[]{
		SortMode.NAME_ASC.getDisplayName(),
		SortMode.NAME_DESC.getDisplayName(),
		"None"
	});
	String currentNameSortMode = configManager.getDefaultNameSortMode();
	if (currentNameSortMode.equals("NAME_ASC")) {
		nameSortModeComboBox.setSelectedIndex(0);
	} else if (currentNameSortMode.equals("NAME_DESC")) {
		nameSortModeComboBox.setSelectedIndex(1);
	} else {
		nameSortModeComboBox.setSelectedIndex(2);
	}
	generalPanel.add(nameSortModeLabel);
	generalPanel.add(nameSortModeComboBox);

	JLabel iconStyleLabel = new JLabel("Icon Style:");
	iconStyleComboBox = new JComboBox<>(new String[]{"Classic", "Modern", "Original"});
	String currentIconStyle = configManager.getIconStyle();
	if ("modern".equals(currentIconStyle)) {
		iconStyleComboBox.setSelectedIndex(1);
	} else if ("original".equals(currentIconStyle)) {
		iconStyleComboBox.setSelectedIndex(2);
	} else {
		iconStyleComboBox.setSelectedIndex(0);
	}
	generalPanel.add(iconStyleLabel);
	generalPanel.add(iconStyleComboBox);

	JLabel logLevelLabel = new JLabel("*Log Level:");
	logLevelComboBox = new JComboBox<>(new String[]{
		"SEVERE",
		"WARNING",
		"INFO",
		"CONFIG",
		"FINE",
		"FINER",
		"FINEST",
		"ALL"
	});
	String currentLogLevel = configManager.getLogLevel();
	for (int i = 0; i < logLevelComboBox.getItemCount(); i++) {
		if (logLevelComboBox.getItemAt(i).equals(currentLogLevel)) {
			logLevelComboBox.setSelectedIndex(i);
			break;
		}
	}
	generalPanel.add(logLevelLabel);
	generalPanel.add(logLevelComboBox);

	JLabel debugModeLabel = new JLabel("*Debug Mode:");
	debugModeCheckBox = new JCheckBox();
	debugModeCheckBox.setSelected(configManager.isDebugMode());
	generalPanel.add(debugModeLabel);
	generalPanel.add(debugModeCheckBox);

	JLabel dragAndDropLabel = new JLabel("*Enable Drag and Drop:");
	dragAndDropCheckBox = new JCheckBox();
	dragAndDropCheckBox.setSelected(configManager.isDragAndDropEnabled());
	generalPanel.add(dragAndDropLabel);
	generalPanel.add(dragAndDropCheckBox);

	JLabel showTypeIconsLabel = new JLabel("Show Type Icons:");
	showTypeIconsCheckBox = new JCheckBox();
	showTypeIconsCheckBox.setSelected(configManager.isShowTypeIcons());
	generalPanel.add(showTypeIconsLabel);
	generalPanel.add(showTypeIconsCheckBox);

	JLabel alwaysShowNamesLabel = new JLabel("Always Show Names:");
	alwaysShowNamesCheckBox = new JCheckBox();
	alwaysShowNamesCheckBox.setSelected(configManager.isAlwaysShowNames());
	generalPanel.add(alwaysShowNamesLabel);
	generalPanel.add(alwaysShowNamesCheckBox);

	JLabel showArrayRawValuesLabel = new JLabel("Show Array Raw Values:");
	showArrayRawValuesCheckBox = new JCheckBox();
	showArrayRawValuesCheckBox.setSelected(configManager.isShowArrayRawValues());
	generalPanel.add(showArrayRawValuesLabel);
	generalPanel.add(showArrayRawValuesCheckBox);

	mainPanel.add(generalPanel);
	mainPanel.add(Box.createVerticalStrut(10));

	JPanel hidpiPanel = new JPanel(new GridLayout(4, 2, 10, 8));
	hidpiPanel.setBorder(BorderFactory.createTitledBorder("HiDPI Settings (Restart Required)"));

	JLabel enableHiDPILabel = new JLabel("Enable HiDPI Support:");
	enableHiDPICheckBox = new JCheckBox();
	enableHiDPICheckBox.setSelected(configManager.isEnableHiDPISupport());
	hidpiPanel.add(enableHiDPILabel);
	hidpiPanel.add(enableHiDPICheckBox);

	JLabel uiScaleLabel = new JLabel("UI Scale:");
	uiScaleComboBox = new JComboBox<>(new String[]{"100%", "125%", "150%", "175%", "200%", "250%", "300%"});
	double currentScale = configManager.getUiScale();
	int scalePercent = (int) (currentScale * 100);
	String[] scaleOptions = {"100%", "125%", "150%", "175%", "200%", "250%", "300%"};
	for (int i = 0; i < scaleOptions.length; i++) {
		int optionPercent = Integer.parseInt(scaleOptions[i].replace("%", ""));
		if (optionPercent == scalePercent) {
			uiScaleComboBox.setSelectedIndex(i);
			break;
		}
	}
	hidpiPanel.add(uiScaleLabel);
	hidpiPanel.add(uiScaleComboBox);

	JLabel enableFontAntialiasingLabel = new JLabel("Enable Font Antialiasing:");
	enableFontAntialiasingCheckBox = new JCheckBox();
	enableFontAntialiasingCheckBox.setSelected(configManager.isEnableFontAntialiasing());
	hidpiPanel.add(enableFontAntialiasingLabel);
	hidpiPanel.add(enableFontAntialiasingCheckBox);

	JLabel enableXRenderLabel = new JLabel("Enable XRender (Linux):");
	enableXRenderCheckBox = new JCheckBox();
	enableXRenderCheckBox.setSelected(configManager.isEnableXRender());
	hidpiPanel.add(enableXRenderLabel);
	hidpiPanel.add(enableXRenderCheckBox);

	mainPanel.add(hidpiPanel);

	JScrollPane scrollPane = new JScrollPane(mainPanel);
	scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
	scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	scrollPane.setBorder(null);
	scrollPane.getVerticalScrollBar().setUnitIncrement(16);

	add(scrollPane, BorderLayout.CENTER);

	JPanel bottomPanel = new JPanel(new BorderLayout());
	bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 10, 20));

	JLabel noteLabel = new JLabel("* Restart Required  ");
	noteLabel.setFont(new Font(noteLabel.getFont().getName(), Font.ITALIC, 12));
	bottomPanel.add(noteLabel, BorderLayout.WEST);

	JPanel buttonPanel = new JPanel();
	buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

	JButton okButton = new JButton("OK");
	okButton.addActionListener(e -> {
		savePreferences();
		confirmed = true;
		dispose();
	});

	JButton cancelButton = new JButton("Cancel");
	cancelButton.addActionListener(e -> dispose());

	buttonPanel.add(okButton);
	buttonPanel.add(cancelButton);

	bottomPanel.add(buttonPanel, BorderLayout.EAST);
	add(bottomPanel, BorderLayout.SOUTH);

	getRootPane().setDefaultButton(okButton);
}

private void savePreferences() {
	int typeSortModeIndex = typeSortModeComboBox.getSelectedIndex();
	if (typeSortModeIndex == 0) {
		configManager.setDefaultTypeSortMode("TYPE_ASC");
	} else if (typeSortModeIndex == 1) {
		configManager.setDefaultTypeSortMode("TYPE_DESC");
	} else {
		configManager.setDefaultTypeSortMode("NONE");
	}

	int nameSortModeIndex = nameSortModeComboBox.getSelectedIndex();
	if (nameSortModeIndex == 0) {
		configManager.setDefaultNameSortMode("NAME_ASC");
	} else if (nameSortModeIndex == 1) {
		configManager.setDefaultNameSortMode("NAME_DESC");
	} else {
		configManager.setDefaultNameSortMode("NONE");
	}

	int iconStyleIndex = iconStyleComboBox.getSelectedIndex();
	if (iconStyleIndex == 1) {
		configManager.setIconStyle("modern");
	} else if (iconStyleIndex == 2) {
		configManager.setIconStyle("original");
	} else {
		configManager.setIconStyle("classic");
	}

	configManager.setDragAndDropEnabled(dragAndDropCheckBox.isSelected());
	configManager.setShowTypeIcons(showTypeIconsCheckBox.isSelected());
	configManager.setAlwaysShowNames(alwaysShowNamesCheckBox.isSelected());
	configManager.setShowArrayRawValues(showArrayRawValuesCheckBox.isSelected());
	configManager.setDebugMode(debugModeCheckBox.isSelected());
	configManager.setLogLevel((String) logLevelComboBox.getSelectedItem());

	configManager.setEnableHiDPISupport(enableHiDPICheckBox.isSelected());
	String scaleStr = (String) uiScaleComboBox.getSelectedItem();
	double scaleValue = Double.parseDouble(scaleStr.replace("%", "")) / 100.0;
	configManager.setUiScale(scaleValue);
	configManager.setEnableFontAntialiasing(enableFontAntialiasingCheckBox.isSelected());
	configManager.setEnableXRender(enableXRenderCheckBox.isSelected());

	configManager.saveConfig();
	logger.info("Preferences saved");
}

public boolean isConfirmed() {
	return confirmed;
}

public boolean shouldRefreshUI() {
	String newIconStyle;
	int iconStyleIndex = iconStyleComboBox.getSelectedIndex();
	if (iconStyleIndex == 1) {
		newIconStyle = "modern";
	} else if (iconStyleIndex == 2) {
		newIconStyle = "original";
	} else {
		newIconStyle = "classic";
	}
	return showTypeIconsCheckBox.isSelected() != configManager.isShowTypeIcons() ||
		alwaysShowNamesCheckBox.isSelected() != configManager.isAlwaysShowNames() ||
		showArrayRawValuesCheckBox.isSelected() != configManager.isShowArrayRawValues() ||
		!newIconStyle.equals(configManager.getIconStyle());
}
}
