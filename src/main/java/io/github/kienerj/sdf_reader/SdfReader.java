/*
 *  Copyright (C) 2013 Joos Kiener <Joos.Kiener@gmail.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.

 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.github.kienerj.sdf_reader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.util.zip.GZIPInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Getter;
import io.github.kienerj.io.OptimizedRandomAccessFile;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

/**
 * <p> A random access SD-File reader. The SD-file can be read sequentially
 * forward and backward or by randomly accessing records. Records are accessed
 * by 0-based index were the first record in the file has index 0 and so forth.
 * </p>
 *
 * <p> The reader returns instances of
 * <code>SdfRecord</code> which contains the name of the record (molfileName),
 * the molfile and all the sdfProperties of the record. </p>
 *
 * <p> An SD-File is indexed when loaded and the index is stored in a separated
 * file with .index appended to the end. Once the same file is read again this
 * index file will be used. Indexing can take some time on large sd-files.<br>
 * If a sd-file was changed the index file must either be deleted or you must
 * force reindexing of the file else the behavior is unpredictable. </p>
 *
 * <p> SD-Files contain chemical structure data and sdfProperties associated
 * with that structure. This file format is very widely spread in chemistry.
 * </p>
 *
 * @author Joos Kiener <Joos.Kiener@gmail.com>
 */
public class SdfReader implements AutoCloseable {

    private static final XLogger logger = XLoggerFactory.getXLogger("SdfReader");
    private static final String DELIMITER = "$$$$";
    private static final boolean SAVE_INDEX = false;
    private static final boolean REINDEX = false;
    protected final Map<Integer, Long> sdfIndex;
    private final OptimizedRandomAccessFile raf;
    @Getter
    private final File file;
    @Getter
    private boolean saveIndex;
    protected int previousRecordIndex;
    protected int lastIndex;
    protected final List<String> properties;

    /**
     * <p> Create a SdfReader for file
     * <code>filePath</code>.</p>
     *
     * @param filePath the path to the sd-file
     * @throws IOException if and issue with IO operations happens
     */
    public SdfReader(String filePath)
            throws IOException {
        this(new File(filePath), SAVE_INDEX, false);
    }

    /**
     * <p> Create a SdfReader for file
     * <code>filePath</code>.</p>
     *
     * @param filePath the path to the sd-file
     * @param saveIndex saves index to file for faster loading on next usage
     * @throws IOException if and issue with IO operations happens
     */
    public SdfReader(String filePath, boolean saveIndex)
            throws IOException {
        this(new File(filePath), saveIndex, REINDEX);
    }

    /**
     * <p> Create a SdfReader for file
     * <code>filePath</code>.</p>
     *
     * @param filePath the path to the sd-file
     * @param saveIndex saves index to file for faster loading on next usage
     * @param reindex force recreation of index
     * @throws IOException if and issue with IO operations happens
     */
    public SdfReader(String filePath, boolean saveIndex, boolean reindex)
            throws IOException {
        this(new File(filePath), saveIndex, reindex);
    }

    /**
     * <p> Create a SdfReader for file
     * <code>file</code>.</p>
     *
     * @param file the sd-file to read from
     * @throws IOException if and issue with IO operations happens
     */
    public SdfReader(File file)
            throws IOException {
        this(file, SAVE_INDEX, REINDEX);
    }

    /**
     * <p> Create a SdfReader for file
     * <code>file</code>.</p>
     *
     * @param file the sd-file to read from
     * @param saveIndex saves index to file for faster loading on next usage
     * @throws IOException if and issue with IO operations happens
     */
    public SdfReader(File file, boolean saveIndex)
            throws IOException {
        this(file, saveIndex, REINDEX);
    }

    /**
     * <p> Create a SdfReader for file
     * <code>file</code>.</p>
     *
     * @param file the sd-file to read from
     * @param saveIndex saves index to file for faster loading on next usage
     * @param reindex force recreation of index
     * @throws IOException if and issue with IO operations happens
     */
    public SdfReader(File file, boolean saveIndex, boolean reindex)
            throws IOException {

        String fileExtension = getFileExtension(file);
        if (fileExtension.equals("gz")) {
            logger.info("Found a gzip compressed file. Decompressing to temp and using that file...");
            file = decompressFile(file);
        }

        this.file = file;
        this.saveIndex = saveIndex;
        this.previousRecordIndex = -1;
        this.sdfIndex = new HashMap<>();
        this.properties = new ArrayList<>();
        raf = new OptimizedRandomAccessFile(file, "r");
        if (raf.length() == 0) {
            throw new IllegalArgumentException("File is empty.");
        }
        validateFile();
        index(reindex);
        scanForProperties();
    }

