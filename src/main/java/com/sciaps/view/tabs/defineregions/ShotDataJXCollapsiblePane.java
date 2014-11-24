package com.sciaps.view.tabs.defineregions;

import com.sciaps.common.spectrum.LIBZPixelSpectrum;
import com.sciaps.global.LibzUnitManager;
import java.awt.Font;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.jdesktop.swingx.JXCollapsiblePane;

/**
 *
 * @author sgowen
 */
public final class ShotDataJXCollapsiblePane extends JXCollapsiblePane
{
    public interface ShotDataJXCollapsiblePaneCallback
    {
        void shotDataSelected(int shotDataIndex);
    }

    private final JList _list;
    private final DefaultListModel _listModel;
    private final ShotDataJXCollapsiblePaneCallback _callback;

    public ShotDataJXCollapsiblePane(Direction direction, ShotDataJXCollapsiblePaneCallback callback)
    {
        super(direction);

        _callback = callback;

        getContentPane().setLayout(new javax.swing.BoxLayout(getContentPane(), javax.swing.BoxLayout.Y_AXIS));

        _listModel = new DefaultListModel();
        _list = new JList(_listModel);
        _list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        _list.setSelectedIndex(0);
        _list.addListSelectionListener(new ListSelectionListener()
        {
            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting() && _list.getModel().getSize() > 0)
                {
                    _callback.shotDataSelected(_list.getSelectedIndex());
                }
            }
        });

        refresh();

        JLabel title = new JLabel(" Shot Data ");
        title.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        title.setFont(new Font("Serif", Font.BOLD, 24));

        add(title);

        JScrollPane listScrollPane = new JScrollPane(_list);

        add(listScrollPane);
    }

    public void refresh()
    {
        _listModel.clear();

        List<LIBZPixelSpectrum> libzPixelSpectra = LibzUnitManager.getInstance().getLIBZPixelSpectra();
        for (int i = 0; i < libzPixelSpectra.size(); i++)
        {
            final int libzPixelSpectrumIndex = i;
            _listModel.addElement("Shot Data " + libzPixelSpectrumIndex);
        }

        _list.setModel(_listModel);
        _list.invalidate();
    }
}