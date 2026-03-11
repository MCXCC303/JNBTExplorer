package com.example.nbt.model;

import com.example.nbt.tag.Tag;
import com.example.nbt.tag.TagCompound;
import com.example.nbt.tag.TagList;

public class DeleteTagCommand implements NBTCommand {
private TagCompound parentCompound;
private TagList parentList;
private final Tag tag;
private String tagName;
private int listIndex;

public DeleteTagCommand(TagCompound parent, Tag tag) {
	this.parentCompound = parent;
	this.tag = tag;
	this.tagName = tag.getName();
}

public DeleteTagCommand(TagList parent, Tag tag, int index) {
	this.parentList = parent;
	this.tag = tag;
	this.listIndex = index;
}

@Override
public void execute() {
	if (parentCompound != null) {
		parentCompound.removeTag(tagName);
	} else if (parentList != null) {
		parentList.removeTag(tag);
	}
}

@Override
public void undo() {
	if (parentCompound != null) {
		parentCompound.putTag(tagName, tag);
	} else if (parentList != null) {
		parentList.getTags().add(listIndex, tag);
	}
}

@Override
public String getDescription() {
	return "Delete tag: " + (tagName != null ? tagName : tag.getName());
}
}
