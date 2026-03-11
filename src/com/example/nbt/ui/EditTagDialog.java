package com.example.nbt.ui;

import com.example.nbt.tag.*;

import javax.swing.*;
import java.awt.*;

public class EditTagDialog extends JDialog {
private final Tag tag;
private boolean confirmed;
private JTextField valueField;
private JTextField nameField;
private JTextField hexArea;

public EditTagDialog(JFrame parent, Tag tag) {
	super(parent, "Edit " + tag.getType().getName(), true);
	this.tag = tag;
	this.confirmed = false;
	initUI();
}

private void initUI() {
	setLayout(new BorderLayout());

	// Set different sizes based on tag type
	TagType type = tag.getType();
	if (type == TagType.TAG_INT_ARRAY || type == TagType.TAG_LONG_ARRAY || type == TagType.TAG_SHORT_ARRAY) {
		setSize(800, 260);
	} else {
		setSize(500, 150);
	}

	setLocationRelativeTo(getParent());

	boolean hasName = tag.getName() != null && !tag.getName().isEmpty();

	JPanel mainPanel = new JPanel(new BorderLayout());
	mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

	JPanel infoPanel = new JPanel(new GridLayout(hasName ? 2 : 1, 2, 5, 5));
	if (hasName) {
		infoPanel.add(new JLabel("Name:"));
		nameField = new JTextField(tag.getName());
		infoPanel.add(nameField);
	}

	infoPanel.add(new JLabel("Value:"));
	valueField = new JTextField(getValueString());
	valueField.setEditable(false);
	infoPanel.add(valueField);

	mainPanel.add(infoPanel, BorderLayout.NORTH);

	if (tag.getType() == TagType.TAG_INT_ARRAY) {
		int[] arr = ((TagIntArray) tag).getValue();
		if (arr.length == 4) {
			JPanel hexPanel = new JPanel(new BorderLayout());
			hexPanel.setBorder(BorderFactory.createTitledBorder("Hex Editor"));

			hexArea = new JTextField(getHexString(arr));
			hexArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

			JScrollPane scrollPane = new JScrollPane(hexArea);
			hexPanel.add(scrollPane, BorderLayout.CENTER);

			mainPanel.add(hexPanel, BorderLayout.CENTER);
		}
	}

	add(mainPanel, BorderLayout.CENTER);

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
			if (arr.length == 4 && !com.example.nbt.model.NBTNode.isShowArrayRawValues()) {
				yield formatUuid(arr);
			} else if (com.example.nbt.model.NBTNode.isShowArrayRawValues()) {
				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < arr.length; i++) {
					sb.append(arr[i]);
					if (i < arr.length - 1) {
						sb.append(", ");
					}
				}
				yield sb.toString();
			} else {
				yield "[" + arr.length + " ints]";
			}
		}
		case TAG_LONG_ARRAY -> {
			long[] arr = ((TagLongArray) tag).getValue();
			if (com.example.nbt.model.NBTNode.isShowArrayRawValues()) {
				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < arr.length; i++) {
					sb.append(arr[i]);
					if (i < arr.length - 1) {
						sb.append(", ");
					}
				}
				yield sb.toString();
			} else {
				yield "[" + arr.length + " longs]";
			}
		}
		case TAG_SHORT_ARRAY -> {
			short[] arr = ((TagShortArray) tag).getValue();
			if (com.example.nbt.model.NBTNode.isShowArrayRawValues()) {
				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < arr.length; i++) {
					sb.append(arr[i]);
					if (i < arr.length - 1) {
						sb.append(", ");
					}
				}
				yield sb.toString();
			} else {
				yield "[" + arr.length + " shorts]";
			}
		}
		default -> "";
	};
}

private String getHexString(int[] arr) {
	StringBuilder sb = new StringBuilder();
	for (int i = 0; i < arr.length; i++) {
		byte[] bytes = new byte[4];
		bytes[0] = (byte) (arr[i] & 0xFF);
		bytes[1] = (byte) ((arr[i] >> 8) & 0xFF);
		bytes[2] = (byte) ((arr[i] >> 16) & 0xFF);
		bytes[3] = (byte) ((arr[i] >> 24) & 0xFF);
		for (int j = 0; j < 4; j++) {
			sb.append(String.format("%02x", bytes[j] & 0xFF));
			if (j < 3) {
				sb.append(" ");
			}
		}
		if (i < arr.length - 1) {
			sb.append("  ");
		}
	}
	return sb.toString();
}

private String formatUuid(int[] arr) {
	if (arr == null || arr.length != 4) return "";
	return String.format("%08x-%04x-%04x-%04x-%012x",
		arr[0],
		(arr[1] >> 16) & 0xFFFF,
		(arr[1] & 0xFFFF),
		(arr[2] >> 16) & 0xFFFF,
		((long) (arr[2] & 0xFFFF) << 32) | ((long) arr[3] & 0xFFFFFFFFL));
}

private int[] parseHex(String hex) {
	hex = hex.trim().replaceAll("\\s+", "");
	if (hex.length() != 32) return null;

	try {
		int[] arr = new int[4];
		for (int i = 0; i < 4; i++) {
			int value = 0;
			for (int j = 0; j < 4; j++) {
				String byteStr = hex.substring(i * 8 + j * 2, i * 8 + j * 2 + 2);
				int byteVal = Integer.parseInt(byteStr, 16);
				value |= (byteVal & 0xFF) << (j * 8);
			}
			arr[i] = value;
		}
		return arr;
	} catch (NumberFormatException e) {
		return null;
	}
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
		// Update tag name if provided
		if (nameField != null) {
			tag.setName(nameField.getText().trim());
		}

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
					if (hexArea != null) {
						int[] parsed = parseHex(hexArea.getText());
						if (parsed == null) {
							JOptionPane.showMessageDialog(this,
								"Invalid hex format. Use 8 hex digits per integer, separated by spaces.",
								"Error", JOptionPane.ERROR_MESSAGE);
							return false;
						}
						((TagIntArray) tag).setValue(parsed);
					} else {
						int[] parsed = parseUuid(value);
						if (parsed == null) {
							JOptionPane.showMessageDialog(this,
								"Invalid UUID format. Use: 00000000-0000-0000-0000-000000000000",
								"Error", JOptionPane.ERROR_MESSAGE);
							return false;
						}
						((TagIntArray) tag).setValue(parsed);
					}
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