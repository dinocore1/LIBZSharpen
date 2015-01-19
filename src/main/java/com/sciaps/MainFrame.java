package com.sciaps;

import com.sciaps.view.LIBZUnitConnectedPanel;
import com.sciaps.view.LIBZUnitDisconnectedPanel;
import java.awt.Image;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
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
        //setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    public void displayFrame()
    {
        setVisible(true);

        try
        {
            URL url = ClassLoader.getSystemResource("sciaps_icon.png");
            Image icon = ImageIO.read(url);
            setIconImage(icon);
        }
        catch (IOException ex)
        {
            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void onLIBZUnitConnected()
    {
        getContentPane().removeAll();
        add(new LIBZUnitConnectedPanel(this));
        refreshUI();
    }

    public void refreshUI()
    {
        invalidate();
        validate();
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