    /**
     * <p>Gets the list of the index detected in the sd-file.</p>
     *
     * @return
     */
    public List<String> getProperties() {
        return new ArrayList<>(properties);
    }

    /**
     * <p>True if sd-file contains another record after the current one</p>
     *
     * @return whether a next record is available
     * @throws SdfReadingException in case of an IO issue
     */
    public boolean hasNext() {
        logger.entry();
        boolean result = hasNext(1);
        logger.exit(result);
        return result;
    }

    /**
     * <p>True if sd-file contains another
     * <code>numRecords</code> records after the current one</p>
     *
     *
     * @return whether <code>numRecords</code> next records are available
     * @throws SdfReadingException in case of an IO issue
     */
    public boolean hasNext(int numRecords) {
        logger.entry(numRecords);
        if (previousRecordIndex + numRecords > lastIndex) {
            logger.exit(false);
            return false;
        } else {
            logger.exit(true);
            return true;
        }
    }

    /**
     * <p>True if sd-file contains another record before the current one</p> *
     *
     * @return whether a previous record is available
     * @throws SdfReadingException in case of an IO issue
     */
    public boolean hasPrevious() {
        logger.entry();
        boolean result = hasPrevious(1);
        logger.exit(result);
        return result;
    }

    /**
     * <p>True if sd-file contains
     * <code>numRecords</code> records before the current one</p>
     *
     * @return whether <code>numRecords</code> previous records are available
     * @throws SdfReadingException in case of an IO issue
     */
    public boolean hasPrevious(int numRecords) {
        logger.entry(numRecords);
        if (previousRecordIndex - numRecords < -1) {
            logger.exit(false);
            return false;
        }
        logger.exit(true);
        return true;
    }

    /**
     * <p>Whether the a record with
     * <code>index</code> exists in the file or not.</p>
     *
     * @param index
     * @return
     */
    public boolean hasRecord(int index) {
        logger.entry(index);
        boolean result = sdfIndex.containsKey(index);
        logger.exit(result);
        return result;
    }

    /**
     * <p>Gets the next entry in the SD-File</p>
     *
     * @return the next entry in the SD-File
     * @throws SdfReadingException in case of an IO issue
     * @throws NoSuchElementException if EOF is reached
     */
    public SdfRecord next() {
        logger.entry();
        if (!hasNext()) {
            throw new NoSuchElementException("Reached end of File.");
        }
        SdfRecord result = getRecord(previousRecordIndex + 1);
        logger.exit(result);
        return result;
    }

    /**
     * <p>Gets the previous entry in the SD-File</p>
     *
     * @return the previous entry in the SD-File
     * @throws SdfReadingException in case of an IO issue
     * @throws NoSuchElementException if BOF is reached
     */
    public SdfRecord previous() {
        logger.entry();
        if (!hasPrevious()) {
            throw new NoSuchElementException("Reached begin of File.");
        }
        SdfRecord result = getRecord(previousRecordIndex);
        logger.exit(result);
        return result;
    }

    /**
     * <p>Returns the next
     * <code>numRecords</code> records sorted ASC by their index.</p>
     *
     * <p> The method returns as many records until end of file is reached. For
     * example if current position is after index 6, the file contains 10
     * records and
     * <code>numRecords</code> is 5, the list will contain records [7,8,9] and
     * no Exception will be raised. The file pointer will then be at the end of
     * file and a subsequent call will raise a
     * <code>NoSuchElementException</code>.</p>
     *
     * @param numRecords number of records to read
     * @return a list containing the next <code>numRecords</code> records
     * @throws SdfReadingException in case of an IO issue
     * @throws NoSuchElementException if pointer is currently at EOF
     */
    public List<SdfRecord> next(int numRecords) {
        logger.entry(numRecords);
        List<SdfRecord> result = getRecords(previousRecordIndex + 1,
                previousRecordIndex + numRecords);
        logger.exit(result);
        return result;
    }

