package com.MCXCC.JNBTExplorer.nbt.ui;

import com.MCXCC.JNBTExplorer.nbt.model.NBTNode;
import com.MCXCC.JNBTExplorer.nbt.model.NBTTreeModel;
import com.MCXCC.JNBTExplorer.nbt.tag.Tag;
import com.MCXCC.JNBTExplorer.nbt.util.Logger;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.List;

public class SearchHandler {
    private final NBTTreeModel treeModel;
    private final Logger logger;
    private final JTree tree;
    private final JLabel statusLabel;
    private final JFrame parent;
    private JButton searchNextButton;
    private JButton searchPrevButton;

    private final List<NBTNode> searchResults = new ArrayList<>();
    private int currentSearchIndex = -1;

    public SearchHandler(JFrame parent, NBTTreeModel treeModel, JTree tree, JLabel statusLabel, Logger logger) {
        this.parent = parent;
        this.treeModel = treeModel;
        this.tree = tree;
        this.statusLabel = statusLabel;
        this.logger = logger;
    }

    public void setSearchButtons(JButton nextButton, JButton prevButton) {
        this.searchNextButton = nextButton;
        this.searchPrevButton = prevButton;
    }

    private void updateSearchButtons() {
        if (searchNextButton != null && searchPrevButton != null) {
            boolean hasResults = !searchResults.isEmpty();
            searchNextButton.setEnabled(hasResults);
            searchPrevButton.setEnabled(hasResults);
        }
    }

    public void performSearch(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            JOptionPane.showMessageDialog(parent, "Please enter search text", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        logger.info("Performing full search: " + searchText);
        searchResults.clear();
        currentSearchIndex = -1;

        NBTNode root = (NBTNode) treeModel.getRoot();
        searchNodesFull(root, searchText.trim());

        if (searchResults.isEmpty()) {
            statusLabel.setText("No results found");
            updateSearchButtons();
            JOptionPane.showMessageDialog(parent, "No results found", "Search", JOptionPane.INFORMATION_MESSAGE);
        } else {
            currentSearchIndex = 0;
            selectSearchResult();
            updateSearchButtons();
            statusLabel.setText("Found " + searchResults.size() + " results - " + (currentSearchIndex + 1) + "/" + searchResults.size());
            logger.info("Found " + searchResults.size() + " results");
        }
    }

    private void searchNodesFull(NBTNode node, String searchText) {
        if (node == null) return;

        boolean wasExpanded = node.getChildCount() > 0;
        if (!wasExpanded) {
            node.loadChildren();
        }

        Tag tag = node.getTag();
        if (tag != null) {
            String name = tag.getName();
            if (name != null && name.toLowerCase().contains(searchText.toLowerCase())) {
                if (!searchResults.contains(node)) {
                    searchResults.add(node);
                }
            }

            String value = node.toString();
            if (value.toLowerCase().contains(searchText.toLowerCase())) {
                if (!searchResults.contains(node)) {
                    searchResults.add(node);
                }
            }
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            NBTNode child = (NBTNode) node.getChildAt(i);
            searchNodesFull(child, searchText);
        }
    }

    public void searchNext() {
        if (!searchResults.isEmpty()) {
            currentSearchIndex = (currentSearchIndex + 1) % searchResults.size();
            selectSearchResult();
            statusLabel.setText("Found " + searchResults.size() + " results - " + (currentSearchIndex + 1) + "/" + searchResults.size());
        }
    }

    public void searchPrevious() {
        if (!searchResults.isEmpty()) {
            currentSearchIndex = (currentSearchIndex - 1 + searchResults.size()) % searchResults.size();
            selectSearchResult();
            statusLabel.setText("Found " + searchResults.size() + " results - " + (currentSearchIndex + 1) + "/" + searchResults.size());
        }
    }

    private void selectSearchResult() {
        if (!searchResults.isEmpty() && currentSearchIndex >= 0 && currentSearchIndex < searchResults.size()) {
            NBTNode node = searchResults.get(currentSearchIndex);
            TreePath path = getTreePath(node);
            if (path != null) {
                tree.scrollPathToVisible(path);
                tree.setSelectionPath(path);
                tree.expandPath(path.getParentPath());
            }
        }
    }

    private TreePath getTreePath(NBTNode node) {
        List<NBTNode> path = new ArrayList<>();
        NBTNode current = node;
        while (current != null) {
            path.add(0, current);
            current = (NBTNode) current.getParent();
        }
        return path.isEmpty() ? null : new TreePath(path.toArray());
    }

    public void searchNodesAdvanced(NBTNode node, FindReplaceDialog dialog) {
        if (node == null) return;

        boolean wasExpanded = node.getChildCount() > 0;
        if (!wasExpanded) {
            node.loadChildren();
        }

        Tag tag = node.getTag();
        if (tag != null) {
            boolean nameMatch = dialog.isSearchByName() && 
                tag.getName() != null && 
                tag.getName().toLowerCase().contains(dialog.getSearchText().toLowerCase());
            
            boolean valueMatch = dialog.isSearchByValue() && 
                node.toString().toLowerCase().contains(dialog.getSearchText().toLowerCase());
            
            boolean typeMatch = dialog.isSearchByType() && 
                tag.getType().name().toLowerCase().contains(dialog.getSearchText().toLowerCase());

            if (nameMatch || valueMatch || typeMatch) {
                if (!searchResults.contains(node)) {
                    searchResults.add(node);
                }
            }
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            NBTNode child = (NBTNode) node.getChildAt(i);
            searchNodesAdvanced(child, dialog);
        }
    }

    public int getResultCount() {
        return searchResults.size();
    }

    public int getCurrentIndex() {
        return currentSearchIndex;
    }

    public List<NBTNode> getSearchResults() {
        return searchResults;
    }

    public void clearResults() {
        searchResults.clear();
        currentSearchIndex = -1;
    }
}
