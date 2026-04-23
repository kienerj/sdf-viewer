/*
 * The MIT License
 *
 * Copyright 2013 Joos Kiener <Joos.Kiener@gmail.com>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.bitbucket.kienerj.io;

import java.io.EOFException;
import java.io.FileDescriptor;
import java.io.IOException;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Joos Kiener <Joos.Kiener@gmail.com>
 */
public class OptimizedRandomAccessFileTest {

    private static final String FILE_PATH = "src/test/resources/unitTestFile.txt";
    private OptimizedRandomAccessFile instance;

    public OptimizedRandomAccessFileTest() {
    }

    @Before
    public void setUp() throws IOException {
        instance = new OptimizedRandomAccessFile(FILE_PATH, "r");
    }

    @After
    public void tearDown() throws IOException {
        instance.close();
    }

    /**
     * Test of getFD method, of class OptimizedRandomAccessFile.
     */
    @Test
    public void testGetFD() throws Exception {
        System.out.println("getFD");
        FileDescriptor result = instance.getFD();
        assertTrue(result.valid());
    }

    /**
     * Test of read method, of class OptimizedRandomAccessFile.
     */
    @Test
    public void testRead() throws Exception {
        System.out.println("read");
        int expResult = 97; // ASCII A
        int result = instance.read();
        assertEquals(expResult, result);
    }

    /**
     * Test of read method, of class OptimizedRandomAccessFile.
     */
    @Test
    public void testReadIntoBufferWithOffset() throws Exception {
        System.out.println("ReadIntoBufferWithOffset");
        byte[] b = new byte[5];
        int off = 0;
        int len = 5;
        int expBytesRead = 5;
        int bytesRead = instance.read(b, off, len);
        assertEquals(expBytesRead, bytesRead);
        String expResult = "a\nb\nc";
        String result = new String(b, "US-ASCII");
        assertEquals(expResult, result);
    }

    /**
     * Test of read method, of class OptimizedRandomAccessFile.
     */
    @Test
    public void testReadIntoBuffer() throws Exception {
        System.out.println("ReadIntoBuffer");
        byte[] b = new byte[5];
        int expBytesRead = 5;
        int bytesRead = instance.read(b);
        assertEquals(expBytesRead, bytesRead);
        String expResult = "a\nb\nc";
        String result = new String(b, "US-ASCII");
        assertEquals(expResult, result);
    }

    /**
     * Test of readFully method, of class OptimizedRandomAccessFile.
     */
    @Test
    public void testReadFullyIntoBuffer() throws Exception {
        System.out.println("readFullyIntoBuffer");
        byte[] b = new byte[5];
        instance.readFully(b);
        String expResult = "a\nb\nc";
        String result = new String(b, "US-ASCII");
        assertEquals(expResult, result);
    }

    /**
     * Test of readFully method, of class OptimizedRandomAccessFile.
     */
    @Test
    public void testReadFullyIntoBufferWithOffset() throws Exception {
        System.out.println("readFullyWithOffset");
        byte[] b = new byte[5];
        int off = 0;
        int len = 5;
        instance.readFully(b, off, len);
        String expResult = "a\nb\nc";
        String result = new String(b, "US-ASCII");
        assertEquals(expResult, result);
    }

    /**
     * Test of readFully method, of class OptimizedRandomAccessFile.
     */
    @Test(expected = EOFException.class)
    public void testReadFullyEOF() throws Exception {
        System.out.println("readFullyIntoBufferEOF");
        byte[] b = new byte[4096];
        instance.readFully(b);
        fail("EOFException should have been thrown.");
    }

    /**
     * Test of skipBytes method, of class OptimizedRandomAccessFile.
     */
    @Test
    public void testSkipBytes() throws Exception {
        System.out.println("skipBytes");
        int n = 6;
        int expResult = 6;
        int result = instance.skipBytes(n);
        assertEquals(expResult, result);
        int expectedValue = 100; // d
        int value = instance.read();
        assertEquals(expectedValue, value);
    }

    /**
     * Test of getFilePointer method, of class OptimizedRandomAccessFile.
     */
    @Test
    public void testGetFilePointer() throws Exception {
        System.out.println("getFilePointer");
        long expResult = 0L;
        long result = instance.getFilePointer();
        assertEquals(expResult, result);
        instance.readLine();
        expResult = 2L;
        result = instance.getFilePointer();
        assertEquals(expResult, result);
    }

    /**
     * Test of seek method, of class OptimizedRandomAccessFile.
     */
    @Test
    public void testSeek() throws Exception {
        System.out.println("seek");
        long pos = 10L;
        instance.seek(pos);
        int expResult = 48; // 0
        int result = instance.read();
        assertEquals(expResult, result);
    }

    /**
     * Test of readLine method, of class OptimizedRandomAccessFile.
     */
    @Test
    public void testReadLineIgnoreLF() throws Exception {
        System.out.println("readLineIgnoreLF");
        boolean ignoreLF = true;
        String expResult = "01234";
        instance.seek(9);
        String result = instance.readLine(ignoreLF);
        assertEquals(expResult, result);
    }

    /**
     * Test of readLine method, of class OptimizedRandomAccessFile.
     */
    @Test
    public void testReadLine() throws Exception {
        System.out.println("readLine");
        String expResult = "a";
        String result = instance.readLine();
        assertEquals(expResult, result);
        instance.readLine();
        instance.readLine();
        instance.readLine();
        instance.readLine();
        String expResult2 = "01234";
        String result2 = instance.readLine();
        assertEquals(expResult2, result2);
    }

    @Test
    public void testReadAfterReadLine() throws Exception {
        System.out.println("readAfterReadLine");
        instance.readLine();
        instance.readLine();
        instance.readLine();
        instance.readLine();
        instance.readLine();
        int expResult = 48; // 0
        int result = instance.read();
        assertEquals(expResult, result);
    }

    @Test
    public void testReadIntoBufferAfterReadLine() throws Exception {
        System.out.println("readAfterReadLine");
        instance.readLine();
        instance.readLine();
        instance.readLine();
        instance.readLine();
        instance.readLine();
        byte[] b = new byte[5];
        int expBytesRead = 5;
        int bytesRead = instance.read(b);
        assertEquals(expBytesRead, bytesRead);
        String expResult = "01234";
        String result = new String(b, "US-ASCII");
        assertEquals(expResult, result);
    }
}
