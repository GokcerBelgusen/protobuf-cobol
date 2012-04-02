package com.legstar.protobuf.cobol;

import com.legstar.cobol.model.CobolDataItem;

public class ProtoCobolDataItemTest extends AbstractTest {

    /** True when references should be created. */
    private static final boolean CREATE_REFERENCES = false;

    public boolean isCreateReferences() {
        return CREATE_REFERENCES;
    }

    public void testMaxStringSizeOnEmptyGroup() {
        CobolDataItem cobolDataItem = new CobolDataItem(1, "CUSTOMER-DATA");
        ProtoCobolDataItem protoCobolDataItem = new ProtoCobolDataItem(
                cobolDataItem);
        assertEquals(0, protoCobolDataItem.getMaxStringSize());
    }

    public void testMaxStringSizeOnGroupWithSingleChild() {
        CobolDataItem cobolDataItem = new CobolDataItem(1, "CUSTOMER-DATA");
        CobolDataItem childDataDataItem = new CobolDataItem(5, "CUSTOMER-NAME");
        childDataDataItem.setPicture("X");
        cobolDataItem.getChildren().add(childDataDataItem);
        ProtoCobolDataItem protoCobolDataItem = new ProtoCobolDataItem(
                cobolDataItem);
        assertEquals(1, protoCobolDataItem.getMaxStringSize());
    }

    public void testMaxStringSizeOnShortStringValue() {
        CobolDataItem cobolDataItem = new CobolDataItem(1, "CUSTOMER-DATA");
        cobolDataItem.setPicture("X(11)");
        ProtoCobolDataItem protoCobolDataItem = new ProtoCobolDataItem(
                cobolDataItem);
        assertEquals(11, protoCobolDataItem.getMaxStringSize());
    }

    public void tesMaxStringSizeOnHierarchy() {
        CobolDataItem cobolDataItem = new CobolDataItem(1, "CUSTOMER-DATA");
        CobolDataItem childDataItem1 = new CobolDataItem(5, "CHILD1");
        CobolDataItem childDataItem3 = new CobolDataItem(10, "CHILD3");
        childDataItem3.setPicture("X(72)");
        childDataItem1.getChildren().add(childDataItem3);
        cobolDataItem.getChildren().add(childDataItem1);
        CobolDataItem childDataItem2 = new CobolDataItem(5, "CHILD2");
        childDataItem2.setPicture("X(11)");
        cobolDataItem.getChildren().add(childDataItem2);
        ProtoCobolDataItem protoCobolDataItem = new ProtoCobolDataItem(
                cobolDataItem);
        assertEquals(72, protoCobolDataItem.getMaxStringSize());
    }

    public void testSubStructuresCobolNameOnEmptyGroup() {
        CobolDataItem cobolDataItem = new CobolDataItem(1, "CUSTOMER-DATA");
        ProtoCobolDataItem protoCobolDataItem = new ProtoCobolDataItem(
                cobolDataItem);
        assertEquals("[]", protoCobolDataItem.getSubStructuresCobolName()
                .toString());
    }

    public void testSubStructuresCobolNameOnGroupWithSingleChild() {
        CobolDataItem cobolDataItem = new CobolDataItem(1, "CUSTOMER-DATA");
        CobolDataItem childDataDataItem = new CobolDataItem(5, "CUSTOMER-NAME");
        childDataDataItem.setPicture("X");
        cobolDataItem.getChildren().add(childDataDataItem);
        ProtoCobolDataItem protoCobolDataItem = new ProtoCobolDataItem(
                cobolDataItem);
        assertEquals("[]", protoCobolDataItem.getSubStructuresCobolName()
                .toString());
    }

