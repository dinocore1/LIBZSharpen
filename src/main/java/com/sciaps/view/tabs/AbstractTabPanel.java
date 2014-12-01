package com.sciaps.view.tabs;

import com.sciaps.MainFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;

/**
 *
 * @author sgowen
 */
public abstract class AbstractTabPanel extends JPanel
{
    protected final MainFrame _mainFrame;

    public AbstractTabPanel(MainFrame mainFrame)
    {
        _mainFrame = mainFrame;
    }

    public abstract String getTabName();

    public abstract void customizeMenuBar(JMenuBar menuBar);

    public abstract void onDisplay();
}