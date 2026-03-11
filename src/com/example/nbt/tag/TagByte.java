package com.example.nbt.tag;

public class TagByte extends Tag {
private byte value;

public TagByte(String name, byte value) {
	super(name);
	this.value = value;
}

public byte getValue() {
	return value;
}

public void setValue(byte value) {
	this.value = value;
}

@Override
public TagType getType() {
	return TagType.TAG_BYTE;
}

@Override
public Tag copy() {
	return new TagByte(name, value);
}

@Override
public String toString() {
	return super.toString() + ": " + value;
}
}
