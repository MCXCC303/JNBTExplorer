package com.example.nbt.ui;

import com.example.nbt.model.NBTNode;
import com.example.nbt.tag.TagType;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

public class NBTTreeCellRenderer extends DefaultTreeCellRenderer {

@Override
public Component getTreeCellRendererComponent(JTree tree, Object value,
					      boolean selected, boolean expanded,
					      boolean leaf, int row, boolean hasFocus) {
	super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);

	if (value instanceof NBTNode node) {
		TagType type = node.getType();

		if (type != null) {
			setIcon(getIconForType(type));
			setText(node.toString());
		}
	}

	return this;
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
