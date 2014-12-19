package com.sciaps.view.tabs.common;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JPanel;

/**
 *
 * @author sgowen
 */
public final class DragDropZonePanel extends JPanel
{
    public DragDropZonePanel()
    {
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);

        Dimension arcs = new Dimension(15, 15);
        int width = getWidth();
        int height = getHeight();
        Graphics2D graphics = (Graphics2D) g;
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // paint background
        graphics.setColor(new Color(114, 187, 83, 255));
        graphics.fillRoundRect(0, 0, width - 1, height - 1, arcs.width, arcs.height);

        // paint border
        graphics.setColor(new Color(0, 0, 0, 0));
        graphics.drawRoundRect(0, 0, width - 1, height - 1, arcs.width, arcs.height);
    }
}