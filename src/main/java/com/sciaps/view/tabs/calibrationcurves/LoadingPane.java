package com.sciaps.view.tabs.calibrationcurves;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;


public class LoadingPane extends JComponent implements MouseListener {

    public final JProgressBar mProgressBar;
    private final JPanel mPanel;

    public LoadingPane() {
        super();

        addMouseListener(this);

        mPanel = new JPanel(new MigLayout("", "", ""));
        add(mPanel);


        mProgressBar = new JProgressBar(0, 100);
        mProgressBar.setIndeterminate(false);
        mPanel.add(mProgressBar, "w 200!, wrap");

        JLabel label = new JLabel("Downloading Data...");
        mPanel.add(label, "align center");
    }

    @Override
    public void doLayout() {
        int x = (getWidth() - mPanel.getPreferredSize().width) / 2 ;
        int y = (getHeight() - mPanel.getPreferredSize().height) / 2;
        mPanel.setLocation(x, y);
        mPanel.setSize(mPanel.getPreferredSize());
    }

    @Override
    protected void paintComponent(Graphics g) {
        g.setColor(new Color(0, 0, 0, 0.6f));
        g.fillRect(0, 0, getWidth(), getHeight());
    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {
        redispatchMouseEvent(mouseEvent, false);
    }

    @Override
    public void mousePressed(MouseEvent mouseEvent) {
        redispatchMouseEvent(mouseEvent, false);
    }

    @Override
    public void mouseReleased(MouseEvent mouseEvent) {
        redispatchMouseEvent(mouseEvent, true);
    }

    @Override
    public void mouseEntered(MouseEvent mouseEvent) {
        redispatchMouseEvent(mouseEvent, false);
    }

    @Override
    public void mouseExited(MouseEvent mouseEvent) {
        redispatchMouseEvent(mouseEvent, false);
    }

    private void redispatchMouseEvent(MouseEvent e, boolean repaint) {

    }
}
