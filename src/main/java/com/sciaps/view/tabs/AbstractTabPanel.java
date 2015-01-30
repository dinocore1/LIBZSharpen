package com.sciaps.view.tabs;

import javax.swing.*;

/**
 *
 * @author sgowen
 */
public abstract class AbstractTabPanel extends JPanel {

    public abstract String getTabName();
    
    public abstract String getToolTip();

    public abstract void customizeMenuBar(JMenuBar menuBar);

    public abstract void onDisplay();
}