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
private JCheckBox dragAndDropCheckBox;
private JCheckBox showTypeIconsCheckBox;
private JCheckBox alwaysShowNamesCheckBox;
private JCheckBox showArrayRawValuesCheckBox;
private boolean confirmed = false;

public PreferencesDialog(JFrame parent, ConfigManager configManager, Logger logger) {
	super(parent, "Preferences", true);
	this.configManager = configManager;
	this.logger = logger;
	initComponents();
	setLocationRelativeTo(parent);
}

private void initComponents() {
	setSize(600, 350);
	setLayout(new BorderLayout());

	JPanel contentPanel = new JPanel();
	contentPanel.setLayout(new GridLayout(6, 2, 10, 10));
	contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

	// Default Type Sort Mode
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
	contentPanel.add(typeSortModeLabel);
	contentPanel.add(typeSortModeComboBox);

	// Default Name Sort Mode
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
	contentPanel.add(nameSortModeLabel);
	contentPanel.add(nameSortModeComboBox);

	// Enable Drag and Drop
	JLabel dragAndDropLabel = new JLabel("*Enable Drag and Drop (Beta):");
	dragAndDropCheckBox = new JCheckBox();
	dragAndDropCheckBox.setSelected(configManager.isDragAndDropEnabled());
	contentPanel.add(dragAndDropLabel);
	contentPanel.add(dragAndDropCheckBox);

	// Show Type Icons
	JLabel showTypeIconsLabel = new JLabel("Show Type Icons:");
	showTypeIconsCheckBox = new JCheckBox();
	showTypeIconsCheckBox.setSelected(configManager.isShowTypeIcons());
	contentPanel.add(showTypeIconsLabel);
	contentPanel.add(showTypeIconsCheckBox);

	// Always Show Names
	JLabel alwaysShowNamesLabel = new JLabel("Always Show Names:");
	alwaysShowNamesCheckBox = new JCheckBox();
	alwaysShowNamesCheckBox.setSelected(configManager.isAlwaysShowNames());
	contentPanel.add(alwaysShowNamesLabel);
	contentPanel.add(alwaysShowNamesCheckBox);

	// Show Array Raw Values
	JLabel showArrayRawValuesLabel = new JLabel("Show Array Raw Values:");
	showArrayRawValuesCheckBox = new JCheckBox();
	showArrayRawValuesCheckBox.setSelected(configManager.isShowArrayRawValues());
	contentPanel.add(showArrayRawValuesLabel);
	contentPanel.add(showArrayRawValuesCheckBox);

	add(contentPanel, BorderLayout.CENTER);

	// Bottom panel with note and buttons
	JPanel bottomPanel = new JPanel(new BorderLayout());
	bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 10, 20));

	// Note label
	JLabel noteLabel = new JLabel("*Require Restart");
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
	// Save default sort modes
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

	// Save other preferences
	configManager.setDragAndDropEnabled(dragAndDropCheckBox.isSelected());
	configManager.setShowTypeIcons(showTypeIconsCheckBox.isSelected());
	configManager.setAlwaysShowNames(alwaysShowNamesCheckBox.isSelected());
	configManager.setShowArrayRawValues(showArrayRawValuesCheckBox.isSelected());

	// Save config to file
	configManager.saveConfig();
	logger.info("Preferences saved");
}

public boolean isConfirmed() {
	return confirmed;
}

public boolean shouldRefreshUI() {
	return showTypeIconsCheckBox.isSelected() != configManager.isShowTypeIcons() ||
		alwaysShowNamesCheckBox.isSelected() != configManager.isAlwaysShowNames() ||
		showArrayRawValuesCheckBox.isSelected() != configManager.isShowArrayRawValues();
}
}
