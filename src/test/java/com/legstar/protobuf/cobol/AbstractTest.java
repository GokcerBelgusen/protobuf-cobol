package com.legstar.protobuf.cobol;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;

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
            if (!found) {
                FileUtils.deleteDirectory(new File(getReferenceFolder(), dir));
            }
        }

    }

    /**
     * Check a result against a reference.
     * 
     * @param result the result obtained
     */
    protected void check(final String result) {
        check(result, null);
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
                FileUtils.writeStringToFile(referenceFile, result, "UTF-8");
            } else {
                String expected = FileUtils.readFileToString(referenceFile,
                        "UTF-8");
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
        String result = FileUtils.readFileToString(new File(getOutputFolder(),
                fileName));
        check(result, fileName);
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
