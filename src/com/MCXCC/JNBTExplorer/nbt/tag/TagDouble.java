package com.MCXCC.JNBTExplorer.nbt.tag;

public class TagDouble extends Tag {
private double value;

public TagDouble(String name, double value) {
	super(name);
	this.value = value;
}

public double getValue() {
	return value;
}

public void setValue(double value) {
	this.value = value;
}

@Override
public TagType getType() {
	return TagType.TAG_DOUBLE;
}

@Override
public Tag copy() {
	return new TagDouble(name, value);
}

@Override
public String toString() {
	return super.toString() + ": " + value;
}
}
