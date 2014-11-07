package com.sciaps;

import com.sciaps.view.LIBZUnitConnectedPanel;
import com.sciaps.view.LIBZUnitDisconnectedPanel;
import javax.swing.JFrame;

/**
 *
 * @author sgowen
 */
public final class MainFrame extends JFrame
{
    public MainFrame()
    {
        super("LIBZ Sharpen");

        initUIForOnLIBZUnitDisconnected();

        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    public void onLIBZUnitConnected()
    {
        getContentPane().removeAll();
        add(new LIBZUnitConnectedPanel(this));
        refreshUI();
    }

    public void refreshUI()
    {
        revalidate();
        repaint();
    }

    public void onLIBZUnitDisconnected()
    {
        getContentPane().removeAll();
        setJMenuBar(null);
        initUIForOnLIBZUnitDisconnected();
        refreshUI();
    }

    private void initUIForOnLIBZUnitDisconnected()
    {
        add(new LIBZUnitDisconnectedPanel(this));
    }
}