package com.example.nbt.ui;

import com.example.nbt.model.*;
import com.example.nbt.tag.*;
import com.example.nbt.util.Logger;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class MainFrame extends JFrame implements DropTargetListener {
private final NBTTreeModel treeModel;
private JTree tree;
private JLabel statusLabel;
private File currentFile;
private final Logger logger;
private JButton pasteButton;
private JMenuItem pasteMenuItem;
private final CommandManager commandManager;

public MainFrame() {
	this(null);
}

public MainFrame(String filePath) {
	this.logger = Logger.getInstance();
	this.commandManager = new CommandManager();
	logger.info("Creating MainFrame");

	setTitle("NBT Editor");
	setSize(800, 600);
	setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	setLocationRelativeTo(null);

	initUI();
	initMenu();
	initToolbar();

	// Add drag and drop support
	new DropTarget(this, this);
	logger.info("Drag and drop support initialized");

	treeModel = new NBTTreeModel();

	if (filePath != null) {
		// Open file from command line
		try {
			treeModel.loadFile(filePath);
			currentFile = new File(filePath);
			commandManager.clear();
			logger.info("Loaded file from command line: " + filePath);
		} catch (IOException e) {
			logger.error("Failed to load file: " + filePath, e);
			JOptionPane.showMessageDialog(this,
				"Error opening file: " + e.getMessage(),
				"Error", JOptionPane.ERROR_MESSAGE);
			treeModel.newFile();
		}
	} else {
		treeModel.newFile();
	}

	tree.setModel(treeModel);
	updateTitle();
	logger.info("Tree model initialized");

	addWindowListener(new WindowAdapter() {
		@Override
		public void windowClosing(WindowEvent e) {
			exit();
		}
	});

	logger.info("MainFrame created successfully");
}

private void initUI() {
	setLayout(new BorderLayout());

	// Tree
	tree = new JTree();
	tree.setCellRenderer(new NBTTreeCellRenderer());
	tree.setEditable(false);

	tree.addTreeWillExpandListener(new javax.swing.event.TreeWillExpandListener() {
		@Override
		public void treeWillExpand(javax.swing.event.TreeExpansionEvent event) {
			TreePath path = event.getPath();
			NBTNode node = (NBTNode) path.getLastPathComponent();
			if (node.getChildCount() == 0) {
				node.loadChildren();
			}
		}

		@Override
		public void treeWillCollapse(javax.swing.event.TreeExpansionEvent event) {
		}
	});

	tree.addMouseListener(new MouseAdapter() {
		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() == 2) {
				editSelectedNode();
			}
		}
	});

	DragSource dragSource = DragSource.getDefaultDragSource();
	dragSource.createDefaultDragGestureRecognizer(tree,
		DnDConstants.ACTION_COPY,
		dge -> {
			TreePath path = tree.getSelectionPath();
			if (path != null) {
				NBTNode node = (NBTNode) path.getLastPathComponent();
				Tag tag = node.getTag();
				if (tag != null) {
					try {
						Transferable transferable = new TagTransferable(tag);
						dge.startDrag(DragSource.DefaultCopyDrop, transferable, new DragSourceListener() {
							@Override
							public void dragEnter(DragSourceDragEvent dsde) {
							}

							@Override
							public void dragOver(DragSourceDragEvent dsde) {
							}

							@Override
							public void dropActionChanged(DragSourceDragEvent dsde) {
							}

							@Override
							public void dragExit(DragSourceEvent dse) {
							}

							@Override
							public void dragDropEnd(DragSourceDropEvent dsde) {
								logger.info("Drag ended, success: " + dsde.getDropSuccess());
							}
						});
						logger.info("Started dragging tag: " + tag.getName());
					} catch (Exception e) {
						logger.error("Error starting drag", e);
					}
				}
			}
		});

	registerKeyboardActions();

	// Initialize paste button state
	updatePasteButtonState();

	JScrollPane treeScroll = new JScrollPane(tree);
	add(treeScroll, BorderLayout.CENTER);

	// Status bar
	statusLabel = new JLabel("Ready");
	statusLabel.setBorder(BorderFactory.createEtchedBorder());
	add(statusLabel, BorderLayout.SOUTH);
}

