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
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.event.MouseInputAdapter;
import javax.swing.table.JTableHeader;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

/**
 * Allows a user to resize all rows by dragging the boarder between the table
 * header and the first row.
 *
 * @author Joos Kiener <Joos.Kiener@gmail.com>
 */
public class AllRowsResizer extends MouseInputAdapter {

    private static final XLogger logger = XLoggerFactory.getXLogger("AllRowsResizer");
    public static Cursor resizeCursor = Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR);
    private boolean isResizing = false;
    private int yOffset;
    private int topRow;
    private Cursor tableCursor = resizeCursor;
    private JTable table;

    public AllRowsResizer(JTable table) {
        this.table = table;
        table.addMouseListener(this);
        table.addMouseMotionListener(this);
        table.getTableHeader().addMouseListener(this);
        table.getTableHeader().addMouseMotionListener(this);
    }

    private boolean isResizingHeader(MouseEvent e) {

        Point p = e.getPoint();

        Object source = e.getSource();
        JTableHeader header = table.getTableHeader();

        if (source instanceof JTableHeader
                && header.contains(p)) {

            logger.debug("Mouse in Table header.");
            logger.debug("Headerheight: {}", header.getHeight());
            logger.debug("Header Y: {}", header.getY());
            logger.debug("Point Y: {}", p.y);
            int col = table.columnAtPoint(p);
            if (col == -1) {
                return false;
            }

            Boolean result = ((header.getY() + header.getHeight()) - 20) < p.y;
            logger.debug(result.toString());
            return result;

        } else if (source instanceof JTable) {

            int topRow = getTopRow();
            int row = table.rowAtPoint(p);

            if (row == topRow) {
                logger.debug("Mouse in top row.");
                int col = table.columnAtPoint(p);
                if (col == -1) {
                    return false;
                }
                Rectangle r = getCellRectangle(row, col);
                r.grow(0, -5);
                return r.y > p.y;

            }
        }
        return false;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        isResizing = isResizingHeader(e);
        yOffset = e.getYOnScreen();
        topRow = getTopRow();
        if (isResizing) {
            table.setAutoscrolls(false);
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (isResizing) {
            table.setAutoscrolls(true);
            if (table.getCursor() == resizeCursor) {
                swapCursor();
            }
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (isResizingHeader(e)
                != (table.getCursor() == resizeCursor)) {
            swapCursor();
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        int mouseY = e.getYOnScreen();
        if (isResizing) {
            int newHeight = table.getRowHeight() + (mouseY - yOffset);
            if (newHeight > 0) {
                yOffset = e.getYOnScreen();
                table.setRowHeight(newHeight);
                JViewport viewport = (JViewport) table.getParent();
                JScrollPane scrollPane = (JScrollPane) viewport.getParent();
                // This rectangle is relative to the table where the
                // northwest corner of cell (0,0) is always (0,0).
                Rectangle rect = table.getCellRect(topRow, 0, true);
                scrollPane.getVerticalScrollBar().setValue(rect.y);
            }
        }
    }

    private int getTopRow() {
        JViewport viewport = ((JViewport) table.getParent());
        Point pViewport = viewport.getViewPosition();
        return table.rowAtPoint(pViewport);
    }

    private void swapCursor() {
        Cursor tmp = table.getCursor();
        table.setCursor(tableCursor);
        tableCursor = tmp;
    }

    /**
     * Get the rectangle of the visible part of the given table cell. The top
     * row might only be partially visible.
     *
     * @param row
     * @param col
     * @return
     */
    private Rectangle getCellRectangle(int row, int col) {
        if (!(table.getParent() instanceof JViewport)) {
            return table.getCellRect(row, col, true);
        }

        JViewport viewport = (JViewport) table.getParent();
        // This rectangle is relative to the table where the
        // northwest corner of cell (0,0) is always (0,0)
        Rectangle rect = table.getCellRect(row, col, true);
        // The location of the viewport relative to the table
        Point pt = viewport.getViewPosition();
        // Translate the cell location so that it is relative
        // to the view, assuming the northwest corner of the
        // view is (0,0)
        //rect.setLocation(rect.x - pt.x, rect.y - pt.y);
        int difference = rect.y - pt.y;
        if (difference < 0) {
            //row only partially visiable
            rect.y = rect.y + difference; // move down
            rect.height = rect.height - difference; // make smaller
        }
        // Check if view completely contains the row
        return rect;
    }
}
