package com.sciaps.view.tabs.intensityratioformulas;

import com.sciaps.view.tabs.common.OperatorsPanel;
import com.sciaps.view.tabs.defineregions.RegionsPanel;
import javax.swing.BorderFactory;
import org.jdesktop.swingx.JXCollapsiblePane;

/**
 *
 * @author sgowen
 */
public final class RegionsAndOperatorsJXCollapsiblePane extends JXCollapsiblePane
{
    private final RegionsPanel _regionsPanel;

    public RegionsAndOperatorsJXCollapsiblePane(Direction direction)
    {
        super(direction);

        getContentPane().setLayout(new javax.swing.BoxLayout(getContentPane(), javax.swing.BoxLayout.Y_AXIS));

        _regionsPanel = new RegionsPanel(null);
        _regionsPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 60, 0));

        add(_regionsPanel);
        add(new OperatorsPanel());
    }

    public void refresh()
    {
        _regionsPanel.refreshData();

        if (!isCollapsed())
        {
            _regionsPanel.refreshUI();
        }
    }
}