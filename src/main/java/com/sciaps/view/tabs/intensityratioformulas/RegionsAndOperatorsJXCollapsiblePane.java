package com.sciaps.view.tabs.intensityratioformulas;

import com.sciaps.view.tabs.defineregions.RegionsPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
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

        _regionsPanel = new RegionsPanel(null);
        _regionsPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 60, 0));

        add(_regionsPanel);

        JLabel operatorsLabel = new JLabel("Operators");
        operatorsLabel.setHorizontalAlignment(SwingConstants.CENTER);
        operatorsLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        operatorsLabel.setFont(new Font("Serif", Font.BOLD, 24));
        operatorsLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, operatorsLabel.getPreferredSize().height));
        operatorsLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        add(operatorsLabel);

        JButton plusSymbolButton = new JButton();
        plusSymbolButton.setHorizontalAlignment(SwingConstants.CENTER);
        plusSymbolButton.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        plusSymbolButton.setBackground(new Color(0, 0, 0, 0));
        plusSymbolButton.setBorderPainted(false);
        plusSymbolButton.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage("plus_symbol.png")));
        plusSymbolButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                // TODO, clicking a box should now add a '+' symbol to the IR formula
            }
        });

        add(plusSymbolButton);
    }

    public void refresh()
    {
        _regionsPanel.refresh();
    }
}