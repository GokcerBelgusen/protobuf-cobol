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

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.EnumDescriptor;
import com.google.protobuf.Descriptors.EnumValueDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType;
import com.google.protobuf.Descriptors.FieldDescriptor.Type;
import com.legstar.cobol.gen.CobolNameResolver;
import com.legstar.cobol.model.CobolDataItem;
import com.legstar.coxb.CobolUsage.Usage;

/**
 * Maps between protocol buffer types and COBOL types.
 * <p/>
 * TODO values
 * 
 */
public class ProtoCobolMapper {

    /** The default maximum number of characters for a string. */
    private static final int DEFAULT_STRING_MAX_SIZE = 32;

    /** The default maximum number of occurrences for an array. */
    private static final int DEFAULT_MAX_OCCURS = 10;

    /** The default COBOL level for root items. */
    private static final int DEFAULT_ROOT_LEVEL = 1;

    /** The default increment for COBOL levels. */
    private static final int DEFAULT_LEVEL_INCREMENT = 2;

    /** COBOL level for conditions. */
    private static final int COBOL_CONDITION_LEVEL = 88;

    /** Picture clause for a 16 bits unsigned integer. */
    public static final String PICTURE_UINT16 = "9(4)";

    /** Picture clause for a 32 bits unsigned integer. */
    public static final String PICTURE_UINT32 = "9(9)";

    /** Picture clause for a 32 bits signed integer. */
    public static final String PICTURE_SINT32 = "S9(9)";

    /** Picture clause for a 64 bits unsigned integer. */
    public static final String PICTURE_UINT64 = "9(18)";

    /** Picture clause for a 64 bits signed integer. */
    public static final String PICTURE_SINT64 = "S9(18)";

    /**
     * The set of counters associated with variable size arrays are added to the
     * final COBOL structure under this group name
     */
    public static final String OCCURS_COUNTERS_GROUP_NAME = "OCCURS-COUNTERS--C";

    /**
     * Dynamic counters also need a cobol name which is built from the
     * corresponding list or array cobol name plus this suffix.
     */
    private static final String COUNTER_COBOL_SUFFIX = "--C";

    /**
     * Dynamic counters COBOL picture clause.
     */
    private static final String COUNTER_COBOL_PICTURE = PICTURE_UINT32;

    /**
     * Dynamic counters COBOL usage clause.
     */
    private static final Usage COUNTER_COBOL_USAGE = Usage.NATIVEBINARY;

    public CobolNameResolver nameResolver;

    /** List of size providers. */
    private List < HasMaxSize > sizeProviders = new ArrayList < HasMaxSize >();

    /**
     * Map a root message to a COBOL data item.
     * <p/>
     * While processing this message fields, we might encounter variable size
     * arrays for which we need to create counters which are additional COBOL
     * numeric fields that hold the current array count. These counters are
     * COBOL only items, they don't map any protocol buffer field
     * <p/>
     * Arrays within arrays in COBOL translate to multidimensional arrays. We
     * use a Stack to keep track of these dimensions.
     * 
     * @param messageDescriptor the message descriptor
     * @return the corresponding decorated COBOL data item
     */
    public ProtoCobolDataItem toCobol(final Descriptor messageDescriptor) {
        nameResolver = new CobolNameResolver();
        CobolDataItem occursCounters = new CobolDataItem(DEFAULT_ROOT_LEVEL
                + DEFAULT_LEVEL_INCREMENT, OCCURS_COUNTERS_GROUP_NAME);
        ProtoCobolDataItem rootDataItem = toCobol(messageDescriptor,
                DEFAULT_ROOT_LEVEL, false, occursCounters, null);
        if (occursCounters.getChildren().size() > 0) {
            rootDataItem.getCobolDataItem().getChildren()
                    .add(0, occursCounters);
        }
        return rootDataItem;
    }

