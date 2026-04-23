/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bitbucket.kienerj.sdfreader;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Joos Kiener <Joos.Kiener@gmail.com>
 */
public class SdfReaderTest {

    private static final String filePath = "/unitTest.sdf";
    private final File sdf;
    private SdfReader sdfReader;

    public SdfReaderTest() {
        URL url = getClass().getResource(filePath);
        sdf = new File(url.getFile());
    }

    @Before
    public void setUp() throws IOException {
        sdfReader = new SdfReader(sdf);
    }

    @After
    public void tearDown() throws IOException {
        sdfReader.close();
        sdfReader = null;
    }

    /**
     * Test of hasNext method, of class SdfReader.
     */
    @Test
    public void testHasNext() {
        System.out.println("hasNext");
        boolean expResult = true;
        boolean result = sdfReader.hasNext();
        assertEquals(expResult, result);
    }

    /**
     * Test of hasPrevious method, of class SdfReader.
     */
    @Test
    public void testHasPrevious() {
        System.out.println("hasPrevious");
        boolean expResult = false;
        boolean result = sdfReader.hasPrevious();
        assertEquals(expResult, result);
    }

    /**
     * Test of next method, of class SdfReader.
     */
    @Test
    public void testNext() {
        System.out.println("next");
        SdfRecord expResult = getBenzene();
        SdfRecord result = sdfReader.next();
        assertEquals(expResult, result);
    }

    /**
     * Test of next method, of class SdfReader.
     */
    @Test(expected=NoSuchElementException.class)
    public void testNextEOF() {
        System.out.println("next on EOF");
        sdfReader.last();
        sdfReader.next();
        fail("NoSuchElementException should have been thrown");
    }

    /**
     * Test of previous method, of class SdfReader.
     */
    @Test
    public void testPrevious() {
        System.out.println("previous");
        SdfRecord expResult = getBenzene();
        sdfReader.getRecord(0);
        SdfRecord result = sdfReader.previous();
        assertEquals(expResult, result);
    }

    /**
     * Test of previous method, of class SdfReader.
     */
    @Test(expected=NoSuchElementException.class)
    public void testPreviousBOF() {
        System.out.println("previous on BOF");
        sdfReader.previous();
        fail("NoSuchElementException should have been thrown");
    }

    /**
     * Test of next method, of class SdfReader.
     */
    @Test
    public void testNextTwo() {
        System.out.println("next two");
        int nrOfRecords = 2;
        List<SdfRecord> list = sdfReader.next(nrOfRecords);
        assertTrue(list.size() == nrOfRecords);
        SdfRecord result = list.get(0);
        assertEquals(getBenzene(), result);
        assertEquals("Mixture_1", list.get(1).getMolfileName());
    }

    /**
     * Test of previous method, of class SdfReader.
     */
    @Test
    public void testPreviousTwo() {
        System.out.println("previous two");
        int nrOfRecords = 2;
        sdfReader.getRecord(1);
        List<SdfRecord> list = sdfReader.previous(nrOfRecords);
        assertTrue(list.size() == nrOfRecords);
        SdfRecord result = list.get(1);
        assertEquals(getBenzene(), result);
        assertEquals("Mixture_1", list.get(0).getMolfileName());
    }

    /**
     * Test of next method, of class SdfReader.
     */
    @Test
    public void testFirst() {
        System.out.println("first");
        SdfRecord expResult = getBenzene();
        SdfRecord result = sdfReader.first();
        assertEquals(expResult, result);
        result = sdfReader.next();
        assertEquals(expResult, result);
    }

    /**
     * Test of next method, of class SdfReader.
     */
    @Test
    public void testLast() {
        System.out.println("last");
        SdfRecord expResult = getLast();
        SdfRecord result = sdfReader.last();
        assertEquals(expResult, result);
        result = sdfReader.previous();
        assertEquals(expResult, result);
    }

    /**
     * Test of close method, of class SdfReader.
     */
    @Test(expected=SdfReadingException.class)
    public void testClose() throws Exception {
        System.out.println("close");
        sdfReader.close();
        sdfReader.next();
        fail("SdfReadingException should have been thrown.");
    }

