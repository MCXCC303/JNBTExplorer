package com.MCXCC.JNBTExplorer.nbt.ui;

import com.MCXCC.JNBTExplorer.nbt.tag.*;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Set;

public class CreateTagDialog extends JDialog {
private Tag newTag;
private boolean confirmed;
private JComboBox<TagType> typeCombo;
private JTextField nameField;
private JTextField valueField;
private JTable arrayTable;
private DefaultTableModel tableModel;
private JPanel mainPanel;
private JPanel valuePanel;
private JPanel arrayPanel;
private final CreateCallback callback;
private final Set<String> existingNames;
private JButton okButton;
private JLabel hintLabel;

public interface CreateCallback {
	void onCreateComplete(boolean confirmed, Tag newTag);
}

public CreateTagDialog(JFrame parent, Set<String> existingNames, CreateCallback callback) {
	super(parent, "Create New Tag", false);
	this.confirmed = false;
	this.existingNames = existingNames;
	this.callback = callback;
	initUI();
}

private void initUI() {
	setLayout(new BorderLayout());
	setSize(500, 200);
	setLocationRelativeTo(getParent());

	JPanel topPanel = new JPanel(new GridLayout(2, 2, 5, 5));
	topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

	topPanel.add(new JLabel("Type:"));
	typeCombo = new JComboBox<>(new TagType[]{
		TagType.TAG_BYTE, TagType.TAG_SHORT, TagType.TAG_INT, TagType.TAG_LONG,
		TagType.TAG_FLOAT, TagType.TAG_DOUBLE, TagType.TAG_BYTE_ARRAY,
		TagType.TAG_STRING, TagType.TAG_LIST, TagType.TAG_COMPOUND,
		TagType.TAG_INT_ARRAY, TagType.TAG_LONG_ARRAY, TagType.TAG_SHORT_ARRAY
	});
	typeCombo.addActionListener(e -> updateValuePanel());
	topPanel.add(typeCombo);

	topPanel.add(new JLabel("Name:"));
	nameField = new JTextField();
	topPanel.add(nameField);

	add(topPanel, BorderLayout.NORTH);

	mainPanel = new JPanel(new BorderLayout());
	mainPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

	valuePanel = new JPanel(new GridLayout(1, 2, 5, 5));
	JLabel valueLabel = new JLabel("Value:");
	valueField = new JTextField();
	valuePanel.add(valueLabel);
	valuePanel.add(valueField);

	arrayPanel = new JPanel(new BorderLayout());
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

	JScrollPane scrollPane = new JScrollPane(arrayTable);
	arrayPanel.add(scrollPane, BorderLayout.CENTER);

	JPanel buttonRowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
	JButton newButton = new JButton("New Element");
	newButton.addActionListener(e -> addNewElement());
	JButton removeButton = new JButton("Remove Selected");
	removeButton.addActionListener(e -> removeSelectedElement());
	buttonRowPanel.add(newButton);
	buttonRowPanel.add(removeButton);
	arrayPanel.add(buttonRowPanel, BorderLayout.SOUTH);

	mainPanel.add(valuePanel, BorderLayout.CENTER);
	add(mainPanel, BorderLayout.CENTER);

	updateValuePanel();

	JPanel bottomPanel = new JPanel(new BorderLayout());

	hintLabel = new JLabel(" ");
	hintLabel.setForeground(Color.RED);
	hintLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 5, 10));
	bottomPanel.add(hintLabel, BorderLayout.NORTH);

	JPanel buttonPanel = new JPanel();
	okButton = new JButton("OK");
	JButton cancelButton = new JButton("Cancel");

	okButton.addActionListener(e -> createTag());
	cancelButton.addActionListener(e -> {
		confirmed = false;
		if (callback != null) {
			callback.onCreateComplete(false, null);
		}
		dispose();
	});

	buttonPanel.add(okButton);
	buttonPanel.add(cancelButton);
	bottomPanel.add(buttonPanel, BorderLayout.SOUTH);

	add(bottomPanel, BorderLayout.SOUTH);

	nameField.getDocument().addDocumentListener(new DocumentListener() {
		@Override
		public void insertUpdate(DocumentEvent e) {
			validateName();
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			validateName();
		}

		@Override
		public void changedUpdate(DocumentEvent e) {
			validateName();
		}
	});

	addWindowListener(new java.awt.event.WindowAdapter() {
		@Override
		public void windowClosed(java.awt.event.WindowEvent e) {
			if (!confirmed && callback != null) {
				callback.onCreateComplete(false, null);
			}
		}
	});
}

private void validateName() {
	if (okButton == null || hintLabel == null) {
		return;
	}
	String name = nameField.getText();
	if (existingNames != null && existingNames.contains(name)) {
		okButton.setEnabled(false);
		hintLabel.setText("Duplicate name: \"" + name + "\" already exists");
	} else {
		okButton.setEnabled(true);
		hintLabel.setText(" ");
	}
}

