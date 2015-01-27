package com.sciaps.components;

import com.sciaps.common.data.IRRatio;
import com.sciaps.common.data.Region;
import com.sciaps.common.swing.view.SVGIcon;
import net.miginfocom.swing.MigLayout;
import org.apache.batik.transcoder.TranscoderException;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;


public class IRBox extends JComponent {

    private class RegionBox extends JPanel {
        public final Region mRegion;

        private final JLabel mLabel;
        private final JButton mDeleteButton;
        private final JButton mEditButton;

        public RegionBox(Region r) {
            super();
            setBackground(Color.BLUE);
            mRegion = r;
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            //setLayout(new MigLayout("debug"));
            //setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 1));


            mLabel = new JLabel(r.name);
            mLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            mLabel.setFont(mLabel.getFont().deriveFont(Font.BOLD, 18));
            add(mLabel);
            //add(mLabel, "span 2 2");

            Box vbox = Box.createVerticalBox();

            mDeleteButton = createIconButton(getClass().getResource("/icons/svg/delete104.svg"), 15, 15);
            vbox.add(mDeleteButton);

            vbox.add(Box.createVerticalStrut(20));
            //add(mDeleteButton, "cell 3 0");

            mEditButton = createIconButton(getClass().getResource("/icons/svg/settings60.svg"), 15, 15);
            vbox.add(mEditButton);
            //add(mEditButton, "cell 3 1");

            add(vbox);


        }

        private JButton createIconButton(URL resouce, int width, int height) {
            JButton retval = new JButton();
            retval.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
            SVGIcon icon = null;
            try {
                icon = new SVGIcon(resouce.toString(), width, height);
            } catch (TranscoderException e) {
                e.printStackTrace();
            }
            retval.setIcon(icon);
            return retval;
        }
    }


    private IRRatio mIRatio;

    private final Box mNumBoxLayout;
    private final Box mDemBoxLayout;

    public IRBox() {
        mNumBoxLayout = Box.createHorizontalBox();
        add(mNumBoxLayout);

        mDemBoxLayout = Box.createHorizontalBox();
        add(mDemBoxLayout);
    }

    public void setIRRatio(IRRatio irRatio) {
        mIRatio = irRatio;

        mNumBoxLayout.removeAll();
        mDemBoxLayout.removeAll();


        mNumBoxLayout.add(Box.createHorizontalGlue());
        for(Iterator<Region> it = irRatio.numerator.iterator();it.hasNext();) {
            Region r = it.next();
            RegionBox newBox = new RegionBox(r);
            mNumBoxLayout.add(newBox);

            if(it.hasNext()){
                mNumBoxLayout.add(createPlusSymbol());
            }
        }
        mNumBoxLayout.add(Box.createHorizontalGlue());

        mDemBoxLayout.add(Box.createHorizontalGlue());
        for(Iterator<Region> it = irRatio.denominator.iterator();it.hasNext();) {
            Region r = it.next();
            RegionBox newBox = new RegionBox(r);
            mDemBoxLayout.add(newBox);

            if(it.hasNext()){
                mDemBoxLayout.add(createPlusSymbol());

            }
        }
        mDemBoxLayout.add(Box.createHorizontalGlue());
    }

    private Component createPlusSymbol() {
        try {
            SVGIcon icon = new SVGIcon(getClass().getResource("/icons/svg/add202.svg").toString(), 30, 30);
            return new JLabel(icon);

        } catch (TranscoderException e) {
            e.printStackTrace();
            return null;
        }
    }


    @Override
    public Dimension getMinimumSize() {
        return new Dimension(150, 100);
    }

    @Override
    public Dimension getPreferredSize() {

        final Container parent = getParent();
        Insets insets = parent.getInsets();

        double width = mNumBoxLayout.getPreferredSize().getWidth();
        width += insets.left + insets.right;

        double height = mNumBoxLayout.getPreferredSize().getHeight();
        height += insets.top + insets.bottom;

        Dimension perferedSize = new Dimension(0,0);
        perferedSize.setSize(width, height);

        return perferedSize;
    }

    @Override
    public void doLayout() {
        final Container parent = getParent();
        Insets insets = parent.getInsets();
        int maxWidth = parent.getWidth() - (insets.left + insets.right);
        int maxHeight = parent.getHeight() - (insets.top + insets.bottom);

        mNumBoxLayout.setBounds(0, 0, maxWidth, (maxHeight - 20) / 2);

        mDemBoxLayout.setBounds(0, (maxHeight + 20)/2, maxWidth, (maxHeight - 20) / 2);
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        final int width = getWidth();
        final int height = getHeight();

        final int lineHeight = 15;

        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(Color.BLACK);

        g2.fillRect(0, (height - lineHeight) / 2, width, lineHeight);


    }

}
