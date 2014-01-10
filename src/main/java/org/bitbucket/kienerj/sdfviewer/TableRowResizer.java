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
package org.bitbucket.kienerj.sdfviewer;

import java.awt.*;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.event.MouseInputAdapter;

/**
 * <p> Allows the user to resize individual rows in a JTable. </p>
 *
 * <p>This code was shamelessly taken from <a
 * href="http://stackoverflow.com/questions/4387995/adjusting-individual-row-height-using-cursor-on-jtable">stackoverflow</a>.
 * </p>
 *
 * @author Joos Kiener <Joos.Kiener@gmail.com>
 */
public class TableRowResizer extends MouseInputAdapter {

    public static Cursor resizeCursor = Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR);
    private int mouseYOffset, resizingRow;
    private Cursor otherCursor = resizeCursor;
    private final JTable table;

    public TableRowResizer(JTable table) {
        this.table = table;
        table.addMouseListener(this);
        table.addMouseMotionListener(this);
    }

    private int getResizingRow(Point p) {
        return getResizingRow(p, table.rowAtPoint(p));
    }

    private int getResizingRow(Point p, int row) {
        if (row == -1) {
            return -1;
        }
        int col = table.columnAtPoint(p);
        if (col == -1) {
            return -1;
        }
        Rectangle r = table.getCellRect(row, col, true);
        r.grow(0, -3);
        if (r.contains(p)) {
            return -1;
        }

        int midPoint = r.y + r.height / 2;
        int rowIndex = (p.y < midPoint) ? row - 1 : row;

        return rowIndex;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        Point p = e.getPoint();
        resizingRow = getResizingRow(p);
        mouseYOffset = p.y - table.getRowHeight(resizingRow);
        if (resizingRow >= 0) {
            table.setRowSelectionAllowed(false);
            table.setAutoscrolls(false);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mouseReleased(MouseEvent e) {
        if (resizingRow >= 0) {
            table.setRowSelectionAllowed(true);
            table.setAutoscrolls(true);
        }
    }

    private void swapCursor() {
        Cursor tmp = table.getCursor();
        table.setCursor(otherCursor);
        otherCursor = tmp;
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if ((getResizingRow(e.getPoint()) >= 0)
                != (table.getCursor() == resizeCursor)) {
            swapCursor();
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        table.clearSelection();
        int mouseY = e.getY();

        if (resizingRow >= 0) {
            int newHeight = mouseY - mouseYOffset;
            if (newHeight > 0) {
                table.setRowHeight(resizingRow, newHeight);
            }
        }
    }
}