    /**
     * <p>Returns the previous
     * <code>numRecords</code> records sorted DESC by their index.</p>
     *
     * <p> If the last record read had index 4 (the fifth record),
     * <code>list = previous(5);</code> will return records 4 to 0 and
     * <code>list.get(0)</code> returns record with index 4. </p>
     *
     * <p> The method returns as many records until begin of file is reached.
     * For example if current position is after index 3 and
     * <code>numRecords</code> is 5, the list will contain records [3,2,1,0] and
     * no Exception will be raised. The file pointer will then be at begin of
     * file and a subsequent call will raise a
     * <code>NoSuchElementException</code>.</p>
     *
     * @param numRecords
     * @return a list containing the previous <code>numRecords</code> records
     * @throws SdfReadingException in case of an IO issue
     * @throws NoSuchElementException if pointer is currently at BOF
     */
    public List<SdfRecord> previous(int numRecords) {
        logger.entry(numRecords);
        List<SdfRecord> result = getRecords(
                previousRecordIndex, previousRecordIndex - (numRecords - 1));
        logger.exit(result);
        return result;
    }

    /**
     * <p> Gets the first record of the sd-file and moves pointer to begin of
     * file.</p> <p>Calling
     * <code>next()</code> after first will return the same record (the first
     * one).</p>
     *
     * @return the first record in the file
     * @throws SdfReadingException in case of an IO issue
     */
    public SdfRecord first() {
        logger.entry();
        SdfRecord result = getRecord(0);
        previousRecordIndex = -1;
        logger.exit(result);
        return result;
    }

    /**
     * <p> Gets the last record of the sd-file and moves pointer to end of file.
     * </p>
     *
     * <p>
     * <code>hasNext()</code> will be false after this method was called. </p>
     *
     * @return the last record in the file
     * @throws SdfReadingException in case of an IO issue
     */
    public SdfRecord last() {
        logger.entry();
        SdfRecord result = getRecord(lastIndex);
        logger.exit(result);
        return result;
    }

    /**
     * <p>Close this reader to free all resources.</p>
     *
     * @throws IOException
     */
    @Override
    public void close() throws IOException {
        logger.entry();
        raf.close();
        logger.exit();
    }

    /**
     * <p>Gets records starting from the given
     * <code>startIndex</code> to
     * <code>endIndex</code>. </p>
     *
     * <p>If endIndex is smaller than startIndex, the records will be returned
     * in reversed order.</p>
     *
     * <p> The method returns as many records until begin or end of file is
     * reached. For example if current position is after index 6, the file
     * contains 10 records and
     * <code>endIndex</code> is 11, the list will contain records [7,8,9] and no
     * Exception will be raised.</p> <p> If
     * <code>startIndex</code>is bigger than the last index or smaller than 0, a
     * <code>NoSuchElementException</code> will be thrown. </p>
     *
     * @param startIndex
     * @param endIndex
     * @return a list containing all of the requested records
     * @throws SdfReadingException in case of an IO issue
     * @throws NoSuchElementException if startIndex is at start or end of file
     */
    public List<SdfRecord> getRecords(int startIndex, int endIndex) {
        logger.entry(startIndex, endIndex);
        if (startIndex < 0 || startIndex > lastIndex) {
            throw new NoSuchElementException("Start index is out of range: " + startIndex);
        }
        int sign = startIndex < endIndex ? 1 : - 1;
        int numRecords = Math.abs(endIndex - startIndex) + 1;
        ArrayList<SdfRecord> records = new ArrayList<>(numRecords);
        try {
            for (int i = 0; i < numRecords; i++) {
                records.add(getRecord(startIndex + (i * sign)));
            }
            logger.exit(records);
            return records;
        } catch (NoSuchElementException ex) {
            logger.catching(ex);
            return records;
        }
    }

    /**
     * <p>Gets the record with the specified index.</p>
     *
     * @param index the 0-based index of the record
     * @return the record at the specified index
     * @throws SdfReadingException in case of an IO issue
     */
    public SdfRecord getRecord(int index) {
        logger.entry(index);
        if (!sdfIndex.containsKey(index)) {
            String message;
            if (index < 0) {
                previousRecordIndex = -1;
                message = "Reached begin of File.";
            } else if (index > lastIndex) {
                previousRecordIndex = lastIndex;
                message = "Reached end of File.";
            } else {
                // should never be called
                message = "Element with index " + index + " is missing. Index is might be corrupt.";
            }
            throw new NoSuchElementException(message);
        }
        previousRecordIndex = index;
        SdfRecord result = readRecord(index);
        logger.exit(result);
        return result;
    }