private void initMenu() {
	JMenuBar menuBar = new JMenuBar();

	// File menu
	JMenu fileMenu = new JMenu("File");
	fileMenu.setMnemonic(KeyEvent.VK_F);

	JMenuItem newItem = new JMenuItem("New", KeyEvent.VK_N);
	newItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
	newItem.addActionListener(e -> newFile());

	JMenuItem openItem = new JMenuItem("Open...", KeyEvent.VK_O);
	openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
	openItem.addActionListener(e -> openFile());

	JMenuItem saveItem = new JMenuItem("Save", KeyEvent.VK_S);
	saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
	saveItem.addActionListener(e -> saveFile());

	JMenuItem saveAsItem = new JMenuItem("Save As...");
	saveAsItem.addActionListener(e -> saveFileAs());

	JMenuItem exitItem = new JMenuItem("Exit", KeyEvent.VK_X);
	exitItem.addActionListener(e -> exit());

	JMenuItem newWindowItem = new JMenuItem("New Window");
	newWindowItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
	newWindowItem.addActionListener(e -> openNewWindow());

	fileMenu.add(newItem);
	fileMenu.add(newWindowItem);
	fileMenu.add(openItem);
	fileMenu.addSeparator();
	fileMenu.add(saveItem);
	fileMenu.add(saveAsItem);
	fileMenu.addSeparator();
	fileMenu.add(exitItem);

	// Edit menu
	JMenu editMenu = new JMenu("Edit");
	editMenu.setMnemonic(KeyEvent.VK_E);

	JMenuItem addItem = new JMenuItem("Add Tag...");
	addItem.addActionListener(e -> addTag());

	JMenuItem editItem = new JMenuItem("Edit Tag...");
	editItem.addActionListener(e -> editSelectedNode());

	JMenuItem deleteItem = new JMenuItem("Delete Tag");
	deleteItem.addActionListener(e -> deleteSelectedNode());

	editMenu.addSeparator();

	JMenuItem copyItem = new JMenuItem("Copy");
	copyItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK));
	copyItem.addActionListener(e -> copySelectedNode());

	pasteMenuItem = new JMenuItem("Paste");
	pasteMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK));
	pasteMenuItem.addActionListener(e -> pasteTag());

	JMenuItem cutItem = new JMenuItem("Cut");
	cutItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_DOWN_MASK));
	cutItem.addActionListener(e -> cutSelectedNode());

	JMenuItem undoItem = new JMenuItem("Undo");
	undoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK));
	undoItem.addActionListener(e -> undo());

	JMenuItem redoItem = new JMenuItem("Redo");
	redoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK));
	redoItem.addActionListener(e -> redo());

	editMenu.add(addItem);
	editMenu.add(editItem);
	editMenu.add(deleteItem);
	editMenu.addSeparator();
	editMenu.add(cutItem);
	editMenu.add(copyItem);
	editMenu.add(pasteMenuItem);
	editMenu.addSeparator();
	editMenu.add(undoItem);
	editMenu.add(redoItem);

	menuBar.add(fileMenu);
	menuBar.add(editMenu);

	setJMenuBar(menuBar);
}

private void initToolbar() {
	JToolBar toolBar = new JToolBar();
	toolBar.setFloatable(false);

	JButton newButton = new JButton("New");
	newButton.addActionListener(e -> newFile());

	JButton openButton = new JButton("Open");
	openButton.addActionListener(e -> openFile());

	JButton saveButton = new JButton("Save");
	saveButton.addActionListener(e -> saveFile());

	JButton addButton = new JButton("Add");
	addButton.addActionListener(e -> addTag());

	JButton deleteButton = new JButton("Delete");
	deleteButton.addActionListener(e -> deleteSelectedNode());

	JButton copyButton = new JButton("Copy");
	copyButton.addActionListener(e -> copySelectedNode());

	pasteButton = new JButton("Paste");
	pasteButton.addActionListener(e -> pasteTag());

	JButton cutButton = new JButton("Cut");
	cutButton.addActionListener(e -> cutSelectedNode());

	JButton undoButton = new JButton("Undo");
	undoButton.addActionListener(e -> undo());

	JButton redoButton = new JButton("Redo");
	redoButton.addActionListener(e -> redo());

	toolBar.add(newButton);
	toolBar.add(openButton);
	toolBar.add(saveButton);
	toolBar.addSeparator();
	toolBar.add(addButton);
	toolBar.add(deleteButton);
	toolBar.addSeparator();
	toolBar.add(cutButton);
	toolBar.add(copyButton);
	toolBar.add(pasteButton);
	toolBar.addSeparator();
	toolBar.add(undoButton);
	toolBar.add(redoButton);

	add(toolBar, BorderLayout.NORTH);
}