    /**
     * Map a message to a COBOL data item.
     * 
     * @param messageDescriptor the message descriptor
     * @param level of this message in the parent message (1 if root)
     * @param isRepeated true if this message repeats itself
     * @param occursCounters a set of counters for variable size arrays
     * @param decoratedParent the parent in the hierarchy or null if root
     * @return the corresponding decorated COBOL data item
     */
    public ProtoCobolDataItem toCobol(final Descriptor messageDescriptor,
            final int level, boolean isRepeated, CobolDataItem occursCounters,
            final ProtoCobolDataItem decoratedParent) {

        CobolDataItem cobolDataItem = new CobolDataItem(level,
                nameToCobol(messageDescriptor.getName()));
        if (isRepeated) {
            repeatedToOccurs(messageDescriptor.getName(), JavaType.MESSAGE,
                    cobolDataItem, occursCounters,
                    decoratedParent.getCobolCounters());
        }
        ProtoCobolDataItem decoratedDataItem = new ProtoCobolDataItem(
                cobolDataItem, Type.MESSAGE, decoratedParent);

        for (FieldDescriptor fd : messageDescriptor.getFields()) {
            ProtoCobolDataItem decoratedChild = toCobol(fd, level
                    + DEFAULT_LEVEL_INCREMENT, occursCounters,
                    decoratedDataItem);
            decoratedDataItem.addChild(decoratedChild);
        }

        return decoratedDataItem;
    }

    /**
     * Map a field to a COBOL data item.
     * 
     * @param fieldDescriptor the field descriptor
     * @param level the level of that field
     * @param occursCounters a set of counters for variable size arrays
     * @param decoratedParent the parent in the hierarchy or null if root
     * @return the corresponding COBOL data item
     */
    public ProtoCobolDataItem toCobol(final FieldDescriptor fieldDescriptor,
            final int level, CobolDataItem occursCounters,
            final ProtoCobolDataItem decoratedParent) {

        ProtoCobolDataItem decoratedDataItem = null;
        if (isMessage(fieldDescriptor)) {
            decoratedDataItem = toCobol(fieldDescriptor.getMessageType(),
                    level, fieldDescriptor.isRepeated(), occursCounters,
                    decoratedParent);
        } else {
            CobolDataItem cobolDataItem = new CobolDataItem(level,
                    nameToCobol(fieldDescriptor.getName()));
            cobolDataItem.setPicture(typeToPicture(fieldDescriptor.getName(),
                    fieldDescriptor.getType()));
            cobolDataItem.setUsage(typeToUsage(fieldDescriptor.getName(),
                    fieldDescriptor.getType()));
            if (fieldDescriptor.getType().equals(Type.ENUM)) {
                cobolDataItem.getChildren().addAll(
                        enumToCobol(fieldDescriptor.getEnumType()));
            }
            if (fieldDescriptor.isRepeated()) {
                repeatedToOccurs(fieldDescriptor.getName(),
                        fieldDescriptor.getJavaType(), cobolDataItem,
                        occursCounters, decoratedParent.getCobolCounters());
            }
            decoratedDataItem = new ProtoCobolDataItem(cobolDataItem,
                    fieldDescriptor.getType(), decoratedParent);
        }
        return decoratedDataItem;
    }

    /**
     * Create a COBOL compatible unique name from a protocol buffer name.
     * 
     * @param fieldName the field name
     * @return a COBOL compatible unique name
     */
    public String nameToCobol(String fieldName) {
        if (fieldName == null) {
            return "FILLER";
        } else {
            return nameResolver.getUniqueName(fieldName);
        }
    }

    /**
     * Map a java type to a COBOL PICTURE clause.
     * <p/>
     * For the moment, we try to map protobuf types to unique pictures so that
     * the wire format can be inferred from the picture clause at runtime. This
     * is weak and reduces the types we can support.
     * <p/>
     * We don't support:
     * <ul>
     * <li>sint32</li>
     * <li>sint64</li>
     * <li>fixed64</li>
     * <li>sfixed64</li>
     * <li>fixed32</li>
     * <li>sfixed32</li>
     * <li>bytes</li>
     * </ul>
     * 
     * @param fieldName the field name
     * @param fieldType the field type
     * @return the COBOL PICTURE clause
     */
    public String typeToPicture(String fieldName, Type fieldType) {
        switch (fieldType) {
        case BOOL:
            return PICTURE_UINT16;
        case STRING:
            return "X(" + getMaxSize(fieldName, fieldType) + ")";
        case DOUBLE:
            return null;
        case ENUM:
            return PICTURE_UINT16;
        case FLOAT:
            return null;
        case INT32:
            return PICTURE_SINT32;
        case INT64:
            return PICTURE_SINT64;
        case SINT32:
            return PICTURE_SINT32;
        case SINT64:
            return PICTURE_SINT64;
        case FIXED32:
            return PICTURE_UINT32;
        case FIXED64:
            return PICTURE_UINT64;
        case SFIXED32:
            return PICTURE_SINT32;
        case SFIXED64:
            return PICTURE_SINT64;
        case UINT32:
            return PICTURE_UINT32;
        case UINT64:
            return PICTURE_UINT64;
        case MESSAGE:
            return null;
        default:
            throw new UnsupportedOperationException("Unsupported field type "
                    + fieldType + " for " + fieldName);
        }
    }

