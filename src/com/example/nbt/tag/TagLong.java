package com.example.nbt.tag;

public class TagLong extends Tag {
private long value;

public TagLong(String name, long value) {
	super(name);
	this.value = value;
}

public long getValue() {
	return value;
}

public void setValue(long value) {
	this.value = value;
}

@Override
public TagType getType() {
	return TagType.TAG_LONG;
}

@Override
public Tag copy() {
	return new TagLong(name, value);
}

@Override
public String toString() {
	return super.toString() + ": " + value;
}
}
