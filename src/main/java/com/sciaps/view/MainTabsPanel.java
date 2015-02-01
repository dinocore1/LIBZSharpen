package com.sciaps.view;

import com.sciaps.Main;
import com.sciaps.MainFrame;
import com.sciaps.view.tabs.AbstractTabPanel;
import com.sciaps.view.tabs.calibrationcurves.CalibrationCurvesPanel;
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


    final JTabbedPane mTablsPane = new JTabbedPane();
    private AbstractTabPanel mCurrentlySelectedTabPanel;

    public MainTabsPanel() {

        mTablsPane.add(Main.mInjector.getInstance(ConfigureStandardsPanel.class), "Standards");
        mTablsPane.add(Main.mInjector.getInstance(CalibrationCurvesPanel.class), "Calibration Curves");

        mTablsPane.addChangeListener(mOnSelectedTabChanged);


        setLayout(new BorderLayout());
        add(mTablsPane, BorderLayout.CENTER);

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                AbstractTabPanel selectedPanel = (AbstractTabPanel) mTablsPane.getSelectedComponent();
                onSelectTab(selectedPanel);
            }
        });

    }

    private final ChangeListener mOnSelectedTabChanged = new ChangeListener() {
        @Override
        public void stateChanged(ChangeEvent changeEvent) {
            AbstractTabPanel tabPanel = (AbstractTabPanel) mTablsPane.getSelectedComponent();
            onSelectTab(tabPanel);
        }
    };

    private void onSelectTab(AbstractTabPanel newPanel) {
        if(mCurrentlySelectedTabPanel != null){
            mCurrentlySelectedTabPanel.onHide();
        }

        mCurrentlySelectedTabPanel = newPanel;
        mCurrentlySelectedTabPanel.onDisplay();
    }

}