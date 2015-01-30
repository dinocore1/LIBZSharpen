package com.sciaps;

import com.devsmart.swing.BackgroundTask;
import com.google.inject.Inject;
import com.sciaps.common.swing.libzunitapi.LibzUnitApiHandler;
import com.sciaps.view.MainTabsPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URL;


public final class MainFrame extends JFrame
{

    static Logger logger = LoggerFactory.getLogger(MainFrame.class);

    private final JLayeredPane mLayeredPane;
    private final MainTabsPanel mMainTabsPanel;

    @Inject
    LibzUnitApiHandler mApiHandler;

    public MainFrame() {
        setTitle("LIBZ Sharpen");

        setSize(900, 700);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setIcon();

        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        mLayeredPane = new JLayeredPane();
        mLayeredPane.setLayout(new BorderLayout());
        contentPane.add(mLayeredPane);

        mMainTabsPanel = new MainTabsPanel(this);
        mLayeredPane.add(mMainTabsPanel, new Integer(1));


        pack();
        setVisible(true);

        String libzUnitIPAddress = JOptionPane.showInputDialog("Enter the LIBZ Unit IP Address:");
        if (libzUnitIPAddress == null) {
            dispose();
        } else {
            Main.mBaseModule.setIpaddress(libzUnitIPAddress);
            doPull();
        }

    }

    private void setIcon() {
        try {
            URL url = ClassLoader.getSystemResource("sciaps_icon.png");
            Image icon = ImageIO.read(url);
            setIconImage(icon);
        } catch (IOException ex) {
            logger.error("", ex);
        }
    }


    public void doPull() {
        BackgroundTask.runBackgroundTask(new BackgroundTask() {
            @Override
            public void onBackground() {
                try {
                    mApiHandler.pullFromLibzUnit();
                } catch (IOException e) {
                    logger.error("", e);
                }
            }
        });

    }
}