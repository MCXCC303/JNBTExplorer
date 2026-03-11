package com.example.nbt.tag;

public class TagLongArray extends Tag {
private long[] value;

public TagLongArray(String name, long[] value) {
	super(name);
	this.value = value != null ? value : new long[0];
}

public long[] getValue() {
	return value;
}

public void setValue(long[] value) {
	this.value = value != null ? value : new long[0];
}

@Override
public TagType getType() {
	return TagType.TAG_LONG_ARRAY;
}

@Override
public Tag copy() {
	return new TagLongArray(name, value.clone());
}

@Override
public String toString() {
	return super.toString() + ": [" + value.length + " longs]";
}
}
