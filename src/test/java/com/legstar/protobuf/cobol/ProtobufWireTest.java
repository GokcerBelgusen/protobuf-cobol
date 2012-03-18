package com.legstar.protobuf.cobol;

import junit.framework.TestCase;

import com.example.alltypes.AllTypesProtos.Fixedint32;
import com.example.alltypes.AllTypesProtos.Fixedint64;
import com.example.alltypes.AllTypesProtos.SFixedint32;
import com.example.alltypes.AllTypesProtos.SFixedint64;
import com.example.alltypes.AllTypesProtos.Sint32;
import com.example.alltypes.AllTypesProtos.Sint32ZigZag;
import com.example.alltypes.AllTypesProtos.Sint64;
import com.example.alltypes.AllTypesProtos.Sint64ZigZag;
import com.example.alltypes.AllTypesProtos.Uint32;
import com.example.alltypes.AllTypesProtos.Uint64;
import com.example.hierarchy.Hierarchy.CustomerData;
import com.example.tutorial.AddressBookProtos.Person;
import com.legstar.coxb.host.HostData;

public class ProtobufWireTest extends TestCase {

    public void test1() {
        Person john = Person
                .newBuilder()
                .setId(1234)
                .setName("John Doe")
                .setEmail("jdoe@example.com")
                .addPhone(
                        Person.PhoneNumber.newBuilder().setNumber("555-4321")
                                .setType(Person.PhoneType.HOME)).build();

        byte[] result = john.toByteArray();
        assertEquals(45, result.length);
        assertEquals("0a084a6f686e20446f65" + "10d209"
                + "1a106a646f65406578616d706c652e636f6d" + "220c"
                + "0a083535352d34333231" + "1001", HostData.toHexString(result));
    }

    public void test2() {
        Person john = Person.newBuilder().setId(1234).setName("John Doe")
                .setEmail("jdoe@example.com").build();

        byte[] result = john.toByteArray();
        assertEquals(31, result.length);
        assertEquals("0a084a6f686e20446f65" + "10d209"
                + "1a106a646f65406578616d706c652e636f6d",
                HostData.toHexString(result));
    }

    public void test3() {
        Person john = Person
                .newBuilder()
                .setId(1234)
                .setName("John Doe")
                .setEmail("jdoe@example.com")
                .addPhone(
                        Person.PhoneNumber.newBuilder().setNumber("555-4321")
                                .setType(Person.PhoneType.HOME))
                .addPhone(
                        Person.PhoneNumber.newBuilder().setNumber("656-5012")
                                .setType(Person.PhoneType.MOBILE)).build();

        byte[] result = john.toByteArray();
        assertEquals(59, result.length);
        assertEquals("0a084a6f686e20446f65" + "10d209"
                + "1a106a646f65406578616d706c652e636f6d"
                + "220c0a083535352d343332311001"
                + "220c0a083635362d353031321000", HostData.toHexString(result));
    }

    public void testUint64Value300() {
        Uint64 uint64 = Uint64.newBuilder().setAuint64(300).build();
        byte[] result = uint64.toByteArray();
        assertEquals(3, result.length);
        assertEquals("08ac02", HostData.toHexString(result));
    }

    public void testUint64Value45483() {
        Uint64 uint64 = Uint64.newBuilder().setAuint64(45483).build();
        byte[] result = uint64.toByteArray();
        assertEquals(4, result.length);
        assertEquals("08abe302", HostData.toHexString(result));
    }

    public void testUint64ValueLargest() {
        Uint64 uint64 = Uint64.newBuilder().setAuint64(Long.MAX_VALUE).build();
        byte[] result = uint64.toByteArray();
        assertEquals(10, result.length);
        assertEquals("08ffffffffffffffff7f", HostData.toHexString(result));
    }

    public void testSint64Value300() {
        Sint64 int64 = Sint64.newBuilder().setAint64(300).build();
        byte[] result = int64.toByteArray();
        assertEquals(3, result.length);
        assertEquals("08ac02", HostData.toHexString(result));
    }

