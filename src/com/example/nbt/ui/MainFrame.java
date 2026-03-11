package com.example.nbt.ui;

import com.example.nbt.model.*;
import com.example.nbt.tag.*;
import com.example.nbt.util.Logger;

import javax.swing.*;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainFrame extends JFrame implements DropTargetListener {
private final NBTTreeModel treeModel;
private JTree tree;
private JLabel statusLabel;
private File currentFile;
private final Logger logger;
private JButton pasteButton;
private JMenuItem pasteMenuItem;
private JMenuItem popupPasteItem;
private JMenuItem blankPasteItem;
private JMenu expandMenu;
private JMenu collapseMenu;
private JMenuItem expandItem;
private JMenuItem expandAllItem;
private JMenuItem collapseItem;
private JMenuItem collapseAllItem;
private final CommandManager commandManager;
private SortMode typeSortMode = SortMode.TYPE_DESC;
private SortMode nameSortMode = SortMode.NAME_ASC;
private JPopupMenu popupMenu;
private JPopupMenu blankPopupMenu;
private JMenuItem addItem;
private JMenuItem editItem;
private JMenuItem deleteItem;
private JSeparator separator1;
private JSeparator separator2;

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
	initPopupMenu();

	new DropTarget(this, this);
	logger.info("Drag and drop support initialized");

	treeModel = new NBTTreeModel();

	if (filePath != null) {

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

	tree = new JTree();
	tree.setCellRenderer(new NBTTreeCellRenderer());
	tree.setEditable(false);
	tree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);

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

		@Override
		public void mousePressed(MouseEvent e) {
			showPopupMenu(e);
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			showPopupMenu(e);
		}
	});

	DragSource dragSource = DragSource.getDefaultDragSource();
	dragSource.createDefaultDragGestureRecognizer(tree,
		DnDConstants.ACTION_COPY,
		dge -> {
			TreePath[] paths = tree.getSelectionPaths();
				if (paths != null && paths.length > 0) {
					List<Tag> tags = new ArrayList<>();
					for (TreePath path : paths) {
						NBTNode node = (NBTNode) path.getLastPathComponent();
						Tag tag = node.getTag();
						if (tag != null) {
							tags.add(tag);
						}
					}
					if (!tags.isEmpty()) {
						try {
							Transferable transferable = new TagTransferable(tags);
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
							logger.info("Started dragging " + tags.size() + " tags");
						} catch (Exception e) {
							logger.error("Error starting drag", e);
						}
					}
				}
		});

	registerKeyboardActions();

	updatePasteButtonState();

	JScrollPane treeScroll = new JScrollPane(tree);
	add(treeScroll, BorderLayout.CENTER);

	statusLabel = new JLabel("Ready");
	statusLabel.setBorder(BorderFactory.createEtchedBorder());
	add(statusLabel, BorderLayout.SOUTH);
}

