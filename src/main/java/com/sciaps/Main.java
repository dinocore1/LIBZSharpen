package com.sciaps;

import com.sciaps.temp.HttpLibzUnitApiHandler;
import com.sciaps.global.InstanceManager;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 *
 * @author sgowen
 */
public final class Main
{
    public static void main(String[] args)
    {
        initModules();

        JFrame.setDefaultLookAndFeelDecorated(true);

        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    UIManager.setLookAndFeel("org.pushingpixels.substance.api.skin.SubstanceGraphiteLookAndFeel");
                }
                catch (Exception e)
                {
                    System.err.println("Substance Graphite failed to initialize");
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, e);
                }

                MainFrame mainFrame = new MainFrame();
                mainFrame.displayFrame();
            }
        });
    }

    private static void initModules()
    {
        InstanceManager.getInstance().storeInstance(HttpLibzUnitApiHandler.class, new HttpLibzUnitApiHandler());
    }
}