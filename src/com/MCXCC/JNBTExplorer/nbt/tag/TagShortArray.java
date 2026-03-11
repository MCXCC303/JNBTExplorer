package com.MCXCC.JNBTExplorer.nbt.tag;

public class TagShortArray extends Tag {
private short[] value;

public TagShortArray(String name, short[] value) {
	super(name);
	this.value = value != null ? value.clone() : new short[0];
}

public short[] getValue() {
	return value.clone();
}

public void setValue(short[] value) {
	this.value = value != null ? value.clone() : new short[0];
}

@Override
public TagType getType() {
	return TagType.TAG_SHORT_ARRAY;
}

@Override
public Tag copy() {
	return new TagShortArray(getName(), value);
}

@Override
public String toString() {
	return "[" + value.length + " shorts]";
}
}
