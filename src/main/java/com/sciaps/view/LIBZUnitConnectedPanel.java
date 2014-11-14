package com.sciaps.view;

import com.sciaps.MainFrame;
import com.sciaps.view.tabs.AbstractTabPanel;
import com.sciaps.view.tabs.CalibrationCurvesPanel;
import com.sciaps.view.tabs.CalibrationModelsPanel;
import com.sciaps.view.tabs.ConfigureStandardsPanel;
import com.sciaps.view.tabs.DefineRegionsPanel;
import com.sciaps.view.tabs.IntensityRatioFormulasPanel;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author sgowen
 */
public final class LIBZUnitConnectedPanel extends JPanel
{
    private final MainFrame _mainFrame;

    public LIBZUnitConnectedPanel(MainFrame mainFrame)
    {
        _mainFrame = mainFrame;

        final List<AbstractTabPanel> panels = new ArrayList<AbstractTabPanel>();
        panels.add(new ConfigureStandardsPanel(_mainFrame));
        panels.add(new DefineRegionsPanel(_mainFrame));
        panels.add(new IntensityRatioFormulasPanel(_mainFrame));
        panels.add(new CalibrationModelsPanel(_mainFrame));
        panels.add(new CalibrationCurvesPanel(_mainFrame));

        final JTabbedPane tabs = new JTabbedPane();
        tabs.setBorder(BorderFactory.createEmptyBorder());
        for (int i = 0; i < panels.size(); i++)
        {
            tabs.add(panels.get(i), panels.get(i).getTabName());
            tabs.setToolTipTextAt(i, panels.get(i).getClass().getSimpleName());
        }
        tabs.addChangeListener(new ChangeListener()
        {
            @Override
            public void stateChanged(ChangeEvent e)
            {
                selectTab(panels.get(tabs.getSelectedIndex()));
            }
        });

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder());

        add(tabs, BorderLayout.CENTER);

        selectTab(panels.get(0));
    }

    private void selectTab(AbstractTabPanel tabPanel)
    {
        JMenu libzUnitMenu = new JMenu("LIBZ Unit");
        libzUnitMenu.setMnemonic(KeyEvent.VK_L);

        JMenuItem pullMenuItem = new JMenuItem("Pull...", KeyEvent.VK_LEFT);
        pullMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, ActionEvent.ALT_MASK));
        pullMenuItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent ae)
            {
                // TODO
            }
        });

        JMenuItem pushMenuItem = new JMenuItem("Push...", KeyEvent.VK_RIGHT);
        pushMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, ActionEvent.ALT_MASK));
        pushMenuItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent ae)
            {
                // TODO
            }
        });

        JMenuItem disconnectMenuItem = new JMenuItem("Disconnect...", KeyEvent.VK_ESCAPE);
        disconnectMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, ActionEvent.ALT_MASK));
        disconnectMenuItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent ae)
            {
                _mainFrame.onLIBZUnitDisconnected();
            }
        });

        libzUnitMenu.add(pullMenuItem);
        libzUnitMenu.add(pushMenuItem);
        libzUnitMenu.add(disconnectMenuItem);

        JMenuBar menuBar = new JMenuBar();
        menuBar.add(libzUnitMenu);

        tabPanel.customizeMenuBar(menuBar);

        _mainFrame.setJMenuBar(menuBar);
    }
}