private void updateValuePanel() {
	TagType type = (TagType) typeCombo.getSelectedItem();

	boolean isArray = (type == TagType.TAG_INT_ARRAY || type == TagType.TAG_LONG_ARRAY ||
		type == TagType.TAG_SHORT_ARRAY || type == TagType.TAG_BYTE_ARRAY);
	boolean isContainer = (type == TagType.TAG_LIST || type == TagType.TAG_COMPOUND);

	if (isContainer) {
		mainPanel.remove(valuePanel);
		mainPanel.remove(arrayPanel);
		setSize(500, 150);
	} else if (isArray) {
		mainPanel.remove(valuePanel);
		mainPanel.add(arrayPanel, BorderLayout.CENTER);
		updateArrayButtonText();
		tableModel.setRowCount(0);
		addNewElement();
		setSize(500, 400);
	} else {
		mainPanel.remove(arrayPanel);
		mainPanel.add(valuePanel, BorderLayout.CENTER);
		valueField.setText("");
		setSize(500, 230);
	}

	revalidate();
	repaint();
}

private void updateArrayButtonText() {
	TagType type = (TagType) typeCombo.getSelectedItem();
	String buttonText = switch (type) {
		case TAG_INT_ARRAY -> "New Int";
		case TAG_LONG_ARRAY -> "New Long";
		case TAG_SHORT_ARRAY -> "New Short";
		case TAG_BYTE_ARRAY -> "New Byte";
		default -> "New Element";
	};

	JPanel buttonRowPanel = (JPanel) arrayPanel.getComponent(1);
	((JButton) buttonRowPanel.getComponent(0)).setText(buttonText);
}

private void addNewElement() {
	int rowIndex = tableModel.getRowCount();
	TagType type = (TagType) typeCombo.getSelectedItem();

	String defaultValue = switch (type) {
		case TAG_INT_ARRAY -> "0";
		case TAG_LONG_ARRAY -> "0";
		case TAG_SHORT_ARRAY -> "0";
		case TAG_BYTE_ARRAY -> "0";
		default -> "0";
	};

	tableModel.addRow(new Object[]{rowIndex, defaultValue});
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

private void createTag() {
	try {
		TagType type = (TagType) typeCombo.getSelectedItem();
		String name = nameField.getText();

		switch (type) {
			case TAG_BYTE:
				newTag = new TagByte(name, Byte.parseByte(valueField.getText()));
				break;
			case TAG_SHORT:
				newTag = new TagShort(name, Short.parseShort(valueField.getText()));
				break;
			case TAG_INT:
				newTag = new TagInt(name, Integer.parseInt(valueField.getText()));
				break;
			case TAG_LONG:
				newTag = new TagLong(name, Long.parseLong(valueField.getText()));
				break;
			case TAG_FLOAT:
				newTag = new TagFloat(name, Float.parseFloat(valueField.getText()));
				break;
			case TAG_DOUBLE:
				newTag = new TagDouble(name, Double.parseDouble(valueField.getText()));
				break;
			case TAG_STRING:
				newTag = new TagString(name, valueField.getText());
				break;
			case TAG_BYTE_ARRAY:
				newTag = new TagByteArray(name, parseByteArray());
				break;
			case TAG_INT_ARRAY:
				newTag = new TagIntArray(name, parseIntArray());
				break;
			case TAG_LONG_ARRAY:
				newTag = new TagLongArray(name, parseLongArray());
				break;
			case TAG_SHORT_ARRAY:
				newTag = new TagShortArray(name, parseShortArray());
				break;
			case TAG_LIST:
				newTag = new TagList(name, TagType.TAG_END);
				break;
			case TAG_COMPOUND:
				newTag = new TagCompound(name);
				break;
		}

		confirmed = true;
		if (callback != null) {
			callback.onCreateComplete(true, newTag);
		}
		dispose();
	} catch (NumberFormatException e) {
		JOptionPane.showMessageDialog(this, "Invalid value format: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
	}
}

private byte[] parseByteArray() {
	int count = tableModel.getRowCount();
	byte[] arr = new byte[count];
	for (int i = 0; i < count; i++) {
		Object val = tableModel.getValueAt(i, 1);
		arr[i] = Byte.parseByte(val.toString());
	}
	return arr;
}

private int[] parseIntArray() {
	int count = tableModel.getRowCount();
	int[] arr = new int[count];
	for (int i = 0; i < count; i++) {
		Object val = tableModel.getValueAt(i, 1);
		arr[i] = Integer.parseInt(val.toString());
	}
	return arr;
}

private long[] parseLongArray() {
	int count = tableModel.getRowCount();
	long[] arr = new long[count];
	for (int i = 0; i < count; i++) {
		Object val = tableModel.getValueAt(i, 1);
		arr[i] = Long.parseLong(val.toString());
	}
	return arr;
}

private short[] parseShortArray() {
	int count = tableModel.getRowCount();
	short[] arr = new short[count];
	for (int i = 0; i < count; i++) {
		Object val = tableModel.getValueAt(i, 1);
		arr[i] = Short.parseShort(val.toString());
	}
	return arr;
}

}