private void newFile() {
	logger.info("Creating new file");
	if (checkSave()) {
		treeModel.newFile();
		currentFile = null;
		// Clear command history when creating a new file
		commandManager.clear();
		updateTitle();
		statusLabel.setText("New file created");
		logger.info("New file created successfully");
	} else {
		logger.info("New file operation cancelled");
	}
}

private void openFile() {
	logger.info("Opening file");
	if (!checkSave()) {
		logger.info("Open file operation cancelled");
		return;
	}

	JFileChooser chooser = new JFileChooser();
	chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
		"NBT Files (*.dat, *.nbt, *.dat_old)", "dat", "nbt", "dat_old"));

	if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
		File file = chooser.getSelectedFile();
		try {
			logger.info("Loading file: " + file.getAbsolutePath());
			treeModel.loadFile(file.getAbsolutePath());
			currentFile = file;
			// Clear command history when opening a new file
			commandManager.clear();
			updateTitle();
			statusLabel.setText("Opened: " + file.getName());
			logger.info("File opened successfully: " + file.getName());
		} catch (IOException e) {
			logger.error("Error opening file: " + file.getAbsolutePath(), e);
			JOptionPane.showMessageDialog(this,
				"Error opening file: " + e.getMessage(),
				"Error", JOptionPane.ERROR_MESSAGE);
		}
	} else {
		logger.info("Open file dialog cancelled");
	}
}

private void saveFile() {
	logger.info("Saving file");
	if (currentFile == null) {
		logger.info("Current file is null, calling saveFileAs");
		saveFileAs();
	} else {
		try {
			logger.info("Saving file: " + currentFile.getAbsolutePath());
			treeModel.saveFile();
			commandManager.markSaved();
			updateTitle();
			statusLabel.setText("Saved: " + currentFile.getName());
			logger.info("File saved successfully: " + currentFile.getName());
		} catch (IOException e) {
			logger.error("Error saving file: " + currentFile.getAbsolutePath(), e);
			JOptionPane.showMessageDialog(this,
				"Error saving file: " + e.getMessage(),
				"Error", JOptionPane.ERROR_MESSAGE);
		}
	}
}

private void saveFileAs() {
	logger.info("Save file as");
	JFileChooser chooser = new JFileChooser();
	chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
		"NBT Files (*.dat, *.nbt, *.dat_old)", "dat", "nbt", "dat_old"));

	if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
		File file = chooser.getSelectedFile();
		try {
			logger.info("Saving file as: " + file.getAbsolutePath());
			treeModel.saveFileAs(file.getAbsolutePath());
			currentFile = file;
			commandManager.markSaved();
			updateTitle();
			statusLabel.setText("Saved: " + file.getName());
			logger.info("File saved successfully: " + file.getName());
		} catch (IOException e) {
			logger.error("Error saving file: " + file.getAbsolutePath(), e);
			JOptionPane.showMessageDialog(this,
				"Error saving file: " + e.getMessage(),
				"Error", JOptionPane.ERROR_MESSAGE);
		}
	} else {
		logger.info("Save file as dialog cancelled");
	}
}

private boolean checkSave() {
	if (treeModel.isModified()) {
		int result = JOptionPane.showConfirmDialog(this,
			"Do you want to save changes?",
			"Save Changes",
			JOptionPane.YES_NO_CANCEL_OPTION);

		if (result == JOptionPane.YES_OPTION) {
			saveFile();
			return !treeModel.isModified();
		} else return result != JOptionPane.CANCEL_OPTION;
	}
	return true;
}

private void exit() {
	if (checkSave()) {
		logger.info("Closing window");
		dispose();
	}
}

private void openNewWindow() {
	logger.info("Opening new window");
	SwingUtilities.invokeLater(() -> {
		MainFrame newFrame = new MainFrame();
		newFrame.setVisible(true);
		logger.info("New window opened");
	});
}

