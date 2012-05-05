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

import org.apache.commons.lang.StringUtils;

import com.google.protobuf.Descriptors.FieldDescriptor.Type;
import com.legstar.cobol.gen.CopybookGenerator;
import com.legstar.cobol.model.CobolDataItem;
import com.legstar.coxb.CobolUsage.Usage;
import com.legstar.coxb.util.NameUtil;
import com.legstar.coxb.util.PictureUtil;

/**
 * This decorates regular LegStar COBOL data item to provide additional methods
 * made available to templates.
 * <p/>
 * Also keeps track of the mapped protocol buffer field descriptor.
 */
public class ProtoCobolDataItem {

    /**
     * Generated protocol buffer parser file base name will have this suffix.
     */
    private static final String COBOL_MEMBER_PARSER_SUFFIX = "P";

    /**
     * Generated protocol buffer writer file base name will have this suffix.
     */
    private static final String COBOL_MEMBER_WRITER_SUFFIX = "W";

    /** Used to create names for working storage variables. */
    public static final String WORKING_STORAGE_PREFIX = "W-";

    /** Used to build COBOL counter variables. */
    public static final String COBOL_COUNTERS_SUFFIX = "-I";

    /**
     * A prefix that can be used for generated program names.
     */
    private String programNamePrefix;

    /** The COBOL data item we are decorating. */
    private final CobolDataItem cobolDataItem;

    /** The corresponding protocol buffer field type. */
    private final Type pbFieldType;

    /**
     * Reflects the repeating parents which influence the way this data item can
     * be referred to.
     */
    private final Stack < String > cobolCounters = new Stack < String >();

    /** Children are also decorated data items. */
    private List < ProtoCobolDataItem > children = new ArrayList < ProtoCobolDataItem >();

    /**
     * Decorate a COBOL data item for a root protocol buffer message.
     * 
     * @param cobolDataItem the decorated item
     */
    public ProtoCobolDataItem(final CobolDataItem cobolDataItem) {
        this(cobolDataItem, Type.MESSAGE, null);
    }

    /**
     * Decorate a COBOL data item.
     * <p/>
     * When a parent is specified, we get a copy of the parent counters stack
     * and add ourself to it if we are an array.
     * 
     * @param cobolDataItem the decorated item
     * @param pbFieldType the corresponding protocol buffer field type
     * @param decoratedParent the parent of this item (assumes there is only
     *            one)
     */
    public ProtoCobolDataItem(final CobolDataItem cobolDataItem,
            final Type pbFieldType, ProtoCobolDataItem decoratedParent) {
        this.cobolDataItem = cobolDataItem;
        this.pbFieldType = pbFieldType;
        programNamePrefix = createProgramNamePrefix();
        if (decoratedParent != null) {
            cobolCounters.addAll(decoratedParent.getCobolCounters());
        }
        if (isArray()) {
            cobolCounters.push(getCobolCounterName());
        }
    }

    /**
     * Add a child to this item.
     * <p/>
     * There are 2 lists to keep in sync because we want both a pure COBOL
     * hierarchy (for copybook generation) and a decorated hierarchy for
     * protocobol artifacts generation.
     * 
     * @param decoratedChild
     */
    public void addChild(ProtoCobolDataItem decoratedChild) {
        getChildren().add(decoratedChild);
        getCobolDataItem().getChildren().add(decoratedChild.getCobolDataItem());
    }

    /**
     * @param cobolName the Cobol element name to set
     */
    public void setCobolName(final String cobolName) {
        cobolDataItem.setCobolName(cobolName);
        programNamePrefix = createProgramNamePrefix();
    }

    /**
     * @return a COBOL counter variable name for this COBOL item (null if this
     *         is not an array)
     */
    public String getCobolCounterName() {
        if (isArray()) {
            return getCobolCounterName(cobolDataItem.getCobolName());
        } else {
            return null;
        }
    }

    /**
     * @return a COBOL counter variable name for this COBOL item
     */
    protected static String getCobolCounterName(String cobolName) {
        return WORKING_STORAGE_PREFIX + cobolName + COBOL_COUNTERS_SUFFIX;
    }