    /**
     * Map a java type to a COBOL USAGE clause.
     * 
     * @param fieldName the field name
     * @param fieldType the fild type
     * @return the COBOL USAGE clause
     */
    public Usage typeToUsage(String fieldName, Type fieldType) {
        switch (fieldType) {
        case BOOL:
            return Usage.BINARY;
        case STRING:
            return Usage.DISPLAY;
        case DOUBLE:
            return Usage.DOUBLEFLOAT;
        case ENUM:
            return Usage.NATIVEBINARY;
        case FLOAT:
            return Usage.SINGLEFLOAT;
        case INT32:
        case INT64:
        case SINT32:
        case SINT64:
        case FIXED32:
        case FIXED64:
        case SFIXED32:
        case SFIXED64:
        case UINT32:
        case UINT64:
            return Usage.NATIVEBINARY;
        case MESSAGE:
            return null;
        default:
            throw new UnsupportedOperationException("Unsupported field type "
                    + fieldType + " for " + fieldName);
        }
    }

    /**
     * Translates a picture/usage back to a protobuf type.
     * <p/>
     * Again, this assumes there is a one to one relationship between the COBOL
     * picture/usage clause and the protobuf type which limits the number of
     * types we support. See typeToPicture.
     * 
     * @param picture the COBOL picture clause
     * @param usage the COBOL usage clause
     * @return the protobuf type
     */
    public static Type pictureToType(String picture, Usage usage) {
        if (picture == null) {
            if (usage == null) {
                return Type.MESSAGE;
            } else if (usage == Usage.SINGLEFLOAT) {
                return Type.FLOAT;
            } else if (usage == Usage.DOUBLEFLOAT) {
                return Type.DOUBLE;
            } else {
                throw new UnsupportedOperationException("Unsupported usage "
                        + usage);
            }
        } else if (picture.equals(PICTURE_UINT16)) {
            return Type.BOOL;
        } else if (picture.startsWith("X")) {
            return Type.STRING;
        } else if (picture.equals(PICTURE_UINT16)) {
            return Type.ENUM;
        } else if (picture.equals(PICTURE_SINT32)) {
            return Type.INT32;
        } else if (picture.equals(PICTURE_SINT64)) {
            return Type.INT64;
        } else if (picture.equals(PICTURE_UINT32)) {
            return Type.UINT32;
        } else if (picture.equals(PICTURE_UINT64)) {
            return Type.UINT64;
        } else {
            throw new UnsupportedOperationException("Unsupported picture "
                    + picture + " and usage " + usage);
        }

    }

    /**
     * Build COBOL array attributes from a repeated field.
     * <p/>
     * There are no fixed size arrays in protobuf, only unlimited variable size
     * ones. The best we can do is translate them into COBOL variable size
     * arrays with a depending on clause.
     * <p/>
     * There is a limitation though: COBOL does not support variable size arrays
     * within variable size arrays because the ODO object cannot be variably
     * located nor can it be an array element. So any array within an array is
     * mapped to a fixed size array.
     * <p/>
     * COBOL mandates that we specify a maximum size for arrays. Here we give a
     * chance to the caller to adjust that maximum or use a default.
     * 
     * @param fieldName the protobuf field name
     * @param fieldType the java type
     * @param cobolDataItem the COBOL data item being built
     * @param occursCounters a set of counters for variable size arrays
     * @param dimensions one entry per parent array in the hierarchy
     */
    protected void repeatedToOccurs(String fieldName, JavaType fieldType,
            CobolDataItem cobolDataItem, CobolDataItem occursCounters,
            Stack < String > dimensions) {

        cobolDataItem.setMaxOccurs(getMaxOccurs(fieldName, fieldType));
        if (dimensions.empty()) {
            cobolDataItem.setMinOccurs(0);
            CobolDataItem counterDataItem = addCounter(cobolDataItem,
                    occursCounters);
            cobolDataItem.setDependingOn(counterDataItem.getCobolName());
        } else {
            cobolDataItem.setMinOccurs(cobolDataItem.getMaxOccurs());
        }

    }

