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
public class ProtoCobolTest extends AbstractTest {

    /** True when references should be created. */
    private static final boolean CREATE_REFERENCES = false;

    public void testSearchRequestProtos() throws Exception {
        HasMaxSize maxSizeProvider = new HasMaxSize() {

            public Integer getMaxSize(String fieldName, Type fieldType) {
                if (fieldName.equals("query")) {
                    return 144;
                }
                return null;
            }

            public Integer getMaxOccurs(String fieldName, JavaType fieldType) {
                return null;
            }

        };
        run("com.example.simple.Simple", maxSizeProvider);
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
        HasMaxSize maxSizeProvider = new HasMaxSize() {

            public Integer getMaxSize(String fieldName, Type fieldType) {
                return null;
            }

            public Integer getMaxOccurs(String fieldName, JavaType fieldType) {
                if (fieldName.equals("snippets")) {
                    return 5;
                }
                return null;
            }

        };
        run(new File("src/test/resources/nontranslated.proto"), maxSizeProvider);
        checkFile("SearchResponseC.cpy");
        checkFile("SEARRESP.cbl");
        checkFile("SEARRESW.cbl");
    }

    public void testCollectionsProtos() throws Exception {
        HasMaxSize maxSizeProvider = new HasMaxSize() {

            public Integer getMaxSize(String fieldName, Type fieldType) {
                return null;
            }

            public Integer getMaxOccurs(String fieldName, JavaType fieldType) {
                if (fieldName.equals("snippets")) {
                    return 5;
                }
                return null;
            }

        };
        run("com.example.collections.Collections", maxSizeProvider);
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
        HasMaxSize maxSizeProvider = new HasMaxSize() {

            public Integer getMaxSize(String fieldName, Type fieldType) {
                if (fieldName.equals("query")) {
                    return 144;
                }
                return null;
            }

            public Integer getMaxOccurs(String fieldName, JavaType fieldType) {
                return null;
            }

        };
        run("com.example.enumsample.Enumsample", maxSizeProvider);
        checkFile("EnumRequestC.cpy");
        checkFile("ENUMREQP.cbl");
        checkFile("ENUMREQW.cbl");
    }

    /**
     * Generate COBOL while overwriting the default maximum sizes.
     * 
     * @throws Exception it test fails
     */
    public void testAllTypes() throws Exception {
        run("com.example.alltypes.AllTypesProtos");
        checkFile("AllTypesC.cpy");
        checkFile("ALLTYPEP.cbl");
        checkFile("ALLTYPEW.cbl");
    }

    public void testCustomers() throws Exception {
        HasMaxSize maxSizeProvider = new HasMaxSize() {

            public Integer getMaxSize(String fieldName, Type fieldType) {
                if (fieldName.equals("customer_name")) {
                    return 20;
                } else if (fieldName.equals("customer_address")) {
                    return 20;
                } else if (fieldName.equals("customer_phone")) {
                    return 8;
                } else if (fieldName.equals("last_transaction_comment")) {
                    return 9;
                }
                return null;
            }

            public Integer getMaxOccurs(String fieldName, JavaType fieldType) {
                if (fieldName.equals("Customer")) {
                    return 1000;
                }
                return null;
            }

        };
        run("com.example.customers.CustomersProtos", maxSizeProvider);
        checkFile("CustomersQueryC.cpy");
        checkFile("CustomersQueryReplyC.cpy");
        checkFile("CUSQUREP.cbl");
        checkFile("CUSQUREW.cbl");
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
                .setQualifiedClassName(javaClassName).run();
    }

    /**
     * Invoke the ProtoCobol generator knowing a protoc-generated java class
     * name.
     * 
     * @param javaClassName the protobuf-java generated Java class name
     * @param maxSizeProvider provides custom max sizes for COBOL
     * @throws ProtoCobolException if generation fails
     */
    protected void run(String javaClassName, HasMaxSize maxSizeProvider)
            throws ProtoCobolException {
        new ProtoCobol().setOutputDir(getOutputFolder())
                .setQualifiedClassName(javaClassName)
                .addSizeProvider(maxSizeProvider).run();
    }

    /**
     * Invoke the ProtoCobol generator knowing the initial proto file.
     * 
     * @param protoFile the initial roto file
     * @throws ProtoCobolException if generation fails
     */
    protected void run(File protoFile) throws ProtoCobolException {
        new ProtoCobol().setOutputDir(getOutputFolder())
                .setProtoFile(protoFile).run();
    }

    /**
     * Invoke the ProtoCobol generator knowing the initial proto file.
     * 
     * @param protoFile the initial roto file
     * @param maxSizeProvider provides custom max sizes for COBOL
     * @throws ProtoCobolException if generation fails
     */
    protected void run(File protoFile, HasMaxSize maxSizeProvider)
            throws ProtoCobolException {
        new ProtoCobol().setOutputDir(getOutputFolder())
                .setProtoFile(protoFile).addSizeProvider(maxSizeProvider).run();
    }

    public boolean isCreateReferences() {
        return CREATE_REFERENCES;
    }

}