    /**
     * Generates a valid COBOL program name for the parser program.
     * 
     * @return a valid COBOL program name for the parser program
     */
    public String getParserProgramName() {
        return getProgramNamePrefix() + COBOL_MEMBER_PARSER_SUFFIX;
    }

    /**
     * Generates a valid COBOL program name for the writer program.
     * 
     * @return a valid COBOL program name for the writer program
     */
    public String getWriterProgramName() {
        return getProgramNamePrefix() + COBOL_MEMBER_WRITER_SUFFIX;
    }

    /**
     * Create a valid COBOL program name prefix (an additional 1 character
     * suffix will be added later).
     * <p/>
     * Assuming PGMNAME(COMPAT) compiler option, the name will be:
     * <ul>
     * <li>Folded to uppercase</li>
     * <li>Truncated to eight characters.</li>
     * </ul>
     * What we do here is that we try to construct a 7 characters prefix by
     * taken characters from each of the words forming the data item name. See
     * NameUtil.toWordList for a definition of words.
     * 
     * @return a valid COBOL program name prefix
     */
    protected String createProgramNamePrefix() {
        String cobolName = cobolDataItem.getCobolName();
        if (StringUtils.isBlank(cobolName)) {
            return cobolName;
        }
        List < String > words = NameUtil.toWordList(cobolName);
        StringBuilder sb = new StringBuilder();
        if (words.size() >= 7) {
            for (int i = 0; i < 7; i++) {
                sb.append(words.get(i).charAt(0));
            }
        } else {
            int q = 7 / words.size();
            int r = 7 % words.size();
            for (String word : words) {
                if (word.length() < q) {
                    r += q - word.length();
                }
            }
            for (String word : words) {
                if (sb.length() == 7) {
                    break;
                } else {
                    if (q < word.length()) {
                        if (r > 0) {
                            int l = Math.min(r, word.length() - q);
                            sb.append(word.substring(0, q + l));
                            r -= l;
                        } else {
                            sb.append(word.substring(0, q));
                        }
                    } else {
                        sb.append(word);
                    }
                }
            }
        }
        return sb.toString().toUpperCase();
    }

    /**
     * Produces the copybook associated with this cobol data item so that it can
     * be embedded in COBOL programs.
     * 
     * @return the copybook associated with this cobol data item
     */
    public String getCopybook() {
        return CopybookGenerator.generate(cobolDataItem, false);
    }

    /**
     * The largest string size in this data item (including children).
     * 
     * @return the largest string size in this data item (including children)
     */
    public int getMaxStringSize() {
        return getMaxStringSize(cobolDataItem);
    }

    /**
     * @return true if this data item or one of this childre contains an
     *         alphanumeric item
     */
    public boolean isHasAlphanumItems() {
        return getMaxStringSize() > 0;
    }

    /**
     * @return true if this data item is a variable size array with a depending
     *         on clause
     */
    public boolean isHasDependingOn() {
        return getCobolDataItem().isVariableSizeArray();
    }

    /**
     * Recursively search for the largest alphanumeric data item.
     * 
     * @param cobolDataItem the current COBOL data item
     * @return the largest alphanumeric data item within this data item and
     *         children
     */
    protected int getMaxStringSize(CobolDataItem cobolDataItem) {
        if (cobolDataItem.getChildren().size() > 0) {
            int maxStringSize = 0;
            for (CobolDataItem child : cobolDataItem.getChildren()) {
                int size = getMaxStringSize(child);
                if (size > maxStringSize) {
                    maxStringSize = size;
                }
            }
            return maxStringSize;
        } else if (cobolDataItem.getPicture() != null) {
            return PictureUtil
                    .getSymbolsNumber('X', cobolDataItem.getPicture());
        } else {
            return 0;
        }
    }

    /**
     * @return true if this data item (or one of its children) contains a sub
     *         structure.
     */
    public boolean isHasSubStructures() {
        return getSubStructuresCobolName().size() > 0;
    }

