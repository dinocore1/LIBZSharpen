package com.sciaps.view;

import com.google.inject.Inject;
import com.sciaps.Main;
import com.sciaps.MainFrame;
import com.sciaps.async.BaseLibzUnitApiSwingWorker.BaseLibzUnitApiSwingWorkerCallback;
import com.sciaps.common.swing.utils.JDialogUtils;
import com.sciaps.common.swing.utils.SwingUtils;
import com.sciaps.view.tabs.AbstractTabPanel;
import com.sciaps.view.tabs.CalibrationCurvesPanel;
import com.sciaps.view.tabs.ConfigureStandardsPanel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author sgowen
 */
public final class MainTabsPanel extends JPanel
{
    private AbstractTabPanel _currentlySelectedTabPanel;


    private MainFrame _mainFrame;

    public MainTabsPanel() {

        final List<AbstractTabPanel> panels = new ArrayList<AbstractTabPanel>();
        panels.add(Main.mInjector.getInstance(ConfigureStandardsPanel.class));
        panels.add(Main.mInjector.getInstance(CalibrationCurvesPanel.class));

        final JTabbedPane tabs = new JTabbedPane();
        tabs.setBorder(BorderFactory.createEmptyBorder());
        for (int i = 0; i < panels.size(); i++) {
            tabs.add(panels.get(i), panels.get(i).getTabName());
            tabs.setToolTipTextAt(i, panels.get(i).getToolTip());
        }
        tabs.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                selectTab(panels.get(tabs.getSelectedIndex()));
            }
        });

        setLayout(new BorderLayout());
        add(tabs, BorderLayout.CENTER);

    }

    public void setMainFrame(MainFrame mainFrame) {
        _mainFrame = mainFrame;
        //selectTab(panels.get(0));
    }

    private void selectTab(AbstractTabPanel tabPanel) {
        _currentlySelectedTabPanel = tabPanel;
        JMenu libzUnitMenu = new JMenu("LIBZ Unit");
        libzUnitMenu.setMnemonic(KeyEvent.VK_L);

        JMenuItem pullMenuItem = new JMenuItem("Pull...", KeyEvent.VK_LEFT);
        pullMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, ActionEvent.ALT_MASK));
        pullMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                _mainFrame.doPull();
            }
        });

        JMenuItem pushMenuItem = new JMenuItem("Push...", KeyEvent.VK_RIGHT);
        pushMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, ActionEvent.ALT_MASK));
        pushMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                _mainFrame.doPush();
            }
        });

        libzUnitMenu.add(pullMenuItem);
        libzUnitMenu.add(pushMenuItem);

        JMenuBar menuBar = new JMenuBar();
        menuBar.add(libzUnitMenu);

        tabPanel.customizeMenuBar(menuBar);
        tabPanel.onDisplay();

        _mainFrame.setJMenuBar(menuBar);
    }
}