    public void testSubStructuresCobolNameOnHierarchy() {
        CobolDataItem cobolDataItem = new CobolDataItem(1, "CUSTOMER-DATA");
        CobolDataItem childDataItem1 = new CobolDataItem(5, "CHILD1");
        CobolDataItem childDataItem3 = new CobolDataItem(10, "CHILD3");
        childDataItem3.setPicture("X(72)");
        childDataItem1.getChildren().add(childDataItem3);
        cobolDataItem.getChildren().add(childDataItem1);
        CobolDataItem childDataItem2 = new CobolDataItem(5, "CHILD2");
        childDataItem2.setPicture("X(11)");
        cobolDataItem.getChildren().add(childDataItem2);
        ProtoCobolDataItem protoCobolDataItem = new ProtoCobolDataItem(
                cobolDataItem);
        assertEquals("[CHILD1]", protoCobolDataItem.getSubStructuresCobolName()
                .toString());
    }

    public void testIndexedCobolNamesOnEmptyGroup() {
        CobolDataItem cobolDataItem = new CobolDataItem(1, "CUSTOMER-DATA");
        ProtoCobolDataItem protoCobolDataItem = new ProtoCobolDataItem(
                cobolDataItem);
        assertEquals("[]", protoCobolDataItem.getAllCobolCounterNames()
                .toString());
    }

    public void testIndexedCobolNamesOnChildArray() {
        CobolDataItem cobolDataItem = new CobolDataItem(1, "CUSTOMER-DATA");
        CobolDataItem childDataItem1 = new CobolDataItem(5, "CHILD1");
        childDataItem1.setMinOccurs(10);
        childDataItem1.setMaxOccurs(10);
        childDataItem1.setPicture("X(72)");
        cobolDataItem.getChildren().add(childDataItem1);
        ProtoCobolDataItem protoCobolDataItem = new ProtoCobolDataItem(
                cobolDataItem);
        assertEquals("[W-CHILD1-I]", protoCobolDataItem
                .getAllCobolCounterNames().toString());
    }

    public void testIndexedCobolNamesOnChildOfGroupArray() {
        CobolDataItem cobolDataItem = new CobolDataItem(1, "CUSTOMER-DATA");
        cobolDataItem.setMinOccurs(0);
        cobolDataItem.setMaxOccurs(10);
        cobolDataItem.setDependingOn("SOME-COUNTER");
        CobolDataItem childDataItem1 = new CobolDataItem(5, "CHILD1");
        childDataItem1.setPicture("X(72)");
        cobolDataItem.getChildren().add(childDataItem1);
        ProtoCobolDataItem protoCobolDataItem = new ProtoCobolDataItem(
                cobolDataItem);
        assertEquals("[SOME-COUNTER]", protoCobolDataItem.getCobolCounters()
                .toString());
    }

    public void testIndexedCobolNamesOnAncestry() {
        CobolDataItem cobolDataItem = new CobolDataItem(1, "CUSTOMER-DATA");
        cobolDataItem.setMinOccurs(10);
        cobolDataItem.setMaxOccurs(10);
        CobolDataItem childDataItem1 = new CobolDataItem(5, "CHILD1");
        cobolDataItem.getChildren().add(childDataItem1);
        CobolDataItem childDataItem2 = new CobolDataItem(10, "CHILD2");
        childDataItem2.setPicture("X(72)");
        childDataItem1.getChildren().add(childDataItem2);
        ProtoCobolDataItem protoCobolDataItem = new ProtoCobolDataItem(
                cobolDataItem);
        assertEquals("[W-CUSTOMER-DATA-I]", protoCobolDataItem
                .getCobolCounters().toString());
    }

    public void testListDependingOnsOnEmptyGroup() {
        CobolDataItem cobolDataItem = new CobolDataItem(1, "CUSTOMER-DATA");
        ProtoCobolDataItem protoCobolDataItem = new ProtoCobolDataItem(
                cobolDataItem);
        assertEquals("[]", protoCobolDataItem.getDependingOns().toString());
    }

    public void testListDependingOnsOnChildArray() {
        CobolDataItem cobolDataItem = new CobolDataItem(1, "CUSTOMER-DATA");
        CobolDataItem childDataItem1 = new CobolDataItem(5, "CHILD1");
        childDataItem1.setMinOccurs(0);
        childDataItem1.setMaxOccurs(10);
        childDataItem1.setDependingOn("SOME-CHILD");
        childDataItem1.setPicture("X(72)");
        cobolDataItem.getChildren().add(childDataItem1);
        ProtoCobolDataItem protoCobolDataItem = new ProtoCobolDataItem(
                cobolDataItem);
        assertEquals("[SOME-CHILD]", protoCobolDataItem.getDependingOns()
                .toString());
    }