    /**
     * Recursively retrieves all sub structures names whatever their depth in
     * the hierarchy.
     * <p/>
     * TODO there is an implicit assumption that names are unique
     * 
     * @return the list of substructures COBOL names
     */
    public List < String > getSubStructuresCobolName() {

        List < String > subStructuresCobolName = new ArrayList < String >();
        if (isStructure()) {
            for (ProtoCobolDataItem child : getChildren()) {
                if (child.isStructure()) {
                    subStructuresCobolName.add(child.getCobolName());
                    subStructuresCobolName.addAll(child
                            .getSubStructuresCobolName());
                }
            }
        }
        return subStructuresCobolName;

    }

    /**
     * Collect all data items which are fixed size arrays. Ecah one will have a
     * corresponding counter name.
     * 
     * @return the list of counter names for data items which are fixed size
     *         arrays
     */
    public List < String > getAllCobolCounterNames() {
        return getAllCobolCounterNames(cobolDataItem);
    }

    /**
     * @return true if there are fixed size arrays in this data item.
     */
    public boolean isHasCobolCounterNames() {
        return getAllCobolCounterNames().size() > 0;
    }

    /**
     * For each array field we might need a working storage counter that we
     * provision for here.
     * 
     * @param cobolDataItem the current COBOL data item
     * @return the list of elementary data items which need to be indexed
     */
    protected List < String > getAllCobolCounterNames(
            CobolDataItem cobolDataItem) {
        List < String > counterNames = new ArrayList < String >();
        if (cobolDataItem.isArray()) {
            counterNames.add(getCobolCounterName(cobolDataItem.getCobolName()));
        }
        if (cobolDataItem.getChildren().size() > 0) {
            for (CobolDataItem child : cobolDataItem.getChildren()) {
                counterNames.addAll(getAllCobolCounterNames(child));
            }
        }
        return counterNames;
    }

    /**
     * For each direct child which are arrays, this contains the corresponding
     * COBOL counter name.
     * 
     * @return the direct children COBOL counter names
     */
    public List < String > getDirectChildrenCobolCounterNames() {
        List < String > counterNames = new ArrayList < String >();
        for (ProtoCobolDataItem child : getChildren()) {
            if (child.isArray()) {
                counterNames.add(child.getCobolCounterName());
            }
        }
        return counterNames;
    }

    /**
     * List of data items which give variable size arrays dimensions.
     * 
     * @return list of data items which give variable size arrays dimensions
     */
    public List < String > getDependingOns() {
        return getDependingOns(cobolDataItem);
    }

    /**
     * List of data items which give variable size arrays dimensions.
     * 
     * @param cobolDataItem the current COBOL data item
     * @return list of data items which give variable size arrays dimensions
     */
    protected List < String > getDependingOns(CobolDataItem cobolDataItem) {
        List < String > dependingOns = new ArrayList < String >();
        if (cobolDataItem.getDependingOn() != null) {
            dependingOns.add(cobolDataItem.getDependingOn());
        }
        for (CobolDataItem child : cobolDataItem.getChildren()) {
            dependingOns.addAll(getDependingOns(child));
        }
        return dependingOns;

    }

    /**
     * @return the Cobol element name
     */
    public String getCobolName() {
        return cobolDataItem.getCobolName();
    }

    /**
     * When referenced in COBOL code, data item need to use indexes either
     * because they are repeating items or because they belong to at least one
     * parent who is a repeating item.
     * 
     * @return the Cobol element name
     */
    public String getIndexedCobolName() {
        StringBuilder sb = new StringBuilder();
        sb.append(cobolDataItem.getCobolName());
        if (cobolCounters.size() > 0) {
            sb.append("(");
            boolean first = true;
            for (String cobolCounter : cobolCounters) {
                if (first) {
                    first = false;
                } else {
                    sb.append(", ");
                }
                sb.append(cobolCounter);
            }
            sb.append(")");
        }
        return sb.toString();
    }

    /**
     * @return the decorated cobol Data Item
     */
    public CobolDataItem getCobolDataItem() {
        return cobolDataItem;
    }

