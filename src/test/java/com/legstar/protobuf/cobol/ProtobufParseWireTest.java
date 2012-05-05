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

import junit.framework.TestCase;

import com.legstar.coxb.host.HostData;

public class ProtobufParseWireTest extends TestCase {

    public void testTutorial() throws Exception {
        com.example.tutorial.AddressBookProtos.AddressBook addressBook = com.example.tutorial.AddressBookProtos.AddressBook
                .parseFrom(HostData
                        .toByteArray("0A770A0B4461727468205661646F7210011A126461727468406461726B737461722E636F6D22100A0C30343136353638393735343"
                                + "2100122100A0C303638363435383931323435100022040A00100022040A00100022040A00100022040A00100022040A00100022040A00100022040A0"
                                + "0100022040A0010000A750A0D4C756B6520536B7977616B657210011A116C756B65407461746F75696E652E636F6D220F0A0B3035363839313234353"
                                + "7381001220E0A0A30363839353337383435100022040A00100022040A00100022040A00100022040A00100022040A00100022040A00100022040A001"
                                + "00022040A001000"));
        assertEquals(2, addressBook.getPersonCount());
        com.example.tutorial.AddressBookProtos.Person person = addressBook
                .getPerson(0);
        assertEquals("Darth Vador", person.getName());
        assertEquals(1, person.getId());
        assertEquals("darth@darkstar.com", person.getEmail());
        assertEquals(10, person.getPhoneCount());
        assertEquals("041656897542", person.getPhone(0).getNumber());
        assertEquals(
                com.example.tutorial.AddressBookProtos.Person.PhoneType.HOME,
                person.getPhone(0).getType());
        assertEquals("068645891245", person.getPhone(1).getNumber());
        assertEquals(
                com.example.tutorial.AddressBookProtos.Person.PhoneType.MOBILE,
                person.getPhone(1).getType());

        person = addressBook.getPerson(1);
        assertEquals("Luke Skywaker", person.getName());
        assertEquals(1, person.getId());
        assertEquals("luke@tatouine.com", person.getEmail());
        assertEquals(10, person.getPhoneCount());
        assertEquals("05689124578", person.getPhone(0).getNumber());
        assertEquals(
                com.example.tutorial.AddressBookProtos.Person.PhoneType.HOME,
                person.getPhone(0).getType());
        assertEquals("0689537845", person.getPhone(1).getNumber());
        assertEquals(
                com.example.tutorial.AddressBookProtos.Person.PhoneType.MOBILE,
                person.getPhone(1).getType());
    }

    public void testSimple() throws Exception {
        com.example.simple.Simple.SearchRequest searchRequest = com.example.simple.Simple.SearchRequest
                .parseFrom(HostData.toByteArray("0A084A6F686E20446F651005180A"));
        assertEquals("John Doe", searchRequest.getQuery());
        assertEquals(5, searchRequest.getPageNumber());
        assertEquals(10, searchRequest.getResultPerPage());
    }

    public void testCollection() throws Exception {
        com.example.collections.Collections.SearchResponse searchResponse = com.example.collections.Collections.SearchResponse
                .parseFrom(HostData
                        .toByteArray("0A480A0B687474703A2F2F75726C311207726573756C74311A0D466972737420736E69707065741A0E5365636F6E6420736E69707"
                                + "065741A0D546869726420736E69707065741A001A000A3B0A0B687474703A2F2F75726C321207726573756C74321A0E466F7572746820736E6970706"
                                + "5741A0D466966746820736E69707065741A001A001A00"));
        assertEquals(2, searchResponse.getResultCount());
        assertEquals("http://url1", searchResponse.getResult(0).getUrl());
        assertEquals("result1", searchResponse.getResult(0).getTitle());
        assertEquals(5, searchResponse.getResult(0).getSnippetsCount());
        assertEquals("First snippet", searchResponse.getResult(0)
                .getSnippets(0));
        assertEquals("Second snippet",
                searchResponse.getResult(0).getSnippets(1));
        assertEquals("Third snippet", searchResponse.getResult(0)
                .getSnippets(2));
        assertEquals("", searchResponse.getResult(0).getSnippets(3));
        assertEquals("", searchResponse.getResult(0).getSnippets(4));
        assertEquals("http://url2", searchResponse.getResult(1).getUrl());
        assertEquals("result2", searchResponse.getResult(1).getTitle());
        assertEquals(5, searchResponse.getResult(1).getSnippetsCount());
        assertEquals("Fourth snippet",
                searchResponse.getResult(1).getSnippets(0));
        assertEquals("Fifth snippet", searchResponse.getResult(1)
                .getSnippets(1));
        assertEquals("", searchResponse.getResult(1).getSnippets(2));
        assertEquals("", searchResponse.getResult(1).getSnippets(3));
        assertEquals("", searchResponse.getResult(1).getSnippets(4));
    }

