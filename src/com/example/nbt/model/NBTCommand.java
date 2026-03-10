package com.example.nbt.model;

public interface NBTCommand {
void execute();

void undo();

String getDescription();
}
