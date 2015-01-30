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
import java.net.URL;
import java.util.Iterator;


public class IRBox extends JComponent {

    private class RegionBox extends JPanel {
        public Region mRegion;

        public Runnable onDeleteClicked;

        private final JLabel mLabel;
        private final JButton mEditButton;
        private final JPopupMenu mContextMenu;
        private final ActionListener mOnDeleteClicked = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(RegionBox.this);

                String message = String.format("Delete '%s': Are you sure?", mRegion.name);

                int selected = JOptionPane.showConfirmDialog(topFrame, message, "Are You Sure?", JOptionPane.YES_NO_OPTION);
                if(selected == JOptionPane.YES_OPTION) {
                    if(onDeleteClicked != null) {
                        onDeleteClicked.run();
                    }
                }
            }
        };

        private final ActionListener mOnEditClicked = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {

                JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(RegionBox.this);

                final RegionEdit paramEdit = new RegionEdit();
                paramEdit.setRegion(mRegion);

                final JDialog editDialog = new JDialog(topFrame, "Edit Region", true);
                editDialog.setLayout(new MigLayout("fill"));

                editDialog.setPreferredSize(new Dimension(430, 330));


                editDialog.add(paramEdit, "grow, wrap");

                JButton saveButton = new JButton("Save");
                editDialog.add(saveButton, "gapy 3mm, align right");

                saveButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        paramEdit.save();
                        editDialog.setVisible(false);
                        editDialog.dispose();
                        setRegion(mRegion);
                    }
                });

                editDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                editDialog.pack();

                editDialog.setLocationRelativeTo(topFrame);
                editDialog.setVisible(true);

            }
        };

        public void setRegion(Region r) {
            mRegion = r;
            mLabel.setText(mRegion.name);
        }

        public RegionBox() {
            mContextMenu = new JPopupMenu("Edit");

            JMenuItem edit = new JMenuItem("Settings");
            edit.addActionListener(mOnEditClicked);
            mContextMenu.add(edit);

            JMenuItem item = new JMenuItem("Delete");
            item.addActionListener(mOnDeleteClicked);
            mContextMenu.add(item);

            setComponentPopupMenu(mContextMenu);

            Box hbox = Box.createHorizontalBox();
            add(hbox);

            mLabel = new JLabel();
            mLabel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 3));
            mLabel.setFont(mLabel.getFont().deriveFont(Font.BOLD, 18));
            hbox.add(mLabel);

            Box vbox = Box.createVerticalBox();
            hbox.add(vbox);

            vbox.add(Box.createVerticalStrut(20));

            mEditButton = createIconButton(getClass().getResource("/icons/svg/settings60.svg"), 10, 10);
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
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));

        Dimension size = scrollPane.getHorizontalScrollBar().getPreferredSize();
        size.setSize(size.width, size.height/2);

        scrollPane.getHorizontalScrollBar().setPreferredSize(size);
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
            final RegionBox newBox = new RegionBox();
            newBox.setRegion(r);

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

                    //gotta call revalidate() because its this component is inside a JScrollPane
                    //see: http://docs.oracle.com/javase/tutorial/uiswing/components/scrollpane.html#update
                    revalidate();
                    repaint();
                }
            };
        }

        mNumBoxLayout.add(Box.createHorizontalStrut(20));
        mNumBoxLayout.add(mNumAddButton);
        mNumBoxLayout.add(Box.createHorizontalGlue());

        mDemBoxLayout.add(Box.createHorizontalGlue());
        for(Iterator<Region> it = irRatio.denominator.iterator();it.hasNext();) {
            Region r = it.next();
            RegionBox newBox = new RegionBox();
            newBox.setRegion(r);
            mDemBoxLayout.add(newBox);

            if(it.hasNext()){
                mDemBoxLayout.add(createPlusSymbol());
            }
        }
        mDemBoxLayout.add(Box.createHorizontalStrut(20));
        mDemBoxLayout.add(mDemAddButton);
        mDemBoxLayout.add(Box.createHorizontalGlue());

        revalidate();
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

        int height = mNumScroll.getPreferredSize().height;
        height += mNumScroll.getHorizontalScrollBar().getPreferredSize().height;
        height += mDenomScroll.getPreferredSize().height;
        height += insets.top + insets.bottom + lineHeight;
        height += mDenomScroll.getHorizontalScrollBar().getPreferredSize().height;

        return new Dimension(width, height);
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
