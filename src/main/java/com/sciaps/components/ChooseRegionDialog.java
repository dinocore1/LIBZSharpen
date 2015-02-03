package com.sciaps.components;


import com.sciaps.Main;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ChooseRegionDialog extends JDialog {

    private final RegionSpectrumView mRegionSpectrumView;
    private final JButton mSelectButton;

    public ChooseRegionDialog(JFrame topFrame) {
        super(topFrame, true);

        setPreferredSize(new Dimension(700, 500));

        setLayout(new MigLayout("fill"));
        
        mRegionSpectrumView = Main.mInjector.getInstance(RegionSpectrumView.class);
        add(mRegionSpectrumView, "grow, wrap");

        mSelectButton = new JButton();
        mSelectButton.addActionListener(mOnSelect);
        add(mSelectButton, "align right");

    }

    private final ActionListener mOnSelect = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {

        }
    };
}
