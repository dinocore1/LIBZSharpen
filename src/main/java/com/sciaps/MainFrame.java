package com.sciaps;

import com.devsmart.swing.BackgroundTask;
import com.google.inject.Inject;
import com.sciaps.common.swing.OverlayPane;
import com.sciaps.common.swing.libzunitapi.LibzUnitApiHandler;
import com.sciaps.view.MainTabsPanel;
import net.miginfocom.swing.MigLayout;
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

    @Inject
    LibzUnitApiHandler mApiHandler;

    MainTabsPanel mMainTabsPanel;

    private final JLayeredPane mLayeredPane;

    public MainFrame() {
        setTitle("LIBZ Sharpen");

        setSize(900, 700);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setIcon();

        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        mLayeredPane = new JLayeredPane();
        //mLayeredPane.setLayout(new BorderLayout());
        contentPane.add(mLayeredPane);

        mMainTabsPanel = Main.mInjector.getInstance(MainTabsPanel.class);
        mMainTabsPanel.setMainFrame(this);
        mLayeredPane.add(mMainTabsPanel, new Integer(1));


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

            public JProgressBar mProgressBar;
            public OverlayPane mOverlayPane;

            @Override
            public void onBefore() {
                mOverlayPane = new OverlayPane();

                mOverlayPane.mContentPanel.setLayout(new MigLayout());
                mProgressBar = new JProgressBar();
                mProgressBar.setIndeterminate(true);
                mOverlayPane.mContentPanel.add(mProgressBar);

                mLayeredPane.add(mOverlayPane, new Integer(1));
            }

            @Override
            public void onBackground() {
                try {
                    mApiHandler.pullFromLibzUnit();
                } catch (IOException e) {
                    logger.error("", e);
                }
            }

            @Override
            public void onAfter() {
                mLayeredPane.remove(mOverlayPane);
            }
        });

    }

    public void doPush() {

    }
}