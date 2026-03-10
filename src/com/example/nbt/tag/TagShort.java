package com.example.nbt.tag;

public class TagShort extends Tag {
private short value;

public TagShort(String name, short value) {
	super(name);
	this.value = value;
}

public short getValue() {
	return value;
}

public void setValue(short value) {
	this.value = value;
}

@Override
public TagType getType() {
	return TagType.TAG_SHORT;
}

@Override
public Tag copy() {
	return new TagShort(name, value);
}

@Override
public String toString() {
	return super.toString() + ": " + value;
}
}