    public void testSint64ValueMinus1() {
        Sint64 int64 = Sint64.newBuilder().setAint64(-1).build();
        byte[] result = int64.toByteArray();
        assertEquals(11, result.length);
        assertEquals("08ffffffffffffffffff01", HostData.toHexString(result));
    }

    public void testSint64ValueMinus45483() {
        Sint64 int64 = Sint64.newBuilder().setAint64(-45483).build();
        byte[] result = int64.toByteArray();
        assertEquals(11, result.length);
        assertEquals("08d59cfdffffffffffff01", HostData.toHexString(result));
    }

    public void testSint64ValueMinus300() {
        Sint64 int64 = Sint64.newBuilder().setAint64(-300).build();
        byte[] result = int64.toByteArray();
        assertEquals(11, result.length);
        assertEquals("08d4fdffffffffffffff01", HostData.toHexString(result));
    }

    public void testSint64ValueSmallest() {
        Sint64 int64 = Sint64.newBuilder().setAint64(Long.MIN_VALUE).build();
        byte[] result = int64.toByteArray();
        assertEquals(11, result.length);
        assertEquals("0880808080808080808001", HostData.toHexString(result));
    }

    public void testUint32Value300() {
        Uint32 uint32 = Uint32.newBuilder().setAuint32(300).build();
        byte[] result = uint32.toByteArray();
        assertEquals(3, result.length);
        assertEquals("08ac02", HostData.toHexString(result));
    }

    public void testUint32Value45483() {
        Uint32 uint32 = Uint32.newBuilder().setAuint32(45483).build();
        byte[] result = uint32.toByteArray();
        assertEquals(4, result.length);
        assertEquals("08abe302", HostData.toHexString(result));
    }

    public void testUint32ValueLargest() {
        Uint32 uint32 = Uint32.newBuilder().setAuint32(Integer.MAX_VALUE)
                .build();
        byte[] result = uint32.toByteArray();
        assertEquals(6, result.length);
        assertEquals("08ffffffff07", HostData.toHexString(result));
    }

    public void testSint32Value300() {
        Sint32 int32 = Sint32.newBuilder().setAint32(300).build();
        byte[] result = int32.toByteArray();
        assertEquals(3, result.length);
        assertEquals("08ac02", HostData.toHexString(result));
    }

    public void testSint32ValueMinus1() {
        Sint32 int32 = Sint32.newBuilder().setAint32(-1).build();
        byte[] result = int32.toByteArray();
        assertEquals(11, result.length);
        assertEquals("08ffffffffffffffffff01", HostData.toHexString(result));
    }

    public void testSint32ValueMinus45483() {
        Sint32 int32 = Sint32.newBuilder().setAint32(-45483).build();
        byte[] result = int32.toByteArray();
        assertEquals(11, result.length);
        assertEquals("08d59cfdffffffffffff01", HostData.toHexString(result));
    }

    public void testSint32ValueMinus300() {
        Sint32 int32 = Sint32.newBuilder().setAint32(-300).build();
        byte[] result = int32.toByteArray();
        assertEquals(11, result.length);
        assertEquals("08d4fdffffffffffffff01", HostData.toHexString(result));
    }

    public void testSint32ValueSmallest() {
        Sint32 int32 = Sint32.newBuilder().setAint32(Integer.MIN_VALUE).build();
        byte[] result = int32.toByteArray();
        assertEquals(11, result.length);
        assertEquals("0880808080f8ffffffff01", HostData.toHexString(result));
    }

    public void testSint64ZigZagValue1() {
        Sint64ZigZag int64 = Sint64ZigZag.newBuilder().setAint64(1).build();
        byte[] result = int64.toByteArray();
        assertEquals(2, result.length);
        assertEquals("0802", HostData.toHexString(result));
    }

    public void testSint64ZigZagValue300() {
        Sint64ZigZag int64 = Sint64ZigZag.newBuilder().setAint64(300).build();
        byte[] result = int64.toByteArray();
        assertEquals(3, result.length);
        assertEquals("08d804", HostData.toHexString(result));
    }

    public void testSint64ZigZagValueMinus1() {
        Sint64ZigZag int64 = Sint64ZigZag.newBuilder().setAint64(-1).build();
        byte[] result = int64.toByteArray();
        assertEquals(2, result.length);
        assertEquals("0801", HostData.toHexString(result));
    }

