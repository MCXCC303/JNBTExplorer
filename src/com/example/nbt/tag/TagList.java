package com.example.nbt.tag;

import java.util.ArrayList;
import java.util.List;

public class TagList extends Tag {
private final List<Tag> tags;
private TagType elementType;

public TagList(String name, TagType elementType) {
	super(name);
	this.elementType = elementType;
	this.tags = new ArrayList<>();
}

public List<Tag> getTags() {
	return tags;
}

public TagType getElementType() {
	return elementType;
}

public void setElementType(TagType elementType) {
	this.elementType = elementType;
}

public void addTag(Tag tag) {
	tags.add(tag);
}

public void removeTag(Tag tag) {
	tags.remove(tag);
}

public int size() {
	return tags.size();
}

@Override
public TagType getType() {
	return TagType.TAG_LIST;
}

@Override
public Tag copy() {
	TagList copy = new TagList(name, elementType);
	for (Tag tag : tags) {
		copy.addTag(tag.copy());
	}
	return copy;
}

@Override
public String toString() {
	return super.toString() + ": [" + tags.size() + " entries]";
}
}
