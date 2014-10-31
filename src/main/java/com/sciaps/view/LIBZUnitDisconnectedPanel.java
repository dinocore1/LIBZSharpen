package com.sciaps.view;

import com.sciaps.MainFrame;
import com.sciaps.async.WebRequestSwingWorker;
import com.sciaps.async.WebRequestSwingWorker.WebRequestSwingWorkerCallback;
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
                final JDialog progressDialog = new JDialog(_mainFrame, "One Moment...", true);
                JProgressBar progressBar = new JProgressBar(0, 100);
                progressBar.setIndeterminate(true);
                progressDialog.add(BorderLayout.CENTER, progressBar);
                progressDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
                progressDialog.setSize(300, 75);
                progressDialog.setLocationRelativeTo(_mainFrame);

                WebRequestSwingWorker webRequestSwingWorker = new WebRequestSwingWorker(libzUnitIPAddressTextField.getText().trim(), new WebRequestSwingWorkerCallback()
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
                            _mainFrame.onLIBZUnitConnected();
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
}