    /**
     * <p>Returns the number of records in the sd-file.</p>
     *
     * @return number of records in the file.
     */
    public int size() {
        return sdfIndex.size();
    }

    public void setSaveIndex(boolean saveIndex) {
        if (this.saveIndex != saveIndex) {
            // flag has changed
            this.saveIndex = saveIndex;
            try {
                if (saveIndex) {
                    saveIndex();
                } else {
                    File index = getIndexFile();
                    index.delete();
                }
            } catch (IOException ex) {
                logger.catching(ex);
            }
        }
    }

    /**
     * Gets the record at the specified index.
     *
     * @param index
     * @return the requested record
     * @throws SdfReadingException in case of an IO issue
     */
    private SdfRecord readRecord(int index) {
        logger.entry(index);

        long startOffset = sdfIndex.get(index);
        long endOffset;
        if (index == lastIndex) {
            try {
                endOffset = raf.length();
            } catch (IOException ex) {
                throw new SdfReadingException(ex);
            }
        } else {
            endOffset = sdfIndex.get(index + 1);
        }

        // single sdf-record will never exceed int max value
        int nrOfBytes = (int) (endOffset - startOffset);
        byte[] buffer = new byte[nrOfBytes];

        logger.trace("Start reading sdf record.");
        try {
            raf.seek(startOffset);
            raf.read(buffer);
            String record = new String(buffer, "UTF-8");
            String[] lines = record.split(getLineSeparator(record));

            ArrayList<String> recordLines = new ArrayList<>(Arrays.asList(lines));

            boolean isFirstLine = true;
            StringBuilder molecule = new StringBuilder();
            SdfRecord sdfRecord = new SdfRecord(index);

            ListIterator<String> lineIterator = recordLines.listIterator();
            while (lineIterator.hasNext()) {
                String line = lineIterator.next();

                if (isFirstLine) {

                    if (line.startsWith(">")) {
                        readSdfProperty(sdfRecord, lineIterator);
                    } else {
                        sdfRecord.setMolfileName(line);
                        molecule.append(line);
                        molecule.append(System.getProperty("line.separator"));
                    }
                    isFirstLine = false;
                } else if (line.startsWith(">")) {
                    readSdfProperty(sdfRecord, lineIterator);
                } else {
                    molecule.append(line);
                    molecule.append(System.getProperty("line.separator"));
                }
            }
            sdfRecord.setMolfile(molecule.toString());
            logger.exit(sdfRecord);
            return sdfRecord;
        } catch (IOException ioEx) {
            throw new SdfReadingException(ioEx);
        }
    }

    /**
     * Reads the sdfProperties of the current record being read.
     *
     * @param sdfRecord
     * @param lineIterator
     */
    private void readSdfProperty(SdfRecord sdfRecord,
            ListIterator<String> lineIterator) {

        logger.trace("Parsing property...");
        String line = lineIterator.previous();
        lineIterator.next();
        // used to read property names in SD-File which are between <>
        Pattern regex = Pattern.compile("(?<=\\<)(.*?)(?=\\>)");

        Matcher matcher = regex.matcher(line);
        matcher.find();
        String propertyName = matcher.group();
        // value of property is on next line
        String value = "";
        while (lineIterator.hasNext()) {
            line = lineIterator.next();
            if (line.equals("")) {
                break;
            } else if (line.startsWith(">")) {
                lineIterator.previous();
                break;
            } else {
                // String concat in loop is no issue here. In most cases
                // there will only be 1 line.
                value = value + System.getProperty("line.separator") + line;
            }
        }
        // remove possible leading line.separator
        value = value.trim();
        sdfRecord.addProperty(propertyName, value);
        logger.trace("Adding property {} with value {}.", propertyName, value);
    }

    /**
     * Loads existing index from file
     *
     * @throws IOException
     */
    private void loadIndex() throws IOException {
        logger.debug("Loading Index from file...");
        sdfIndex.clear();
        Properties index = new Properties();
        try (FileInputStream in = new FileInputStream(file.getAbsolutePath() + ".index");) {
            index.load(in);

            for (Map.Entry<Object, Object> entry : index.entrySet()) {
                sdfIndex.put(Integer.parseInt((String) entry.getKey()), Long.parseLong((String) entry.getValue()));
            }
            lastIndex = sdfIndex.size() - 1;
        }
    }