    /**
     * Test of getRecords method, of class SdfReader.
     */
    @Test
    public void testGetRecords() {
        System.out.println("getRecords");
        int startIndex = 0;
        int endIndex = 1;
        List<SdfRecord> list = sdfReader.getRecords(startIndex, endIndex);
        assertTrue(list.size() == 2);
        SdfRecord result = list.get(0);
        assertEquals(getBenzene(), result);
        assertEquals("Mixture_1", list.get(1).getMolfileName());
    }

    private SdfRecord getBenzene() {

        SdfRecord sdfRecord = new SdfRecord(0);
        sdfRecord.setMolfileName("Benzene");
        sdfRecord.setMolfile(BENZENE);
        return sdfRecord;
    }

    private SdfRecord getLast() {

        SdfRecord sdfRecord = new SdfRecord(4);
        sdfRecord.setMolfileName("ZINC02782238");
        sdfRecord.setMolfile(ZINC_MOL);
        return sdfRecord;
    }

    private static final String BENZENE = "Benzene" + System.getProperty("line.separator")
                + "CDXL12" + System.getProperty("line.separator")
                + "" + System.getProperty("line.separator")
                + "  6  6  0  0  0  0  0  0  0  0999 V2000" + System.getProperty("line.separator")
                + "   -0.7145    0.4125    0.0000 C   0  0  0  0  0  0  0  2  0  0  0  0" + System.getProperty("line.separator")
                + "   -0.7145   -0.4125    0.0000 C   0  0  0  0  0  0  0  2  0  0  0  0" + System.getProperty("line.separator")
                + "    0.0000   -0.8250    0.0000 C   0  0  0  0  0  0  0  2  0  0  0  0" + System.getProperty("line.separator")
                + "    0.7145   -0.4125    0.0000 C   0  0  0  0  0  0  0  2  0  0  0  0" + System.getProperty("line.separator")
                + "    0.7145    0.4125    0.0000 C   0  0  0  0  0  0  0  2  0  0  0  0" + System.getProperty("line.separator")
                + "    0.0000    0.8250    0.0000 C   0  0  0  0  0  0  0  2  0  0  0  0" + System.getProperty("line.separator")
                + "  1  2  2  0        0" + System.getProperty("line.separator")
                + "  2  3  1  0        0" + System.getProperty("line.separator")
                + "  3  4  2  0        0" + System.getProperty("line.separator")
                + "  4  5  1  0        0" + System.getProperty("line.separator")
                + "  5  6  2  0        0" + System.getProperty("line.separator")
                + "  6  1  1  0        0" + System.getProperty("line.separator")
                + "M  END" + System.getProperty("line.separator")
                + "$$$$" + System.getProperty("line.separator");

