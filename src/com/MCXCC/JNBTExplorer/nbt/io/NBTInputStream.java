package com.MCXCC.JNBTExplorer.nbt.io;

import com.MCXCC.JNBTExplorer.nbt.tag.*;
import com.MCXCC.JNBTExplorer.nbt.util.Logger;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;

public class NBTInputStream implements Closeable {
private final DataInputStream input;
private final Logger logger;

public NBTInputStream(InputStream in, boolean compressed, Logger logger) throws IOException {
	this.logger = logger;
	if (compressed) {
		this.input = new DataInputStream(new GZIPInputStream(in));
		logger.fine("NBTInputStream created with GZIP compression");
	} else {
		this.input = new DataInputStream(in);
		logger.fine("NBTInputStream created without compression");
	}
}

public Tag readTag() throws IOException {
	return readTag(0);
}

private Tag readTag(int depth) throws IOException {
	int typeId = input.readUnsignedByte();
	TagType type = TagType.fromId(typeId);

	if (type == TagType.TAG_END) {
		return null;
	}

	String name = readString();
	return readTagPayload(type, name, depth);
}

private Tag readTagPayload(TagType type, String name, int depth) throws IOException {
	switch (type) {
		case TAG_BYTE:
			return new TagByte(name, input.readByte());

		case TAG_SHORT:
			return new TagShort(name, input.readShort());

		case TAG_INT:
			return new TagInt(name, input.readInt());

		case TAG_LONG:
			return new TagLong(name, input.readLong());

		case TAG_FLOAT:
			return new TagFloat(name, input.readFloat());

		case TAG_DOUBLE:
			return new TagDouble(name, input.readDouble());

		case TAG_BYTE_ARRAY:
			int byteLength = input.readInt();
			byte[] bytes = new byte[byteLength];
			input.readFully(bytes);
			return new TagByteArray(name, bytes);

		case TAG_STRING:
			return new TagString(name, readString());

		case TAG_LIST:
			int elementTypeId = input.readUnsignedByte();
			TagType elementType = TagType.fromId(elementTypeId);
			int listSize = input.readInt();
			TagList list = new TagList(name, elementType);
			for (int i = 0; i < listSize; i++) {
				Tag element = readTagPayload(elementType, "", depth + 1);
				if (element != null) {
					list.addTag(element);
				}
			}
			return list;

		case TAG_COMPOUND:
			TagCompound compound = new TagCompound(name);
			while (true) {
				Tag child = readTag(depth + 1);
				if (child == null) {
					break;
				}
				compound.putTag(child.getName(), child);
			}
			return compound;

		case TAG_INT_ARRAY:
			int intLength = input.readInt();
			int[] ints = new int[intLength];
			for (int i = 0; i < intLength; i++) {
				ints[i] = input.readInt();
			}
			return new TagIntArray(name, ints);

		case TAG_LONG_ARRAY:
			int longLength = input.readInt();
			long[] longs = new long[longLength];
			for (int i = 0; i < longLength; i++) {
				longs[i] = input.readLong();
			}
			return new TagLongArray(name, longs);

		case TAG_SHORT_ARRAY:
			int shortLength = input.readInt();
			short[] shorts = new short[shortLength];
			for (int i = 0; i < shortLength; i++) {
				shorts[i] = input.readShort();
			}
			return new TagShortArray(name, shorts);

		default:
			throw new IOException("Unknown tag type: " + type);
	}
}

private String readString() throws IOException {
	int length = input.readUnsignedShort();
	byte[] bytes = new byte[length];
	input.readFully(bytes);
	return new String(bytes, StandardCharsets.UTF_8);
}

@Override
public void close() throws IOException {
	input.close();
}
}
