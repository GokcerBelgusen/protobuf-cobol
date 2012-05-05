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

import com.google.protobuf.Descriptors.FieldDescriptor.Type;
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
        CobolDataItem childDataItem2 = new CobolDataItem(5, "CHILD2");
        childDataItem2.setPicture("X(11)");
        CobolDataItem childDataItem3 = new CobolDataItem(10, "CHILD3");
        childDataItem3.setPicture("X(72)");

        ProtoCobolDataItem protoCobolDataItem = new ProtoCobolDataItem(
                cobolDataItem);
        ProtoCobolDataItem protoCobolDataItem1 = new ProtoCobolDataItem(
                childDataItem1, Type.MESSAGE, protoCobolDataItem);
        protoCobolDataItem.addChild(protoCobolDataItem1);

        ProtoCobolDataItem protoCobolDataItem2 = new ProtoCobolDataItem(
                childDataItem2, Type.STRING, protoCobolDataItem);
        protoCobolDataItem.addChild(protoCobolDataItem2);

        ProtoCobolDataItem protoCobolDataItem3 = new ProtoCobolDataItem(
                childDataItem3, Type.STRING, protoCobolDataItem1);
        protoCobolDataItem1.addChild(protoCobolDataItem3);

        check(ProtoCobolGenerator.generateParser(protoCobolDataItem));
    }

    public void testParserArrays() {
        CobolDataItem cobolDataItem = new CobolDataItem(1, "CUSTOMER-DATA");
        cobolDataItem.setMinOccurs(10);
        cobolDataItem.setMaxOccurs(10);
        CobolDataItem cobolDataItem1 = new CobolDataItem(5, "CHILD1");
        CobolDataItem cobolDataItem2 = new CobolDataItem(10, "CHILD2");
        cobolDataItem2.setPicture("X(72)");
        CobolDataItem cobolDataItem3 = new CobolDataItem(10, "CHILD3");
        cobolDataItem3.setPicture("9(4)");

        ProtoCobolDataItem protoCobolDataItem = new ProtoCobolDataItem(
                cobolDataItem);
        ProtoCobolDataItem protoCobolDataItem1 = new ProtoCobolDataItem(
                cobolDataItem1, Type.MESSAGE, protoCobolDataItem);
        protoCobolDataItem.addChild(protoCobolDataItem1);

        ProtoCobolDataItem protoCobolDataItem2 = new ProtoCobolDataItem(
                cobolDataItem2, Type.STRING, protoCobolDataItem1);
        protoCobolDataItem1.addChild(protoCobolDataItem2);

        ProtoCobolDataItem protoCobolDataItem3 = new ProtoCobolDataItem(
                cobolDataItem3, Type.UINT32, protoCobolDataItem1);
        protoCobolDataItem1.addChild(protoCobolDataItem3);

        check(ProtoCobolGenerator.generateParser(protoCobolDataItem));
    }

    public void testParserIndexedFieldsWithDependingOns() {
        CobolDataItem cobolDataItem = new CobolDataItem(1, "CUSTOMER-DATA");
        CobolDataItem cobolDataItem1 = new CobolDataItem(5, "CHILD1");
        cobolDataItem1.setMinOccurs(0);
        cobolDataItem1.setMaxOccurs(10);
        cobolDataItem1.setDependingOn("SOME-CHILD");
        CobolDataItem cobolDataItem2 = new CobolDataItem(10, "CHILD2");
        cobolDataItem2.setPicture("X(72)");
        CobolDataItem cobolDataItem3 = new CobolDataItem(5, "CHILD3");
        cobolDataItem3.setMinOccurs(0);
        cobolDataItem3.setMaxOccurs(10);
        cobolDataItem3.setDependingOn("SOME-OTHER-CHILD");

        ProtoCobolDataItem protoCobolDataItem = new ProtoCobolDataItem(
                cobolDataItem);
        ProtoCobolDataItem protoCobolDataItem1 = new ProtoCobolDataItem(
                cobolDataItem1, Type.MESSAGE, protoCobolDataItem);
        protoCobolDataItem.addChild(protoCobolDataItem1);

        ProtoCobolDataItem protoCobolDataItem2 = new ProtoCobolDataItem(
                cobolDataItem2, Type.STRING, protoCobolDataItem1);
        protoCobolDataItem1.addChild(protoCobolDataItem2);

        ProtoCobolDataItem protoCobolDataItem3 = new ProtoCobolDataItem(
                cobolDataItem3, Type.MESSAGE, protoCobolDataItem);
        protoCobolDataItem.addChild(protoCobolDataItem3);

        check(ProtoCobolGenerator.generateParser(protoCobolDataItem));
    }

    public void testParserMixedElementaryTypes() {
        CobolDataItem cobolDataItem = new CobolDataItem(1, "CUSTOMER-DATA");

        CobolDataItem childDataItemBool = new CobolDataItem(5, "BOOL");
        childDataItemBool.setPicture("9(1)");

        CobolDataItem childDataItemString = new CobolDataItem(5, "ALPHANUM");
        childDataItemString.setPicture("X(10)");

        CobolDataItem childDataItemDouble = new CobolDataItem(5, "DOUBLE");
        childDataItemDouble.setUsage(Usage.DOUBLEFLOAT);

        CobolDataItem childDataItemEnum = new CobolDataItem(5, "ENUM");
        childDataItemEnum.setPicture("9(4)");

        CobolDataItem childDataItemFloat = new CobolDataItem(5, "FLOAT");
        childDataItemFloat.setUsage(Usage.SINGLEFLOAT);

        CobolDataItem childDataItemInt32 = new CobolDataItem(5, "INT32");
        childDataItemInt32.setPicture("S9(9)");

        CobolDataItem childDataItemInt64 = new CobolDataItem(5, "INT64");
        childDataItemInt64.setPicture("S9(18)");

        CobolDataItem childDataItemUint32 = new CobolDataItem(5, "UINT32");
        childDataItemUint32.setPicture("9(9)");

        CobolDataItem childDataItemUint64 = new CobolDataItem(5, "UINT64");
        childDataItemUint64.setPicture("9(18)");

        CobolDataItem childDataItemSint32 = new CobolDataItem(5, "SINT32");
        childDataItemSint32.setPicture("S9(9)");

        CobolDataItem childDataItemSint64 = new CobolDataItem(5, "SINT64");
        childDataItemSint64.setPicture("S9(18)");

        CobolDataItem childDataItemFixed32 = new CobolDataItem(5, "FIXED32");
        childDataItemFixed32.setPicture("9(9)");

        CobolDataItem childDataItemFixed64 = new CobolDataItem(5, "FIXED64");
        childDataItemFixed64.setPicture("9(18)");

        CobolDataItem childDataItemSfixed32 = new CobolDataItem(5, "SFIXED32");
        childDataItemSfixed32.setPicture("S9(9)");

        CobolDataItem childDataItemSfixed64 = new CobolDataItem(5, "SFIXED64");
        childDataItemSfixed64.setPicture("S9(18)");

        ProtoCobolDataItem protoCobolDataItem = new ProtoCobolDataItem(
                cobolDataItem);

        ProtoCobolDataItem protoCobolDataItem1 = new ProtoCobolDataItem(
                childDataItemBool, Type.BOOL, protoCobolDataItem);
        protoCobolDataItem.addChild(protoCobolDataItem1);

        ProtoCobolDataItem protoCobolDataItem2 = new ProtoCobolDataItem(
                childDataItemString, Type.STRING, protoCobolDataItem);
        protoCobolDataItem.addChild(protoCobolDataItem2);

        ProtoCobolDataItem protoCobolDataItem3 = new ProtoCobolDataItem(
                childDataItemDouble, Type.DOUBLE, protoCobolDataItem);
        protoCobolDataItem.addChild(protoCobolDataItem3);

        ProtoCobolDataItem protoCobolDataItem4 = new ProtoCobolDataItem(
                childDataItemEnum, Type.ENUM, protoCobolDataItem);
        protoCobolDataItem.addChild(protoCobolDataItem4);

        ProtoCobolDataItem protoCobolDataItem5 = new ProtoCobolDataItem(
                childDataItemFloat, Type.FLOAT, protoCobolDataItem);
        protoCobolDataItem.addChild(protoCobolDataItem5);

        ProtoCobolDataItem protoCobolDataItem6 = new ProtoCobolDataItem(
                childDataItemInt32, Type.INT32, protoCobolDataItem);
        protoCobolDataItem.addChild(protoCobolDataItem6);

        ProtoCobolDataItem protoCobolDataItem7 = new ProtoCobolDataItem(
                childDataItemInt64, Type.INT64, protoCobolDataItem);
        protoCobolDataItem.addChild(protoCobolDataItem7);

        ProtoCobolDataItem protoCobolDataItem8 = new ProtoCobolDataItem(
                childDataItemUint32, Type.UINT32, protoCobolDataItem);
        protoCobolDataItem.addChild(protoCobolDataItem8);

        ProtoCobolDataItem protoCobolDataItem9 = new ProtoCobolDataItem(
                childDataItemUint64, Type.UINT64, protoCobolDataItem);
        protoCobolDataItem.addChild(protoCobolDataItem9);

        ProtoCobolDataItem protoCobolDataItem10 = new ProtoCobolDataItem(
                childDataItemSint32, Type.SINT32, protoCobolDataItem);
        protoCobolDataItem.addChild(protoCobolDataItem10);

        ProtoCobolDataItem protoCobolDataItem11 = new ProtoCobolDataItem(
                childDataItemSint64, Type.SINT64, protoCobolDataItem);
        protoCobolDataItem.addChild(protoCobolDataItem11);

        ProtoCobolDataItem protoCobolDataItem12 = new ProtoCobolDataItem(
                childDataItemFixed32, Type.FIXED32, protoCobolDataItem);
        protoCobolDataItem.addChild(protoCobolDataItem12);

        ProtoCobolDataItem protoCobolDataItem13 = new ProtoCobolDataItem(
                childDataItemFixed64, Type.FIXED64, protoCobolDataItem);
        protoCobolDataItem.addChild(protoCobolDataItem13);

        ProtoCobolDataItem protoCobolDataItem14 = new ProtoCobolDataItem(
                childDataItemSfixed32, Type.SFIXED32, protoCobolDataItem);
        protoCobolDataItem.addChild(protoCobolDataItem14);

        ProtoCobolDataItem protoCobolDataItem15 = new ProtoCobolDataItem(
                childDataItemSfixed64, Type.SFIXED64, protoCobolDataItem);
        protoCobolDataItem.addChild(protoCobolDataItem15);

        check(ProtoCobolGenerator.generateParser(protoCobolDataItem));
    }

    public void testWriterEmptyGroup() {
        CobolDataItem cobolDataItem = new CobolDataItem(1, "CUSTOMER-DATA");
        ProtoCobolDataItem protoCobolDataItem = new ProtoCobolDataItem(
                cobolDataItem);
        check(ProtoCobolGenerator.generateWriter(protoCobolDataItem));
    }

}
