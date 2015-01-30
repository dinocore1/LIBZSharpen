package com.sciaps.view;

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
    private final MainFrame _mainFrame;
    private AbstractTabPanel _currentlySelectedTabPanel;

    public MainTabsPanel(MainFrame mainFrame)
    {
        _mainFrame = mainFrame;

        final List<AbstractTabPanel> panels = new ArrayList<AbstractTabPanel>();
        panels.add(new ConfigureStandardsPanel(_mainFrame));
        panels.add(new CalibrationCurvesPanel(_mainFrame));

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
        selectTab(panels.get(0));
    }

    private void selectTab(AbstractTabPanel tabPanel) {
        _currentlySelectedTabPanel = tabPanel;
        JMenu libzUnitMenu = new JMenu("LIBZ Unit");
        libzUnitMenu.setMnemonic(KeyEvent.VK_L);

        JMenuItem pullMenuItem = new JMenuItem("Pull...", KeyEvent.VK_LEFT);
        pullMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, ActionEvent.ALT_MASK));
        pullMenuItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent ae)
            {
                final int choice = JOptionPane.showOptionDialog(
                        null,
                        "Are you sure you want to pull?\nAll data changed since the last push will be lost.",
                        "Confirm Pull",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.INFORMATION_MESSAGE,
                        null,
                        new String[]
                        {
                            "Cancel", "Pull"
                        },
                        "Cancel");

                if (choice == 1)
                {
                    final JDialog progressDialog = JDialogUtils.createDialogWithMessage(_mainFrame, "Pulling Data...");
                    LibzUnitPullSwingWorker libzUnitPullSwingWorker = new LibzUnitPullSwingWorker(new BaseLibzUnitApiSwingWorkerCallback<Boolean>()
                    {
                        @Override
                        public void onComplete(Boolean isSuccessful)
                        {
                            SwingUtils.hideDialog(progressDialog);

                            if (isSuccessful)
                            {
                                _currentlySelectedTabPanel.onDisplay();
                                JOptionPane.showMessageDialog(new JFrame(), "Data pulled from the LIBZ Unit successfully!", "Success!", JOptionPane.INFORMATION_MESSAGE);
                            }
                            else
                            {
                                onFail();
                            }
                        }

                        @Override
                        public void onFail()
                        {
                            SwingUtils.hideDialog(progressDialog);

                            JOptionPane.showMessageDialog(new JFrame(), "Error pulling data from the LIBZ Unit", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    });

                    libzUnitPullSwingWorker.start();

                    progressDialog.setVisible(true);
                }
            }
        });

        JMenuItem pushMenuItem = new JMenuItem("Push...", KeyEvent.VK_RIGHT);
        pushMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, ActionEvent.ALT_MASK));
        pushMenuItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent ae)
            {
                final JDialog progressDialog = JDialogUtils.createDialogWithMessage(_mainFrame, "Pushing Data...");
                LibzUnitPushSwingWorker libzUnitPushSwingWorker = new LibzUnitPushSwingWorker(new BaseLibzUnitApiSwingWorkerCallback<Boolean>()
                {
                    @Override
                    public void onComplete(Boolean isSuccessful)
                    {
                        SwingUtils.hideDialog(progressDialog);

                        if (isSuccessful)
                        {
                            JOptionPane.showMessageDialog(new JFrame(), "Data pushed to the LIBZ Unit successfully!", "Success!", JOptionPane.INFORMATION_MESSAGE);
                        }
                        else
                        {
                            onFail();
                        }
                    }

                    @Override
                    public void onFail()
                    {
                        SwingUtils.hideDialog(progressDialog);

                        JOptionPane.showMessageDialog(new JFrame(), "Error pushing data to the LIBZ Unit", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                });

                libzUnitPushSwingWorker.start();

                progressDialog.setVisible(true);
            }
        });

        JMenuItem disconnectMenuItem = new JMenuItem("Disconnect...", KeyEvent.VK_ESCAPE);
        disconnectMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, ActionEvent.ALT_MASK));
        disconnectMenuItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent ae)
            {
                final int choice = JOptionPane.showOptionDialog(
                        null,
                        "Are you sure you want to disconnect?\nAll data changed since the last push will be lost.",
                        "Confirm Disconnect",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.INFORMATION_MESSAGE,
                        null,
                        new String[]
                        {
                            "Cancel", "Disconnect"
                        },
                        "Cancel");

                if (choice == 1)
                {
                    _mainFrame.onLIBZUnitDisconnected();
                }
            }
        });

        libzUnitMenu.add(pullMenuItem);
        libzUnitMenu.add(pushMenuItem);
        libzUnitMenu.add(disconnectMenuItem);

        JMenuBar menuBar = new JMenuBar();
        menuBar.add(libzUnitMenu);

        tabPanel.customizeMenuBar(menuBar);
        tabPanel.onDisplay();

        _mainFrame.setJMenuBar(menuBar);
    }
}