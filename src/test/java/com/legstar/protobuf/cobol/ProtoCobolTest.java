package com.legstar.protobuf.cobol;

import java.io.File;

import org.apache.commons.io.FileUtils;

/**
 * Unit test for Proto Cobol. The pom.xml has an antrun plugin bound to
 * generate-test-sources which invokes protoc.exe for java. It will compile the
 * *.proto to java so we can locate our test names by java class name (the PST
 * compilation is broken, at least on Windows).
 */
public class ProtoCobolTest extends AbstractTest {

    /** True when references should be created. */
    private static final boolean CREATE_REFERENCES = false;

    public void testSearchRequestProtos() throws Exception {
        run("com.example.simple.Simple");
        check(FileUtils.readFileToString(new File(getOutputFolder(),
                "SearchRequestC.cpy")));
    }

    public void testAddressBookProtos() throws Exception {
        run("com.example.tutorial.AddressBookProtos");
        check(FileUtils.readFileToString(new File(getOutputFolder(),
                "PersonC.cpy")));
    }

    public void testNonTranslatedProtos() throws Exception {
        run(new File("src/test/resources/nontranslated.proto"));
        check(FileUtils.readFileToString(new File(getOutputFolder(),
                "SearchResponseC.cpy")));
    }

    public void testEnumSample() throws Exception {
        run("com.example.enumsample.Enumsample");
        check(FileUtils.readFileToString(new File(getOutputFolder(),
                "SearchRequestC.cpy")));
    }

    public void testAllTypes() throws Exception {
        run("com.example.alltypes.AllTypesProtos");
        check(FileUtils.readFileToString(new File(getOutputFolder(),
                "AllTypesC.cpy")));
    }

    /**
     * Invoke the ProtoCobol generator knowing a protoc-generated java class
     * name.
     * 
     * @param javaClassName the protobuf-java generated Java class name
     * @throws ProtoCobolException if generation fails
     */
    protected void run(String javaClassName) throws ProtoCobolException {
        ProtoCobol pc = new ProtoCobol();
        pc.setOutputDir(getOutputFolder());
        pc.setQualifiedClassName(javaClassName);
        pc.run();
    }

    /**
     * Invoke the ProtoCobol generator knowing the initial proto file.
     * 
     * @param protoFile the initial roto file
     * @throws ProtoCobolException if generation fails
     */
    protected void run(File protoFile) throws ProtoCobolException {
        ProtoCobol pc = new ProtoCobol();
        pc.setOutputDir(getOutputFolder());
        pc.setProtoFile(protoFile);
        pc.run();
    }

    public boolean isCreateReferences() {
        return CREATE_REFERENCES;
    }

}
