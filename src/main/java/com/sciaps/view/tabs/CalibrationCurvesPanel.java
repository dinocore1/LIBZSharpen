package com.sciaps.view.tabs;

import com.sciaps.MainFrame;
import javax.swing.JMenuBar;

/**
 *
 * @author sgowen
 */
public final class CalibrationCurvesPanel extends AbstractTabPanel
{
    private static final String TAB_NAME = "Calibration Curves";
    
    public CalibrationCurvesPanel(MainFrame mainFrame)
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
    
    @Override
    public void onDisplay()
    {
        // TODO
    }
}