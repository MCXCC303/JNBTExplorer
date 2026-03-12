package com.MCXCC.JNBTExplorer.nbt.ui;

import com.MCXCC.JNBTExplorer.nbt.model.NBTNode;
import com.MCXCC.JNBTExplorer.nbt.tag.*;
import com.MCXCC.JNBTExplorer.nbt.util.ConfigManager;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class NBTTreeCellRenderer extends DefaultTreeCellRenderer {
private final ConfigManager configManager;
private Map<TagType, Icon> originalIcons;
private boolean originalIconsLoaded = false;

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
				boolean hasName = node.getName() != null && !node.getName().isEmpty();
				setIcon(getIconForType(type, hasName));
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
		case TAG_BYTE_ARRAY -> {
			byte[] arr = ((TagByteArray) tag).getValue();
			yield formatByteArrayHex(arr);
		}
		case TAG_STRING -> "\"" + ((TagString) tag).getValue() + "\"";
		case TAG_LIST -> "[" + ((TagList) tag).size() + " entries]";
		case TAG_COMPOUND -> "[" + ((TagCompound) tag).size() + " entries]";
		case TAG_INT_ARRAY -> {
			int[] arr = ((TagIntArray) tag).getValue();
			if (arr.length == 4) {
				yield formatUuid(arr);
			} else {
				yield formatIntArrayHex(arr);
			}
		}
		case TAG_LONG_ARRAY -> {
			long[] arr = ((TagLongArray) tag).getValue();
			yield formatLongArrayHex(arr);
		}
		case TAG_SHORT_ARRAY -> {
			short[] arr = ((TagShortArray) tag).getValue();
			yield formatShortArrayHex(arr);
		}
		default -> "";
	};
}

private String formatIntArrayHex(int[] arr) {
	StringBuilder sb = new StringBuilder();
	for (int i = 0; i < arr.length; i++) {
		sb.append(String.format("%08x", arr[i]));
		if (i < arr.length - 1) {
			sb.append("-");
		}
	}
	return sb.toString();
}

private String formatLongArrayHex(long[] arr) {
	StringBuilder sb = new StringBuilder();
	for (int i = 0; i < arr.length; i++) {
		sb.append(String.format("%016x", arr[i]));
		if (i < arr.length - 1) {
			sb.append("-");
		}
	}
	return sb.toString();
}

private String formatShortArrayHex(short[] arr) {
	StringBuilder sb = new StringBuilder();
	for (int i = 0; i < arr.length; i++) {
		sb.append(String.format("%04x", arr[i] & 0xFFFF));
		if (i < arr.length - 1) {
			sb.append("-");
		}
	}
	return sb.toString();
}

private String formatByteArrayHex(byte[] arr) {
	StringBuilder sb = new StringBuilder();
	for (int i = 0; i < arr.length; i++) {
		sb.append(String.format("%02x", arr[i] & 0xFF));
		if (i < arr.length - 1) {
			sb.append("-");
		}
	}
	return sb.toString();
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

private Icon getIconForType(TagType type, boolean hasName) {
	String iconStyle = configManager.getIconStyle();
	if ("original".equals(iconStyle)) {
		return getOriginalIconForType(type);
	} else if ("modern".equals(iconStyle)) {
		return getModernIconForType(type, hasName);
	}
	return getClassicIconForType(type);
}

private void loadOriginalIcons() {
	if (originalIconsLoaded) return;

	originalIcons = new HashMap<>();

	String[] iconNames = {
		"tag_byte", "tag_short", "tag_int", "tag_long",
		"tag_float", "tag_double", "tag_byte_array", "tag_string",
		"tag_list", "tag_compound", "tag_int_array", "tag_long_array", "tag_short_array"
	};

	TagType[] types = {
		TagType.TAG_BYTE, TagType.TAG_SHORT, TagType.TAG_INT, TagType.TAG_LONG,
		TagType.TAG_FLOAT, TagType.TAG_DOUBLE, TagType.TAG_BYTE_ARRAY, TagType.TAG_STRING,
		TagType.TAG_LIST, TagType.TAG_COMPOUND, TagType.TAG_INT_ARRAY, TagType.TAG_LONG_ARRAY, TagType.TAG_SHORT_ARRAY
	};

	for (int i = 0; i < iconNames.length; i++) {
		String path = "/com/MCXCC/JNBTExplorer/resources/original/" + iconNames[i] + ".png";
		URL url = getClass().getResource(path);
		if (url != null) {
			ImageIcon icon = new ImageIcon(url);
			if (icon.getIconWidth() > 16 || icon.getIconHeight() > 16) {
				icon = new ImageIcon(icon.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH));
			}
			originalIcons.put(types[i], icon);
		}
	}

	originalIconsLoaded = true;
}

