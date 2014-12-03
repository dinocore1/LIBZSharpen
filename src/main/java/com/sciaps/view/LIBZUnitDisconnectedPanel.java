package com.sciaps.view;

import com.sciaps.common.swing.view.JTextComponentHintLabel;
import com.sciaps.MainFrame;
import com.sciaps.common.swing.async.BaseLibzUnitApiSwingWorker.BaseLibzUnitApiSwingWorkerCallback;
import com.sciaps.common.swing.async.LibzUnitConnectSwingWorker;
import com.sciaps.common.swing.async.LibzUnitPullSwingWorker;
import com.sciaps.common.swing.global.LibzUnitManager;
import com.sciaps.common.swing.libzunitapi.HttpLibzUnitApiHandler;
import com.sciaps.common.swing.utils.JDialogUtils;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

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

        JLabel welcomeMessageLabel = new JLabel("<html><div style=\"text-align: center;\">Welcome to the SciAps Calibration Tool!"
                + "<br>To get started, simply type in the IP address"
                + "<br>of the LIBZ unit you wish to connect to."
                + "<br>If you are connected to a single LIBZ unit"
                + "<br>via usb, just press connect."
                + "</div></html>", SwingConstants.CENTER);
        welcomeMessageLabel.setFont(new Font("Serif", Font.BOLD, 64));
        welcomeMessageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        final JTextField libzUnitIPAddressTextField = new JTextField();
        libzUnitIPAddressTextField.setMaximumSize(new Dimension(300, libzUnitIPAddressTextField.getPreferredSize().height));
        libzUnitIPAddressTextField.setPreferredSize(new Dimension(300, libzUnitIPAddressTextField.getPreferredSize().height));
        libzUnitIPAddressTextField.setHorizontalAlignment(SwingConstants.CENTER);
        JTextComponentHintLabel textComponentHintLabel = new JTextComponentHintLabel("LIBZ Unit IP Address", libzUnitIPAddressTextField);
        textComponentHintLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.X_AXIS));
        inputPanel.setBorder(BorderFactory.createEmptyBorder());
        inputPanel.add(Box.createHorizontalGlue());
        inputPanel.add(libzUnitIPAddressTextField);
        inputPanel.add(Box.createHorizontalGlue());

        JButton connectButton = new JButton("Connect");
        connectButton.setHorizontalAlignment(SwingConstants.CENTER);

        connectButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                final JDialog progressDialog = JDialogUtils.createDialogWithMessage(_mainFrame, "One Moment...");

                final String libzUnitIPAddress = libzUnitIPAddressTextField.getText().trim() + ":9000";
                LibzUnitManager.getInstance().setIpAddress(libzUnitIPAddress);
                LibzUnitConnectSwingWorker libzUnitConnectSwingWorker = new LibzUnitConnectSwingWorker(new BaseLibzUnitApiSwingWorkerCallback()
                {
                    @Override
                    public void onComplete(boolean isSuccessful)
                    {
                        progressDialog.setVisible(false);

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
                        progressDialog.setVisible(false);

                        JOptionPane.showMessageDialog(new JFrame(), "Error connecting to the LIBZ Unit", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }, HttpLibzUnitApiHandler.class);

                libzUnitConnectSwingWorker.start();

                progressDialog.setVisible(true);
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder());
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(connectButton);
        buttonPanel.add(Box.createHorizontalGlue());

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder());

        add(Box.createGlue());
        add(Box.createGlue());
        add(welcomeMessageLabel);
        add(Box.createGlue());
        add(inputPanel);
        add(Box.createGlue());
        add(buttonPanel);
        add(Box.createGlue());
        add(Box.createGlue());
    }

    private void pullFromLibzUnit()
    {
        final JDialog progressDialog = JDialogUtils.createDialogWithMessage(_mainFrame, "Connection Successful! Pulling Data...");
        LibzUnitPullSwingWorker libzUnitPullSwingWorker = new LibzUnitPullSwingWorker(new BaseLibzUnitApiSwingWorkerCallback()
        {
            @Override
            public void onComplete(boolean isSuccessful)
            {
                progressDialog.setVisible(false);

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
                progressDialog.setVisible(false);

                JOptionPane.showMessageDialog(new JFrame(), "Error pulling data from the LIBZ Unit", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }, HttpLibzUnitApiHandler.class);

        libzUnitPullSwingWorker.start();

        progressDialog.setVisible(true);
    }
}