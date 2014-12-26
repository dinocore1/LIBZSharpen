package com.sciaps.view.tabs.intensityratioformulas;

import com.sciaps.MainFrame;
import com.sciaps.view.tabs.defineregions.RegionsPanel;
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
import javax.swing.SwingConstants;
import javax.swing.TransferHandler;
import org.jdesktop.swingx.JXCollapsiblePane;

/**
 *
 * @author sgowen
 */
public final class RegionsAndOperatorsJXCollapsiblePane extends JXCollapsiblePane
{
    private final RegionsPanel _regionsPanel;

    public RegionsAndOperatorsJXCollapsiblePane(Direction direction)
    {
        super(direction);

        getContentPane().setLayout(new javax.swing.BoxLayout(getContentPane(), javax.swing.BoxLayout.Y_AXIS));

        _regionsPanel = new RegionsPanel(null, true);
        _regionsPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 60, 0));

        add(_regionsPanel);

        JLabel operatorsLabel = new JLabel("Operators");
        operatorsLabel.setHorizontalAlignment(SwingConstants.CENTER);
        operatorsLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        operatorsLabel.setFont(new Font("Serif", Font.BOLD, 24));
        operatorsLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, operatorsLabel.getPreferredSize().height));
        operatorsLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        add(operatorsLabel);

        try
        {
            URL url = ClassLoader.getSystemResource("res/plus_symbol.png");
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

            add(plusSymbolImage);
        }
        catch (IOException e)
        {
            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public void refresh()
    {
        _regionsPanel.refresh();
    }
}