    private static final String ZINC_MOL = "ZINC02782238" + System.getProperty("line.separator")
                + "  -OEChem-04211110313D" + System.getProperty("line.separator")
                + "" + System.getProperty("line.separator")
                + " 39 40  0     0  0  0  0  0  0999 V2000" + System.getProperty("line.separator")
                + "    0.0021   -0.0041    0.0020 C   0  0  0  0  0  0  0  0  0  0  0  0" + System.getProperty("line.separator")
                + "   -0.0187    1.5258    0.0104 C   0  0  0  0  0  0  0  0  0  0  0  0" + System.getProperty("line.separator")
                + "   -0.7044    1.9982    1.2158 N   0  0  0  0  0  0  0  0  0  0  0  0" + System.getProperty("line.separator")
                + "   -0.1980    2.8468    2.1248 C   0  0  0  0  0  0  0  0  0  0  0  0" + System.getProperty("line.separator")
                + "   -1.1606    3.0341    3.0965 C   0  0  0  0  0  0  0  0  0  0  0  0" + System.getProperty("line.separator")
                + "   -2.2770    2.2471    2.7348 C   0  0  0  0  0  0  0  0  0  0  0  0" + System.getProperty("line.separator")
                + "   -1.9953    1.6365    1.6169 N   0  0  0  0  0  0  0  0  0  0  0  0" + System.getProperty("line.separator")
                + "   -3.5668    2.1327    3.5056 C   0  0  0  0  0  0  0  0  0  0  0  0" + System.getProperty("line.separator")
                + "   -1.0433    3.8899    4.2911 C   0  0  0  0  0  0  0  0  0  0  0  0" + System.getProperty("line.separator")
                + "   -1.9672    3.9649    5.0785 O   0  0  0  0  0  0  0  0  0  0  0  0" + System.getProperty("line.separator")
                + "    0.0877    4.5891    4.5107 N   0  0  0  0  0  0  0  0  0  0  0  0" + System.getProperty("line.separator")
                + "    0.1994    5.4036    5.6477 C   0  0  0  0  0  0  0  0  0  0  0  0" + System.getProperty("line.separator")
                + "   -0.7358    5.6009    6.6147 C   0  0  0  0  0  0  0  0  0  0  0  0" + System.getProperty("line.separator")
                + "   -0.1986    6.4599    7.5189 N   0  0  0  0  0  0  0  0  0  0  0  0" + System.getProperty("line.separator")
                + "    0.9870    6.7986    7.1592 N   0  0  0  0  0  0  0  0  0  0  0  0" + System.getProperty("line.separator")
                + "    1.3098    6.2005    6.0286 C   0  0  0  0  0  0  0  0  0  0  0  0" + System.getProperty("line.separator")
                + "    2.5846    6.3344    5.2987 C   0  0  0  0  0  0  0  0  0  0  0  0" + System.getProperty("line.separator")
                + "    2.7580    5.7246    4.2611 O   0  0  0  0  0  0  0  0  0  0  0  0" + System.getProperty("line.separator")
                + "    3.5572    7.1325    5.7820 N   0  0  0  0  0  0  0  0  0  0  0  0" + System.getProperty("line.separator")
                + "   -0.8761    6.9365    8.7272 C   0  0  0  0  0  0  0  0  0  0  0  0" + System.getProperty("line.separator")
                + "   -0.0035    6.6454    9.9497 C   0  0  0  0  0  0  0  0  0  0  0  0" + System.getProperty("line.separator")
                + "    0.5293   -0.3651    0.8851 H   0  0  0  0  0  0  0  0  0  0  0  0" + System.getProperty("line.separator")
                + "   -1.0205   -0.3814    0.0098 H   0  0  0  0  0  0  0  0  0  0  0  0" + System.getProperty("line.separator")
                + "    0.5123   -0.3556   -0.8948 H   0  0  0  0  0  0  0  0  0  0  0  0" + System.getProperty("line.separator")
                + "    1.0039    1.9031    0.0027 H   0  0  0  0  0  0  0  0  0  0  0  0" + System.getProperty("line.separator")
                + "   -0.5459    1.8868   -0.8726 H   0  0  0  0  0  0  0  0  0  0  0  0" + System.getProperty("line.separator")
                + "    0.7814    3.3012    2.0993 H   0  0  0  0  0  0  0  0  0  0  0  0" + System.getProperty("line.separator")
                + "   -3.4913    1.3136    4.2208 H   0  0  0  0  0  0  0  0  0  0  0  0" + System.getProperty("line.separator")
                + "   -3.7552    3.0645    4.0390 H   0  0  0  0  0  0  0  0  0  0  0  0" + System.getProperty("line.separator")
                + "   -4.3869    1.9373    2.8147 H   0  0  0  0  0  0  0  0  0  0  0  0" + System.getProperty("line.separator")
                + "    0.8246    4.5292    3.8827 H   0  0  0  0  0  0  0  0  0  0  0  0" + System.getProperty("line.separator")
                + "   -1.7191    5.1564    6.6571 H   0  0  0  0  0  0  0  0  0  0  0  0" + System.getProperty("line.separator")
                + "    3.4189    7.6189    6.6097 H   0  0  0  0  0  0  0  0  0  0  0  0" + System.getProperty("line.separator")
                + "    4.3955    7.2205    5.3020 H   0  0  0  0  0  0  0  0  0  0  0  0" + System.getProperty("line.separator")
                + "   -1.0455    8.0105    8.6491 H   0  0  0  0  0  0  0  0  0  0  0  0" + System.getProperty("line.separator")
                + "   -1.8326    6.4246    8.8333 H   0  0  0  0  0  0  0  0  0  0  0  0" + System.getProperty("line.separator")
                + "    0.1659    5.5715   10.0278 H   0  0  0  0  0  0  0  0  0  0  0  0" + System.getProperty("line.separator")
                + "    0.9530    7.1573    9.8436 H   0  0  0  0  0  0  0  0  0  0  0  0" + System.getProperty("line.separator")
                + "   -0.5076    7.0001   10.8487 H   0  0  0  0  0  0  0  0  0  0  0  0" + System.getProperty("line.separator")
                + "  1  2  1  0  0  0  0" + System.getProperty("line.separator")
                + "  1 22  1  0  0  0  0" + System.getProperty("line.separator")
                + "  1 23  1  0  0  0  0" + System.getProperty("line.separator")
                + "  1 24  1  0  0  0  0" + System.getProperty("line.separator")
                + "  2  3  1  0  0  0  0" + System.getProperty("line.separator")
                + "  2 25  1  0  0  0  0" + System.getProperty("line.separator")
                + "  2 26  1  0  0  0  0" + System.getProperty("line.separator")
                + "  3  7  1  0  0  0  0" + System.getProperty("line.separator")
                + "  3  4  1  0  0  0  0" + System.getProperty("line.separator")
                + "  4  5  2  0  0  0  0" + System.getProperty("line.separator")
                + "  4 27  1  0  0  0  0" + System.getProperty("line.separator")
                + "  5  6  1  0  0  0  0" + System.getProperty("line.separator")
                + "  5  9  1  0  0  0  0" + System.getProperty("line.separator")
                + "  6  7  2  0  0  0  0" + System.getProperty("line.separator")
                + "  6  8  1  0  0  0  0" + System.getProperty("line.separator")
                + "  8 28  1  0  0  0  0" + System.getProperty("line.separator")
                + "  8 29  1  0  0  0  0" + System.getProperty("line.separator")
                + "  8 30  1  0  0  0  0" + System.getProperty("line.separator")
                + "  9 10  2  0  0  0  0" + System.getProperty("line.separator")
                + "  9 11  1  0  0  0  0" + System.getProperty("line.separator")
                + " 11 12  1  0  0  0  0" + System.getProperty("line.separator")
                + " 11 31  1  0  0  0  0" + System.getProperty("line.separator")
                + " 12 16  1  0  0  0  0" + System.getProperty("line.separator")
                + " 12 13  2  0  0  0  0" + System.getProperty("line.separator")
                + " 13 14  1  0  0  0  0" + System.getProperty("line.separator")
                + " 13 32  1  0  0  0  0" + System.getProperty("line.separator")
                + " 14 15  1  0  0  0  0" + System.getProperty("line.separator")
                + " 14 20  1  0  0  0  0" + System.getProperty("line.separator")
                + " 15 16  2  0  0  0  0" + System.getProperty("line.separator")
                + " 16 17  1  0  0  0  0" + System.getProperty("line.separator")
                + " 17 18  2  0  0  0  0" + System.getProperty("line.separator")
                + " 17 19  1  0  0  0  0" + System.getProperty("line.separator")
                + " 19 33  1  0  0  0  0" + System.getProperty("line.separator")
                + " 19 34  1  0  0  0  0" + System.getProperty("line.separator")
                + " 20 21  1  0  0  0  0" + System.getProperty("line.separator")
                + " 20 35  1  0  0  0  0" + System.getProperty("line.separator")
                + " 20 36  1  0  0  0  0" + System.getProperty("line.separator")
                + " 21 37  1  0  0  0  0" + System.getProperty("line.separator")
                + " 21 38  1  0  0  0  0" + System.getProperty("line.separator")
                + " 21 39  1  0  0  0  0" + System.getProperty("line.separator")
                + "M  END" + System.getProperty("line.separator")
                + "$$$$" + System.getProperty("line.separator");
}