    /**
     * @return the children
     */
    public List < ProtoCobolDataItem > getChildren() {
        return children;
    }

    /**
     * @param children the children to set
     */
    public void setChildren(List < ProtoCobolDataItem > children) {
        this.children = children;
    }

    /**
     * @return true if this data item is a structure (group).
     */
    public boolean isStructure() {
        return (cobolDataItem.getPicture() == null)
                && (cobolDataItem.getUsage() == null)
                && (cobolDataItem.getChildren().size() > 0);
    }

    /**
     * @return true if this is the group holding all variable size array
     *         counters. This group exist in the COBOL structure but not in the
     *         protocol buffer stream.
     */
    public boolean isOccursCountersGroup() {
        return cobolDataItem.getCobolName().equals(
                ProtoCobolMapper.OCCURS_COUNTERS_GROUP_NAME);
    }

    /**
     * @return true if this maps a protobuf Message to a COBOL Group
     */
    public boolean isPMessageZGroup() {
        return pbFieldType.equals(Type.MESSAGE) && isStructure();
    }

    /**
     * @return true if this maps a protobuf bool to a COBOL unsigned integer 16
     */
    public boolean isPBoolZUint16() {
        return pbFieldType.equals(Type.BOOL) && isZUint16();
    }

    /**
     * @return true if this maps a protobuf string to a COBOL alphanum
     */
    public boolean isPStringZAlphanum() {
        return pbFieldType.equals(Type.STRING) && isZAlphanum();
    }

    /**
     * @return true if this maps a protobuf double to a COBOL COMP-2
     */
    public boolean isPDoubleZComp2() {
        return pbFieldType.equals(Type.DOUBLE) && isZComp2();
    }

    /**
     * @return true if this maps a protobuf float to a COBOL COMP-1
     */
    public boolean isPFloatZComp1() {
        return pbFieldType.equals(Type.FLOAT) && isZComp1();
    }

    /**
     * @return true if this maps a protobuf enum to a COBOL unsigned integer 32
     */
    public boolean isPEnumZUInt16() {
        return pbFieldType.equals(Type.ENUM) && isZUint16();
    }

    /**
     * @return true if this maps a protobuf varint to a COBOL signed integer 32
     */
    public boolean isPVarintZInt32() {
        return isVarint() && isZint32();
    }

    /**
     * @return true if this maps a protobuf varint to a COBOL signed integer 64
     */
    public boolean isPVarintZInt64() {
        return isVarint() && isZint64();
    }

    /**
     * @return true if this maps a protobuf varint to a COBOL unsigned integer
     *         32
     */
    public boolean isPVarintZUInt32() {
        return isVarint() && isZUint32();
    }

    /**
     * @return true if this maps a protobuf varint to a COBOL unsigned integer
     *         64
     */
    public boolean isPVarintZUInt64() {
        return isVarint() && isZUint64();
    }

    /**
     * @return true if this maps a protobuf zigzag integer to a COBOL signed
     *         integer 32
     */
    public boolean isPVarzigZInt32() {
        return isVarzig() && isZint32();
    }

    /**
     * @return true if this maps a protobuf zigzag integer to a COBOL signed
     *         integer 64
     */
    public boolean isPVarzigZInt64() {
        return isVarzig() && isZint64();
    }

    /**
     * @return true if this maps a protobuf fixed signed 32 bits integer to a
     *         COBOL signed integer 32
     */
    public boolean isPSFixed32ZInt32() {
        return pbFieldType.equals(Type.SFIXED32) && isZint32();
    }

    /**
     * @return true if this maps a protobuf fixed signed 64 bits integer to a
     *         COBOL signed integer 64
     */
    public boolean isPSFixed64ZInt64() {
        return pbFieldType.equals(Type.SFIXED64) && isZint64();
    }

    /**
     * @return true if this maps a protobuf fixed unsigned 32 bits integer to a
     *         COBOL unsigned integer 32
     */
    public boolean isPFixed32ZUInt32() {
        return pbFieldType.equals(Type.FIXED32) && isZUint32();
    }

