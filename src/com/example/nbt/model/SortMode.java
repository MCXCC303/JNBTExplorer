package com.example.nbt.model;

public enum SortMode {
	TYPE_DESC("Type (Desc)"),
	TYPE_ASC("Type (Asc)"),
	NAME_ASC("Name (Asc)"),
	NAME_DESC("Name (Desc)"),
	NONE("None (Original Order)");

	private final String displayName;

	SortMode(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}
}
