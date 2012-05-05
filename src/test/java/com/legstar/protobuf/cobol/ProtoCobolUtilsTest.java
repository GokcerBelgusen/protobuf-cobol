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

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;

import com.legstar.protobuf.cobol.ProtoCobolUtils.ProtoFileJavaProperties;

public class ProtoCobolUtilsTest extends TestCase {

    public void testExtractNoPackageNameFromProtoFile() throws Exception {
        File protoFile = File.createTempFile(getName(), ".proto");
        protoFile.deleteOnExit();
        FileUtils.writeStringToFile(protoFile, "");
        ProtoFileJavaProperties javaProperties = ProtoCobolUtils
                .getJavaProperties(protoFile);
        assertNull(javaProperties.getJavaPackageName());
    }

    public void testExtractPackageNameFromProtoFile() throws Exception {
        File protoFile = File.createTempFile(getName(), ".proto");
        protoFile.deleteOnExit();
        FileUtils.writeStringToFile(protoFile,
                "option java_package = \"com.example.tutorial\";");
        ProtoFileJavaProperties javaProperties = ProtoCobolUtils
                .getJavaProperties(protoFile);
        assertEquals("com.example.tutorial",
                javaProperties.getJavaPackageName());
    }

    public void testExtractPackageNameNoWhiteSpacesFromProtoFile()
            throws Exception {
        File protoFile = File.createTempFile(getName(), ".proto");
        protoFile.deleteOnExit();
        FileUtils.writeStringToFile(protoFile,
                "option java_package=\"com.example.tutorial\";");
        ProtoFileJavaProperties javaProperties = ProtoCobolUtils
                .getJavaProperties(protoFile);
        assertEquals("com.example.tutorial",
                javaProperties.getJavaPackageName());
    }

    public void testExtractPackageNameExtraWhiteSpacesFromProtoFile()
            throws Exception {
        File protoFile = File.createTempFile(getName(), ".proto");
        protoFile.deleteOnExit();
        FileUtils.writeStringToFile(protoFile,
                "option  java_package  =  \"com.example.tutorial\"  ;");
        ProtoFileJavaProperties javaProperties = ProtoCobolUtils
                .getJavaProperties(protoFile);
        assertEquals("com.example.tutorial",
                javaProperties.getJavaPackageName());
    }

    public void testExtractPackageNameFromDefaultInProtoFile() throws Exception {
        File protoFile = File.createTempFile(getName(), ".proto");
        protoFile.deleteOnExit();
        FileUtils.writeStringToFile(protoFile, "package tutorial;");
        ProtoFileJavaProperties javaProperties = ProtoCobolUtils
                .getJavaProperties(protoFile);
        assertEquals("tutorial", javaProperties.getJavaPackageName());
    }

    public void testExtractDefaultClassNameFromProtoFile() throws Exception {
        File protoFile = File.createTempFile(getName(), ".proto");
        protoFile.deleteOnExit();
        FileUtils.writeStringToFile(protoFile, "");
        ProtoFileJavaProperties javaProperties = ProtoCobolUtils
                .getJavaProperties(protoFile);
        assertEquals(ProtoCobolUtils.getDefaultJavaClassName(protoFile),
                javaProperties.getJavaClassName());
    }

    public void testExtractClassNameFromProtoFile() throws Exception {
        File protoFile = File.createTempFile(getName(), ".proto");
        protoFile.deleteOnExit();
        FileUtils.writeStringToFile(protoFile,
                "option java_outer_classname = \"AddressBookProtos\";");
        ProtoFileJavaProperties javaProperties = ProtoCobolUtils
                .getJavaProperties(protoFile);
        assertEquals("AddressBookProtos.java",
                javaProperties.getJavaClassName());
    }
}
