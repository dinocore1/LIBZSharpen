package com.sciaps.view.tabs;

import javax.swing.*;

/**
 *
 * @author sgowen
 */
public abstract class AbstractTabPanel extends JPanel {

    public abstract void onDisplay();
    public abstract void onHide();
}