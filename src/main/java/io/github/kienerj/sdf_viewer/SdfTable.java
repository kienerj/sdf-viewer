/*
 * Copyright (C) 2014 Joos Kiener <Joos.Kiener@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.github.kienerj.sdf_viewer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import org.bitbucket.kienerj.sdfreader.SdfReader;

/**
 *
 * @author Joos Kiener <Joos.Kiener@gmail.com>
 */
public class SdfTable extends JTable {

    private final static int STRUCTURE_COLUMN_WIDTH = 200;
    private final DefaultTableModel headerModel;
    //private final TableRowSorter<TableModel> sorter;
    private final JTable headerTable;

    public SdfTable(JScrollPane scrollPane, SdfReader sdfReader, int rowHeight) {
        super();
        DefaultTableCellRenderer r = new SdfTableCellRenderer();
        setDefaultRenderer(Object.class, r);
        TableRowResizer rowResizer = new TableRowResizer(this);
        AllRowsResizer allRowsResizer = new AllRowsResizer(this);

        TableModel tableModel = new SdfTableModel(sdfReader);
        setModel(tableModel);
        super.setRowHeight(rowHeight);
        getColumnModel().getColumn(0).setPreferredWidth(STRUCTURE_COLUMN_WIDTH);

        //sorter = new TableRowSorter<>(getModel());
        //setRowSorter(sorter);
        headerModel = new RowHeaderModel(getRowCount());
        headerTable = new JTable(headerModel);
        headerTable.setRowHeight(getRowHeight());
        headerTable.setShowGrid(false);
        headerTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        headerTable.setPreferredScrollableViewportSize(new Dimension(60, 0));
        headerTable.getColumnModel().getColumn(0).setPreferredWidth(60);
        headerTable.getColumnModel().getColumn(0).setCellRenderer(new RowHeaderCellRenderer());
        // synchronize selection by using the same selection model in both tables
        headerTable.setSelectionModel(this.getSelectionModel());
        scrollPane.setRowHeaderView(headerTable);
        setPreferredScrollableViewportSize(getPreferredSize());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setRowHeight(int rowHeight) {
        super.setRowHeight(rowHeight);
        if (headerTable != null) {
            headerTable.setRowHeight(rowHeight);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setRowHeight(int row, int rowHeight) {
        super.setRowHeight(row, rowHeight);
        if (headerTable != null) {
            headerTable.setRowHeight(row, rowHeight);
        }
    }

    private class SdfTableCellRenderer extends DefaultTableCellRenderer {

        @Override
        public void setValue(Object value) {
            if (value instanceof ImageIcon) {
                setIcon((ImageIcon) value);
                setText("");
            } else {
                setIcon(null);
                super.setValue(value);
            }
        }
    }

    private class RowHeaderCellRenderer implements TableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable t, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {

            boolean selected = t.getSelectionModel().isSelectedIndex(row);
            Component component = t.getTableHeader().getDefaultRenderer()
                    .getTableCellRendererComponent(t, value, false, false, -1, -2);
            ((JLabel) component).setHorizontalAlignment(SwingConstants.CENTER);
            if (selected) {
                component.setFont(component.getFont().deriveFont(Font.BOLD));
                component.setForeground(Color.red);
            } else {
                component.setFont(component.getFont().deriveFont(Font.PLAIN));
            }
            return component;
        }
    }
}
