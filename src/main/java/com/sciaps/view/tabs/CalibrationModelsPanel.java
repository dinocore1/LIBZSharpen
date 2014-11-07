package com.sciaps.view.tabs;

import com.sciaps.MainFrame;
import javax.swing.JMenuBar;

/**
 *
 * @author sgowen
 */
public final class CalibrationModelsPanel extends AbstractTabPanel
{
    private static final String TAB_NAME = "Calibration Models";
    
    public CalibrationModelsPanel(MainFrame mainFrame)
    {
        super(mainFrame);
    }
    
    @Override
    public String getTabName()
    {
        return TAB_NAME;
    }

    @Override
    public void customizeMenuBar(JMenuBar menuBar)
    {
        // TODO
    }
}