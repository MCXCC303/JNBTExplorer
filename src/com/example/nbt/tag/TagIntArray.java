package com.example.nbt.tag;

public class TagIntArray extends Tag {
private int[] value;

public TagIntArray(String name, int[] value) {
	super(name);
	this.value = value != null ? value : new int[0];
}

public int[] getValue() {
	return value;
}

public void setValue(int[] value) {
	this.value = value != null ? value : new int[0];
}

@Override
public TagType getType() {
	return TagType.TAG_INT_ARRAY;
}

@Override
public Tag copy() {
	return new TagIntArray(name, value.clone());
}

@Override
public String toString() {
	return super.toString() + ": [" + value.length + " ints]";
}
}