private void registerKeyboardActions() {
	tree.getInputMap(JComponent.WHEN_FOCUSED);
	ActionMap actionMap = tree.getActionMap();

	actionMap.put("copy", new AbstractAction() {
		@Override
		public void actionPerformed(ActionEvent e) {
			copySelectedNode();
		}
	});

	actionMap.put("paste", new AbstractAction() {
		@Override
		public void actionPerformed(ActionEvent e) {
			pasteTag();
		}
	});

	actionMap.put("cut", new AbstractAction() {
		@Override
		public void actionPerformed(ActionEvent e) {
			cutSelectedNode();
		}
	});

	actionMap.put("undo", new AbstractAction() {
		@Override
		public void actionPerformed(ActionEvent e) {
			undo();
		}
	});

	actionMap.put("redo", new AbstractAction() {
		@Override
		public void actionPerformed(ActionEvent e) {
			redo();
		}
	});
}

private void updatePasteButtonState() {
	boolean hasTag = NBTClipboard.getInstance().hasTag();
	if (pasteButton != null) {
		pasteButton.setEnabled(hasTag);
	}
	if (pasteMenuItem != null) {
		pasteMenuItem.setEnabled(hasTag);
	}
}

private record TagTransferable(Tag tag) implements Transferable {
	public static final DataFlavor TAG_FLAVOR = new DataFlavor(Tag.class, "NBT Tag");

	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[]{TAG_FLAVOR};
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return flavor.equals(TAG_FLAVOR);
	}

	@Override
	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
		if (flavor.equals(TAG_FLAVOR)) {
			return tag;
		}
		throw new UnsupportedFlavorException(flavor);
	}
}

private void addTag() {
	TreePath path = tree.getSelectionPath();
	if (path == null) {
		path = new TreePath(treeModel.getRoot());
	}

	NBTNode parent = (NBTNode) path.getLastPathComponent();
	if (!parent.isContainer()) {
		JOptionPane.showMessageDialog(this,
			"Can only add tags to compound or list tags",
			"Error", JOptionPane.ERROR_MESSAGE);
		return;
	}

	CreateTagDialog dialog = new CreateTagDialog(this);
	dialog.setVisible(true);

	if (dialog.isConfirmed()) {
		Tag newTag = dialog.getNewTag();
		if (newTag != null) {
			// Create and execute command
			NBTCommand command;
			if (parent.getTag() instanceof TagCompound) {
				command = new AddTagCommand((TagCompound) parent.getTag(), newTag);
			} else {
				command = new AddTagCommand((TagList) parent.getTag(), newTag);
			}
			commandManager.executeCommand(command);

			parent.loadChildren();
			treeModel.fireTreeStructureChanged(path);
			statusLabel.setText("Added tag: " + newTag.getName());
		}
	}
}

private void editSelectedNode() {
	TreePath path = tree.getSelectionPath();
	if (path == null) {
		return;
	}

	NBTNode node = (NBTNode) path.getLastPathComponent();
	Tag tag = node.getTag();

	if (tag instanceof TagCompound || tag instanceof TagList) {
		// For containers, just expand/collapse
		if (tree.isExpanded(path)) {
			tree.collapsePath(path);
			logger.info("Collapsed container tag: " + tag.getName());
		} else {
			tree.expandPath(path);
			logger.info("Expanded container tag: " + tag.getName());
		}
		return;
	}

	// Store old value for undo
	Object oldValue = getTagValue(tag);
	logger.info("Editing tag: " + tag.getName() + " (old value: " + oldValue + ")");

	EditTagDialog dialog = new EditTagDialog(this, tag);
	dialog.setVisible(true);

	if (dialog.isConfirmed()) {
		Object newValue = getTagValue(tag);

		// Create and execute edit command
		NBTCommand command = new EditTagCommand(tag, oldValue, newValue);
		commandManager.executeCommand(command);

		node.setModified(true);
		treeModel.fireTreeNodesChanged(path);
		updateTitle();
		statusLabel.setText("Edited tag: " + tag.getName());
		logger.info("Edited tag: " + tag.getName() + " (new value: " + newValue + ")");
	} else {
		logger.info("Edit tag operation cancelled: " + tag.getName());
	}
}