private Icon getOriginalIconForType(TagType type) {
	loadOriginalIcons();
	Icon icon = originalIcons.get(type);
	if (icon != null) {
		return icon;
	}
	return getClassicIconForType(type);
}

private Icon getClassicIconForType(TagType type) {
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

private Icon getModernIconForType(TagType type, boolean hasName) {
	int size = hasName ? 16 : 10;
	return switch (type) {
		case TAG_BYTE -> createModernIcon(new Color(0, 188, 212), Shape.CIRCLE, size);
		case TAG_SHORT -> createModernIcon(new Color(0, 151, 167), Shape.CIRCLE, size);
		case TAG_INT -> createModernIcon(new Color(33, 150, 243), Shape.SQUARE, size);
		case TAG_LONG -> createModernIcon(new Color(25, 118, 210), Shape.SQUARE, size);
		case TAG_FLOAT -> createModernIcon(new Color(76, 175, 80), Shape.DIAMOND, size);
		case TAG_DOUBLE -> createModernIcon(new Color(56, 142, 60), Shape.DIAMOND, size);
		case TAG_BYTE_ARRAY -> createModernIcon(new Color(156, 39, 176), Shape.HEXAGON, size);
		case TAG_STRING -> createModernIcon(new Color(255, 193, 7), Shape.ROUNDED, size);
		case TAG_LIST -> createModernIcon(new Color(255, 152, 0), Shape.FOLDER, size);
		case TAG_COMPOUND -> createModernIcon(new Color(244, 67, 54), Shape.FOLDER, size);
		case TAG_INT_ARRAY -> createModernIcon(new Color(123, 31, 162), Shape.HEXAGON, size);
		case TAG_LONG_ARRAY -> createModernIcon(new Color(74, 20, 140), Shape.HEXAGON, size);
		case TAG_SHORT_ARRAY -> createModernIcon(new Color(186, 104, 200), Shape.HEXAGON, size);
		default -> null;
	};
}

private enum Shape {
	CIRCLE, SQUARE, DIAMOND, HEXAGON, ROUNDED, FOLDER
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

private Icon createModernIcon(Color color, Shape shape, int size) {
	return new Icon() {
		@Override
		public void paintIcon(Component c, Graphics g, int x, int y) {
			Graphics2D g2d = (Graphics2D) g.create();
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			int offset = (16 - size) / 2;

			g2d.setColor(color);
			switch (shape) {
				case CIRCLE -> g2d.fillOval(x + offset, y + offset, size, size);
				case SQUARE -> {
					g2d.fillRect(x + offset, y + offset, size, size);
					g2d.setColor(color.darker());
					g2d.drawRect(x + offset, y + offset, size - 1, size - 1);
				}
				case DIAMOND -> {
					int halfSize = size / 2;
					int[] xp = {x + offset + halfSize, x + offset + size - 1, x + offset + halfSize, x + offset};
					int[] yp = {y + offset, y + offset + halfSize, y + offset + size - 1, y + offset + halfSize};
					g2d.fillPolygon(xp, yp, 4);
				}
				case HEXAGON -> {
					int quarter = size / 4;
					int[] xp = {x + offset + quarter, x + offset + size - quarter - 1, x + offset + size - 1, x + offset + size - quarter - 1, x + offset + quarter, x + offset};
					int[] yp = {y + offset, y + offset, y + offset + size / 2, y + offset + size - 1, y + offset + size - 1, y + offset + size / 2};
					g2d.fillPolygon(xp, yp, 6);
				}
				case ROUNDED -> g2d.fillRoundRect(x + offset, y + offset, size, size, 4, 4);
				case FOLDER -> {
					int folderHeight = size * 11 / 16;
					int folderY = y + offset + size - folderHeight;
					g2d.fillRect(x + offset, folderY, size, folderHeight);
					g2d.fillRect(x + offset, y + offset, size * 6 / 16, size * 3 / 16);
				}
			}

			g2d.dispose();
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
