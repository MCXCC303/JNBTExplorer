package com.MCXCC.JNBTExplorer.nbt.model;

import java.util.Stack;

public class CommandManager {
private final Stack<NBTCommand> undoStack;
private final Stack<NBTCommand> redoStack;
private final int maxHistorySize;
private int savedStateIndex;

public CommandManager() {
	this.undoStack = new Stack<>();
	this.redoStack = new Stack<>();
	this.maxHistorySize = 100;
	this.savedStateIndex = 0;
}

public void executeCommand(NBTCommand command) {
	command.execute();
	undoStack.push(command);
	redoStack.clear();

	if (undoStack.size() > maxHistorySize) {
		undoStack.remove(0);
	}
}

public void markSaved() {
	savedStateIndex = undoStack.size();
}

public boolean isModified() {
	return undoStack.size() != savedStateIndex;
}

public void undo() {
	if (!canUndo()) {
		return;
	}
	NBTCommand command = undoStack.pop();
	command.undo();
	redoStack.push(command);
}

public void redo() {
	if (!canRedo()) {
		return;
	}
	NBTCommand command = redoStack.pop();
	command.execute();
	undoStack.push(command);
}

public boolean canUndo() {
	return !undoStack.isEmpty();
}

public boolean canRedo() {
	return !redoStack.isEmpty();
}

public String getUndoDescription() {
	if (canUndo()) {
		return undoStack.peek().getDescription();
	}
	return null;
}

public String getRedoDescription() {
	if (canRedo()) {
		return redoStack.peek().getDescription();
	}
	return null;
}

public void clear() {
	undoStack.clear();
	redoStack.clear();
	savedStateIndex = 0;
}
}
