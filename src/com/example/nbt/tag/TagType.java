package com.example.nbt.tag;

public enum TagType {
	TAG_END(0, "END"),
	TAG_BYTE(1, "BYTE"),
	TAG_SHORT(2, "SHORT"),
	TAG_INT(3, "INT"),
	TAG_LONG(4, "LONG"),
	TAG_FLOAT(5, "FLOAT"),
	TAG_DOUBLE(6, "DOUBLE"),
	TAG_BYTE_ARRAY(7, "BYTE_ARRAY"),
	TAG_STRING(8, "STRING"),
	TAG_LIST(9, "LIST"),
	TAG_COMPOUND(10, "COMPOUND"),
	TAG_INT_ARRAY(11, "INT_ARRAY"),
	TAG_LONG_ARRAY(12, "LONG_ARRAY");

private final int id;
private final String name;

TagType(int id, String name) {
	this.id = id;
	this.name = name;
}

public int getId() {
	return id;
}

public String getName() {
	return name;
}

public static TagType fromId(int id) {
	for (TagType type : values()) {
		if (type.id == id) {
			return type;
		}
	}
	return null;
}
}
