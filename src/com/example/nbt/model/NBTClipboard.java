package com.example.nbt.model;

import com.example.nbt.tag.Tag;

import java.util.ArrayList;
import java.util.List;

public class NBTClipboard {
private static NBTClipboard instance;
private List<Tag> copiedTags;

private NBTClipboard() {
}

public static NBTClipboard getInstance() {
	if (instance == null) {
		instance = new NBTClipboard();
	}
	return instance;
}

public void copyTag(Tag tag) {
	this.copiedTags = new ArrayList<>();
	if (tag != null) {
		copiedTags.add(tag.copy());
	}
}

public void copyTags(List<Tag> tags) {
	this.copiedTags = new ArrayList<>();
	if (tags != null) {
		for (Tag tag : tags) {
			copiedTags.add(tag.copy());
		}
	}
}

public List<Tag> getCopiedTags() {
	return copiedTags;
}

public boolean hasTag() {
	return copiedTags != null && !copiedTags.isEmpty();
}

}
