package com.MCXCC.JNBTExplorer.nbt.model;

import java.util.ArrayList;
import java.util.List;

public class CompositeCommand implements NBTCommand {
private final List<NBTCommand> commands;
private final String description;

public CompositeCommand(String description) {
	this.commands = new ArrayList<>();
	this.description = description;
}

public void addCommand(NBTCommand command) {
	commands.add(command);
}

@Override
public void execute() {
	for (NBTCommand command : commands) {
		command.execute();
	}
}

@Override
public void undo() {
	for (int i = commands.size() - 1; i >= 0; i--) {
		commands.get(i).undo();
	}
}

@Override
public String getDescription() {
	return description;
}

public boolean isEmpty() {
	return commands.isEmpty();
}
}