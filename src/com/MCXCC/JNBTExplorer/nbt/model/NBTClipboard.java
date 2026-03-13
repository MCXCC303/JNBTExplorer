package com.MCXCC.JNBTExplorer.nbt.model;

import com.MCXCC.JNBTExplorer.nbt.tag.Tag;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class NBTClipboard {
private static NBTClipboard instance;
private List<Tag> copiedTags;
private final Clipboard systemClipboard;

private NBTClipboard() {
	systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
}

public static NBTClipboard getInstance() {
	if (instance == null) {
		instance = new NBTClipboard();
	}
	return instance;
}

public void copyTag(Tag tag) {
	List<Tag> tags = new ArrayList<>();
	if (tag != null) {
		Tag copiedTag = tag.copy();
		tags.add(copiedTag);
		copiedTags = tags;
		copyToSystemClipboard(tags);
	}
}

public void copyTags(List<Tag> tags) {
	List<Tag> copiedTagsList = new ArrayList<>();
	if (tags != null) {
		for (Tag tag : tags) {
			copiedTagsList.add(tag.copy());
		}
		copiedTags = copiedTagsList;
		copyToSystemClipboard(copiedTagsList);
	}
}

public List<Tag> getCopiedTags() {
	List<Tag> tags = getFromSystemClipboard();
	if (tags != null && !tags.isEmpty()) {
		copiedTags = tags;
		return tags;
	}
	return copiedTags;
}

public boolean hasTag() {
	List<Tag> tags = getFromSystemClipboard();
	if (tags != null && !tags.isEmpty()) {
		return true;
	}
	return copiedTags != null && !copiedTags.isEmpty();
}

private void copyToSystemClipboard(List<Tag> tags) {
	try {
		ClipboardContents contents = new ClipboardContents(tags);
		systemClipboard.setContents(contents, null);
	} catch (Exception e) {
		System.err.println("Failed to copy to system clipboard: " + e.getMessage());
	}
}

private List<Tag> getFromSystemClipboard() {
	try {
		Transferable contents = systemClipboard.getContents(null);
		if (contents != null && contents.isDataFlavorSupported(ClipboardContents.NBT_FLAVOR)) {
			ClipboardContents nbtContents = (ClipboardContents) contents.getTransferData(ClipboardContents.NBT_FLAVOR);
			return nbtContents.tags();
		}
	} catch (Exception e) {
		System.err.println("Failed to get from system clipboard: " + e.getMessage());
	}
	return null;
}

private record ClipboardContents(List<Tag> tags) implements Transferable, Serializable {
	public static final DataFlavor NBT_FLAVOR = new DataFlavor(ClipboardContents.class, "NBT Tags");
	@Serial
	private static final long serialVersionUID = 1L;


	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[]{NBT_FLAVOR};
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return flavor.equals(NBT_FLAVOR);
	}

	@Override
	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
		if (flavor.equals(NBT_FLAVOR)) {
			return this;
		}
		throw new UnsupportedFlavorException(flavor);
	}
}

}
