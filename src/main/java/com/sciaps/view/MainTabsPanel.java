package com.sciaps.view;

import com.sciaps.Main;
import com.sciaps.MainFrame;
import com.sciaps.view.tabs.AbstractTabPanel;
import com.sciaps.view.tabs.CalibrationCurvesPanel;
import com.sciaps.view.tabs.ConfigureStandardsPanel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author sgowen
 */
public final class MainTabsPanel extends JPanel {


    private MainFrame _mainFrame;

    final JTabbedPane tabs = new JTabbedPane();

    public MainTabsPanel() {

        final List<AbstractTabPanel> panels = new ArrayList<AbstractTabPanel>();
        panels.add(Main.mInjector.getInstance(ConfigureStandardsPanel.class));
        panels.add(Main.mInjector.getInstance(CalibrationCurvesPanel.class));


        tabs.setBorder(BorderFactory.createEmptyBorder());
        for (int i = 0; i < panels.size(); i++) {
            tabs.add(panels.get(i), panels.get(i).getTabName());
            tabs.setToolTipTextAt(i, panels.get(i).getToolTip());
        }
        tabs.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                AbstractTabPanel tabPanel = (AbstractTabPanel) tabs.getSelectedComponent();
                tabPanel.customizeMenuBar(_mainFrame.mMenuBar);
                tabPanel.onDisplay();

            }
        });

        setLayout(new BorderLayout());
        add(tabs, BorderLayout.CENTER);

    }

    public void setMainFrame(MainFrame mainFrame) {
        _mainFrame = mainFrame;
    }

}