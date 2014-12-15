package com.sciaps.view.tabs.intensityratioformulas;

import com.sciaps.view.tabs.defineregions.RegionsPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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

        JLabel plusSymbolImage = new JLabel(new ImageIcon(Toolkit.getDefaultToolkit().getImage("plus_symbol.png")));
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

    public void refresh()
    {
        _regionsPanel.refresh();
    }
}