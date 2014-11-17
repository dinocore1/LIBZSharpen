package com.sciaps.view.tabs;

import com.sciaps.MainFrame;
import javax.swing.JMenuBar;

/**
 *
 * @author sgowen
 */
public final class IntensityRatioFormulasPanel extends AbstractTabPanel
{
    private static final String TAB_NAME = "Intensity Ratio Formulas";
    
    public IntensityRatioFormulasPanel(MainFrame mainFrame)
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