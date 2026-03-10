package com.example.nbt.model;

import com.example.nbt.tag.*;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.ArrayList;
import java.util.List;

public class NBTNode extends DefaultMutableTreeNode {
private Tag tag;
private boolean modified;
private final List<NBTNode> children;

public NBTNode(Tag tag) {
	super(tag);
	this.tag = tag;
	this.children = new ArrayList<>();
	this.modified = false;
}

public Tag getTag() {
	return tag;
}

public void setTag(Tag tag) {
	this.tag = tag;
	setUserObject(tag);
}

public boolean isModified() {
	return modified;
}

public void setModified(boolean modified) {
	this.modified = modified;
	if (modified && getParent() instanceof NBTNode) {
		((NBTNode) getParent()).setModified(true);
	}
}

public TagType getType() {
	return tag != null ? tag.getType() : null;
}

public String getName() {
	return tag != null ? tag.getName() : "";
}

public void setName(String name) {
	if (tag != null) {
		tag.setName(name);
		setModified(true);
	}
}

public boolean isContainer() {
	return tag instanceof TagCompound || tag instanceof TagList;
}

public void loadChildren() {
	clearChildren();

	if (tag instanceof TagCompound compound) {
		for (Tag childTag : compound.getTags()) {
			NBTNode childNode = new NBTNode(childTag);
			add(childNode);
			children.add(childNode);
		}
	} else if (tag instanceof TagList list) {
		for (Tag childTag : list.getTags()) {
			NBTNode childNode = new NBTNode(childTag);
			add(childNode);
			children.add(childNode);
		}
	}
}

public void clearChildren() {
	children.clear();
	removeAllChildren();
}

public void refresh() {
	clearChildren();
	loadChildren();
}

public void addChildTag(Tag childTag) {
	if (tag instanceof TagCompound compound) {
		compound.putTag(childTag.getName(), childTag);
		NBTNode childNode = new NBTNode(childTag);
		add(childNode);
		children.add(childNode);
		setModified(true);
	} else if (tag instanceof TagList list) {
		list.addTag(childTag);
		NBTNode childNode = new NBTNode(childTag);
		add(childNode);
		children.add(childNode);
		setModified(true);
	}
}

public void removeChildTag(NBTNode childNode) {
	if (tag instanceof TagCompound compound) {
		compound.removeTag(childNode.getName());
		remove(childNode);
		children.remove(childNode);
		setModified(true);
	} else if (tag instanceof TagList list) {
		list.removeTag(childNode.getTag());
		remove(childNode);
		children.remove(childNode);
		setModified(true);
	}
}

@Override
public String toString() {
	if (tag == null) return "";

	String name = tag.getName();
	if (name == null || name.isEmpty()) {
		name = "<unnamed>";
	}

	return switch (tag.getType()) {
		case TAG_BYTE -> name + ": " + ((TagByte) tag).getValue();
		case TAG_SHORT -> name + ": " + ((TagShort) tag).getValue();
		case TAG_INT -> name + ": " + ((TagInt) tag).getValue();
		case TAG_LONG -> name + ": " + ((TagLong) tag).getValue();
		case TAG_FLOAT -> name + ": " + ((TagFloat) tag).getValue();
		case TAG_DOUBLE -> name + ": " + ((TagDouble) tag).getValue();
		case TAG_BYTE_ARRAY -> name + " [" + ((TagByteArray) tag).getValue().length + " bytes]";
		case TAG_STRING -> name + ": \"" + ((TagString) tag).getValue() + "\"";
		case TAG_LIST -> name + " [" + ((TagList) tag).size() + " entries]";
		case TAG_COMPOUND -> name + " [" + ((TagCompound) tag).size() + " entries]";
		case TAG_INT_ARRAY -> name + " [" + ((TagIntArray) tag).getValue().length + " ints]";
		case TAG_LONG_ARRAY -> name + " [" + ((TagLongArray) tag).getValue().length + " longs]";
		default -> name;
	};
}
}