    /**
     * Adds a new variable size array counter.
     * <p/>
     * A counter is a numeric COBOL data item that holds an array count.
     * 
     * @param cobolDataItem the COBOL array
     * @param occursCounters the group that holds all counters
     * @return the created counter
     */
    protected CobolDataItem addCounter(CobolDataItem cobolDataItem,
            CobolDataItem occursCounters) {
        CobolDataItem counterDataItem = new CobolDataItem(
                occursCounters.getLevelNumber() + DEFAULT_LEVEL_INCREMENT,
                getCounterCobolName(cobolDataItem.getCobolName()));
        counterDataItem.setPicture(COUNTER_COBOL_PICTURE);
        counterDataItem.setUsage(COUNTER_COBOL_USAGE);
        occursCounters.getChildren().add(counterDataItem);
        return counterDataItem;
    }

    /**
     * Dynamic counters need a unique Cobol name. This method determines such a
     * name based on the related array or list cobol name.
     * 
     * 
     * @param cobolName cobol name of corresponding list or array
     * @return the proposed counter cobol name
     */
    protected String getCounterCobolName(final String cobolName) {
        if (cobolName.length() < 31 - COUNTER_COBOL_SUFFIX.length()) {
            return cobolName + COUNTER_COBOL_SUFFIX;
        } else {
            return cobolName.substring(0, 30 - COUNTER_COBOL_SUFFIX.length())
                    + COUNTER_COBOL_SUFFIX;
        }
    }

    /**
     * Get the max size from a provider or return the default.
     * 
     * @param fieldName the field name
     * @param fieldType the field type
     * @return the maximum size for this field
     */
    protected int getMaxSize(String fieldName, Type fieldType) {
        Integer maxSize = null;
        for (HasMaxSize provider : sizeProviders) {
            maxSize = provider.getMaxSize(fieldName, fieldType);
            if (maxSize != null) {
                return maxSize;
            }
        }
        return DEFAULT_STRING_MAX_SIZE;
    }

    /**
     * Get the max occurrences from a provider or return the default.
     * 
     * @param fieldName the field name
     * @param fieldType the field type
     * @return the maximum occurrences for this field
     */
    protected int getMaxOccurs(String fieldName, JavaType fieldType) {
        Integer maxOccurs = null;
        for (HasMaxSize provider : sizeProviders) {
            maxOccurs = provider.getMaxOccurs(fieldName, fieldType);
            if (maxOccurs != null) {
                return maxOccurs;
            }
        }
        return DEFAULT_MAX_OCCURS;
    }

    /**
     * @return whether a field is a message
     */
    public boolean isMessage(final FieldDescriptor fieldDescriptor) {
        return fieldDescriptor.getType().equals(FieldDescriptor.Type.MESSAGE);
    }

    /**
     * Callers can implement this interface and listen for requests to size
     * COBOL items that need a maximum size.
     * <p/>
     * Using this interface, caller can prevent the generator from picking the
     * default maximum size
     * 
     */
    public interface HasMaxSize {

        /**
         * Return the maximum size for the corresponding COBOL item.
         * <p/>
         * This is used for COBOL alphanumeric types which are not unlimited. A
         * max size must be specified.
         * <p/>
         * If caller responds with null, then the next listener will be invoked.
         * If no more listeners are available, then the default maximum size is
         * used.
         * 
         * @param fieldName the protocol buffer field name
         * @param fieldType the field's type
         * @return the maximum COBOL size or null to use default
         */
        public Integer getMaxSize(String fieldName, Type fieldType);

        /**
         * For repeated fields, we need a maximum number of occurrences because
         * COBOL does not allow unlimited arrays.
         * 
         * @param fieldName the protocol buffer repeated field name
         * @param fieldType the field's java type
         * @return the maximum number of occurrences or null to use default
         */
        public Integer getMaxOccurs(String fieldName, JavaType fieldType);
    }

    /**
     * Add a new size provider.
     * 
     * @param provider a size provider
     */
    public void addSizeProvider(HasMaxSize provider) {
        sizeProviders.add(provider);
    }

    /**
     * Enum types are translated to COBOL conditions. Each enum value becomes a
     * level 88 condition.
     * 
     * @param enumType the enum type
     * @return a list of level 88 data items
     */
    public List < CobolDataItem > enumToCobol(EnumDescriptor enumType) {
        List < CobolDataItem > conditions = new ArrayList < CobolDataItem >();
        for (EnumValueDescriptor evd : enumType.getValues()) {
            CobolDataItem condition = new CobolDataItem(COBOL_CONDITION_LEVEL,
                    nameToCobol(evd.getName()));
            condition.getConditionLiterals().add(
                    Integer.toString(evd.getNumber()));
            conditions.add(condition);
        }
        return conditions;
    }

}