    public void testSint64ZigZagValue45483() {
        Sint64ZigZag int64 = Sint64ZigZag.newBuilder().setAint64(45483).build();
        byte[] result = int64.toByteArray();
        assertEquals(4, result.length);
        assertEquals("08d6c605", HostData.toHexString(result));
    }

    public void testSint64ZigZagValueMinus45483() {
        Sint64ZigZag int64 = Sint64ZigZag.newBuilder().setAint64(-45483)
                .build();
        byte[] result = int64.toByteArray();
        assertEquals(4, result.length);
        assertEquals("08d5c605", HostData.toHexString(result));
    }

    public void testSint64ZigZagValueMinus300() {
        Sint64ZigZag int64 = Sint64ZigZag.newBuilder().setAint64(-300).build();
        byte[] result = int64.toByteArray();
        assertEquals(3, result.length);
        assertEquals("08d704", HostData.toHexString(result));
    }

    public void testSint64ZigZagValueLargest() {
        Sint64ZigZag int64 = Sint64ZigZag.newBuilder()
                .setAint64(Long.MAX_VALUE).build();
        byte[] result = int64.toByteArray();
        assertEquals(11, result.length);
        assertEquals("08feffffffffffffffff01", HostData.toHexString(result));
    }

    public void testSint64ZigZagValueSmallest() {
        Sint64ZigZag int64 = Sint64ZigZag.newBuilder()
                .setAint64(Long.MIN_VALUE).build();
        byte[] result = int64.toByteArray();
        assertEquals(11, result.length);
        assertEquals("08ffffffffffffffffff01", HostData.toHexString(result));
    }

    public void testSint32ZigZagValueLargest() {
        Sint32ZigZag int32 = Sint32ZigZag.newBuilder()
                .setAint32(Integer.MAX_VALUE).build();
        byte[] result = int32.toByteArray();
        assertEquals(6, result.length);
        assertEquals("08feffffff0f", HostData.toHexString(result));
    }

    public void testSint32ZigZagValueSmallest() {
        Sint32ZigZag int32 = Sint32ZigZag.newBuilder()
                .setAint32(Integer.MIN_VALUE).build();
        byte[] result = int32.toByteArray();
        assertEquals(6, result.length);
        assertEquals("08ffffffff0f", HostData.toHexString(result));
    }

    public void testFixedint64Value300() {
        Fixedint64 uint64 = Fixedint64.newBuilder().setAuint64(300).build();
        byte[] result = uint64.toByteArray();
        assertEquals(9, result.length);
        assertEquals("092c01000000000000", HostData.toHexString(result));
    }

    public void testFixedint64Value45483() {
        Fixedint64 uint64 = Fixedint64.newBuilder().setAuint64(45483).build();
        byte[] result = uint64.toByteArray();
        assertEquals(9, result.length);
        assertEquals("09abb1000000000000", HostData.toHexString(result));
    }

    public void testFixedint64ValueLargest() {
        Fixedint64 uint64 = Fixedint64.newBuilder().setAuint64(Long.MAX_VALUE)
                .build();
        byte[] result = uint64.toByteArray();
        assertEquals(9, result.length);
        assertEquals("09ffffffffffffff7f", HostData.toHexString(result));
    }

    public void testSFixedint64Value300() {
        SFixedint64 uint64 = SFixedint64.newBuilder().setAint64(300).build();
        byte[] result = uint64.toByteArray();
        assertEquals(9, result.length);
        assertEquals("092c01000000000000", HostData.toHexString(result));
    }

    public void testSFixedint64Value45483() {
        SFixedint64 uint64 = SFixedint64.newBuilder().setAint64(45483).build();
        byte[] result = uint64.toByteArray();
        assertEquals(9, result.length);
        assertEquals("09abb1000000000000", HostData.toHexString(result));
    }

    public void testSFixedint64ValueLargest() {
        SFixedint64 uint64 = SFixedint64.newBuilder().setAint64(Long.MAX_VALUE)
                .build();
        byte[] result = uint64.toByteArray();
        assertEquals(9, result.length);
        assertEquals("09ffffffffffffff7f", HostData.toHexString(result));
    }

