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

import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import javax.swing.JTable;
import javax.swing.event.MouseInputAdapter;
import javax.swing.table.JTableHeader;

/**
 * Allows a user to resize all rows by dragging the boarder between the table
 * header and the first row.
 *
 * @author Joos Kiener <Joos.Kiener@gmail.com>
 */
public class AllRowsResizer extends MouseInputAdapter {

    public static Cursor resizeCursor = Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR);
    private boolean isResizing = false;
    private Cursor otherCursor = resizeCursor;
    private JTable table;

    public AllRowsResizer(JTable table) {
        this.table = table;
        table.addMouseListener(this);
        table.addMouseMotionListener(this);
    }

    private boolean isResizingHeader(Point p) {

        JTableHeader header = table.getTableHeader();
        if (header.contains(p)) {

            int col = header.columnAtPoint(p);
            if (col == -1) {
                return false;
            }
            Rectangle r = header.getBounds();
            r.grow(0, -3);
            return !r.contains(p);

//        } else if (table.rowAtPoint(p) == 0) {
//
//            int col = table.columnAtPoint(p);
//            if (col == -1) {
//                return false;
//            }
//            Rectangle r = table.getCellRect(0, col, true);
//            r.grow(0, -3);
//            return !r.contains(p);
        } else {
            return false;
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        Point p = e.getPoint();
        isResizing = isResizingHeader(p);
    }

    private void swapCursor() {
        Cursor tmp = table.getCursor();
        table.setCursor(otherCursor);
        otherCursor = tmp;
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (isResizingHeader(e.getPoint())
                != (table.getCursor() == resizeCursor)) {
            swapCursor();
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        int mouseY = e.getY();

        if (isResizing) {
            int newHeight = mouseY + table.getRowHeight();
            if (newHeight > 0) {
                table.setRowHeight(newHeight);
            }
        }
    }
}