private Object getTagValue(Tag tag) {
	if (tag instanceof TagByte) {
		return ((TagByte) tag).getValue();
	} else if (tag instanceof TagShort) {
		return ((TagShort) tag).getValue();
	} else if (tag instanceof TagInt) {
		return ((TagInt) tag).getValue();
	} else if (tag instanceof TagLong) {
		return ((TagLong) tag).getValue();
	} else if (tag instanceof TagFloat) {
		return ((TagFloat) tag).getValue();
	} else if (tag instanceof TagDouble) {
		return ((TagDouble) tag).getValue();
	} else if (tag instanceof TagString) {
		return ((TagString) tag).getValue();
	}
	return null;
}

private void copySelectedNode() {
	TreePath path = tree.getSelectionPath();
	if (path == null) {
		return;
	}

	NBTNode node = (NBTNode) path.getLastPathComponent();
	Tag tag = node.getTag();

	if (tag != null) {
		NBTClipboard.getInstance().copyTag(tag);
		statusLabel.setText("Copied tag: " + tag.getName());
		logger.info("Copied tag: " + tag.getName() + " (type: " + tag.getType() + ")");
		updatePasteButtonState();
	}
}

private void cutSelectedNode() {
	TreePath path = tree.getSelectionPath();
	if (path == null || path.getParentPath() == null) {
		return;
	}

	NBTNode node = (NBTNode) path.getLastPathComponent();
	NBTNode parent = (NBTNode) path.getParentPath().getLastPathComponent();
	Tag tag = node.getTag();

	logger.info("Cutting tag: " + tag.getName() + " (type: " + tag.getType() + ")");

	// Copy to clipboard first
	NBTClipboard.getInstance().copyTag(tag);

	// Create and execute delete command
	NBTCommand command;
	if (parent.getTag() instanceof TagCompound) {
		command = new DeleteTagCommand((TagCompound) parent.getTag(), tag);
	} else {
		int index = parent.getIndex(node);
		command = new DeleteTagCommand((TagList) parent.getTag(), tag, index);
	}
	commandManager.executeCommand(command);

	parent.removeChildTag(node);
	treeModel.fireTreeStructureChanged(path.getParentPath());
	updateTitle();
	statusLabel.setText("Cut tag: " + node.getName());
	logger.info("Cut tag: " + tag.getName() + " (type: " + tag.getType() + ")");
	updatePasteButtonState();
}

private void undo() {
	if (!commandManager.canUndo()) {
		JOptionPane.showMessageDialog(this,
			"Nothing to undo",
			"Undo", JOptionPane.INFORMATION_MESSAGE);
		return;
	}

	String description = commandManager.getUndoDescription();
	commandManager.undo();
	refreshTree();
	// Update modified state based on whether we're back at the saved state
	if (commandManager.isModified()) {
		markAsModified();
	} else {
		clearModifiedState();
	}
	updateTitle();
	statusLabel.setText("Undone: " + (description != null ? description : ""));
	logger.info("Undo operation: " + (description != null ? description : ""));
}

private void clearModifiedState() {
	NBTNode root = treeModel.getRootNode();
	if (root != null) {
		clearModifiedRecursive(root);
	}
}

private void clearModifiedRecursive(NBTNode node) {
	node.setModified(false);
	for (int i = 0; i < node.getChildCount(); i++) {
		clearModifiedRecursive((NBTNode) node.getChildAt(i));
	}
}

private void markAsModified() {
	NBTNode root = treeModel.getRootNode();
	if (root != null) {
		root.setModified(true);
	}
}

private void redo() {
	if (!commandManager.canRedo()) {
		JOptionPane.showMessageDialog(this,
			"Nothing to redo",
			"Redo", JOptionPane.INFORMATION_MESSAGE);
		return;
	}

	String description = commandManager.getRedoDescription();
	commandManager.redo();
	refreshTree();
	// Update modified state based on whether we're back at the saved state
	if (commandManager.isModified()) {
		markAsModified();
	} else {
		clearModifiedState();
	}
	updateTitle();
	statusLabel.setText("Redone: " + (description != null ? description : ""));
	logger.info("Redo operation: " + (description != null ? description : ""));
}

private void refreshTree() {
	NBTNode root = treeModel.getRootNode();
	if (root != null) {
		root.refresh();
		treeModel.fireTreeStructureChanged(new TreePath(root));
	}
}

