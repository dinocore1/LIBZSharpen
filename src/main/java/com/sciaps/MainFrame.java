package com.sciaps;

import com.sciaps.view.LIBZUnitConnectedPanel;
import java.awt.Image;
import java.awt.event.WindowEvent;
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

        add(new LIBZUnitConnectedPanel(this));

        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    public void displayFrame()
    {
        setVisible(true);

        try
        {
            URL url = ClassLoader.getSystemResource("res/sciaps_icon.png");
            Image icon = ImageIO.read(url);
            setIconImage(icon);
        }
        catch (IOException ex)
        {
            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void refreshUI()
    {
        invalidate();
        validate();
        repaint();
    }

    public void onLIBZUnitDisconnected()
    {
        dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    }
}