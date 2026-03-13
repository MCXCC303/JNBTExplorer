package com.MCXCC.JNBTExplorer.nbt.ui;

import com.MCXCC.JNBTExplorer.nbt.tag.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class EditTagDialog extends JDialog {
private final Tag tag;
private boolean confirmed;
private JTextField nameField;
private JTextField valueField;
private JTextField hexArea;
private JTable arrayTable;
private DefaultTableModel tableModel;
private boolean isArrayEditor = false;

public interface EditCallback {
	void onEditComplete(boolean confirmed, Tag tag);
}

public EditTagDialog(JFrame parent, Tag tag, EditCallback callback) {
	super(parent, "Edit " + tag.getType().getName(), false);
	this.tag = tag;
	this.confirmed = false;
	initUI();

	addWindowListener(new java.awt.event.WindowAdapter() {
		@Override
		public void windowClosed(java.awt.event.WindowEvent e) {
			if (callback != null) {
				callback.onEditComplete(confirmed, tag);
			}
		}
	});
}

private void initUI() {
	setLayout(new BorderLayout());

	boolean hasName = tag.getName() != null && !tag.getName().isEmpty();
	TagType type = tag.getType();

	isArrayEditor = (type == TagType.TAG_INT_ARRAY || type == TagType.TAG_LONG_ARRAY || type == TagType.TAG_SHORT_ARRAY || type == TagType.TAG_BYTE_ARRAY);

	if (isArrayEditor) {
		setSize(800, 400);
	} else if (hasName) {
		setSize(500, 150);
	} else {
		setSize(500, 120);
	}

	setLocationRelativeTo(getParent());

	JPanel mainPanel = new JPanel(new BorderLayout());
	mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

	JPanel infoPanel = new JPanel(new GridLayout(hasName ? 2 : 1, 2, 5, 5));
	if (hasName) {
		infoPanel.add(new JLabel("Name:"));
		nameField = new JTextField(tag.getName());
		infoPanel.add(nameField);
	}

	if (isArrayEditor) {
		infoPanel.add(new JLabel("Value:"));
		valueField = new JTextField(getArraySummary());
		valueField.setEditable(false);
		infoPanel.add(valueField);

		mainPanel.add(infoPanel, BorderLayout.NORTH);

		JPanel arrayPanel = new JPanel(new BorderLayout());
		arrayPanel.setBorder(BorderFactory.createTitledBorder("Array Elements"));

		String[] columnNames = {"Index", "Value"};
		tableModel = new DefaultTableModel(columnNames, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return column == 1;
			}
		};

		arrayTable = new JTable(tableModel);
		arrayTable.getColumnModel().getColumn(0).setPreferredWidth(60);
		arrayTable.getColumnModel().getColumn(1).setPreferredWidth(200);

		loadArrayData();

		JScrollPane scrollPane = new JScrollPane(arrayTable);
		arrayPanel.add(scrollPane, BorderLayout.CENTER);

		JPanel buttonRowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		String buttonText = getNewButtonText();
		JButton newButton = new JButton(buttonText);
		newButton.addActionListener(e -> addNewElement());
		JButton removeButton = new JButton("Remove Selected");
		removeButton.addActionListener(e -> removeSelectedElement());
		buttonRowPanel.add(newButton);
		buttonRowPanel.add(removeButton);
		arrayPanel.add(buttonRowPanel, BorderLayout.SOUTH);

		mainPanel.add(arrayPanel, BorderLayout.CENTER);
	} else {
		infoPanel.add(new JLabel("Value:"));
		valueField = new JTextField(getValueString());
		infoPanel.add(valueField);

		boolean isHexEditor = tag.getType() == TagType.TAG_INT_ARRAY && ((TagIntArray) tag).getValue().length == 4;
		if (isHexEditor) {
			valueField.setEditable(false);
		}

		mainPanel.add(infoPanel, BorderLayout.NORTH);

		if (isHexEditor) {
			JPanel hexPanel = new JPanel(new BorderLayout());
			hexPanel.setBorder(BorderFactory.createTitledBorder("Hex Editor"));

			hexArea = new JTextField(getHexString(((TagIntArray) tag).getValue()));
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

private String getNewButtonText() {
	return switch (tag.getType()) {
		case TAG_INT_ARRAY -> "New Int";
		case TAG_LONG_ARRAY -> "New Long";
		case TAG_SHORT_ARRAY -> "New Short";
		case TAG_BYTE_ARRAY -> "New Byte";
		default -> "New Element";
	};
}

private String getArraySummary() {
	boolean showRaw = com.MCXCC.JNBTExplorer.nbt.model.NBTNode.isShowArrayRawValues();

	return switch (tag.getType()) {
		case TAG_INT_ARRAY -> {
			int[] arr = ((TagIntArray) tag).getValue();
			if (showRaw) {
				yield formatIntArray(arr);
			} else if (arr.length == 4) {
				yield formatUuid(arr);
			} else {
				yield formatIntArrayHex(arr);
			}
		}
		case TAG_LONG_ARRAY -> {
			long[] arr = ((TagLongArray) tag).getValue();
			if (showRaw) {
				yield formatLongArray(arr);
			} else {
				yield formatLongArrayHex(arr);
			}
		}
		case TAG_SHORT_ARRAY -> {
			short[] arr = ((TagShortArray) tag).getValue();
			if (showRaw) {
				yield formatShortArray(arr);
			} else {
				yield formatShortArrayHex(arr);
			}
		}
		case TAG_BYTE_ARRAY -> {
			byte[] arr = ((TagByteArray) tag).getValue();
			if (showRaw) {
				yield formatByteArray(arr);
			} else {
				yield formatByteArrayHex(arr);
			}
		}
		default -> "[]";
	};
}

private String formatIntArrayHex(int[] arr) {
	StringBuilder sb = new StringBuilder();
	for (int i = 0; i < arr.length; i++) {
		sb.append(String.format("%08x", arr[i]));
		if (i < arr.length - 1) {
			sb.append("-");
		}
	}
	return sb.toString();
}

private String formatLongArrayHex(long[] arr) {
	StringBuilder sb = new StringBuilder();
	for (int i = 0; i < arr.length; i++) {
		sb.append(String.format("%016x", arr[i]));
		if (i < arr.length - 1) {
			sb.append("-");
		}
	}
	return sb.toString();
}

private String formatShortArrayHex(short[] arr) {
	StringBuilder sb = new StringBuilder();
	for (int i = 0; i < arr.length; i++) {
		sb.append(String.format("%04x", arr[i] & 0xFFFF));
		if (i < arr.length - 1) {
			sb.append("-");
		}
	}
	return sb.toString();
}

private String formatByteArrayHex(byte[] arr) {
	StringBuilder sb = new StringBuilder();
	for (int i = 0; i < arr.length; i++) {
		sb.append(String.format("%02x", arr[i] & 0xFF));
		if (i < arr.length - 1) {
			sb.append("-");
		}
	}
	return sb.toString();
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

private String formatByteArray(byte[] arr) {
	StringBuilder sb = new StringBuilder();
	for (int i = 0; i < arr.length; i++) {
		sb.append(arr[i] & 0xFF);
		if (i < arr.length - 1) {
			sb.append(", ");
		}
	}
	return sb.toString();
}

private void loadArrayData() {
	tableModel.setRowCount(0);

	switch (tag.getType()) {
		case TAG_INT_ARRAY -> {
			int[] arr = ((TagIntArray) tag).getValue();
			for (int i = 0; i < arr.length; i++) {
				tableModel.addRow(new Object[]{i, String.valueOf(arr[i])});
			}
		}
		case TAG_LONG_ARRAY -> {
			long[] arr = ((TagLongArray) tag).getValue();
			for (int i = 0; i < arr.length; i++) {
				tableModel.addRow(new Object[]{i, String.valueOf(arr[i])});
			}
		}
		case TAG_SHORT_ARRAY -> {
			short[] arr = ((TagShortArray) tag).getValue();
			for (int i = 0; i < arr.length; i++) {
				tableModel.addRow(new Object[]{i, String.valueOf(arr[i])});
			}
		}
		case TAG_BYTE_ARRAY -> {
			byte[] arr = ((TagByteArray) tag).getValue();
			for (int i = 0; i < arr.length; i++) {
				tableModel.addRow(new Object[]{i, String.valueOf(arr[i] & 0xFF)});
			}
		}
	}
}

private void addNewElement() {
	int newIndex = tableModel.getRowCount();
	tableModel.addRow(new Object[]{newIndex, "0"});
}

private void removeSelectedElement() {
	int selectedRow = arrayTable.getSelectedRow();
	if (selectedRow >= 0) {
		tableModel.removeRow(selectedRow);
		for (int i = 0; i < tableModel.getRowCount(); i++) {
			tableModel.setValueAt(i, i, 0);
		}
	}
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
			if (com.MCXCC.JNBTExplorer.nbt.model.NBTNode.isShowArrayRawValues()) {
				yield formatIntArray(arr);
			} else {
				yield "[" + arr.length + " ints]";
			}
		}
		case TAG_LONG_ARRAY -> {
			long[] arr = ((TagLongArray) tag).getValue();
			if (com.MCXCC.JNBTExplorer.nbt.model.NBTNode.isShowArrayRawValues()) {
				yield formatLongArray(arr);
			} else {
				yield "[" + arr.length + " longs]";
			}
		}
		case TAG_SHORT_ARRAY -> {
			short[] arr = ((TagShortArray) tag).getValue();
			if (com.MCXCC.JNBTExplorer.nbt.model.NBTNode.isShowArrayRawValues()) {
				yield formatShortArray(arr);
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

private String formatIntArray(int[] arr) {
	StringBuilder sb = new StringBuilder();
	for (int i = 0; i < arr.length; i++) {
		sb.append(arr[i]);
		if (i < arr.length - 1) {
			sb.append(", ");
		}
	}
	return sb.toString();
}

private String formatLongArray(long[] arr) {
	StringBuilder sb = new StringBuilder();
	for (int i = 0; i < arr.length; i++) {
		sb.append(arr[i]);
		if (i < arr.length - 1) {
			sb.append(", ");
		}
	}
	return sb.toString();
}

private String formatShortArray(short[] arr) {
	StringBuilder sb = new StringBuilder();
	for (int i = 0; i < arr.length; i++) {
		sb.append(arr[i]);
		if (i < arr.length - 1) {
			sb.append(", ");
		}
	}
	return sb.toString();
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

private boolean parseValue() {
	try {
		if (nameField != null) {
			tag.setName(nameField.getText().trim());
		}

		if (isArrayEditor) {
			return parseArrayValue();
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
				if (arr.length == 4 && hexArea != null) {
					int[] parsed = parseHex(hexArea.getText());
					if (parsed == null) {
						JOptionPane.showMessageDialog(this,
							"Invalid hex format. Use 8 hex digits per integer, separated by spaces.",
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

private boolean parseArrayValue() {
	int rowCount = tableModel.getRowCount();
	if (rowCount == 0) {
		JOptionPane.showMessageDialog(this, "Array must have at least one element", "Error", JOptionPane.ERROR_MESSAGE);
		return false;
	}

	try {
		switch (tag.getType()) {
			case TAG_INT_ARRAY -> {
				int[] arr = new int[rowCount];
				for (int i = 0; i < rowCount; i++) {
					Object val = tableModel.getValueAt(i, 1);
					arr[i] = Integer.parseInt(val != null ? val.toString().trim() : "0");
				}
				((TagIntArray) tag).setValue(arr);
			}
			case TAG_LONG_ARRAY -> {
				long[] arr = new long[rowCount];
				for (int i = 0; i < rowCount; i++) {
					Object val = tableModel.getValueAt(i, 1);
					arr[i] = Long.parseLong(val != null ? val.toString().trim() : "0");
				}
				((TagLongArray) tag).setValue(arr);
			}
			case TAG_SHORT_ARRAY -> {
				short[] arr = new short[rowCount];
				for (int i = 0; i < rowCount; i++) {
					Object val = tableModel.getValueAt(i, 1);
					arr[i] = Short.parseShort(val != null ? val.toString().trim() : "0");
				}
				((TagShortArray) tag).setValue(arr);
			}
			case TAG_BYTE_ARRAY -> {
				byte[] arr = new byte[rowCount];
				for (int i = 0; i < rowCount; i++) {
					Object val = tableModel.getValueAt(i, 1);
					arr[i] = (byte) Integer.parseInt(val != null ? val.toString().trim() : "0");
				}
				((TagByteArray) tag).setValue(arr);
			}
		}
		return true;
	} catch (NumberFormatException e) {
		JOptionPane.showMessageDialog(this, "Invalid value format in array", "Error", JOptionPane.ERROR_MESSAGE);
		return false;
	}
}

}
