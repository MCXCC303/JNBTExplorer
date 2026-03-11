package com.example.nbt.model;

import com.example.nbt.tag.Tag;
import com.example.nbt.tag.TagCompound;
import com.example.nbt.tag.TagList;

public class AddTagCommand implements NBTCommand {
private TagCompound parentCompound;
private TagList parentList;
private final Tag tag;
private final String tagName;
private int listIndex = -1;
private Tag originalTag;

public AddTagCommand(TagCompound parent, Tag tag) {
	this.parentCompound = parent;
	this.tag = tag;
	this.tagName = tag.getName();
	if (parent != null && parent.hasTag(tagName)) {
		this.originalTag = parent.getTag(tagName);
	}
}

public AddTagCommand(TagList parent, Tag tag) {
	this.parentList = parent;
	this.tag = tag;
	this.tagName = tag.getName();
	this.listIndex = parent.size();
}

@Override
public void execute() {
	if (parentCompound != null) {
		parentCompound.putTag(tagName, tag);
	} else if (parentList != null) {
		parentList.addTag(tag);
	}
}

@Override
public void undo() {
	if (parentCompound != null) {
		if (originalTag != null) {

			parentCompound.putTag(tagName, originalTag);
		} else {

			parentCompound.removeTag(tagName);
		}
	} else if (parentList != null && listIndex >= 0) {
		parentList.getTags().remove(listIndex);
	}
}

@Override
public String getDescription() {
	return "Add tag: " + tagName;
}
}
