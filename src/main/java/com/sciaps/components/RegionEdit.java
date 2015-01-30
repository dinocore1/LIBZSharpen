package com.sciaps.components;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sciaps.common.data.Region;
import com.sciaps.common.swing.utils.TableColumnAdjuster;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.lang.math.DoubleRange;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class RegionEdit extends JPanel {


    private class ParamTableModel extends AbstractTableModel {

        ArrayList<String> key = new ArrayList<String>();
        ArrayList<String> values = new ArrayList<String>();

        @Override
        public int getRowCount() {
            return key.size();
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return key.get(rowIndex);

                case 1:
                    return values.get(rowIndex);

                default:
                    return null;
            }
        }

        @Override
        public String getColumnName(int column) {
            switch (column) {
                case 0:
                    return "Key";

                case 1:
                    return "Value";

                default:
                    return null;
            }
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return String.class;
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            String value = (String) aValue;
            switch (columnIndex) {
                case 0:
                    key.set(rowIndex, value);
                    break;

                case 1:
                    values.set(rowIndex, value);
                    break;

            }
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return true;
        }
    }

    private Region mRegion;

    private final JTextField mNameField;
    private final SpinnerNumberModel mMinModel;
    private final JSpinner mMinSpinner;
    private final SpinnerNumberModel mMaxModel;
    private final JSpinner mMaxSpinner;
    private JTable mTable;
    private final JButton mAddButton;
    private final JButton mDeleteButton;
    private ParamTableModel mTableModel = new ParamTableModel();

    public RegionEdit() {

        setLayout(new MigLayout("fill", "[][]", ""));

        JLabel nameLabel = new JLabel("Name");
        add(nameLabel, "");
        mNameField = new JTextField();
        add(mNameField, "growx, wrap");

        JLabel regionLabel = new JLabel("Nm Range");
        add(regionLabel, "");

        mMinModel = new SpinnerNumberModel(new Double(0), new Double(0), new Double(1000), new Double(0.05));
        mMinSpinner = new JSpinner(mMinModel);
        add(mMinSpinner, "split");

        JLabel label = new JLabel("-");
        add(label, "split");

        mMaxModel = new SpinnerNumberModel(new Double(0), new Double(0), new Double(1000), new Double(0.05));
        mMaxSpinner = new JSpinner(mMaxModel);
        add(mMaxSpinner, "wrap");

        {
            JPanel panel = new JPanel(new MigLayout("fill"));
            panel.setBorder(BorderFactory.createTitledBorder("Params"));
            add(panel, "span, grow");

            mTable = new JTable(mTableModel);
            mTable.getColumnModel().getColumn(0).setMinWidth(100);
            mTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
            mTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            TableColumnAdjuster tca = new TableColumnAdjuster(mTable);
            tca.adjustColumns();

            JScrollPane mScrollPane = new JScrollPane(mTable);
            panel.add(mScrollPane, "span 2, grow, wrap");

            mDeleteButton = new JButton("-");
            mDeleteButton.addActionListener(mOnDeleteClicked);
            panel.add(mDeleteButton, "");


            mAddButton = new JButton("+");
            mAddButton.addActionListener(mOnAddClicked);
            panel.add(mAddButton, "");
        }


    }

    public void setRegion(Region region) {
        mRegion = region;

        mNameField.setText(mRegion.name);
        mMinModel.setValue(mRegion.wavelengthRange.getMinimumDouble());
        mMaxModel.setValue(mRegion.wavelengthRange.getMaximumDouble());

        mTableModel.key.clear();
        mTableModel.values.clear();
        for(Map.Entry<String, String> entry : region.params.entrySet()) {
            mTableModel.key.add(entry.getKey());
            mTableModel.values.add(String.valueOf(entry.getValue()));
        }

        mTableModel.fireTableDataChanged();

    }

    public void save() {

        mRegion.name = mNameField.getText();
        mRegion.wavelengthRange = new DoubleRange(mMinModel.getNumber(), mMaxModel.getNumber());

        Map<String, String> newParams = new HashMap<String, String>();
        for(int i=0;i<mTableModel.key.size();i++){
            String key = mTableModel.key.get(i);
            String value = mTableModel.values.get(i);
            newParams.put(key, value);
        }

        mRegion.setParams(newParams);
    }

    private ActionListener mOnAddClicked = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            mTableModel.key.add("Key");
            mTableModel.values.add("");
            mTableModel.fireTableDataChanged();
        }
    };

    private ActionListener mOnDeleteClicked = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            int row = mTable.getSelectedRow();
            row = mTable.convertRowIndexToModel(row);
            mTableModel.key.remove(row);
            mTableModel.values.remove(row);
            mTableModel.fireTableDataChanged();
        }
    };
}
