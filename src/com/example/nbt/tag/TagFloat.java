package com.example.nbt.tag;

public class TagFloat extends Tag {
private float value;

public TagFloat(String name, float value) {
	super(name);
	this.value = value;
}

public float getValue() {
	return value;
}

public void setValue(float value) {
	this.value = value;
}

@Override
public TagType getType() {
	return TagType.TAG_FLOAT;
}

@Override
public Tag copy() {
	return new TagFloat(name, value);
}

@Override
public String toString() {
	return super.toString() + ": " + value;
}
}
