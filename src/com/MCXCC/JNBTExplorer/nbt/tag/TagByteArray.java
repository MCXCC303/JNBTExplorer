package com.MCXCC.JNBTExplorer.nbt.tag;

public class TagByteArray extends Tag {
private byte[] value;

public TagByteArray(String name, byte[] value) {
	super(name);
	this.value = value != null ? value : new byte[0];
}

public byte[] getValue() {
	return value;
}

public void setValue(byte[] value) {
	this.value = value != null ? value : new byte[0];
}

@Override
public TagType getType() {
	return TagType.TAG_BYTE_ARRAY;
}

@Override
public Tag copy() {
	return new TagByteArray(name, value.clone());
}

@Override
public String toString() {
	return super.toString() + ": [" + value.length + " bytes]";
}
}
