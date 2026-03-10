package com.example.nbt.io;

import com.example.nbt.tag.Tag;
import com.example.nbt.util.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class NBTFile {
private final File file;
private Tag rootTag;
private final Logger logger;

public NBTFile(String path) {
	this.file = new File(path);
	this.logger = Logger.getInstance();
}

public void load(boolean compressed) throws IOException {
	logger.info("Loading NBT file: " + file.getAbsolutePath() + " (compressed: " + compressed + ")");
	try (NBTInputStream in = new NBTInputStream(new FileInputStream(file), compressed)) {
		rootTag = in.readTag();
		logger.info("Loaded NBT file successfully: " + file.getAbsolutePath());
	} catch (IOException e) {
		logger.error("Error loading NBT file: " + file.getAbsolutePath(), e);
		throw e;
	}
}

public void save(boolean compressed) throws IOException {
	if (rootTag == null) {
		throw new IllegalStateException("No root tag to save");
	}
	logger.info("Saving NBT file: " + file.getAbsolutePath() + " (compressed: " + compressed + ")");
	try (NBTOutputStream out = new NBTOutputStream(new FileOutputStream(file), compressed)) {
		out.writeTag(rootTag);
		logger.info("Saved NBT file successfully: " + file.getAbsolutePath());
	} catch (IOException e) {
		logger.error("Error saving NBT file: " + file.getAbsolutePath(), e);
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
