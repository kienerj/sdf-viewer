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

import com.epam.indigo.Indigo;
import com.epam.indigo.IndigoRenderer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import javax.swing.ImageIcon;
import javax.swing.table.AbstractTableModel;
import io.github.kienerj.sdf_reader.SdfReader;
import io.github.kienerj.sdf_reader.SdfRecord;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

/**
 *
 * @author Joos Kiener <Joos.Kiener@gmail.com>
 */
public class SdfTableModel extends AbstractTableModel {

    private static final XLogger logger = XLoggerFactory.getXLogger("SdfTableModel");
    private static final int CACHE_SIZE = 100;
    private static final int LOAD_THRESHOLD = 25;
    private final SdfReader sdfReader;
    private final List<String> columns;
    private final Indigo indigo;
    private final IndigoRenderer renderer;
    private final HashMap<Integer, SdfRecord> rowCache;
    private int cacheLowerBound = 0;
    private final int imageWidth = 300;
    private final int imageHeight = 150;

    public SdfTableModel(SdfReader sdfReader) {
        this.sdfReader = sdfReader;
        List<String> columns = sdfReader.getProperties();
        if (columns.isEmpty()) {
            columns.add("Structure");
        } else {
            columns.addFirst("Structure");
        }
        this.columns = columns;
        this.indigo = new Indigo();
        this.renderer = new IndigoRenderer(indigo);
        indigo.setOption("render-output-format", "png");
        indigo.setOption("render-label-mode", "hetero");
        indigo.setOption("render-coloring", true);
        indigo.setOption("render-margins", 5, 5);
        indigo.setOption("render-stereo-style", "none");
        indigo.setOption("render-bond-length",75);
        indigo.setOption("render-relative-thickness", 1.5);
        indigo.setOption("render-image-size", imageWidth, imageHeight);
        indigo.setOption("image-resolution", 96);
        // init row cache
        this.rowCache = new HashMap<>();
        updateRowCache(0, true);

    }

    @Override
    public int getRowCount() {
        logger.entry();
        int result = sdfReader.size();
        logger.exit(result);
        return result;
    }

    @Override
    public int getColumnCount() {
        logger.entry();
        int result = columns.size();
        logger.exit(result);
        return result;
    }

    @Override
    public String getColumnName(int columnIndex) {
        logger.entry(columnIndex);
        String result = columns.get(columnIndex);
        logger.exit(result);
        return result;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        logger.entry(columnIndex);
        if (columnIndex == 0) {
            logger.exit(ImageIcon.class);
            return ImageIcon.class;
        } else {
            logger.exit(String.class);
            return String.class;
        }

    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        logger.entry(rowIndex, columnIndex);
        logger.exit(false);
        return false;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {

        logger.entry(rowIndex, columnIndex);
        logger.trace("Getting record with index {}.", rowIndex);
        if (rowIndex < cacheLowerBound
                || rowIndex > cacheLowerBound + CACHE_SIZE) {
            // jump to different position in table; reload row cache
            // and set nw row in middle of cache
            int newLowerBound = rowIndex - CACHE_SIZE / 2;
            logger.debug("Fully reloading cache starting at {}.", newLowerBound);
            updateRowCache(newLowerBound, true);
        } else if (cacheLowerBound + (CACHE_SIZE - LOAD_THRESHOLD - 1) < rowIndex) {
            int newLowerBound = cacheLowerBound + LOAD_THRESHOLD;
            logger.debug("Updating cache increasing lower bound to {}.", newLowerBound);
            updateRowCache(newLowerBound, false);
        } else if (cacheLowerBound + LOAD_THRESHOLD > rowIndex
                && cacheLowerBound > 0) {
            int newLowerBound = cacheLowerBound - LOAD_THRESHOLD;
            logger.debug("Updating cache decreasing lower bound to {}.", newLowerBound);
            updateRowCache(newLowerBound, false);
        }

        SdfRecord record = rowCache.get(rowIndex);
        logger.trace("Fetched record with index {}.", record.getIndex());

        if (columnIndex == 0) {
            String molfile = record.getMolfile();
            ImageIcon chemicalStructure = new ChemicalStructureIcon(molfile, indigo, renderer, imageWidth, imageHeight);
            logger.exit(chemicalStructure);
            return chemicalStructure;

        } else {

            String columnName = getColumnName(columnIndex);
            String value = record.getProperty(columnName);
            logger.exit(value);
            return value;
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        //do nothing
    }

    private void updateRowCache(int cacheLowerBound, boolean reloadFully) {

        logger.entry(cacheLowerBound, reloadFully);
        if (cacheLowerBound < 0) {
            cacheLowerBound = 0;
        }

        int firstRow;
        int lastRow;
        if (reloadFully) {
            logger.debug("Reloading cache fully: clearing cache and adding new records.");
            rowCache.clear();
            firstRow = cacheLowerBound;
            lastRow = cacheLowerBound + CACHE_SIZE;
            addRowsToCache(firstRow, lastRow);
            this.cacheLowerBound = cacheLowerBound;
        } else if (this.cacheLowerBound - cacheLowerBound < 0) {
            // scroll down (to bigger index)
            removeFromCache(this.cacheLowerBound, LOAD_THRESHOLD);
            firstRow = cacheLowerBound + CACHE_SIZE - LOAD_THRESHOLD;
            lastRow = cacheLowerBound + CACHE_SIZE - 1;
            addRowsToCache(firstRow, lastRow);
            this.cacheLowerBound = cacheLowerBound;
        } else if (this.cacheLowerBound - cacheLowerBound > 0) {
            //scroll up (to smaller index)
            removeFromCache(this.cacheLowerBound + CACHE_SIZE - LOAD_THRESHOLD, LOAD_THRESHOLD);
            firstRow = cacheLowerBound;
            lastRow = cacheLowerBound + LOAD_THRESHOLD - 1;
            addRowsToCache(firstRow, lastRow);
            this.cacheLowerBound = cacheLowerBound;
        }
//        else if (this.cacheLowerBound == cacheLowerBound) {
//              // do nothing as cache is already correct
//        }
        logger.exit();
    }

    private void addRowsToCache(int firstRow, int lastRow) {
        logger.entry(firstRow, lastRow);
        try {
            List<SdfRecord> records = sdfReader.getRecords(firstRow, lastRow);
            for (SdfRecord record : records) {
                rowCache.put(record.getIndex(), record);
            }
            logger.exit();
        } catch (NoSuchElementException ex) {
            //cacheLowerBound is bigger than sdfReader.size(). Load cache with
            // last CACHE_SIZE records
            rowCache.clear();
            this.cacheLowerBound = sdfReader.size() - CACHE_SIZE;
            List<SdfRecord> records = sdfReader.getRecords(this.cacheLowerBound, sdfReader.size() - 1);
            for (SdfRecord record : records) {
                rowCache.put(record.getIndex(), record);
            }
            logger.exit();
        }
    }

    private void removeFromCache(int startIndex, int numRecords) {

        logger.entry(startIndex, numRecords);
        int endIndex = startIndex + numRecords;
        HashSet<Integer> keys = new HashSet<>();
        for (int i = startIndex; i < endIndex; i++) {
            keys.add(i);
        }
        rowCache.keySet().removeAll(keys);
        logger.exit();
    }
}
