package com.sciaps.view;

import com.sciaps.MainFrame;
import com.sciaps.async.LibzUnitConnectSwingWorker;
import com.sciaps.async.LibzUnitConnectSwingWorker.LibzUnitConnectSwingWorkerCallback;
import com.sciaps.async.LibzUnitPullSwingWorker;
import com.sciaps.async.LibzUnitPullSwingWorker.LibzUnitPullSwingWorkerCallback;
import com.sciaps.global.LibzSharpenManager;
import com.sciaps.model.IsAlive;
import java.awt.BorderLayout;
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
import javax.swing.JProgressBar;
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
                final JDialog progressDialog = createDialogWithMessage("One Moment...");

                final String libzUnitIPAddress = libzUnitIPAddressTextField.getText().trim() + ":9000";
                LibzUnitConnectSwingWorker webRequestSwingWorker = new LibzUnitConnectSwingWorker(libzUnitIPAddress, new LibzUnitConnectSwingWorkerCallback()
                {
                    @Override
                    public void onComplete(IsAlive isAlive)
                    {
                        progressDialog.setVisible(false);

                        if (isAlive == null)
                        {
                            onFail();
                        }
                        else
                        {
                            LibzSharpenManager.getInstance().setIpAddress(libzUnitIPAddress);
                            LibzSharpenManager.getInstance().setLibzUnitUniqueIdentifier(isAlive.libzUnitUniqueIdentifier);
                            pullFromLibzUnit();
                        }
                    }

                    @Override
                    public void onFail()
                    {
                        progressDialog.setVisible(false);

                        JOptionPane.showMessageDialog(new JFrame(), "Error connecting to LIBZ Unit", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                });

                webRequestSwingWorker.start();

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
        final JDialog progressDialog = createDialogWithMessage("Connection Successful! Pulling Data...");
        LibzUnitPullSwingWorker libzUnitPullSwingWorker = new LibzUnitPullSwingWorker(new LibzUnitPullSwingWorkerCallback()
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

                JOptionPane.showMessageDialog(new JFrame(), "Error pulling data from LIBZ Unit", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        libzUnitPullSwingWorker.start();

        progressDialog.setVisible(true);
    }

    private JDialog createDialogWithMessage(String message)
    {
        final JDialog progressDialog = new JDialog(_mainFrame, message, true);
        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setIndeterminate(true);
        progressDialog.add(BorderLayout.CENTER, progressBar);
        progressDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        progressDialog.setSize(400, 120);
        progressDialog.setLocationRelativeTo(_mainFrame);

        return progressDialog;
    }
}