    public void testSFixedint64ValueMinus1() {
        SFixedint64 uint64 = SFixedint64.newBuilder().setAint64(-1).build();
        byte[] result = uint64.toByteArray();
        assertEquals(9, result.length);
        assertEquals("09ffffffffffffffff", HostData.toHexString(result));
    }

    public void testSFixedint64ValueMinus300() {
        SFixedint64 uint64 = SFixedint64.newBuilder().setAint64(-300).build();
        byte[] result = uint64.toByteArray();
        assertEquals(9, result.length);
        assertEquals("09d4feffffffffffff", HostData.toHexString(result));
    }

    public void testSFixedint64ValueMinus45483() {
        SFixedint64 uint64 = SFixedint64.newBuilder().setAint64(-45483).build();
        byte[] result = uint64.toByteArray();
        assertEquals(9, result.length);
        assertEquals("09554effffffffffff", HostData.toHexString(result));
    }

    public void testSFixedint64ValueSmallest() {
        SFixedint64 uint64 = SFixedint64.newBuilder().setAint64(Long.MIN_VALUE)
                .build();
        byte[] result = uint64.toByteArray();
        assertEquals(9, result.length);
        assertEquals("090000000000000080", HostData.toHexString(result));
    }

    public void testFixedint32Value300() {
        Fixedint32 uint32 = Fixedint32.newBuilder().setAuint32(300).build();
        byte[] result = uint32.toByteArray();
        assertEquals(5, result.length);
        assertEquals("0d2c010000", HostData.toHexString(result));
    }

    public void testFixedint32Value45483() {
        Fixedint32 uint32 = Fixedint32.newBuilder().setAuint32(45483).build();
        byte[] result = uint32.toByteArray();
        assertEquals(5, result.length);
        assertEquals("0dabb10000", HostData.toHexString(result));
    }

    public void testFixedint32ValueLargest() {
        Fixedint32 uint32 = Fixedint32.newBuilder()
                .setAuint32(Integer.MAX_VALUE).build();
        byte[] result = uint32.toByteArray();
        assertEquals(5, result.length);
        assertEquals("0dffffff7f", HostData.toHexString(result));
    }

    public void testSFixedint32Value300() {
        SFixedint32 int32 = SFixedint32.newBuilder().setAint32(300).build();
        byte[] result = int32.toByteArray();
        assertEquals(5, result.length);
        assertEquals("0d2c010000", HostData.toHexString(result));
    }

    public void testSFixedint32ValueMinus1() {
        SFixedint32 int32 = SFixedint32.newBuilder().setAint32(-1).build();
        byte[] result = int32.toByteArray();
        assertEquals(5, result.length);
        assertEquals("0dffffffff", HostData.toHexString(result));
    }

    public void testSFixedint32ValueMinus45483() {
        SFixedint32 int32 = SFixedint32.newBuilder().setAint32(-45483).build();
        byte[] result = int32.toByteArray();
        assertEquals(5, result.length);
        assertEquals("0d554effff", HostData.toHexString(result));
    }

    public void testSFixedint32ValueMinus300() {
        SFixedint32 int32 = SFixedint32.newBuilder().setAint32(-300).build();
        byte[] result = int32.toByteArray();
        assertEquals(5, result.length);
        assertEquals("0dd4feffff", HostData.toHexString(result));
    }

    public void testSFixedint32ValueSmallest() {
        SFixedint32 int32 = SFixedint32.newBuilder()
                .setAint32(Integer.MIN_VALUE).build();
        byte[] result = int32.toByteArray();
        assertEquals(5, result.length);
        assertEquals("0d00000080", HostData.toHexString(result));
    }

    public void testHierarchy() {
        CustomerData customerData = CustomerData
                .newBuilder()
                .setChild1(CustomerData.Child1.newBuilder().setChild2("child2"))
                .setChild3("child3").build();
        byte[] result = customerData.toByteArray();
        assertEquals(18, result.length);
        assertEquals("0a080a066368696c643212066368696c6433",
                HostData.toHexString(result));
    }

}
