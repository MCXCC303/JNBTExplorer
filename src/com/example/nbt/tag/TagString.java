package com.example.nbt.tag;

public class TagString extends Tag {
private String value;

public TagString(String name, String value) {
	super(name);
	this.value = value != null ? value : "";
}

public String getValue() {
	return value;
}

public void setValue(String value) {
	this.value = value != null ? value : "";
}

@Override
public TagType getType() {
	return TagType.TAG_STRING;
}

@Override
public Tag copy() {
	return new TagString(name, value);
}

@Override
public String toString() {
	return super.toString() + ": \"" + value + "\"";
}
}
