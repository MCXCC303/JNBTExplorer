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

	boolean hasName = tag.getName() != null && !tag.getName().isEmpty();
	int rows = hasName ? 2 : 1;

	JPanel panel = new JPanel(new GridLayout(rows, 2, 5, 5));
	panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

	if (hasName) {
		panel.add(new JLabel("Name:"));
		panel.add(new JLabel(tag.getName()));
	}

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
		case TAG_INT_ARRAY -> {
			int[] arr = ((TagIntArray) tag).getValue();
			if (arr.length == 4) {
				yield formatUuid(arr);
			}
			yield "";
		}
		default -> "";
	};
}

private String formatUuid(int[] arr) {
	if (arr == null || arr.length != 4) return "";
	return String.format("%08x-%04x-%04x-%04x-%012x", 
		arr[0], 
		(arr[1] >> 16) & 0xFFFF, 
		arr[1] & 0xFFFF, 
		(arr[2] >> 16) & 0xFFFF, 
		((long) (arr[2] & 0xFFFF) << 32) | ((long) arr[3] & 0xFFFFFFFFL));
}

private int[] parseUuid(String value) {
	value = value.trim().replace("-", "");
	if (value.length() != 32) return null;

	try {
		long mostSig = Long.parseLong(value.substring(0, 16), 16);
		long leastSig = Long.parseLong(value.substring(16, 32), 16);

		int[] arr = new int[4];
		arr[0] = (int) (mostSig >> 32);
		arr[1] = (int) (((mostSig >> 16) & 0xFFFFL) | ((mostSig & 0xFFFFL) << 16));
		arr[2] = (int) (((leastSig >> 48) & 0xFFFFL) | ((leastSig >> 32 & 0xFFFFL) << 16));
		arr[3] = (int) (leastSig & 0xFFFFFFFFL);
		return arr;
	} catch (NumberFormatException e) {
		return null;
	}
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
			case TAG_INT_ARRAY:
				int[] arr = ((TagIntArray) tag).getValue();
				if (arr.length == 4) {
					int[] parsed = parseUuid(value);
					if (parsed == null) {
						JOptionPane.showMessageDialog(this,
							"Invalid UUID format. Use: 00000000-0000-0000-0000-000000000000",
							"Error", JOptionPane.ERROR_MESSAGE);
						return false;
					}
					((TagIntArray) tag).setValue(parsed);
				}
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
