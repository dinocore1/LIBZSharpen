package com.sciaps;

import com.google.inject.Guice;
import com.google.inject.Injector;
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
    public static Injector mInjector;
    public static BaseModule mBaseModule;

    public static void main(String[] args)
    {
        initModules();

        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                JFrame.setDefaultLookAndFeelDecorated(true);
                try {
                    UIManager.setLookAndFeel("org.pushingpixels.substance.api.skin.SubstanceOfficeSilver2007LookAndFeel");
                } catch (Exception e) {}

                mInjector.getInstance(MainFrame.class);
            }
        });
    }

    private static void initModules() {
        mBaseModule = new BaseModule();
        mInjector = Guice.createInjector(
                mBaseModule,
                //new HttpModule()
                new MockModule()
        );
    }
}