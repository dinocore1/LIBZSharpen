package com.sciaps;

import com.sciaps.async.BaseLibzUnitApiSwingWorker;
import com.sciaps.async.LibzUnitConnectSwingWorker;
import com.sciaps.async.LibzUnitPullSwingWorker;
import com.sciaps.common.swing.global.LibzUnitManager;
import com.sciaps.common.swing.libzunitapi.HttpLibzUnitApiHandler;
import com.sciaps.common.swing.utils.JDialogUtils;
import com.sciaps.common.swing.utils.SwingUtils;
import com.sciaps.global.InstanceManager;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JDialog;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.apache.commons.lang.StringUtils;

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
                    UIManager.setLookAndFeel("org.pushingpixels.substance.api.skin.SubstanceOfficeSilver2007LookAndFeel");
                }
                catch (Exception e)
                {
                    System.err.println("Substance Graphite failed to initialize");
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, e);
                }

                connect();
            }
        });
    }

    private static void initModules()
    {
        InstanceManager.getInstance().storeInstance(HttpLibzUnitApiHandler.class, new HttpLibzUnitApiHandler());
    }

    private static void connect()
    {
        String libzUnitIPAddress = JOptionPane.showInputDialog("Enter the LIBZ Unit IP Address:");
        if (StringUtils.isEmpty(libzUnitIPAddress))
        {
            libzUnitIPAddress = "localhost";
        }
        libzUnitIPAddress += ":9000";
        LibzUnitManager.getInstance().setIpAddress(libzUnitIPAddress);

        final JDialog progressDialog = JDialogUtils.createDialogWithMessage(new JFrame(), "One Moment...");

        LibzUnitConnectSwingWorker libzUnitConnectSwingWorker = new LibzUnitConnectSwingWorker(new BaseLibzUnitApiSwingWorker.BaseLibzUnitApiSwingWorkerCallback()
        {
            @Override
            public void onComplete(boolean isSuccessful)
            {
                SwingUtils.hideDialog(progressDialog);

                if (isSuccessful)
                {
                    pullFromLibzUnit();
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

                JOptionPane.showMessageDialog(new JFrame(), "Error connecting to the LIBZ Unit", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }, HttpLibzUnitApiHandler.class);

        libzUnitConnectSwingWorker.start();

        progressDialog.setVisible(true);
    }

    private static void pullFromLibzUnit()
    {
        final JDialog progressDialog = JDialogUtils.createDialogWithMessage(new JFrame(), "Connection Successful! Pulling Data...");
        LibzUnitPullSwingWorker libzUnitPullSwingWorker = new LibzUnitPullSwingWorker(new BaseLibzUnitApiSwingWorker.BaseLibzUnitApiSwingWorkerCallback()
        {
            @Override
            public void onComplete(boolean isSuccessful)
            {
                SwingUtils.hideDialog(progressDialog);

                if (isSuccessful)
                {
                    final MainFrame mainFrame = new MainFrame();
                    mainFrame.displayFrame();
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
        }, HttpLibzUnitApiHandler.class);

        libzUnitPullSwingWorker.start();

        progressDialog.setVisible(true);
    }
}