    /**
     * Saves generated index to file
     *
     * @throws IOException
     */
    private void saveIndex() throws IOException {
        logger.debug("Saving Index to file...");
        Properties index = new Properties();

        for (Map.Entry<Integer, Long> entry : sdfIndex.entrySet()) {
            index.put(entry.getKey().toString(), entry.getValue().toString());
        }
        try (FileOutputStream out = new FileOutputStream(file.getAbsolutePath() + ".index");) {
            index.store(out, null);
        }
    }

    private void validateFile() throws IOException {

        if (!file.isFile()) {
            throw new IllegalArgumentException("File " + file.getAbsolutePath() + " is not a file.");
        }

        String extension = getFileExtension(file);

        if (!extension.equals("sdf") && !extension.equals("sd")) {
            logger.warn("File has unexpected file extension: {}.", extension);
        }

        logger.debug("Validating file contents...");
        raf.readLine();
        raf.readLine();
        raf.readLine();
        String line = raf.readLine();
        if (!line.contains("V2000") && !line.contains("V3000")) {
            String message = "File does not seem to be a valid V2000 or V3000 sd-file. No mol file version specifier found on line 4.";
            throw new IllegalArgumentException(message);
        }
    }

    private static String getFileExtension(File file) {
        String extension = "";
        String fileName = file.getName();
        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            extension = fileName.substring(i + 1);
        }
        return extension;
    }

    private File decompressFile(File file) throws IOException {
        logger.debug("Decompressing file {}...", file.getAbsolutePath());
        String tempDir = System.getProperty("java.io.tmpdir");
        String fileName = file.getName().substring(0, file.getName().lastIndexOf("."));
        File sdf =  new File(tempDir, fileName);
        GZIPInputStream gzStream = new GZIPInputStream(new FileInputStream(file));
        FileOutputStream out = new FileOutputStream(sdf);

        byte[] results = new byte[1024];
        int count = gzStream.read(results);
        while (count != -1) {
            byte[] result = Arrays.copyOf(results, count);
            out.write(result);
            count = gzStream.read(results);
        }
        out.flush();
        out.close();
        gzStream.close();
        return sdf;
    }

    /**
     * Must be called after indexing
     */
    private void scanForProperties() {
        List<SdfRecord> records = getRecords(0, 9);
        for (SdfRecord record : records) {
            Map<String, String> sdfProperties = record.getProperties();
            for (String property : sdfProperties.keySet()) {
                if (!properties.contains(property)) {
                    properties.add(property);
                }
            }
        }
        previousRecordIndex = -1; // set pointer to BOF
    }

    /**
     * Indexes the file for fast random access.
     *
     * @param forceIndexing
     * @throws IOException
     */
    private void index(boolean forceIndexing) throws IOException {

        File index = getIndexFile();
        if (!forceIndexing && index.exists()) {
            loadIndex();
            return;
        }
        logger.debug("Generating Index...");
        sdfIndex.put(0, 0L); // first record
        int recordIndex = 1;
        String line;

        while ((line = raf.readLine()) != null) {
            if (line.equals(DELIMITER)) {
                long recordOffset = raf.getFilePointer();
                sdfIndex.put(recordIndex, recordOffset);
                recordIndex++;
            }
        }
        if (sdfIndex.isEmpty()) {
            throw new IllegalArgumentException("File is not an sd-file.");
        }
        // sd-files terminate with DELIMITER
        // hence the last entry in index must be removed as no
        // record will be there.
        sdfIndex.remove(recordIndex - 1);
        lastIndex = sdfIndex.size() - 1;
        logger.debug("Index generated");
        if (saveIndex) {
            saveIndex();
        }
    }

    private File getIndexFile(){
        return new File(file.getAbsolutePath() + ".index");
    }

    /**
     * Determine line separator in based in string.
     *
     * out in the wild there are sd-files that contain both types of separators
     * in the same file.
     *
     * @param data
     * @return
     */
    private String getLineSeparator(String data) {
        if (data.indexOf("\r\n") >= 0) {
            return "\r\n";
        } else {
            return "\n";
        }
    }
}
