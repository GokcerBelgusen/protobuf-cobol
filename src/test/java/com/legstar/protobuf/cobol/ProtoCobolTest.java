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
        checkFile("SEARREQP.cbl");
        checkFile("SEARREQW.cbl");
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
        checkFile("SEARRESP.cbl");
        checkFile("SEARRESW.cbl");
    }

    public void testCollectionsProtos() throws Exception {
        run("com.example.collections.Collections");
        checkFile("SearchResponseC.cpy");
        checkFile("SEARRESP.cbl");
        checkFile("SEARRESW.cbl");
    }

    public void testHierarchyProtos() throws Exception {
        run("com.example.hierarchy.Hierarchy");
        checkFile("CustomerDataC.cpy");
        checkFile("CUSTDATP.cbl");
        checkFile("CUSTDATW.cbl");
    }

    public void testComplexArrays() throws Exception {
        run("com.example.complexarrays.Complexarrays");
        checkFile("AC.cpy");
        checkFile("AP.cbl");
        checkFile("AW.cbl");
    }

    public void testEnumSample() throws Exception {
        run("com.example.enumsample.Enumsample");
        checkFile("EnumRequestC.cpy");
        checkFile("ENUMREQP.cbl");
        checkFile("ENUMREQW.cbl");
    }

    public void testAllTypes() throws Exception {
        run("com.example.alltypes.AllTypesProtos");
        checkFile("AllTypesC.cpy");
        checkFile("ALLTYPEP.cbl");
        checkFile("ALLTYPEW.cbl");
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