private void initMenu() {
	JMenuBar menuBar = new JMenuBar();

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

	JMenu editMenu = new JMenu("Edit");
	editMenu.setMnemonic(KeyEvent.VK_E);

	JMenuItem addItem = new JMenuItem("Add Tag...");
	addItem.addActionListener(e -> addTag());

	JMenuItem editItem = new JMenuItem("Edit Tag...");
	editItem.addActionListener(e -> editSelectedNode());

	JMenuItem deleteItem = new JMenuItem("Delete Tag");
	deleteItem.addActionListener(e -> deleteSelectedNode());

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

	JMenu viewMenu = new JMenu("View");
	viewMenu.setMnemonic(KeyEvent.VK_V);

	JMenuItem viewExpandItem = new JMenuItem("Expand");
	viewExpandItem.addActionListener(e -> expandSelectedNode());

	JMenuItem viewCollapseItem = new JMenuItem("Collapse");
	viewCollapseItem.addActionListener(e -> collapseSelectedNode());

	JMenuItem viewExpandAllItem = new JMenuItem("Expand All");
	viewExpandAllItem.addActionListener(e -> expandAll());

	JMenuItem viewCollapseAllItem = new JMenuItem("Collapse All");
	viewCollapseAllItem.addActionListener(e -> collapseAll());

	viewMenu.add(viewExpandItem);
	viewMenu.add(viewCollapseItem);
	viewMenu.addSeparator();
	viewMenu.add(viewExpandAllItem);
	viewMenu.add(viewCollapseAllItem);
	viewMenu.addSeparator();

	JMenu sortMenu = new JMenu("Sort By");

	JMenu typeSortMenu = new JMenu("Type");
	ButtonGroup typeSortGroup = new ButtonGroup();

	JRadioButtonMenuItem typeDescItem = new JRadioButtonMenuItem(SortMode.TYPE_DESC.getDisplayName());
	typeDescItem.setSelected(true);
	typeDescItem.addActionListener(e -> {
		typeSortMode = SortMode.TYPE_DESC;
		NBTNode.setTypeSortMode(typeSortMode);
		refreshTree();
	});
	typeSortGroup.add(typeDescItem);

	JRadioButtonMenuItem typeAscItem = new JRadioButtonMenuItem(SortMode.TYPE_ASC.getDisplayName());
	typeAscItem.addActionListener(e -> {
		typeSortMode = SortMode.TYPE_ASC;
		NBTNode.setTypeSortMode(typeSortMode);
		refreshTree();
	});
	typeSortGroup.add(typeAscItem);

	JRadioButtonMenuItem typeNoneItem = new JRadioButtonMenuItem("None");
	typeNoneItem.addActionListener(e -> {
		typeSortMode = SortMode.NONE;
		NBTNode.setTypeSortMode(typeSortMode);
		refreshTree();
	});
	typeSortGroup.add(typeNoneItem);

	typeSortMenu.add(typeDescItem);
	typeSortMenu.add(typeAscItem);
	typeSortMenu.add(typeNoneItem);

	JMenu nameSortMenu = new JMenu("Name");
	ButtonGroup nameSortGroup = new ButtonGroup();

	JRadioButtonMenuItem nameAscItem = new JRadioButtonMenuItem(SortMode.NAME_ASC.getDisplayName());
	nameAscItem.setSelected(true);
	nameAscItem.addActionListener(e -> {
		nameSortMode = SortMode.NAME_ASC;
		NBTNode.setNameSortMode(nameSortMode);
		refreshTree();
	});
	nameSortGroup.add(nameAscItem);

	JRadioButtonMenuItem nameDescItem = new JRadioButtonMenuItem(SortMode.NAME_DESC.getDisplayName());
	nameDescItem.addActionListener(e -> {
		nameSortMode = SortMode.NAME_DESC;
		NBTNode.setNameSortMode(nameSortMode);
		refreshTree();
	});
	nameSortGroup.add(nameDescItem);

	JRadioButtonMenuItem nameNoneItem = new JRadioButtonMenuItem("None");
	nameNoneItem.addActionListener(e -> {
		nameSortMode = SortMode.NONE;
		NBTNode.setNameSortMode(nameSortMode);
		refreshTree();
	});
	nameSortGroup.add(nameNoneItem);

	nameSortMenu.add(nameAscItem);
	nameSortMenu.add(nameDescItem);
	nameSortMenu.add(nameNoneItem);

	sortMenu.add(typeSortMenu);
	sortMenu.add(nameSortMenu);
	viewMenu.add(sortMenu);

	menuBar.add(fileMenu);
	menuBar.add(editMenu);
	menuBar.add(viewMenu);

	JMenu helpMenu = new JMenu("Help");
	helpMenu.setMnemonic(KeyEvent.VK_H);

	JMenuItem aboutItem = new JMenuItem("About");
	aboutItem.addActionListener(e -> showAboutDialog());
	helpMenu.add(aboutItem);

	menuBar.add(helpMenu);

	setJMenuBar(menuBar);
}

