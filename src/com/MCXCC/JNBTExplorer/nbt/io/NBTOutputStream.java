package com.MCXCC.JNBTExplorer.nbt.io;

import com.MCXCC.JNBTExplorer.nbt.tag.*;
import com.MCXCC.JNBTExplorer.nbt.util.Logger;

import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPOutputStream;

public class NBTOutputStream implements Closeable {
private final DataOutputStream output;
private final Logger logger;

public NBTOutputStream(OutputStream out, boolean compressed, Logger logger) throws IOException {
	this.logger = logger;
	if (compressed) {
		this.output = new DataOutputStream(new GZIPOutputStream(out));
		logger.fine("NBTOutputStream created with GZIP compression");
	} else {
		this.output = new DataOutputStream(out);
		logger.fine("NBTOutputStream created without compression");
	}
}

public void writeTag(Tag tag) throws IOException {
	writeTagInternal(tag);
}

private void writeTagInternal(Tag tag) throws IOException {
	TagType type = tag.getType();
	output.writeByte(type.getId());

	if (type != TagType.TAG_END) {
		writeString(tag.getName());
		writeTagPayload(tag);
	}
}

private void writeTagPayload(Tag tag) throws IOException {
	switch (tag.getType()) {
		case TAG_BYTE:
			output.writeByte(((TagByte) tag).getValue());
			break;

		case TAG_SHORT:
			output.writeShort(((TagShort) tag).getValue());
			break;

		case TAG_INT:
			output.writeInt(((TagInt) tag).getValue());
			break;

		case TAG_LONG:
			output.writeLong(((TagLong) tag).getValue());
			break;

		case TAG_FLOAT:
			output.writeFloat(((TagFloat) tag).getValue());
			break;

		case TAG_DOUBLE:
			output.writeDouble(((TagDouble) tag).getValue());
			break;

		case TAG_BYTE_ARRAY:
			byte[] bytes = ((TagByteArray) tag).getValue();
			output.writeInt(bytes.length);
			output.write(bytes);
			break;

		case TAG_STRING:
			writeString(((TagString) tag).getValue());
			break;

		case TAG_LIST:
			TagList list = (TagList) tag;
			TagType elementType = list.getElementType();
			output.writeByte(elementType != null ? elementType.getId() : 0);
			output.writeInt(list.size());
			for (Tag element : list.getTags()) {
				writeTagPayload(element);
			}
			break;

		case TAG_COMPOUND:
			TagCompound compound = (TagCompound) tag;
			for (Tag child : compound.getTags()) {
				writeTagInternal(child);
			}
			output.writeByte(0);
			break;

		case TAG_INT_ARRAY:
			int[] ints = ((TagIntArray) tag).getValue();
			output.writeInt(ints.length);
			for (int value : ints) {
				output.writeInt(value);
			}
			break;

		case TAG_LONG_ARRAY:
			long[] longs = ((TagLongArray) tag).getValue();
			output.writeInt(longs.length);
			for (long value : longs) {
				output.writeLong(value);
			}
			break;

		case TAG_SHORT_ARRAY:
			short[] shorts = ((TagShortArray) tag).getValue();
			output.writeInt(shorts.length);
			for (short value : shorts) {
				output.writeShort(value);
			}
			break;

		default:
			throw new IOException("Unknown tag type: " + tag.getType());
	}
}

private void writeString(String str) throws IOException {
	byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
	output.writeShort(bytes.length);
	output.write(bytes);
}

@Override
public void close() throws IOException {
	output.close();
}
}
