package com.MCXCC.JNBTExplorer.nbt.model;

import com.MCXCC.JNBTExplorer.nbt.io.NBTFile;
import com.MCXCC.JNBTExplorer.nbt.tag.Tag;
import com.MCXCC.JNBTExplorer.nbt.tag.TagCompound;
import com.MCXCC.JNBTExplorer.nbt.util.Logger;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NBTTreeModel implements TreeModel {
private NBTNode root;
private NBTFile nbtFile;
private final List<TreeModelListener> listeners;
private final Logger logger;

public NBTTreeModel(Logger logger) {
	this.listeners = new ArrayList<>();
	this.logger = logger;
}

public void loadFile(String path) throws IOException {
	loadFile(path, true);
}

public void loadFile(String path, boolean compressed) throws IOException {
	logger.fine("loadFile() called - path: " + path + ", compressed: " + compressed);
	logger.info("Loading NBT file: " + path + " (compressed: " + compressed + ")");
	nbtFile = new NBTFile(path, logger);
	nbtFile.load(compressed);
	Tag rootTag = nbtFile.getRootTag();
	if (rootTag != null) {
		root = new NBTNode(rootTag);
		logger.fine("Root tag created: " + rootTag.getType() + " - " + rootTag.getName());
		if (root.getTag() != null) {
			root.getTag().setName(nbtFile.getFile().getAbsolutePath());
		}
		root.loadChildren();
		logger.fine("Root children loaded, child count: " + root.getChildCount());
		logger.info("Loaded NBT file successfully: " + path);
	} else {
		root = null;
		logger.warning("Loaded NBT file with no root tag: " + path);
	}
	fireTreeStructureChanged(new TreePath(root));
	logger.fine("Tree structure changed event fired");
}

public void newFile() {
	logger.fine("newFile() called - creating empty NBT file");
	TagCompound rootTag = new TagCompound("");
	nbtFile = null;
	root = new NBTNode(rootTag);
	logger.fine("New root node created: " + rootTag.getType());
	fireTreeStructureChanged(new TreePath(root));
	logger.fine("Tree structure changed event fired for new file");
}

public void saveFile() throws IOException {
	saveFile(true);
}

public void saveFile(boolean compressed) throws IOException {
	if (nbtFile == null) {
		logger.fine("saveFile() called but nbtFile is null");
		throw new IllegalStateException("No file to save");
	}
	logger.fine("saveFile() called - path: " + nbtFile.getFile().getPath() + ", compressed: " + compressed);
	logger.info("Saving NBT file: " + nbtFile.getFile().getPath() + " (compressed: " + compressed + ")");
	nbtFile.setRootTag(root.getTag());
	nbtFile.save(compressed);
	clearModified(root);
	logger.fine("Modified flags cleared");
	logger.info("Saved NBT file successfully: " + nbtFile.getFile().getPath());
}

public void saveFileAs(String path) throws IOException {
	saveFileAs(path, true);
}

public void saveFileAs(String path, boolean compressed) throws IOException {
	logger.fine("saveFileAs() called - path: " + path + ", compressed: " + compressed);
	logger.info("Saving NBT file as: " + path + " (compressed: " + compressed + ")");
	nbtFile = new NBTFile(path, logger);
	nbtFile.setRootTag(root.getTag());
	nbtFile.save(compressed);

	if (root.getTag() != null) {
		root.getTag().setName(nbtFile.getFile().getAbsolutePath());
		logger.fine("Root tag name updated to: " + nbtFile.getFile().getAbsolutePath());
	}
	clearModified(root);
	fireTreeStructureChanged(new TreePath(root));
	logger.fine("Tree structure changed event fired for save as");
	logger.info("Saved NBT file successfully: " + path);
}

private void clearModified(NBTNode node) {
	logger.fine("clearModified() called for node: " + node.getName());
	node.setModified(false);
	for (int i = 0; i < node.getChildCount(); i++) {
		clearModified((NBTNode) node.getChildAt(i));
	}
}

public boolean isModified() {
	return isModified(root);
}

private boolean isModified(NBTNode node) {
	if (node.isModified()) {
		return true;
	}
	for (int i = 0; i < node.getChildCount(); i++) {
		if (isModified((NBTNode) node.getChildAt(i))) {
			return true;
		}
	}
	return false;
}

public NBTNode getRootNode() {
	return root;
}

@Override
public Object getRoot() {
	return root;
}

@Override
public Object getChild(Object parent, int index) {
	return ((NBTNode) parent).getChildAt(index);
}

@Override
public int getChildCount(Object parent) {
	return ((NBTNode) parent).getChildCount();
}

@Override
public boolean isLeaf(Object node) {
	return !((NBTNode) node).isContainer();
}

@Override
public void valueForPathChanged(TreePath path, Object newValue) {
	NBTNode node = (NBTNode) path.getLastPathComponent();
	node.setTag((Tag) newValue);
	fireTreeNodesChanged(path);
}

@Override
public int getIndexOfChild(Object parent, Object child) {
	return ((NBTNode) parent).getIndex((NBTNode) child);
}

@Override
public void addTreeModelListener(TreeModelListener l) {
	listeners.add(l);
}

@Override
public void removeTreeModelListener(TreeModelListener l) {
	listeners.remove(l);
}

public void fireTreeNodesChanged(TreePath path) {
	TreeModelEvent event = new TreeModelEvent(this, path);
	for (TreeModelListener listener : listeners) {
		listener.treeNodesChanged(event);
	}
}

public void fireTreeStructureChanged(TreePath path) {
	TreeModelEvent event = new TreeModelEvent(this, path);
	for (TreeModelListener listener : listeners) {
		listener.treeStructureChanged(event);
	}
}

}
