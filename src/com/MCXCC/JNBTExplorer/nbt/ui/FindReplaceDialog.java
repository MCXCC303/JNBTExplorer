package com.MCXCC.JNBTExplorer.nbt.ui;

import javax.swing.*;
import java.awt.*;

public class FindReplaceDialog extends JDialog {
private boolean confirmed = false;
private JTextField findNameField;
private JTextField findValueField;
private JTextField replaceNameField;
private JTextField replaceValueField;
private JCheckBox findNameCheckBox;
private JCheckBox findValueCheckBox;
private JCheckBox replaceNameCheckBox;
private JCheckBox replaceValueCheckBox;
private JComboBox<String> nameOperatorCombo;
private JComboBox<String> valueOperatorCombo;

public FindReplaceDialog(JFrame parent) {
	super(parent, "Find and Replace (Beta)", true);
	initUI();
}

private void initUI() {
	setSize(550, 280);
	setLocationRelativeTo(getParent());
	setLayout(new BorderLayout());

	JPanel contentPanel = new JPanel(new GridLayout(4, 3, 10, 10));
	contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

	// Row 1: Find Name
	findNameCheckBox = new JCheckBox("Find Name:", true);
	findNameField = new JTextField(15);
	nameOperatorCombo = new JComboBox<>(new String[]{"Contains", "Equals", "Starts With", "Ends With"});
	findNameField.addCaretListener(e -> {
		if (!findNameField.getText().isEmpty()) {
			findNameCheckBox.setSelected(true);
		}
	});

	contentPanel.add(findNameCheckBox);
	contentPanel.add(findNameField);
	contentPanel.add(nameOperatorCombo);

	// Row 2: Find Value
	findValueCheckBox = new JCheckBox("Find Value:", true);
	findValueField = new JTextField(15);
	valueOperatorCombo = new JComboBox<>(new String[]{"Contains", "Equals", "Starts With", "Ends With"});
	findValueField.addCaretListener(e -> {
		if (!findValueField.getText().isEmpty()) {
			findValueCheckBox.setSelected(true);
		}
	});

	contentPanel.add(findValueCheckBox);
	contentPanel.add(findValueField);
	contentPanel.add(valueOperatorCombo);

	// Row 3: Replace Name
	replaceNameCheckBox = new JCheckBox("Replace Name:", false);
	replaceNameField = new JTextField(15);
	replaceNameField.setEnabled(false);
	replaceNameCheckBox.addActionListener(e -> replaceNameField.setEnabled(replaceNameCheckBox.isSelected()));

	contentPanel.add(replaceNameCheckBox);
	contentPanel.add(replaceNameField);
	contentPanel.add(new JLabel());

	// Row 4: Replace Value
	replaceValueCheckBox = new JCheckBox("Replace Value:", false);
	replaceValueField = new JTextField(15);
	replaceValueField.setEnabled(false);
	replaceValueCheckBox.addActionListener(e -> replaceValueField.setEnabled(replaceValueCheckBox.isSelected()));

	contentPanel.add(replaceValueCheckBox);
	contentPanel.add(replaceValueField);
	contentPanel.add(new JLabel());

	add(contentPanel, BorderLayout.CENTER);

	// Button panel
	JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
	JButton findNextButton = new JButton("Find Next");
	JButton replaceNextButton = new JButton("Replace Next");
	JButton replaceAllButton = new JButton("Replace All");
	JButton cancelButton = new JButton("Cancel");

	findNextButton.addActionListener(e -> {
		confirmed = true;
		dispose();
	});

	replaceNextButton.addActionListener(e -> {
		confirmed = true;
		dispose();
	});

	replaceAllButton.addActionListener(e -> {
		confirmed = true;
		dispose();
	});

	cancelButton.addActionListener(e -> dispose());

	buttonPanel.add(findNextButton);
	buttonPanel.add(replaceNextButton);
	buttonPanel.add(replaceAllButton);
	buttonPanel.add(cancelButton);

	add(buttonPanel, BorderLayout.SOUTH);

	getRootPane().setDefaultButton(findNextButton);
}

public boolean isConfirmed() {
	return confirmed;
}

public boolean isFindByName() {
	return findNameCheckBox.isSelected();
}

public boolean isFindByValue() {
	return findValueCheckBox.isSelected();
}

public boolean isReplaceName() {
	return replaceNameCheckBox.isSelected();
}

public boolean isReplaceValue() {
	return replaceValueCheckBox.isSelected();
}

public String getFindName() {
	return findNameField.getText();
}

public String getFindValue() {
	return findValueField.getText();
}

public String getReplaceName() {
	return replaceNameField.getText();
}

public String getReplaceValue() {
	return replaceValueField.getText();
}

public String getNameOperator() {
	return (String) nameOperatorCombo.getSelectedItem();
}

public String getValueOperator() {
	return (String) valueOperatorCombo.getSelectedItem();
}

public boolean matchesName(String name) {
	if (!isFindByName() || getFindName().isEmpty()) return true;
	String search = getFindName().toLowerCase();
	String target = name != null ? name.toLowerCase() : "";
	return matchesOperator(target, search, getNameOperator());
}

public boolean matchesValue(String value) {
	if (!isFindByValue() || getFindValue().isEmpty()) return true;
	String search = getFindValue().toLowerCase();
	String target = value != null ? value.toLowerCase() : "";
	return matchesOperator(target, search, getValueOperator());
}

private boolean matchesOperator(String target, String search, String operator) {
	return switch (operator) {
		case "Equals" -> target.equals(search);
		case "Contains" -> target.contains(search);
		case "Starts With" -> target.startsWith(search);
		case "Ends With" -> target.endsWith(search);
		default -> target.contains(search);
	};
}

public boolean isSearchByName() {
	return findNameCheckBox.isSelected();
}

public boolean isSearchByValue() {
	return findValueCheckBox.isSelected();
}

public boolean isSearchByType() {
	return false;
}

public String getSearchText() {
	StringBuilder sb = new StringBuilder();
	if (isSearchByName() && !getFindName().isEmpty()) {
		sb.append(getFindName());
	}
	if (isSearchByValue() && !getFindValue().isEmpty()) {
		if (sb.length() > 0) sb.append(" ");
		sb.append(getFindValue());
	}
	return sb.toString();
}
}
