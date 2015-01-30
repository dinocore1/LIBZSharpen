package com.sciaps.view;

import com.sciaps.MainFrame;
import com.sciaps.async.BaseLibzUnitApiSwingWorker;
import com.sciaps.async.LibzUnitConnectSwingWorker;
import com.sciaps.async.LibzUnitPullSwingWorker;
import com.sciaps.common.swing.global.LibzUnitManager;
import com.sciaps.common.swing.libzunitapi.HttpLibzUnitApiHandler;
import com.sciaps.common.swing.utils.JDialogUtils;
import com.sciaps.common.swing.utils.SwingUtils;
import java.awt.event.WindowEvent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author sgowen
 */
public final class LIBZUnitDisconnectedPanel extends JPanel
{
    private final MainFrame _mainFrame;

    public LIBZUnitDisconnectedPanel(MainFrame mainFrame)
    {
        _mainFrame = mainFrame;

        new SwingWorker<Void, Void>()
        {
            @Override
            protected Void doInBackground() throws Exception
            {
                Thread.sleep(1000);

                return null;
            }

            @Override
            public void done()
            {
                connect();
            }
        }.execute();
    }

    private void connect()
    {
        String libzUnitIPAddress = JOptionPane.showInputDialog("Enter the LIBZ Unit IP Address:");
        if (libzUnitIPAddress == null)
        {
            exit();
        }
        else
        {
            if (StringUtils.isEmpty(libzUnitIPAddress))
            {
                libzUnitIPAddress = "localhost";
            }
            libzUnitIPAddress += ":9000";
            LibzUnitManager.getInstance().setIpAddress(libzUnitIPAddress);

            final JDialog progressDialog = JDialogUtils.createDialogWithMessage(_mainFrame, "One Moment...");

            LibzUnitConnectSwingWorker libzUnitConnectSwingWorker = new LibzUnitConnectSwingWorker(HttpLibzUnitApiHandler.class, new BaseLibzUnitApiSwingWorker.BaseLibzUnitApiSwingWorkerCallback<Boolean>()
            {
                @Override
                public void onComplete(Boolean isSuccessful)
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

                    JOptionPane.showConfirmDialog(_mainFrame, "Error connecting to the LIBZ Unit", "Error", JOptionPane.DEFAULT_OPTION);

                    exit();
                }
            });

            libzUnitConnectSwingWorker.start();

            progressDialog.setVisible(true);
        }
    }

    private void pullFromLibzUnit()
    {
        final JDialog progressDialog = JDialogUtils.createDialogWithMessage(_mainFrame, "Connection Successful! Pulling Data...");
        LibzUnitPullSwingWorker libzUnitPullSwingWorker = new LibzUnitPullSwingWorker(new BaseLibzUnitApiSwingWorker.BaseLibzUnitApiSwingWorkerCallback<Boolean>()
        {
            @Override
            public void onComplete(Boolean isSuccessful)
            {
                SwingUtils.hideDialog(progressDialog);

                if (isSuccessful)
                {
                    _mainFrame.onLIBZUnitConnected();
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

                JOptionPane.showConfirmDialog(_mainFrame, "Error pulling data from the LIBZ Unit", "Error", JOptionPane.DEFAULT_OPTION);

                exit();
            }
        });

        libzUnitPullSwingWorker.start();

        progressDialog.setVisible(true);
    }

    private void exit() {
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                _mainFrame.dispatchEvent(new WindowEvent(_mainFrame, WindowEvent.WINDOW_CLOSING));
            }
        });
    }
}