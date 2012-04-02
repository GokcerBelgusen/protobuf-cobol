package com.legstar.protobuf.cobol;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.apache.commons.lang.StringUtils;

import com.google.protobuf.Descriptors.FieldDescriptor.Type;
import com.legstar.cobol.gen.CopybookGenerator;
import com.legstar.cobol.model.CobolDataItem;
import com.legstar.coxb.util.NameUtil;
import com.legstar.coxb.util.PictureUtil;

/**
 * This decorates regular LegStar COBOL data item to provide additional methods
 * made available to templates.
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

    /** The COBOL deta item we are decorating. */
    private final CobolDataItem cobolDataItem;

    /**
     * Reflects the repeating parents which influence the way this data item can
     * be referred to.
     */
    private final Stack < String > cobolCounters;

    /** Children are also decorated data items. */
    private List < ProtoCobolDataItem > children;

    /**
     * Decorate a COBOL data item.
     * 
     * @param cobolDataItem the decorated item
     */
    public ProtoCobolDataItem(final CobolDataItem cobolDataItem) {
        this(cobolDataItem, new Stack < String >());
    }

    /**
     * Decorate a COBOL data item.
     * 
     * @param cobolDataItem the decorated item
     * @param parentCobolCounters the parent's stack of array counters
     */
    public ProtoCobolDataItem(final CobolDataItem cobolDataItem,
            Stack < String > parentCobolCounters) {
        this.cobolDataItem = cobolDataItem;
        this.cobolCounters = new Stack < String >();
        cobolCounters.addAll(parentCobolCounters);
        if (cobolDataItem.isArray()) {
            if (StringUtils.isBlank(cobolDataItem.getDependingOn())) {
                cobolCounters.push(getCobolCounterName());
            } else {
                cobolCounters.push(cobolDataItem.getDependingOn());
            }
        }
        decorateChildren(this, cobolCounters);
        programNamePrefix = createProgramNamePrefix();
    }

    /**
     * Recursively decorate children.
     * <p/>
     * Only keeps real children in particular, don't include the variable arrays
     * counters.
     * 
     * @param protoCobolDataItem the item being decorated
     */
    protected void decorateChildren(ProtoCobolDataItem protoCobolDataItem,
            Stack < String > parentCobolCounters) {
        List < ProtoCobolDataItem > decoratedChildren = new ArrayList < ProtoCobolDataItem >();
        if (isStructure()) {
            for (CobolDataItem child : protoCobolDataItem.getCobolDataItem()
                    .getChildren()) {
                ProtoCobolDataItem decoratedChild = new ProtoCobolDataItem(
                        child, parentCobolCounters);
                if (decoratedChild.isOccursCountersGroup()) {
                    continue;
                }
                decoratedChildren.add(decoratedChild);
            }
        }
        protoCobolDataItem.setChildren(decoratedChildren);
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
        if (StringUtils.isBlank(cobolDataItem.getDependingOn())) {
            if (isArray()) {
                return getCobolCounterName(cobolDataItem.getCobolName());
            } else {
                return null;
            }
        } else {
            return cobolDataItem.getDependingOn();
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
     * High level arrays, which are not themselves children of an array, become
     * variable size arrays where the size (ODO object) is an additional field
     * added to the structure.
     * <p/>
     * All other array fields however become fixed size arrays. For each one of
     * these we need a working storage counter that we provision for here.
     * 
     * @param cobolDataItem the current COBOL data item
     * @param ancestorOccurs true if one ancestor at least has the OCCURS
     *            attribute
     * @return the list of elementary data items which need to be indexed
     */
    protected List < String > getAllCobolCounterNames(
            CobolDataItem cobolDataItem) {
        List < String > counterNames = new ArrayList < String >();
        if (cobolDataItem.isArray()
                && StringUtils.isBlank(cobolDataItem.getDependingOn())) {
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
     * @return true if this maps to a protobuf bool
     */
    public boolean isMappedToMessage() {
        return ProtoCobolMapper.pictureToType(cobolDataItem.getPicture(),
                cobolDataItem.getUsage()).equals(Type.MESSAGE);
    }

    /**
     * @return true if this maps to a protobuf bool
     */
    public boolean isMappedToBool() {
        return ProtoCobolMapper.pictureToType(cobolDataItem.getPicture(),
                cobolDataItem.getUsage()).equals(Type.BOOL);
    }

    /**
     * @return true if this maps to a protobuf string
     */
    public boolean isMappedToString() {
        return ProtoCobolMapper.pictureToType(cobolDataItem.getPicture(),
                cobolDataItem.getUsage()).equals(Type.STRING);
    }

    /**
     * @return true if this maps to a protobuf double
     */
    public boolean isMappedToDouble() {
        return ProtoCobolMapper.pictureToType(cobolDataItem.getPicture(),
                cobolDataItem.getUsage()).equals(Type.DOUBLE);
    }

    /**
     * @return true if this maps to a protobuf enum
     */
    public boolean isMappedToEnum() {
        return ProtoCobolMapper.pictureToType(cobolDataItem.getPicture(),
                cobolDataItem.getUsage()).equals(Type.ENUM);
    }

    /**
     * @return true if this maps to a protobuf float
     */
    public boolean isMappedToFloat() {
        return ProtoCobolMapper.pictureToType(cobolDataItem.getPicture(),
                cobolDataItem.getUsage()).equals(Type.FLOAT);
    }

    /**
     * @return true if this maps to a protobuf int32
     */
    public boolean isMappedToInt32() {
        return ProtoCobolMapper.pictureToType(cobolDataItem.getPicture(),
                cobolDataItem.getUsage()).equals(Type.INT32);
    }

    /**
     * @return true if this maps to a protobuf int64
     */
    public boolean isMappedToInt64() {
        return ProtoCobolMapper.pictureToType(cobolDataItem.getPicture(),
                cobolDataItem.getUsage()).equals(Type.INT64);
    }

    /**
     * @return true if this maps to a protobuf uint32
     */
    public boolean isMappedToUint32() {
        return ProtoCobolMapper.pictureToType(cobolDataItem.getPicture(),
                cobolDataItem.getUsage()).equals(Type.UINT32);
    }

    /**
     * @return true if this maps to a protobuf uint64
     */
    public boolean isMappedToUint64() {
        return ProtoCobolMapper.pictureToType(cobolDataItem.getPicture(),
                cobolDataItem.getUsage()).equals(Type.UINT64);
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

}
