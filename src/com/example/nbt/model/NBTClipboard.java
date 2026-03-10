package com.example.nbt.model;

import com.example.nbt.tag.*;

public class NBTClipboard {
private static NBTClipboard instance;
private Tag copiedTag;

private NBTClipboard() {
}

public static NBTClipboard getInstance() {
	if (instance == null) {
		instance = new NBTClipboard();
	}
	return instance;
}

public void copyTag(Tag tag) {
	this.copiedTag = tag != null ? tag.copy() : null;
}

public Tag getCopiedTag() {
	return copiedTag;
}

public boolean hasTag() {
	return copiedTag != null;
}

}
