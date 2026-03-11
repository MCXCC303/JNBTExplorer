package com.MCXCC.JNBTExplorer.nbt.tag;

public class TagInt extends Tag {
private int value;

public TagInt(String name, int value) {
	super(name);
	this.value = value;
}

public int getValue() {
	return value;
}

public void setValue(int value) {
	this.value = value;
}

@Override
public TagType getType() {
	return TagType.TAG_INT;
}

@Override
public Tag copy() {
	return new TagInt(name, value);
}

@Override
public String toString() {
	return super.toString() + ": " + value;
}
}
