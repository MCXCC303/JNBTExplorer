package com.MCXCC.JNBTExplorer.nbt.ui;

import com.MCXCC.JNBTExplorer.nbt.model.NBTNode;
import com.MCXCC.JNBTExplorer.nbt.tag.*;
import com.MCXCC.JNBTExplorer.nbt.util.ConfigManager;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

public class NBTTreeCellRenderer extends DefaultTreeCellRenderer {
private final ConfigManager configManager;

public NBTTreeCellRenderer(ConfigManager configManager) {
	this.configManager = configManager;
}

@Override
public Component getTreeCellRendererComponent(JTree tree, Object value,
					      boolean selected, boolean expanded,
					      boolean leaf, int row, boolean hasFocus) {
	super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);

	if (value instanceof NBTNode node) {
		TagType type = node.getType();

		if (type != null) {
			// Set icon based on config
			if (configManager.isShowTypeIcons()) {
				setIcon(getIconForType(type));
			} else {
				setIcon(null);
			}

			// Set text based on config
			String nodeText = node.toString();
			String tagName = node.getName();

			if (configManager.isAlwaysShowNames()) {
				// Always show name, even if it's empty
				if (tagName == null || tagName.isEmpty()) {
					// For tags without name, show <unnamed>: value
					String valueText = getValueText(node.getTag());
					setText("<unnamed>: " + valueText);
				} else {
					setText(nodeText);
				}
			} else {
				// Show only value for non-container tags without name
				if (type == TagType.TAG_COMPOUND || type == TagType.TAG_LIST) {
					setText(nodeText);
				} else if (tagName == null || tagName.isEmpty()) {
					// For tags without name, show only value
					setText(getValueText(node.getTag()));
				} else {
					// For tags with name, show name: value
					setText(nodeText);
				}
			}
		}
	}

	return this;
}

private String getValueText(Tag tag) {
	if (tag == null) return "";

	return switch (tag.getType()) {
		case TAG_BYTE -> String.valueOf(((TagByte) tag).getValue());
		case TAG_SHORT -> String.valueOf(((TagShort) tag).getValue());
		case TAG_INT -> String.valueOf(((TagInt) tag).getValue());
		case TAG_LONG -> String.valueOf(((TagLong) tag).getValue());
		case TAG_FLOAT -> String.valueOf(((TagFloat) tag).getValue());
		case TAG_DOUBLE -> String.valueOf(((TagDouble) tag).getValue());
		case TAG_BYTE_ARRAY -> "[" + ((TagByteArray) tag).getValue().length + " bytes]";
		case TAG_STRING -> "\"" + ((TagString) tag).getValue() + "\"";
		case TAG_LIST -> "[" + ((TagList) tag).size() + " entries]";
		case TAG_COMPOUND -> "[" + ((TagCompound) tag).size() + " entries]";
		case TAG_INT_ARRAY -> {
			int[] arr = ((TagIntArray) tag).getValue();
			if (arr.length == 4) {
				yield formatUuid(arr);
			}
			yield "[" + arr.length + " ints]";
		}
		case TAG_LONG_ARRAY -> "[" + ((TagLongArray) tag).getValue().length + " longs]";
		default -> "";
	};
}

private String formatUuid(int[] arr) {
	if (arr == null || arr.length != 4) return "";
	return String.format("%08x-%04x-%04x-%04x-%012x",
		arr[0],
		(arr[1] >> 16) & 0xFFFF,
		arr[1] & 0xFFFF,
		(arr[2] >> 16) & 0xFFFF,
		((long) (arr[2] & 0xFFFF) << 32) | ((long) arr[3] & 0xFFFFFFFFL));
}

private Icon getIconForType(TagType type) {
	return switch (type) {
		case TAG_BYTE -> createColorIcon(Color.CYAN, "B");
		case TAG_SHORT -> createColorIcon(Color.CYAN.darker(), "S");
		case TAG_INT -> createColorIcon(Color.BLUE, "I");
		case TAG_LONG -> createColorIcon(Color.BLUE.darker(), "L");
		case TAG_FLOAT -> createColorIcon(Color.GREEN, "F");
		case TAG_DOUBLE -> createColorIcon(Color.GREEN.darker(), "D");
		case TAG_BYTE_ARRAY -> createColorIcon(Color.MAGENTA, "BA");
		case TAG_STRING -> createColorIcon(Color.YELLOW, "T");
		case TAG_LIST -> createColorIcon(Color.ORANGE, "LI");
		case TAG_COMPOUND -> createColorIcon(Color.RED, "C");
		case TAG_INT_ARRAY -> createColorIcon(Color.MAGENTA.darker(), "IA");
		case TAG_LONG_ARRAY -> createColorIcon(Color.MAGENTA.darker().darker(), "LA");
		case TAG_SHORT_ARRAY -> createColorIcon(Color.MAGENTA.brighter(), "SA");
		default -> null;
	};
}

private Icon createColorIcon(Color color, String text) {
	return new Icon() {
		@Override
		public void paintIcon(Component c, Graphics g, int x, int y) {
			g.setColor(color);
			g.fillRect(x, y, 16, 16);
			g.setColor(Color.BLACK);
			g.drawRect(x, y, 15, 15);
			if (text != null && !text.isEmpty()) {
				FontMetrics fm = g.getFontMetrics();
				int textX = x + (16 - fm.stringWidth(text)) / 2;
				int textY = y + (16 + fm.getAscent() - fm.getDescent()) / 2;
				g.drawString(text, textX, textY);
			}
		}

		@Override
		public int getIconWidth() {
			return 16;
		}

		@Override
		public int getIconHeight() {
			return 16;
		}
	};
}
}
