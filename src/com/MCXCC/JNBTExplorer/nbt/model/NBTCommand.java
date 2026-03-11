package com.MCXCC.JNBTExplorer.nbt.model;

public interface NBTCommand {
void execute();

void undo();

String getDescription();
}