    /**
     * @return true if this maps a protobuf fixed unsigned 64 bits integer to a
     *         COBOL unsigned integer 64
     */
    public boolean isPFixed64ZUInt64() {
        return pbFieldType.equals(Type.FIXED64) && isZUint64();
    }

    /**
     * @return a prefix that can be used for generated program names
     */
    public String getProgramNamePrefix() {
        return programNamePrefix;
    }

    /**
     * @param programNamePrefix a prefix that can be used for generated program
     *            names to set
     */
    public void setProgramNamePrefix(String programNamePrefix) {
        this.programNamePrefix = programNamePrefix;
    }

    /**
     * @return the cobolCounters
     */
    public Stack < String > getCobolCounters() {
        return cobolCounters;
    }

    /**
     * @return true if this item is an array
     */
    public boolean isArray() {
        return cobolDataItem.isArray();
    }

    /**
     * @return the depending on object
     */
    public String getDependingOn() {
        return cobolDataItem.getDependingOn();
    }

    /**
     * @return the maximum number of occurrences
     */
    public int getMaxOccurs() {
        return cobolDataItem.getMaxOccurs();
    }

    /**
     * @return the corresponding protocol buffer field type
     */
    public Type getPbFieldType() {
        return pbFieldType;
    }

    /**
     * @return true if this is a COBOL unsigned integer 16
     */
    public boolean isZUint16() {
        if (cobolDataItem.getPicture() == null) {
            return false;
        }
        return cobolDataItem.getPicture().equals(
                ProtoCobolMapper.PICTURE_UINT16);
    }

    /**
     * @return true if this is a COBOL unsigned integer 32
     */
    public boolean isZUint32() {
        if (cobolDataItem.getPicture() == null) {
            return false;
        }
        return cobolDataItem.getPicture().equals(
                ProtoCobolMapper.PICTURE_UINT32);
    }

    /**
     * @return true if this is a COBOL unsigned integer 64
     */
    public boolean isZUint64() {
        if (cobolDataItem.getPicture() == null) {
            return false;
        }
        return cobolDataItem.getPicture().equals(
                ProtoCobolMapper.PICTURE_UINT64);
    }

    /**
     * @return true if this is a COBOL signed integer 32
     */
    public boolean isZint32() {
        if (cobolDataItem.getPicture() == null) {
            return false;
        }
        return cobolDataItem.getPicture().equals(
                ProtoCobolMapper.PICTURE_SINT32);
    }

    /**
     * @return true if this is a COBOL signed integer 64
     */
    public boolean isZint64() {
        if (cobolDataItem.getPicture() == null) {
            return false;
        }
        return cobolDataItem.getPicture().equals(
                ProtoCobolMapper.PICTURE_SINT64);
    }

    /**
     * @return true if this is a COBOL alphanumeric
     */
    public boolean isZAlphanum() {
        if (cobolDataItem.getPicture() == null) {
            return false;
        }
        return cobolDataItem.getPicture().startsWith("X");
    }

    /**
     * @return true if this is a COBOL COMP-2
     */
    public boolean isZComp2() {
        if (cobolDataItem.getUsage() == null) {
            return false;
        }
        return cobolDataItem.getUsage().equals(Usage.DOUBLEFLOAT);
    }

    /**
     * @return true if this is a COBOL COMP-1
     */
    public boolean isZComp1() {
        if (cobolDataItem.getUsage() == null) {
            return false;
        }
        return cobolDataItem.getUsage().equals(Usage.SINGLEFLOAT);
    }

    /**
     * @return true if this is a protocol buffer variable integer
     */
    public boolean isVarint() {
        return pbFieldType.equals(Type.INT32)
                || pbFieldType.equals(Type.UINT32)
                || pbFieldType.equals(Type.INT64)
                || pbFieldType.equals(Type.UINT64);
    }

    /**
     * @return true if this is a protocol buffer zigzag integer
     */
    public boolean isVarzig() {
        return pbFieldType.equals(Type.SINT32)
                || pbFieldType.equals(Type.SINT64);
    }
}