    public void testHierarchy() throws Exception {
        com.example.hierarchy.Hierarchy.CustomerData customerData = com.example.hierarchy.Hierarchy.CustomerData
                .parseFrom(HostData
                        .toByteArray("0A080A066368696C643212066368696C6433"));
        assertEquals("child2", customerData.getChild1().getChild2());
        assertEquals("child3", customerData.getChild3());
    }

    public void testComplexArrays() throws Exception {
        com.example.complexarrays.Complexarrays.A a = com.example.complexarrays.Complexarrays.A
                .parseFrom(HostData
                        .toByteArray("0A1C0A1A0A034431310A034431320A000A000A000A000A000A000A000A000A1C0A1A0A034432310A034432320A000A000A000A000"
                                + "A000A000A000A0012040A02463112040A02463212040A024633"));
        assertEquals(2, a.getBCount());
        assertEquals(10, a.getB(0).getC().getDCount());
        assertEquals("D11", a.getB(0).getC().getD(0));
        assertEquals("D12", a.getB(0).getC().getD(1));
        assertEquals("", a.getB(0).getC().getD(2));
        assertEquals("", a.getB(0).getC().getD(3));
        assertEquals("", a.getB(0).getC().getD(4));
        assertEquals("", a.getB(0).getC().getD(5));
        assertEquals("", a.getB(0).getC().getD(6));
        assertEquals("", a.getB(0).getC().getD(7));
        assertEquals("", a.getB(0).getC().getD(8));
        assertEquals("", a.getB(0).getC().getD(9));
        assertEquals(10, a.getB(1).getC().getDCount());
        assertEquals("D21", a.getB(1).getC().getD(0));
        assertEquals("D22", a.getB(1).getC().getD(1));
        assertEquals("", a.getB(1).getC().getD(2));
        assertEquals("", a.getB(1).getC().getD(3));
        assertEquals("", a.getB(1).getC().getD(4));
        assertEquals("", a.getB(1).getC().getD(5));
        assertEquals("", a.getB(1).getC().getD(6));
        assertEquals("", a.getB(1).getC().getD(7));
        assertEquals("", a.getB(1).getC().getD(8));
        assertEquals("", a.getB(1).getC().getD(9));
        assertEquals(3, a.getECount());
        assertEquals("F1", a.getE(0).getF());
        assertEquals("F2", a.getE(1).getF());
        assertEquals("F3", a.getE(2).getF());
    }

    public void testEnum() throws Exception {
        com.example.enumsample.Enumsample.EnumRequest searchRequest = com.example.enumsample.Enumsample.EnumRequest
                .parseFrom(HostData
                        .toByteArray("0A084A6F686E20446F651005180A2002"));
        assertEquals("John Doe", searchRequest.getQuery());
        assertEquals(5, searchRequest.getPageNumber());
        assertEquals(10, searchRequest.getResultPerPage());
        assertEquals(
                com.example.enumsample.Enumsample.EnumRequest.Corpus.IMAGES,
                searchRequest.getCorpus());
    }

    public void testAlltypes() throws Exception {
        com.example.alltypes.AllTypesProtos.AllTypes allTypes = com.example.alltypes.AllTypesProtos.AllTypes
                .parseFrom(HostData
                        .toByteArray("0801120A636C617373696669656419AAF1D24D62B0394020052D88C1D4D2303838C0FFFFFFFFFFFFFFFF0140BFE602480250E1C1B"
                                + "60558F28FC0B30361FFFFFF7F000000006900000080FFFFFFFF75A30200007D3F07F9FF"));
        assertEquals(true, allTypes.getAbool());
        assertEquals(25.689D, allTypes.getAdouble());
        assertEquals(675, allTypes.getAfixed32());
        assertEquals(Integer.MAX_VALUE, allTypes.getAfixed64());
        assertEquals(-4.56889991E11F, allTypes.getAfloat());
        assertEquals(56, allTypes.getAint32());
        assertEquals(-64, allTypes.getAint64());
        assertEquals(-456897, allTypes.getAsfixed32());
        assertEquals(Integer.MIN_VALUE, allTypes.getAsfixed64());
        assertEquals(-5689457, allTypes.getAsint32());
        assertEquals(456655865, allTypes.getAsint64());
        assertEquals("classified", allTypes.getAstring());
        assertEquals(45887, allTypes.getAuint32());
        assertEquals(2, allTypes.getAuint64());
        assertEquals(
                com.example.alltypes.AllTypesProtos.AllTypes.Aenum.PRODUCTS,
                allTypes.getAenum());
    }
}
