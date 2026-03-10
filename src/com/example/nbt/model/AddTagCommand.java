package com.example.nbt.model;

import com.example.nbt.tag.Tag;
import com.example.nbt.tag.TagCompound;
import com.example.nbt.tag.TagList;

public class AddTagCommand implements NBTCommand {
private TagCompound parentCompound;
private TagList parentList;
private final Tag tag;
private final String tagName;

public AddTagCommand(TagCompound parent, Tag tag) {
	this.parentCompound = parent;
	this.tag = tag;
	this.tagName = tag.getName();
}

public AddTagCommand(TagList parent, Tag tag) {
	this.parentList = parent;
	this.tag = tag;
	this.tagName = tag.getName();
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
		parentCompound.removeTag(tagName);
	} else if (parentList != null) {
		parentList.removeTag(tag);
	}
}

@Override
public String getDescription() {
	return "Add tag: " + tagName;
}
}
