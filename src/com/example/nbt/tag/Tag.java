package com.example.nbt.tag;

public abstract class Tag {
protected String name;

public Tag(String name) {
	this.name = name;
}

public String getName() {
	return name;
}

public void setName(String name) {
	this.name = name;
}

public abstract TagType getType();

public abstract Tag copy();

@Override
public String toString() {
	return name != null ? name : "";
}
}
