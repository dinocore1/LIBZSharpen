package com.sciaps;


import com.sciaps.common.data.IRRatio;
import com.sciaps.common.data.Region;
import com.sciaps.components.IRBox;

import javax.swing.*;
import java.awt.*;

public class IRRatioComponentTest {

    public static void main(String[] args) {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {

                    JFrame window = new JFrame();

                    window.setBounds(0, 0, 400, 400);
                    window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                    window.setLayout(new BorderLayout());
                    window.setBackground(Color.RED);

                    IRBox irbox = new IRBox();
                    irbox.setBackground(Color.GREEN);

                    window.add(irbox, BorderLayout.CENTER);

                    //window.pack();

                    window.setVisible(true);


                    IRRatio ratio = new IRRatio();

                    Region r = new Region();
                    r.name = "Mo 550";
                    ratio.numerator.add(r);

                    r = new Region();
                    r.name = "Mo 553";
                    ratio.numerator.add(r);

                    r = new Region();
                    r.name = "Fe 372";
                    ratio.denominator.add(r);

                    irbox.setIRRatio(ratio);

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
