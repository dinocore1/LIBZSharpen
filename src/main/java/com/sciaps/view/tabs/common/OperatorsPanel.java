package com.sciaps.view.tabs.common;

import com.sciaps.MainFrame;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.TransferHandler;

/**
 *
 * @author sgowen
 */
public final class OperatorsPanel extends JPanel
{
    public OperatorsPanel()
    {
        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.Y_AXIS));

        JLabel operatorsLabel = new JLabel("Operators");
        operatorsLabel.setHorizontalAlignment(SwingConstants.CENTER);
        operatorsLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        operatorsLabel.setFont(new Font("Serif", Font.BOLD, 24));
        operatorsLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, operatorsLabel.getPreferredSize().height));

        add(operatorsLabel);

        try
        {
            URL url = ClassLoader.getSystemResource("plus_symbol.png");
            Image icon = ImageIO.read(url);
            JLabel plusSymbolImage = new JLabel(new ImageIcon(icon));
            plusSymbolImage.setText("");
            plusSymbolImage.setHorizontalAlignment(SwingConstants.CENTER);
            plusSymbolImage.setAlignmentX(JLabel.CENTER_ALIGNMENT);
            plusSymbolImage.setBackground(new Color(0, 0, 0, 0));
            plusSymbolImage.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            plusSymbolImage.setTransferHandler(new TransferHandler("text"));
            plusSymbolImage.addMouseListener(new MouseAdapter()
            {
                @Override
                public void mousePressed(MouseEvent evt)
                {
                    JComponent comp = (JComponent) evt.getSource();
                    TransferHandler th = comp.getTransferHandler();

                    th.exportAsDrag(comp, evt, TransferHandler.COPY);
                }
            });

            plusSymbolImage.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
            add(plusSymbolImage);
        }
        catch (IOException e)
        {
            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, e);
        }
    }
}