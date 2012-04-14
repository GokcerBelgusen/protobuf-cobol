package com.legstar.protobuf.cobol;

import com.legstar.cobol.model.CobolDataItem;
import com.legstar.coxb.CobolUsage.Usage;

public class ProtoCobolGeneratorTest extends AbstractTest {

    /** True when references should be created. */
    private static final boolean CREATE_REFERENCES = false;

    public boolean isCreateReferences() {
        return CREATE_REFERENCES;
    }

    public void testParserEmptyGroup() {
        CobolDataItem cobolDataItem = new CobolDataItem(1, "CUSTOMER-DATA");
        ProtoCobolDataItem protoCobolDataItem = new ProtoCobolDataItem(
                cobolDataItem);
        check(ProtoCobolGenerator.generateParser(protoCobolDataItem));
    }

    public void testParserHierarchy() {
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
        check(ProtoCobolGenerator.generateParser(protoCobolDataItem));
    }

    public void testParserArrays() {
        CobolDataItem cobolDataItem = new CobolDataItem(1, "CUSTOMER-DATA");
        cobolDataItem.setMinOccurs(10);
        cobolDataItem.setMaxOccurs(10);
        CobolDataItem childDataItem1 = new CobolDataItem(5, "CHILD1");
        cobolDataItem.getChildren().add(childDataItem1);
        CobolDataItem childDataItem2 = new CobolDataItem(10, "CHILD2");
        childDataItem2.setPicture("X(72)");
        childDataItem1.getChildren().add(childDataItem2);
        CobolDataItem childDataItem3 = new CobolDataItem(10, "CHILD3");
        childDataItem3.setPicture("9(4)");
        childDataItem1.getChildren().add(childDataItem3);
        ProtoCobolDataItem protoCobolDataItem = new ProtoCobolDataItem(
                cobolDataItem);
        check(ProtoCobolGenerator.generateParser(protoCobolDataItem));
    }

    public void testParserIndexedFieldsWithDependingOns() {
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
        check(ProtoCobolGenerator.generateParser(protoCobolDataItem));
    }

    public void testParserMixedElementaryTypes() {
        CobolDataItem cobolDataItem = new CobolDataItem(1, "CUSTOMER-DATA");
        CobolDataItem childDataItemBool = new CobolDataItem(5, "BOOL");
        childDataItemBool.setPicture("9(1)");
        cobolDataItem.getChildren().add(childDataItemBool);

        CobolDataItem childDataItemString = new CobolDataItem(5, "ALPHANUM");
        childDataItemString.setPicture("X(10)");
        cobolDataItem.getChildren().add(childDataItemString);

        CobolDataItem childDataItemDouble = new CobolDataItem(5, "DOUBLE");
        childDataItemDouble.setUsage(Usage.DOUBLEFLOAT);
        cobolDataItem.getChildren().add(childDataItemDouble);

        CobolDataItem childDataItemEnum = new CobolDataItem(5, "ENUM");
        childDataItemEnum.setPicture("9(4)");
        cobolDataItem.getChildren().add(childDataItemEnum);

        CobolDataItem childDataItemFloat = new CobolDataItem(5, "FLOAT");
        childDataItemFloat.setUsage(Usage.SINGLEFLOAT);
        cobolDataItem.getChildren().add(childDataItemFloat);

        CobolDataItem childDataItemInt32 = new CobolDataItem(5, "INT32");
        childDataItemInt32.setPicture("S9(9)");
        cobolDataItem.getChildren().add(childDataItemInt32);

        CobolDataItem childDataItemInt64 = new CobolDataItem(5, "INT64");
        childDataItemInt64.setPicture("S9(18)");
        cobolDataItem.getChildren().add(childDataItemInt64);

        CobolDataItem childDataItemUint32 = new CobolDataItem(5, "UINT32");
        childDataItemUint32.setPicture("9(9)");
        cobolDataItem.getChildren().add(childDataItemUint32);

        CobolDataItem childDataItemUint64 = new CobolDataItem(5, "UINT64");
        childDataItemUint64.setPicture("9(18)");
        cobolDataItem.getChildren().add(childDataItemUint64);

        ProtoCobolDataItem protoCobolDataItem = new ProtoCobolDataItem(
                cobolDataItem);
        check(ProtoCobolGenerator.generateParser(protoCobolDataItem));
    }

    public void testWriterEmptyGroup() {
        CobolDataItem cobolDataItem = new CobolDataItem(1, "CUSTOMER-DATA");
        ProtoCobolDataItem protoCobolDataItem = new ProtoCobolDataItem(
                cobolDataItem);
        check(ProtoCobolGenerator.generateWriter(protoCobolDataItem));
    }

}
