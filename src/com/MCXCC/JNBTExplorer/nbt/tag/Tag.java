package com.MCXCC.JNBTExplorer.nbt.tag;

import java.io.Serial;
import java.io.Serializable;

public abstract class Tag implements Serializable {
@Serial
private static final long serialVersionUID = 1L;
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
