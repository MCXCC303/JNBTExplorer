package com.MCXCC.JNBTExplorer.nbt.model;

import com.MCXCC.JNBTExplorer.nbt.tag.*;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class NBTNode extends DefaultMutableTreeNode {
private Tag tag;
private boolean modified;
private final List<NBTNode> children;
private static SortMode typeSortMode = SortMode.TYPE_DESC;
private static SortMode nameSortMode = SortMode.NAME_ASC;
private static boolean showArrayRawValues = false;

public NBTNode(Tag tag) {
	super(tag);
	this.tag = tag;
	this.children = new ArrayList<>();
	this.modified = false;
}

public static void setTypeSortMode(SortMode mode) {
	typeSortMode = mode;
}

public static void setNameSortMode(SortMode mode) {
	nameSortMode = mode;
}

public static void setShowArrayRawValues(boolean show) {
	showArrayRawValues = show;
}

public static boolean isShowArrayRawValues() {
	return showArrayRawValues;
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
		List<Tag> sortedTags = new ArrayList<>(compound.getTags());

		Comparator<Tag> comparator = getTagComparator();

		sortedTags.sort(comparator);

		for (Tag childTag : sortedTags) {
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

private static Comparator<Tag> getTagComparator() {
	Comparator<Tag> comparator = (t1, t2) -> 0;
	boolean hasTypeSort = typeSortMode != SortMode.NONE;
	boolean hasNameSort = nameSortMode != SortMode.NONE;

	if (hasTypeSort || hasNameSort) {
		comparator = Comparator.comparing((Tag t) -> {
			if (hasTypeSort) {
				if (typeSortMode == SortMode.TYPE_DESC) {
					return -t.getType().getId();
				} else {
					return t.getType().getId();
				}
			}
			return 0;
		});

		if (hasNameSort) {
			comparator = comparator.thenComparing((Tag t) -> t.getName() != null ? t.getName() : "", nameSortMode == SortMode.NAME_DESC
				? String.CASE_INSENSITIVE_ORDER.reversed()
				: String.CASE_INSENSITIVE_ORDER);
		}
	}
	return comparator;
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
	boolean hasName = name != null && !name.isEmpty();

	return switch (tag.getType()) {
		case TAG_BYTE ->
			hasName ? name + ": " + ((TagByte) tag).getValue() : String.valueOf(((TagByte) tag).getValue());
		case TAG_SHORT ->
			hasName ? name + ": " + ((TagShort) tag).getValue() : String.valueOf(((TagShort) tag).getValue());
		case TAG_INT ->
			hasName ? name + ": " + ((TagInt) tag).getValue() : String.valueOf(((TagInt) tag).getValue());
		case TAG_LONG ->
			hasName ? name + ": " + ((TagLong) tag).getValue() : String.valueOf(((TagLong) tag).getValue());
		case TAG_FLOAT ->
			hasName ? name + ": " + ((TagFloat) tag).getValue() : String.valueOf(((TagFloat) tag).getValue());
		case TAG_DOUBLE ->
			hasName ? name + ": " + ((TagDouble) tag).getValue() : String.valueOf(((TagDouble) tag).getValue());
		case TAG_BYTE_ARRAY -> {
			byte[] arr = ((TagByteArray) tag).getValue();
			String valueStr = formatByteArrayHex(arr);
			yield hasName ? name + ": " + valueStr : valueStr;
		}
		case TAG_STRING ->
			hasName ? name + ": \"" + ((TagString) tag).getValue() + "\"" : "\"" + ((TagString) tag).getValue() + "\"";
		case TAG_LIST ->
			hasName ? name + " [" + ((TagList) tag).size() + " entries]" : "[" + ((TagList) tag).size() + " entries]";
		case TAG_COMPOUND ->
			hasName ? name + " [" + ((TagCompound) tag).size() + " entries]" : "[" + ((TagCompound) tag).size() + " entries]";
		case TAG_INT_ARRAY -> {
			int[] arr = ((TagIntArray) tag).getValue();
			String valueStr;
			if (arr.length == 4) {
				valueStr = formatUuid(arr);
			} else {
				valueStr = formatIntArrayHex(arr);
			}
			yield hasName ? name + ": " + valueStr : valueStr;
		}
		case TAG_LONG_ARRAY -> {
			long[] arr = ((TagLongArray) tag).getValue();
			String valueStr = formatLongArrayHex(arr);
			yield hasName ? name + ": " + valueStr : valueStr;
		}
		case TAG_SHORT_ARRAY -> {
			short[] arr = ((TagShortArray) tag).getValue();
			String valueStr = formatShortArrayHex(arr);
			yield hasName ? name + ": " + valueStr : valueStr;
		}
		default -> name;
	};
}

private String formatUuid(int[] arr) {
	if (arr == null || arr.length != 4) return "";
	return String.format("%08x-%04x-%04x-%04x-%012x",
		arr[0],
		(arr[1] >> 16) & 0xFFFF,
		arr[1] & 0xFFFF,
		(arr[2] >> 16) & 0xFFFF,
		((long) (arr[2] & 0xFFFF) << 32) | ((long) arr[3] & 0xFFFFFFFFL));
}

private String formatIntArrayHex(int[] arr) {
	StringBuilder sb = new StringBuilder();
	for (int i = 0; i < arr.length; i++) {
		sb.append(String.format("%08x", arr[i]));
		if (i < arr.length - 1) sb.append("-");
	}
	return sb.toString();
}

private String formatLongArrayHex(long[] arr) {
	StringBuilder sb = new StringBuilder();
	for (int i = 0; i < arr.length; i++) {
		sb.append(String.format("%016x", arr[i]));
		if (i < arr.length - 1) sb.append("-");
	}
	return sb.toString();
}

private String formatShortArrayHex(short[] arr) {
	StringBuilder sb = new StringBuilder();
	for (int i = 0; i < arr.length; i++) {
		sb.append(String.format("%04x", arr[i] & 0xFFFF));
		if (i < arr.length - 1) sb.append("-");
	}
	return sb.toString();
}

private String formatByteArrayHex(byte[] arr) {
	StringBuilder sb = new StringBuilder();
	for (int i = 0; i < arr.length; i++) {
		sb.append(String.format("%02x", arr[i] & 0xFF));
		if (i < arr.length - 1) sb.append("-");
	}
	return sb.toString();
}
}