private void initPopupMenu() {
	popupMenu = new JPopupMenu();

	addItem = new JMenuItem("Add Tag...");
	addItem.addActionListener(e -> addTag());

	editItem = new JMenuItem("Edit Tag...");
	editItem.addActionListener(e -> editSelectedNode());

	deleteItem = new JMenuItem("Delete Tag");
	deleteItem.addActionListener(e -> deleteSelectedNode());

	expandMenu = new JMenu("Expand");
	expandItem = new JMenuItem("Expand");
	expandItem.addActionListener(e -> expandSelectedNode());
	expandAllItem = new JMenuItem("Expand All");
	expandAllItem.addActionListener(e -> expandAll());
	expandMenu.add(expandItem);
	expandMenu.add(expandAllItem);

	collapseMenu = new JMenu("Collapse");
	collapseItem = new JMenuItem("Collapse");
	collapseItem.addActionListener(e -> collapseSelectedNode());
	collapseAllItem = new JMenuItem("Collapse All");
	collapseAllItem.addActionListener(e -> collapseAll());
	collapseMenu.add(collapseItem);
	collapseMenu.add(collapseAllItem);

	JMenuItem copyItem = new JMenuItem("Copy");
	copyItem.addActionListener(e -> copySelectedNode());

	popupPasteItem = new JMenuItem("Paste");
	popupPasteItem.addActionListener(e -> pasteTag());

	JMenuItem cutItem = new JMenuItem("Cut");
	cutItem.addActionListener(e -> cutSelectedNode());

	popupMenu.add(addItem);
	popupMenu.add(editItem);
	popupMenu.add(deleteItem);
	separator1 = new JSeparator();
	popupMenu.add(separator1);
	popupMenu.add(expandMenu);
	popupMenu.add(collapseMenu);
	separator2 = new JSeparator();
	popupMenu.add(separator2);
	popupMenu.add(cutItem);
	popupMenu.add(copyItem);
	popupMenu.add(popupPasteItem);

	blankPopupMenu = new JPopupMenu();

	JMenuItem blankAddItem = new JMenuItem("Add Tag...");
	blankAddItem.addActionListener(e -> addTag());

	blankPasteItem = new JMenuItem("Paste");
	blankPasteItem.addActionListener(e -> pasteTag());

	blankPopupMenu.add(blankAddItem);
	blankPopupMenu.addSeparator();
	blankPopupMenu.add(blankPasteItem);
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

private void showAboutDialog() {
	logger.info("Showing About dialog");
	About aboutDialog = new About(this);
	aboutDialog.setVisible(true);
	logger.info("About dialog closed");
}

private void registerKeyboardActions() {
	InputMap inputMap = tree.getInputMap(JComponent.WHEN_FOCUSED);
	ActionMap actionMap = tree.getActionMap();

	inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK), "copy");
	inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK), "paste");
	inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_DOWN_MASK), "cut");
	inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK), "undo");
	inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK), "redo");
	inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "delete");

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

	actionMap.put("delete", new AbstractAction() {
		@Override
		public void actionPerformed(ActionEvent e) {
			deleteSelectedNode();
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
	if (popupPasteItem != null) {
		popupPasteItem.setEnabled(hasTag);
	}
	if (blankPasteItem != null) {
		blankPasteItem.setEnabled(hasTag);
	}
}

private void showPopupMenu(MouseEvent e) {
	if (e.isPopupTrigger()) {
		TreePath path = tree.getPathForLocation(e.getX(), e.getY());
		if (path != null) {
			if (!tree.isPathSelected(path)) {
				tree.setSelectionPath(path);
			}
			TreePath[] selectedPaths = tree.getSelectionPaths();
			boolean isMultiSelect = selectedPaths != null && selectedPaths.length > 1;

			addItem.setVisible(!isMultiSelect);
			editItem.setVisible(!isMultiSelect);
			deleteItem.setVisible(!isMultiSelect);
			separator1.setVisible(!isMultiSelect);
			separator2.setVisible(!isMultiSelect);

			if (selectedPaths != null && selectedPaths.length > 0) {
				NBTNode firstNode = (NBTNode) selectedPaths[0].getLastPathComponent();
				boolean isContainer = firstNode.isContainer();

				expandMenu.setVisible(isContainer);
				collapseMenu.setVisible(isContainer);

				if (!isContainer) {
					separator2.setVisible(false);
				}
			}

			popupMenu.show(tree, e.getX(), e.getY());
		} else {
			tree.clearSelection();
			blankPopupMenu.show(tree, e.getX(), e.getY());
		}
	}
}

private record TagTransferable(List<Tag> tags) implements Transferable {
	public static final DataFlavor TAG_LIST_FLAVOR = new DataFlavor(List.class, "NBT Tag List");

	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[]{TAG_LIST_FLAVOR};
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return flavor.equals(TAG_LIST_FLAVOR);
	}

	@Override
	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
		if (flavor.equals(TAG_LIST_FLAVOR)) {
			return tags;
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
		logger.warning("Add tag failed: parent is not a container");
		return;
	}

	logger.info("Adding new tag to parent: " + parent.getName());

	CreateTagDialog dialog = new CreateTagDialog(this);
	dialog.setVisible(true);

	if (dialog.isConfirmed()) {
		Tag newTag = dialog.getNewTag();
		if (newTag != null) {

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
			logger.info("Added tag: " + newTag.getName() + " (type: " + newTag.getType() + ") to parent: " + parent.getName());
		} else {
			logger.info("Add tag cancelled: no tag created");
		}
	} else {
		logger.info("Add tag operation cancelled");
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

		if (tree.isExpanded(path)) {
			tree.collapsePath(path);
			logger.info("Collapsed container tag: " + tag.getName());
		} else {
			tree.expandPath(path);
			logger.info("Expanded container tag: " + tag.getName());
		}
		return;
	}

	Object oldValue = getTagValue(tag);
	logger.info("Editing tag: " + tag.getName() + " (old value: " + oldValue + ")");

	EditTagDialog dialog = new EditTagDialog(this, tag);
	dialog.setVisible(true);

	if (dialog.isConfirmed()) {
		Object newValue = getTagValue(tag);

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
	TreePath[] paths = tree.getSelectionPaths();
	if (paths == null || paths.length == 0) {
		return;
	}

	List<Tag> tagsToCopy = new ArrayList<>();
	for (TreePath path : paths) {
		NBTNode node = (NBTNode) path.getLastPathComponent();
		Tag tag = node.getTag();
		if (tag != null) {
			tagsToCopy.add(tag);
		}
	}

	if (!tagsToCopy.isEmpty()) {
		if (tagsToCopy.size() == 1) {
			NBTClipboard.getInstance().copyTag(tagsToCopy.get(0));
			statusLabel.setText("Copied tag: " + tagsToCopy.get(0).getName());
			logger.info("Copied tag: " + tagsToCopy.get(0).getName() + " (type: " + tagsToCopy.get(0).getType() + ")");
		} else {
			NBTClipboard.getInstance().copyTags(tagsToCopy);
			statusLabel.setText("Copied " + tagsToCopy.size() + " tags");
			logger.info("Copied " + tagsToCopy.size() + " tags");
		}
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

	NBTClipboard.getInstance().copyTag(tag);

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
		logger.warning("Paste failed: parent is not a container");
		return;
	}

	if (!NBTClipboard.getInstance().hasTag()) {
		JOptionPane.showMessageDialog(this,
			"Clipboard is empty",
			"Error", JOptionPane.ERROR_MESSAGE);
		logger.warning("Paste failed: clipboard is empty");
		return;
	}

	List<Tag> copiedTags = NBTClipboard.getInstance().getCopiedTags();
	if (copiedTags == null || copiedTags.isEmpty()) {
		logger.warning("Paste failed: no tags in clipboard");
		return;
	}

	logger.info("Pasting " + copiedTags.size() + " tag(s) to parent: " + parent.getName());

	CompositeCommand compositeCommand = new CompositeCommand("Paste " + copiedTags.size() + " tags");

	for (Tag copiedTag : copiedTags) {
		Tag newTag = copiedTag.copy();

		String tagName = newTag.getName();

		NBTCommand command;
		if (parent.getTag() instanceof TagCompound) {

			newTag.setName(tagName);
			command = new AddTagCommand((TagCompound) parent.getTag(), newTag);
			logger.info("Pasting tag: " + tagName + " (type: " + newTag.getType() + ") to TagCompound");
		} else {

			String originalName = tagName;
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
			command = new AddTagCommand((TagList) parent.getTag(), newTag);
			logger.info("Pasting tag: " + newName + " (type: " + newTag.getType() + ") to TagList");
		}
		compositeCommand.addCommand(command);
	}

	commandManager.executeCommand(compositeCommand);

	parent.setModified(true);
	parent.loadChildren();
	treeModel.fireTreeStructureChanged(path);

	if (copiedTags.size() == 1) {
		statusLabel.setText("Pasted tag: " + copiedTags.get(0).getName());
		logger.info("Successfully pasted 1 tag: " + copiedTags.get(0).getName());
	} else {
		statusLabel.setText("Pasted " + copiedTags.size() + " tags");
		logger.info("Successfully pasted " + copiedTags.size() + " tags");
	}
	updatePasteButtonState();
}

private void expandSelectedNode() {
	TreePath path = tree.getSelectionPath();
	if (path != null) {
		tree.expandPath(path);
		logger.info("Expanded node: " + ((NBTNode) path.getLastPathComponent()).getName());
	}
}

private void collapseSelectedNode() {
	TreePath path = tree.getSelectionPath();
	if (path != null) {
		tree.collapsePath(path);
		logger.info("Collapsed node: " + ((NBTNode) path.getLastPathComponent()).getName());
	}
}

private void expandAll() {
	TreePath path = tree.getSelectionPath();
	if (path != null) {

		expandRecursively(path);
		logger.info("Expanded all children of node: " + ((NBTNode) path.getLastPathComponent()).getName());
	} else {

		for (int i = 0; i < tree.getRowCount(); i++) {
			tree.expandRow(i);
		}
		logger.info("Expanded all nodes");
	}
}

private void collapseAll() {
	TreePath path = tree.getSelectionPath();
	if (path != null) {

		collapseRecursively(path);
		logger.info("Collapsed all children of node: " + ((NBTNode) path.getLastPathComponent()).getName());
	} else {

		for (int i = tree.getRowCount() - 1; i >= 0; i--) {
			tree.collapseRow(i);
		}
		logger.info("Collapsed all nodes");
	}
}

private void expandRecursively(TreePath path) {
	tree.expandPath(path);
	Object node = path.getLastPathComponent();
	int childCount = tree.getModel().getChildCount(node);
	for (int i = 0; i < childCount; i++) {
		Object child = tree.getModel().getChild(node, i);
		TreePath childPath = path.pathByAddingChild(child);
		expandRecursively(childPath);
	}
}

private void collapseRecursively(TreePath path) {
	Object node = path.getLastPathComponent();
	int childCount = tree.getModel().getChildCount(node);
	for (int i = 0; i < childCount; i++) {
		Object child = tree.getModel().getChild(node, i);
		TreePath childPath = path.pathByAddingChild(child);
		collapseRecursively(childPath);
	}
	tree.collapsePath(path);
}

private void deleteSelectedNode() {
	TreePath[] paths = tree.getSelectionPaths();
	if (paths == null || paths.length == 0) {
		logger.info("Delete selected node: no nodes selected");
		return;
	}

	for (TreePath path : paths) {
		if (path.getParentPath() == null) {
			JOptionPane.showMessageDialog(this,
				"Cannot delete root node",
				"Error", JOptionPane.ERROR_MESSAGE);
			logger.warning("Delete failed: cannot delete root node");
			return;
		}
	}

	int count = paths.length;
	String message;
	if (count == 1) {
		NBTNode node = (NBTNode) paths[0].getLastPathComponent();
		String nodeName = node.getName();
		if (nodeName == null || nodeName.isEmpty()) {
			message = "Are you sure you want to delete this element?";
		} else {
			message = "Are you sure you want to delete '" + nodeName + "'?";
		}
	} else {
		message = "Are you sure you want to delete " + count + " selected tag(s)?";
	}

	logger.info("Delete confirmation requested for " + count + " tag(s)");

	int result = JOptionPane.showConfirmDialog(this,
		message,
		"Confirm Delete",
		JOptionPane.YES_NO_OPTION);

	if (result != JOptionPane.YES_OPTION) {
		logger.info("Delete operation cancelled by user");
		return;
	}

	CompositeCommand compositeCommand = new CompositeCommand("Delete " + count + " tag(s)");

	for (TreePath path : paths) {
		NBTNode node = (NBTNode) path.getLastPathComponent();
		NBTNode parent = (NBTNode) path.getParentPath().getLastPathComponent();

		NBTCommand command;
		if (parent.getTag() instanceof TagCompound) {
			command = new DeleteTagCommand((TagCompound) parent.getTag(), node.getTag());
		} else {
			int index = parent.getIndex(node);
			command = new DeleteTagCommand((TagList) parent.getTag(), node.getTag(), index);
		}
		compositeCommand.addCommand(command);

		parent.removeChildTag(node);
		logger.info("Deleted tag: " + node.getName() + " (type: " + node.getTag().getType() + ") from parent: " + parent.getName());
	}

	if (!compositeCommand.isEmpty()) {
		commandManager.executeCommand(compositeCommand);
		logger.info("Executed delete command for " + count + " tag(s)");
	}

	TreePath rootPath = new TreePath(treeModel.getRoot());
	treeModel.fireTreeStructureChanged(rootPath);
	updateTitle();
	statusLabel.setText("Deleted " + count + " tag(s)");
	logger.info("Successfully deleted " + count + " tag(s)");
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
		if ((flavor.getRepresentationClass() != null &&
			Tag.class.isAssignableFrom(flavor.getRepresentationClass())) ||
			(flavor.getRepresentationClass() != null &&
			List.class.isAssignableFrom(flavor.getRepresentationClass()))) {
			return true;
		}
	}
	return false;
}

private boolean isTagFlavorSupportedDrop(DropTargetDropEvent dtde) {
	for (DataFlavor flavor : dtde.getCurrentDataFlavors()) {
		if ((flavor.getRepresentationClass() != null &&
			Tag.class.isAssignableFrom(flavor.getRepresentationClass())) ||
			(flavor.getRepresentationClass() != null &&
			List.class.isAssignableFrom(flavor.getRepresentationClass()))) {
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
		Object data = null;
		boolean isTagList = false;

		for (DataFlavor flavor : dtde.getCurrentDataFlavors()) {
			if (flavor.getRepresentationClass() != null &&
				List.class.isAssignableFrom(flavor.getRepresentationClass())) {
				data = dtde.getTransferable().getTransferData(flavor);
				isTagList = true;
				break;
			} else if (flavor.getRepresentationClass() != null &&
				Tag.class.isAssignableFrom(flavor.getRepresentationClass())) {
				data = dtde.getTransferable().getTransferData(flavor);
				isTagList = false;
				break;
			}
		}

		if (data != null) {
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

			if (isTagList) {
				@SuppressWarnings("unchecked")
				List<Tag> tags = (List<Tag>) data;
				if (!tags.isEmpty()) {
					boolean allNamesUnique = true;
					if (parent.getTag() instanceof TagCompound compound) {
						for (Tag tag : tags) {
							if (compound.hasTag(tag.getName())) {
								allNamesUnique = false;
								break;
							}
						}
					}

					if (allNamesUnique) {
						dtde.acceptDrop(DnDConstants.ACTION_COPY);

						CompositeCommand compositeCommand = new CompositeCommand("Drop " + tags.size() + " tags");

						for (Tag tag : tags) {
							Tag newTag = tag.copy();
							newTag.setName(tag.getName());
							NBTCommand command;
							if (parent.getTag() instanceof TagCompound) {
								command = new AddTagCommand((TagCompound) parent.getTag(), newTag);
							} else {
								command = new AddTagCommand((TagList) parent.getTag(), newTag);
							}
							compositeCommand.addCommand(command);
							parent.addChildTag(newTag);
						}

						commandManager.executeCommand(compositeCommand);
						parent.loadChildren();
						treeModel.fireTreeStructureChanged(path);
						statusLabel.setText("Dropped " + tags.size() + " tags");
						logger.info("Dropped " + tags.size() + " tags");
					} else {
						dtde.rejectDrop();
						dtde.dropComplete(false);
						statusLabel.setText("Cannot drop: some tags have duplicate names");
						logger.info("Drop rejected: some tags have duplicate names");
					}
				} else {
					dtde.rejectDrop();
					dtde.dropComplete(false);
				}
			} else {
				Tag tag = (Tag) data;

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
			}
		} else {
			dtde.rejectDrop();
			dtde.dropComplete(false);
		}
	} catch (Exception e) {
		logger.error("Error handling tag drop", e);
		dtde.rejectDrop();
		dtde.dropComplete(false);
	}
}
}

