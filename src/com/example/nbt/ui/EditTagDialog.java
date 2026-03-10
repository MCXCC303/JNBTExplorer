package com.example.nbt.ui;

import com.example.nbt.tag.*;

import javax.swing.*;
import java.awt.*;

public class EditTagDialog extends JDialog {
private final Tag tag;
private boolean confirmed;
private JTextField valueField;

public EditTagDialog(JFrame parent, Tag tag) {
	super(parent, "Edit " + tag.getType().getName(), true);
	this.tag = tag;
	this.confirmed = false;
	initUI();
}

private void initUI() {
	setLayout(new BorderLayout());
	setSize(400, 150);
	setLocationRelativeTo(getParent());

	JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
	panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

	panel.add(new JLabel("Name:"));
	panel.add(new JLabel(tag.getName()));

	panel.add(new JLabel("Value:"));
	valueField = new JTextField(getValueString());
	panel.add(valueField);

	add(panel, BorderLayout.CENTER);

	JPanel buttonPanel = new JPanel();
	JButton okButton = new JButton("OK");
	JButton cancelButton = new JButton("Cancel");

	okButton.addActionListener(e -> {
		if (parseValue()) {
			confirmed = true;
			dispose();
		}
	});

	cancelButton.addActionListener(e -> dispose());

	buttonPanel.add(okButton);
	buttonPanel.add(cancelButton);
	add(buttonPanel, BorderLayout.SOUTH);
}

private String getValueString() {
	return switch (tag.getType()) {
		case TAG_BYTE -> String.valueOf(((TagByte) tag).getValue());
		case TAG_SHORT -> String.valueOf(((TagShort) tag).getValue());
		case TAG_INT -> String.valueOf(((TagInt) tag).getValue());
		case TAG_LONG -> String.valueOf(((TagLong) tag).getValue());
		case TAG_FLOAT -> String.valueOf(((TagFloat) tag).getValue());
		case TAG_DOUBLE -> String.valueOf(((TagDouble) tag).getValue());
		case TAG_STRING -> ((TagString) tag).getValue();
		default -> "";
	};
}

private boolean parseValue() {
	try {
		String value = valueField.getText();
		switch (tag.getType()) {
			case TAG_BYTE:
				((TagByte) tag).setValue(Byte.parseByte(value));
				break;
			case TAG_SHORT:
				((TagShort) tag).setValue(Short.parseShort(value));
				break;
			case TAG_INT:
				((TagInt) tag).setValue(Integer.parseInt(value));
				break;
			case TAG_LONG:
				((TagLong) tag).setValue(Long.parseLong(value));
				break;
			case TAG_FLOAT:
				((TagFloat) tag).setValue(Float.parseFloat(value));
				break;
			case TAG_DOUBLE:
				((TagDouble) tag).setValue(Double.parseDouble(value));
				break;
			case TAG_STRING:
				((TagString) tag).setValue(value);
				break;
		}
		return true;
	} catch (NumberFormatException e) {
		JOptionPane.showMessageDialog(this, "Invalid value format", "Error", JOptionPane.ERROR_MESSAGE);
		return false;
	}
}

public boolean isConfirmed() {
	return confirmed;
}
}
