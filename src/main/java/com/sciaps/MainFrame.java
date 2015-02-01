package com.sciaps;

import com.devsmart.swing.BackgroundTask;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.sciaps.common.swing.FramePanel;
import com.sciaps.common.swing.OverlayPane;
import com.sciaps.common.swing.libzunitapi.LibzUnitApiHandler;
import com.sciaps.events.PullEvent;
import com.sciaps.view.MainTabsPanel;
import net.miginfocom.swing.MigLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.URL;


public final class MainFrame extends JFrame
{

    static Logger logger = LoggerFactory.getLogger(MainFrame.class);


    public final JMenuBar mMenuBar = new JMenuBar();
    public final FramePanel mLayeredPane = new FramePanel();

    @Inject
    LibzUnitApiHandler mApiHandler;

    @Inject
    EventBus mGlobalEventBus;

    MainTabsPanel mMainTabsPanel;

    public MainFrame() {
        setTitle("LIBZ Sharpen");

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setIcon();

        add(mLayeredPane, BorderLayout.CENTER);
        setupMenuBar();


        mMainTabsPanel = Main.mInjector.getInstance(MainTabsPanel.class);
        mLayeredPane.add(mMainTabsPanel, new Integer(0));


        setSize(900, 700);
        setLocationRelativeTo(null);
        setVisible(true);

        String libzUnitIPAddress = JOptionPane.showInputDialog("Enter the LIBZ Unit IP Address:");
        if (libzUnitIPAddress == null) {
            dispose();
        } else {
            Main.mBaseModule.setIpaddress(libzUnitIPAddress);
            doPull();
        }

    }


    private void setupMenuBar() {

        JMenu libzUnitMenu = new JMenu("LIBZ Unit");

        JMenuItem pullMenuItem = new JMenuItem("Pull...");
        pullMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                doPull();
            }
        });

        JMenuItem pushMenuItem = new JMenuItem("Push...", KeyEvent.VK_RIGHT);
        pushMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, ActionEvent.ALT_MASK));
        pushMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                doPush();
            }
        });

        libzUnitMenu.add(pullMenuItem);
        libzUnitMenu.add(pushMenuItem);

        mMenuBar.add(libzUnitMenu);

        setJMenuBar(mMenuBar);
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

            private boolean mSuccess = false;
            public JProgressBar mProgressBar;
            public OverlayPane mOverlayPane;

            @Override
            public void onBefore() {
                mOverlayPane = new OverlayPane();

                mOverlayPane.mContentPanel.setLayout(new MigLayout(""));
                mProgressBar = new JProgressBar();
                mProgressBar.setIndeterminate(true);
                mOverlayPane.mContentPanel.add(mProgressBar, "wrap");

                JLabel label = new JLabel("Loading...");
                mOverlayPane.mContentPanel.add(label, "");

                mLayeredPane.add(mOverlayPane, new Integer(1));

                revalidate();
                repaint();
            }

            @Override
            public void onBackground() {
                try {
                    mApiHandler.pullFromLibzUnit();
                    mSuccess = true;
                } catch (IOException e) {
                    logger.error("", e);
                }
            }

            @Override
            public void onAfter() {
                mGlobalEventBus.post(new PullEvent(mSuccess));
                mLayeredPane.remove(mOverlayPane);
                revalidate();
                repaint();
            }
        });

    }

    public void doPush() {

    }
}