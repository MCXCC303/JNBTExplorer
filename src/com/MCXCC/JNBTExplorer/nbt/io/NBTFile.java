package com.MCXCC.JNBTExplorer.nbt.io;

import com.MCXCC.JNBTExplorer.nbt.tag.Tag;
import com.MCXCC.JNBTExplorer.nbt.util.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;

public class NBTFile {
private final File file;
private Tag rootTag;
private final Logger logger;

public NBTFile(String path, Logger logger) {
	this.file = new File(path);
	this.logger = logger;
}

public void load(boolean compressed) throws IOException {
	logger.fine("load() called - file: " + file.getAbsolutePath() + ", compressed: " + compressed);
	logger.info("Loading NBT file: " + file.getAbsolutePath() + " (compressed: " + compressed + ")");
	try (NBTInputStream in = new NBTInputStream(new FileInputStream(file), compressed, logger)) {
		rootTag = in.readTag();
		logger.fine("Root tag loaded: " + (rootTag != null ? rootTag.getType() + " - " + rootTag.getName() : "null"));
		logger.info("Loaded NBT file successfully: " + file.getAbsolutePath());
	} catch (IOException e) {
		logger.log(Level.SEVERE, "Error loading NBT file: " + file.getAbsolutePath(), e);
		throw e;
	}
}

public void save(boolean compressed) throws IOException {
	if (rootTag == null) {
		logger.fine("save() called but rootTag is null");
		throw new IllegalStateException("No root tag to save");
	}
	logger.fine("save() called - file: " + file.getAbsolutePath() + ", compressed: " + compressed);
	logger.info("Saving NBT file: " + file.getAbsolutePath() + " (compressed: " + compressed + ")");
	try (NBTOutputStream out = new NBTOutputStream(new FileOutputStream(file), compressed, logger)) {
		out.writeTag(rootTag);
		logger.fine("Root tag written: " + rootTag.getType() + " - " + rootTag.getName());
		logger.info("Saved NBT file successfully: " + file.getAbsolutePath());
	} catch (IOException e) {
		logger.log(Level.SEVERE, "Error saving NBT file: " + file.getAbsolutePath(), e);
		throw e;
	}
}

public Tag getRootTag() {
	return rootTag;
}

public void setRootTag(Tag rootTag) {
	this.rootTag = rootTag;
}

public File getFile() {
	return file;
}

}
