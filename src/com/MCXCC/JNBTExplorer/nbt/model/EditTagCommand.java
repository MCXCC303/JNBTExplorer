package com.MCXCC.JNBTExplorer.nbt.model;

import com.MCXCC.JNBTExplorer.nbt.tag.*;

public class EditTagCommand implements NBTCommand {
private final Tag tag;
private final Object oldValue;
private final Object newValue;

public EditTagCommand(Tag tag, Object oldValue, Object newValue) {
	this.tag = tag;
	this.oldValue = oldValue;
	this.newValue = newValue;
}

@Override
public void execute() {
	applyValue(newValue);
}

@Override
public void undo() {
	applyValue(oldValue);
}

private void applyValue(Object value) {
	if (tag instanceof TagByte) {
		((TagByte) tag).setValue((Byte) value);
	} else if (tag instanceof TagShort) {
		((TagShort) tag).setValue((Short) value);
	} else if (tag instanceof TagInt) {
		((TagInt) tag).setValue((Integer) value);
	} else if (tag instanceof TagLong) {
		((TagLong) tag).setValue((Long) value);
	} else if (tag instanceof TagFloat) {
		((TagFloat) tag).setValue((Float) value);
	} else if (tag instanceof TagDouble) {
		((TagDouble) tag).setValue((Double) value);
	} else if (tag instanceof TagString) {
		((TagString) tag).setValue((String) value);
	}
}

@Override
public String getDescription() {
	return "Edit tag: " + tag.getName();
}
}
