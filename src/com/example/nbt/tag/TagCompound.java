package com.example.nbt.tag;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class TagCompound extends Tag {
private final Map<String, Tag> tags;

public TagCompound(String name) {
	super(name);
	this.tags = new LinkedHashMap<>();
}

public void putTag(String key, Tag tag) {
	tags.put(key, tag);
	if (tag != null) {
		tag.setName(key);
	}
}

public void removeTag(String key) {
	tags.remove(key);
}

public Tag getTag(String key) {
	return tags.get(key);
}

public boolean hasTag(String key) {
	return tags.containsKey(key);
}

public Collection<Tag> getTags() {
	return tags.values();
}

public int size() {
	return tags.size();
}

@Override
public TagType getType() {
	return TagType.TAG_COMPOUND;
}

@Override
public Tag copy() {
	TagCompound copy = new TagCompound(name);
	for (Map.Entry<String, Tag> entry : tags.entrySet()) {
		copy.putTag(entry.getKey(), entry.getValue().copy());
	}
	return copy;
}

@Override
public String toString() {
	return super.toString() + ": [" + tags.size() + " entries]";
}
}
