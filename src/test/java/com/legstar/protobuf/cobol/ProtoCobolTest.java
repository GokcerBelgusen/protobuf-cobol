package com.legstar.protobuf.cobol;

import java.io.File;

import com.google.protobuf.Descriptors.FieldDescriptor.JavaType;
import com.google.protobuf.Descriptors.FieldDescriptor.Type;
import com.legstar.protobuf.cobol.ProtoCobolMapper.HasMaxSize;

/**
 * Unit test for Proto Cobol. The pom.xml has an antrun plugin bound to
 * generate-test-sources which invokes protoc.exe for java. It will compile the
 * *.proto to java so we can locate our test names by java class name (the PST
 * compilation is broken, at least on Windows).
 */
public class ProtoCobolTest extends AbstractTest implements HasMaxSize {

    /** True when references should be created. */
    private static final boolean CREATE_REFERENCES = false;

    public void testSearchRequestProtos() throws Exception {
        run("com.example.simple.Simple");
        checkFile("SearchRequestC.cpy");
    }

    public void testAddressBookProtos() throws Exception {
        run("com.example.tutorial.AddressBookProtos");
        checkFile("PersonC.cpy");
        checkFile("ADDRBOOP.cbl");
        checkFile("ADDRBOOW.cbl");
    }

    public void testNonTranslatedProtos() throws Exception {
        run(new File("src/test/resources/nontranslated.proto"));
        checkFile("SearchResponseC.cpy");
    }

    public void testEnumSample() throws Exception {
        run("com.example.enumsample.Enumsample");
        checkFile("SearchRequestC.cpy");
    }

    public void testAllTypes() throws Exception {
        run("com.example.alltypes.AllTypesProtos");
        checkFile("AllTypesC.cpy");
    }

    public void testCollectionsProtos() throws Exception {
        run("com.example.collections.Collections");
        checkFile("SearchResponseC.cpy");
    }

    public void testComplexArrays() throws Exception {
        run("com.example.complexarrays.Complexarrays");
        checkFile("AC.cpy");
        checkFile("AP.cbl");
    }

    /**
     * Invoke the ProtoCobol generator knowing a protoc-generated java class
     * name.
     * 
     * @param javaClassName the protobuf-java generated Java class name
     * @throws ProtoCobolException if generation fails
     */
    protected void run(String javaClassName) throws ProtoCobolException {
        new ProtoCobol().setOutputDir(getOutputFolder())
                .setQualifiedClassName(javaClassName).addSizeProvider(this)
                .run();
    }

    /**
     * Invoke the ProtoCobol generator knowing the initial proto file.
     * 
     * @param protoFile the initial roto file
     * @throws ProtoCobolException if generation fails
     */
    protected void run(File protoFile) throws ProtoCobolException {
        new ProtoCobol().setOutputDir(getOutputFolder())
                .setProtoFile(protoFile).addSizeProvider(this).run();
    }

    public boolean isCreateReferences() {
        return CREATE_REFERENCES;
    }

    public Integer getMaxSize(String fieldName, Type fieldType) {
        if (fieldName.equals("query")) {
            return 144;
        }
        return null;
    }

    public Integer getMaxOccurs(String fieldName, JavaType fieldType) {
        if (fieldName.equals("snippets")) {
            return 5;
        }
        return null;
    }

}
