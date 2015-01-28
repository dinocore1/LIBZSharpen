package com.sciaps.components;

import com.sciaps.common.data.IRRatio;
import com.sciaps.common.data.Region;
import com.sciaps.common.swing.view.SVGIcon;
import net.miginfocom.swing.MigLayout;
import org.apache.batik.transcoder.TranscoderException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;


public class IRBox extends JComponent {

    private class RegionBox extends JPanel {
        public final Region mRegion;

        public Runnable onDeleteClicked;

        private final JLabel mLabel;
        private final JButton mDeleteButton;
        private final JButton mEditButton;
        private final JPopupMenu mContextMenu;
        private final ActionListener mOnDeleteClicked = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(onDeleteClicked != null) {
                    onDeleteClicked.run();
                }
            }
        };

        public RegionBox(Region r) {
            mRegion = r;

            mContextMenu = new JPopupMenu("Edit");

            JMenuItem item = new JMenuItem("Delete");
            mContextMenu.add(item);

            JMenuItem edit = new JMenuItem("Settings");
            mContextMenu.add(edit);

            setComponentPopupMenu(mContextMenu);

            Box hbox = Box.createHorizontalBox();
            add(hbox);

            mLabel = new JLabel(r.name);
            mLabel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 7));
            mLabel.setFont(mLabel.getFont().deriveFont(Font.BOLD, 18));
            hbox.add(mLabel);

            Box vbox = Box.createVerticalBox();
            hbox.add(vbox);

            mDeleteButton = createIconButton(getClass().getResource("/icons/svg/delete104.svg"), 15, 15);
            mDeleteButton.addActionListener(mOnDeleteClicked);
            vbox.add(mDeleteButton);

            vbox.add(Box.createVerticalStrut(10));

            mEditButton = createIconButton(getClass().getResource("/icons/svg/settings60.svg"), 15, 15);
            mEditButton.addActionListener(new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    RegionBox.this.getComponentPopupMenu().show(mEditButton, mEditButton.getX(), mEditButton.getY());
                }
            });
            vbox.add(mEditButton);

            setMaximumSize(getPreferredSize());

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
            retval.setOpaque(false);
            retval.setContentAreaFilled(false);
            retval.setIcon(icon);
            return retval;
        }
    }


    private IRRatio mIRatio;

    private JScrollPane mNumScroll;
    private JScrollPane mDenomScroll;

    private final Box mNumBoxLayout;
    private final Box mDemBoxLayout;

    private final JButton mNumAddButton;
    private final JButton mDemAddButton;

    public IRBox() {
        mNumBoxLayout = Box.createHorizontalBox();
        JScrollPane scrollPane = new JScrollPane(mNumBoxLayout);
        mNumScroll = scrollPane;
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane);

        mDemBoxLayout = Box.createHorizontalBox();
        scrollPane = new JScrollPane(mDemBoxLayout);
        mDenomScroll = scrollPane;
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane);

        mNumAddButton = createAddNewButton();
        mDemAddButton = createAddNewButton();

    }

    public void setIRRatio(final IRRatio irRatio) {
        mIRatio = irRatio;

        mNumBoxLayout.removeAll();
        mDemBoxLayout.removeAll();

        mNumBoxLayout.add(Box.createHorizontalGlue());
        for(Iterator<Region> it = irRatio.numerator.iterator();it.hasNext();) {
            final Region r = it.next();
            final RegionBox newBox = new RegionBox(r);

            mNumBoxLayout.add(newBox);

            final Component plusSymbol = createPlusSymbol();
            if(it.hasNext()){
                mNumBoxLayout.add(plusSymbol);
            }

            newBox.onDeleteClicked = new Runnable() {
                @Override
                public void run() {
                    irRatio.numerator.remove(r);
                    mNumBoxLayout.remove(newBox);
                    if(plusSymbol != null) {
                        mNumBoxLayout.remove(plusSymbol);
                    }
                    //invalidate();
                    revalidate();
                }
            };
        }

        mNumBoxLayout.add(Box.createHorizontalStrut(20));
        mNumBoxLayout.add(mNumAddButton);
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
        mDemBoxLayout.add(Box.createHorizontalStrut(20));
        mDemBoxLayout.add(mDemAddButton);
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

    private JButton createAddNewButton() {
        try {
            SVGIcon icon = new SVGIcon(getClass().getResource("/icons/svg/add201.svg").toString(), 20, 20);
            JButton retval = new JButton(icon);
            //retval.setMaximumSize(new Dimension(20, 20));
            //retval.setMinimumSize(new Dimension(20, 20));
            //retval.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
            //retval.setOpaque(false);
            retval.setContentAreaFilled(false);



            return retval;

        } catch (TranscoderException e) {
            e.printStackTrace();
            return null;
        }
    }


    @Override
    public Dimension getMinimumSize() {
        int width = 150;

        int height = mNumBoxLayout.getPreferredSize().height;
        height += mDemBoxLayout.getPreferredSize().height;
        height += lineHeight;

        Dimension minSize = new Dimension(width, height);
        return minSize;
    }

    @Override
    public Dimension getPreferredSize() {
        final Container parent = getParent();
        Insets insets = parent.getInsets();

        int width = mNumBoxLayout.getPreferredSize().width;
        width = Math.max(width, mDemBoxLayout.getPreferredSize().width);
        width += insets.left + insets.right;

        int height = mNumBoxLayout.getPreferredSize().height;
        height += mDemBoxLayout.getPreferredSize().height;
        height += insets.top + insets.bottom + lineHeight;

        Dimension perferedSize = new Dimension(width, height);
        return perferedSize;
    }


    final int lineHeight = 5;

    @Override
    public void doLayout() {
        Insets insets = getInsets();
        int maxWidth = getWidth() - (insets.left + insets.right);
        int maxHeight = getHeight() - (insets.top + insets.bottom);

        mNumScroll.setBounds(insets.left, insets.top, maxWidth, (maxHeight - lineHeight) / 2);
        mDenomScroll.setBounds(insets.left, insets.top + (maxHeight + lineHeight)/2, maxWidth, (maxHeight - lineHeight) / 2);
    }


    @Override
    protected void paintComponent(Graphics g) {
        Insets insets = getInsets();
        int maxWidth = getWidth() - (insets.left + insets.right);
        int maxHeight = getHeight() - (insets.top + insets.bottom);

        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(insets.left, insets.top + (maxHeight - lineHeight) / 2, maxWidth, lineHeight);

    }


}
