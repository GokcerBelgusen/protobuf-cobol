/*******************************************************************************
 * Copyright (c) 2012 LegSem EURL.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 * 
 * Contributors:
 *     LegSem EURL - initial API and implementation
 ******************************************************************************/
package com.legstar.protobuf.cobol;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class AbstractTest extends TestCase {

    /** Reference folder. */
    public static final File REF_DIR = new File("src/test/resources/reference");

    /** Where cobol generated code ends up. */
    private File outputFolder = new File("target/generated-test-sources/cobol");

    /** Extension added to reference files. */
    public static final String REF_FILE_EXT = "txt";

    /** Reference files and test results are encoded with this. */
    public static final String FILES_ENCODING = "UTF-8";

    private static Log logger = LogFactory.getLog(AbstractTest.class);

    public void setUp() throws Exception {
        if (isCreateReferences()) {
            cleanOldReferences();
        }
    }

    /**
     * This is our chance to remove reference files that are no longer used by a
     * test case. This happens when test cases are renamed or removed.
     * 
     * @throws IOException
     */
    protected void cleanOldReferences() throws IOException {
        if (!getReferenceFolder().exists()) {
            return;
        }
        Method[] methods = getClass().getDeclaredMethods();

        for (File refFile : FileUtils.listFiles(getReferenceFolder(),
                new String[] { REF_FILE_EXT }, false)) {
            boolean found = false;
            for (int i = 0; i < methods.length; i++) {
                if (methods[i].getName().equals(
                        FilenameUtils.getBaseName(refFile.getName()))) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                refFile.delete();
            }
        }
        String[] dirs = getReferenceFolder().list(DirectoryFileFilter.INSTANCE);
        for (String dir : dirs) {
            boolean found = false;
            for (int i = 0; i < methods.length; i++) {
                if (methods[i].getName().equals(dir)) {
                    found = true;
                    break;
                }
            }
            if (!found && !dir.equals(".svn")) {
                FileUtils.deleteDirectory(new File(getReferenceFolder(), dir));
            }
        }

    }

    /**
     * Check a result against a reference.
     * <p/>
     * Neutralize platform specific line separator (produced by StringTemplate)
     * 
     * @param result the result obtained
     */
    protected void check(final String result) {
        check(result.replace("\r\n", "\n"), null);
    }

    /**
     * Check a result against a reference.
     * 
     * @param result the result obtained
     * @param fileName the corresponding file name generated (null if none)
     */
    protected void check(final String result, String fileName) {
        String debugName = getClass().getSimpleName() + "-" + getName()
                + ((fileName == null) ? "" : "." + fileName);
        try {

            logger.debug(debugName + ":\n" + result);
            File referenceFolder = (fileName == null) ? getReferenceFolder()
                    : new File(getReferenceFolder(), getName());
            File referenceFile = new File(referenceFolder,
                    ((fileName == null) ? getName() : fileName) + "."
                            + REF_FILE_EXT);

            if (isCreateReferences()) {
                FileUtils.writeStringToFile(referenceFile, result,
                        FILES_ENCODING);
            } else {
                String expected = fileToString(referenceFile);
                assertEquals(expected, result);
            }
        } catch (IOException e) {
            logger.error("Test " + debugName + " failed", e);
            fail(e.getMessage());
        }

    }

    /**
     * Read the content of a file and check that it corresponds to the
     * reference.
     * 
     * @param fileName the file name
     * @throws IOException if reading the file fails
     */
    public void checkFile(String fileName) throws IOException {
        check(fileToString(fileName), fileName);
    }

    /**
     * Turns the content of a file into a string with platform neutral line
     * separator.
     * 
     * @param fileName the file name
     * @return a string with lines separated by "\n"
     * @throws IOException if file cannot be read
     */
    public String fileToString(String fileName) throws IOException {
        return fileToString(new File(getOutputFolder(), fileName));
    }

    /**
     * Turns the content of a file into a string with platform neutral line
     * separator.
     * 
     * @param file the file
     * @return a string with lines separated by "\n"
     * @throws IOException if file cannot be read
     */
    public String fileToString(File file) throws IOException {
        List < String > lines = FileUtils.readLines(file, FILES_ENCODING);
        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            if (sb.length() > 0) {
                sb.append("\n");
            }
            sb.append(line);
        }
        return sb.toString();
    }

    /**
     * Location where where reference files are stored for this test case.
     * 
     * @return the test case reference files folder
     */
    public File getReferenceFolder() {
        return new File(REF_DIR, getClass().getSimpleName());
    }

    /**
     * @return a folder where each test can produce its output
     */
    public File getOutputFolder() {
        return new File(outputFolder, getName());
    }

    /**
     * @return true if references should be created instead of compared to
     *         results
     */
    public abstract boolean isCreateReferences();

}
