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

}