    public void testListDependingOnsOnAncestry() {
        CobolDataItem cobolDataItem = new CobolDataItem(1, "CUSTOMER-DATA");
        CobolDataItem childDataItem1 = new CobolDataItem(5, "CHILD1");
        childDataItem1.setMinOccurs(0);
        childDataItem1.setMaxOccurs(10);
        childDataItem1.setDependingOn("SOME-CHILD");
        cobolDataItem.getChildren().add(childDataItem1);
        CobolDataItem childDataItem2 = new CobolDataItem(10, "CHILD2");
        childDataItem2.setPicture("X(72)");
        childDataItem1.getChildren().add(childDataItem2);
        CobolDataItem childDataItem3 = new CobolDataItem(5, "CHILD3");
        childDataItem3.setMinOccurs(0);
        childDataItem3.setMaxOccurs(10);
        childDataItem3.setDependingOn("SOME-OTHER-CHILD");
        cobolDataItem.getChildren().add(childDataItem3);
        ProtoCobolDataItem protoCobolDataItem = new ProtoCobolDataItem(
                cobolDataItem);
        assertEquals("[SOME-CHILD, SOME-OTHER-CHILD]", protoCobolDataItem
                .getDependingOns().toString());
    }

    public void testProgramNamePrefix() {
        CobolDataItem cobolDataItem = new CobolDataItem(1, "CUSTOMER-DATA");
        ProtoCobolDataItem protoCobolDataItem = new ProtoCobolDataItem(
                cobolDataItem);
        assertEquals("CUSTDAT", protoCobolDataItem.getProgramNamePrefix());

        protoCobolDataItem.setCobolName("SOME-OTHER-CHILD");
        assertEquals("SOMOTCH", protoCobolDataItem.getProgramNamePrefix());

        protoCobolDataItem.setCobolName("SOME-OTHER-CHI");
        assertEquals("SOMOTCH", protoCobolDataItem.getProgramNamePrefix());

        protoCobolDataItem.setCobolName("SOME-OTHER-CH");
        assertEquals("SOMOTCH", protoCobolDataItem.getProgramNamePrefix());

        protoCobolDataItem.setCobolName("SOME-OTHER-C");
        assertEquals("SOMEOTC", protoCobolDataItem.getProgramNamePrefix());

        protoCobolDataItem.setCobolName("S-OTHER-CHILD");
        assertEquals("SOTHECH", protoCobolDataItem.getProgramNamePrefix());

        protoCobolDataItem
                .setCobolName("SOME-OTHER-CHILD-AND-YET-SOME-OTHER-CHILD");
        assertEquals("SOCAYSO", protoCobolDataItem.getProgramNamePrefix());

        protoCobolDataItem.setCobolName("SOME-OTHER-CHILD-AND-YET-SOME-OTHER");
        assertEquals("SOCAYSO", protoCobolDataItem.getProgramNamePrefix());

        protoCobolDataItem.setCobolName("SOME-OTHER-CHILD-AND-YET-SOME");
        assertEquals("SOOCAYS", protoCobolDataItem.getProgramNamePrefix());

        protoCobolDataItem.setCobolName("SOMEOTHER");
        assertEquals("SOMEOTH", protoCobolDataItem.getProgramNamePrefix());

        protoCobolDataItem.setCobolName("SOMEOTH");
        assertEquals("SOMEOTH", protoCobolDataItem.getProgramNamePrefix());

        protoCobolDataItem.setCobolName("SOMEOT");
        assertEquals("SOMEOT", protoCobolDataItem.getProgramNamePrefix());

        protoCobolDataItem.setCobolName("");
        assertEquals("", protoCobolDataItem.getProgramNamePrefix());

    }
}