private void pasteTag() {
	TreePath path = tree.getSelectionPath();
	if (path == null) {
		path = new TreePath(treeModel.getRoot());
	}

	NBTNode parent = (NBTNode) path.getLastPathComponent();
	if (!parent.isContainer()) {
		JOptionPane.showMessageDialog(this,
			"Can only paste tags to compound or list tags",
			"Error", JOptionPane.ERROR_MESSAGE);
		return;
	}

	if (!NBTClipboard.getInstance().hasTag()) {
		JOptionPane.showMessageDialog(this,
			"Clipboard is empty",
			"Error", JOptionPane.ERROR_MESSAGE);
		return;
	}

	Tag copiedTag = NBTClipboard.getInstance().getCopiedTag();
	if (copiedTag != null) {
		// Create copy of the tag
		Tag newTag = copiedTag.copy();

		// Ensure unique name
		String originalName = newTag.getName();
		String newName = originalName;
		int counter = 1;
		while (true) {
			boolean nameExists = false;
			if (parent.getTag() instanceof TagCompound compound) {
				nameExists = compound.hasTag(newName);
			}
			if (!nameExists) {
				break;
			}
			newName = originalName + "_" + counter;
			counter++;
		}
		newTag.setName(newName);

		// Create and execute add command
		NBTCommand command;
		if (parent.getTag() instanceof TagCompound) {
			command = new AddTagCommand((TagCompound) parent.getTag(), newTag);
		} else {
			command = new AddTagCommand((TagList) parent.getTag(), newTag);
		}
		commandManager.executeCommand(command);

		// Add to parent node
		parent.addChildTag(newTag);
		parent.loadChildren();
		treeModel.fireTreeStructureChanged(path);
		statusLabel.setText("Pasted tag: " + newName);
		updatePasteButtonState();
	}
}

private void deleteSelectedNode() {
	TreePath path = tree.getSelectionPath();
	if (path == null || path.getParentPath() == null) {
		return;
	}

	NBTNode node = (NBTNode) path.getLastPathComponent();
	NBTNode parent = (NBTNode) path.getParentPath().getLastPathComponent();

	int result = JOptionPane.showConfirmDialog(this,
		"Are you sure you want to delete '" + node.getName() + "'?",
		"Confirm Delete",
		JOptionPane.YES_NO_OPTION);

	if (result == JOptionPane.YES_OPTION) {
		// Create and execute delete command
		NBTCommand command;
		if (parent.getTag() instanceof TagCompound) {
			command = new DeleteTagCommand((TagCompound) parent.getTag(), node.getTag());
		} else {
			int index = parent.getIndex(node);
			command = new DeleteTagCommand((TagList) parent.getTag(), node.getTag(), index);
		}
		commandManager.executeCommand(command);

		parent.removeChildTag(node);
		treeModel.fireTreeStructureChanged(path.getParentPath());
		updateTitle();
		statusLabel.setText("Deleted tag: " + node.getName());
	}
}

private void updateTitle() {
	String title = "NBT Editor";
	if (currentFile != null) {
		title += " - " + currentFile.getName();
	} else {
		title += " - Untitled";
	}
	if (treeModel.isModified()) {
		title += " *";
	}
	setTitle(title);
}

public void dragEnter(DropTargetDragEvent dtde) {
	if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
		dtde.acceptDrag(DnDConstants.ACTION_COPY);
	} else if (isTagFlavorSupported(dtde)) {
		dtde.acceptDrag(DnDConstants.ACTION_COPY);
	} else {
		dtde.rejectDrag();
	}
}

@Override
public void dragOver(DropTargetDragEvent dtde) {
	if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
		dtde.acceptDrag(DnDConstants.ACTION_COPY);
	} else if (isTagFlavorSupported(dtde)) {
		dtde.acceptDrag(DnDConstants.ACTION_COPY);
	} else {
		dtde.rejectDrag();
	}
}

@Override
public void dropActionChanged(DropTargetDragEvent dtde) {
}

@Override
public void dragExit(DropTargetEvent dte) {
}

@Override
public void drop(DropTargetDropEvent dtde) {
	if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
		handleFileDrop(dtde);
	} else if (isTagFlavorSupportedDrop(dtde)) {
		handleTagDrop(dtde);
	} else {
		dtde.rejectDrop();
		dtde.dropComplete(false);
	}
}

