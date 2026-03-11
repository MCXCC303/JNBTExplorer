package com.example.nbt.ui;

import com.example.nbt.tag.*;

import javax.swing.*;
import java.awt.*;

public class CreateTagDialog extends JDialog {
private Tag newTag;
private boolean confirmed;
private JComboBox<TagType> typeCombo;
private JTextField nameField;
private JTextField valueField;

public CreateTagDialog(JFrame parent) {
	super(parent, "Create New Tag", true);
	this.confirmed = false;
	initUI();
}

private void initUI() {
	setLayout(new BorderLayout());
	setSize(400, 200);
	setLocationRelativeTo(getParent());

	JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));
	panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

	panel.add(new JLabel("Type:"));
	typeCombo = new JComboBox<>(new TagType[]{
		TagType.TAG_BYTE, TagType.TAG_SHORT, TagType.TAG_INT, TagType.TAG_LONG,
		TagType.TAG_FLOAT, TagType.TAG_DOUBLE, TagType.TAG_BYTE_ARRAY,
		TagType.TAG_STRING, TagType.TAG_LIST, TagType.TAG_COMPOUND,
		TagType.TAG_INT_ARRAY, TagType.TAG_LONG_ARRAY
	});
	panel.add(typeCombo);

	panel.add(new JLabel("Name:"));
	nameField = new JTextField();
	panel.add(nameField);

	panel.add(new JLabel("Value (for simple types):"));
	valueField = new JTextField();
	panel.add(valueField);

	add(panel, BorderLayout.CENTER);

	JPanel buttonPanel = new JPanel();
	JButton okButton = new JButton("OK");
	JButton cancelButton = new JButton("Cancel");

	okButton.addActionListener(e -> createTag());
	cancelButton.addActionListener(e -> dispose());

	buttonPanel.add(okButton);
	buttonPanel.add(cancelButton);
	add(buttonPanel, BorderLayout.SOUTH);
}

private void createTag() {
	try {
		TagType type = (TagType) typeCombo.getSelectedItem();
		String name = nameField.getText();
		String value = valueField.getText();

		switch (type) {
			case TAG_BYTE:
				newTag = new TagByte(name, Byte.parseByte(value));
				break;
			case TAG_SHORT:
				newTag = new TagShort(name, Short.parseShort(value));
				break;
			case TAG_INT:
				newTag = new TagInt(name, Integer.parseInt(value));
				break;
			case TAG_LONG:
				newTag = new TagLong(name, Long.parseLong(value));
				break;
			case TAG_FLOAT:
				newTag = new TagFloat(name, Float.parseFloat(value));
				break;
			case TAG_DOUBLE:
				newTag = new TagDouble(name, Double.parseDouble(value));
				break;
			case TAG_STRING:
				newTag = new TagString(name, value);
				break;
			case TAG_BYTE_ARRAY:
				newTag = new TagByteArray(name, new byte[0]);
				break;
			case TAG_INT_ARRAY:
				newTag = new TagIntArray(name, new int[0]);
				break;
			case TAG_LONG_ARRAY:
				newTag = new TagLongArray(name, new long[0]);
				break;
			case TAG_LIST:
				newTag = new TagList(name, TagType.TAG_END);
				break;
			case TAG_COMPOUND:
				newTag = new TagCompound(name);
				break;
		}

		confirmed = true;
		dispose();
	} catch (NumberFormatException e) {
		JOptionPane.showMessageDialog(this, "Invalid value format", "Error", JOptionPane.ERROR_MESSAGE);
	}
}

public Tag getNewTag() {
	return newTag;
}

public boolean isConfirmed() {
	return confirmed;
}
}
