package com.MCXCC.JNBTExplorer.nbt.model;

import com.MCXCC.JNBTExplorer.nbt.util.Logger;

import java.util.Stack;

public class CommandManager {
private final Stack<NBTCommand> undoStack;
private final Stack<NBTCommand> redoStack;
private final int maxHistorySize;
private int savedStateIndex;
private final Logger logger;

public CommandManager(Logger logger) {
	this.undoStack = new Stack<>();
	this.redoStack = new Stack<>();
	this.maxHistorySize = 100;
	this.savedStateIndex = 0;
	this.logger = logger;
}

public void executeCommand(NBTCommand command) {
	String commandDesc = command != null ? command.getDescription() : "null";
	logger.fine("executeCommand() called - " + commandDesc);
	command.execute();
	undoStack.push(command);
	redoStack.clear();

	if (undoStack.size() > maxHistorySize) {
		undoStack.remove(0);
		logger.fine("Command history exceeded max size, removed oldest command");
	}
	logger.fine("Command executed - undo stack size: " + undoStack.size() + ", redo stack size: " + redoStack.size());
}

public void markSaved() {
	savedStateIndex = undoStack.size();
}

public boolean isModified() {
	return undoStack.size() != savedStateIndex;
}

public void undo() {
	if (!canUndo()) {
		logger.fine("undo() called but cannot undo");
		return;
	}
	NBTCommand command = undoStack.pop();
	logger.fine("undo() executing - " + command.getDescription());
	command.undo();
	redoStack.push(command);
	logger.fine("undo completed - undo stack size: " + undoStack.size() + ", redo stack size: " + redoStack.size());
}

public void redo() {
	if (!canRedo()) {
		logger.fine("redo() called but cannot redo");
		return;
	}
	NBTCommand command = redoStack.pop();
	logger.fine("redo() executing - " + command.getDescription());
	command.execute();
	undoStack.push(command);
	logger.fine("redo completed - undo stack size: " + undoStack.size() + ", redo stack size: " + redoStack.size());
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