private boolean isTagFlavorSupported(DropTargetDragEvent dtde) {
	for (DataFlavor flavor : dtde.getCurrentDataFlavors()) {
		if (flavor.getRepresentationClass() != null &&
			Tag.class.isAssignableFrom(flavor.getRepresentationClass())) {
			return true;
		}
	}
	return false;
}

private boolean isTagFlavorSupportedDrop(DropTargetDropEvent dtde) {
	for (DataFlavor flavor : dtde.getCurrentDataFlavors()) {
		if (flavor.getRepresentationClass() != null &&
			Tag.class.isAssignableFrom(flavor.getRepresentationClass())) {
			return true;
		}
	}
	return false;
}

private void handleFileDrop(DropTargetDropEvent dtde) {
	dtde.acceptDrop(DnDConstants.ACTION_COPY);

	try {
		Transferable transferable = dtde.getTransferable();
		Object data = transferable.getTransferData(DataFlavor.javaFileListFlavor);
		@SuppressWarnings("unchecked")
		List<File> files = (List<File>) data;

		if (!files.isEmpty()) {
			File file = files.get(0);
			if (file.getName().endsWith(".dat") || file.getName().endsWith(".nbt") || file.getName().endsWith(".dat_old")) {
				if (checkSave()) {
					try {
						treeModel.loadFile(file.getAbsolutePath());
						currentFile = file;
						// Clear command history when opening a file via drag and drop
						commandManager.clear();
						updateTitle();
						statusLabel.setText("Opened: " + file.getName());
					} catch (IOException e) {
						JOptionPane.showMessageDialog(this,
							"Error opening file: " + e.getMessage(),
							"Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			} else {
				JOptionPane.showMessageDialog(this,
					"Please drag a .dat or .nbt file",
					"Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	} catch (Exception e) {
		JOptionPane.showMessageDialog(this,
			"Error processing file: " + e.getMessage(),
			"Error", JOptionPane.ERROR_MESSAGE);
	} finally {
		dtde.dropComplete(true);
	}
}

private void handleTagDrop(DropTargetDropEvent dtde) {
	try {
		DataFlavor tagFlavor = null;
		for (DataFlavor flavor : dtde.getCurrentDataFlavors()) {
			if (flavor.getRepresentationClass() != null &&
				Tag.class.isAssignableFrom(flavor.getRepresentationClass())) {
				tagFlavor = flavor;
				break;
			}
		}

		if (tagFlavor != null) {
			Tag tag = (Tag) dtde.getTransferable().getTransferData(tagFlavor);
			Point location = dtde.getLocation();
			TreePath path = tree.getPathForLocation(location.x, location.y);
			if (path == null) {
				path = new TreePath(treeModel.getRoot());
			}
			NBTNode parent = (NBTNode) path.getLastPathComponent();
			if (!parent.isContainer()) {
				if (path.getParentPath() != null) {
					parent = (NBTNode) path.getParentPath().getLastPathComponent();
				} else {
					parent = treeModel.getRootNode();
				}
			}

			// Check if tag with same name already exists
			boolean nameExists = false;
			if (parent.getTag() instanceof TagCompound compound) {
				nameExists = compound.hasTag(tag.getName());
			}

			if (nameExists) {
				dtde.rejectDrop();
				dtde.dropComplete(false);
				statusLabel.setText("Cannot drop: tag with same name already exists");
				logger.info("Drop rejected: tag with same name already exists");
				return;
			}

			dtde.acceptDrop(DnDConstants.ACTION_COPY);

			Tag newTag = tag.copy();
			newTag.setName(tag.getName());
			NBTCommand command;
			if (parent.getTag() instanceof TagCompound) {
				command = new AddTagCommand((TagCompound) parent.getTag(), newTag);
			} else {
				command = new AddTagCommand((TagList) parent.getTag(), newTag);
			}
			commandManager.executeCommand(command);
			parent.addChildTag(newTag);
			parent.loadChildren();
			treeModel.fireTreeStructureChanged(path);
			statusLabel.setText("Dropped tag: " + newTag.getName());
			logger.info("Dropped tag: " + newTag.getName());
		} else {
			dtde.rejectDrop();
			dtde.dropComplete(false);
		}
	} catch (Exception e) {
		logger.error("Error handling tag drop", e);
		dtde.dropComplete(false);
	}
}
}

