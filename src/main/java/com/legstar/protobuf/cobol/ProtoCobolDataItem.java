package com.legstar.protobuf.cobol;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.google.protobuf.Descriptors.FieldDescriptor.Type;
import com.legstar.cobol.gen.CopybookGenerator;
import com.legstar.cobol.model.CobolDataItem;
import com.legstar.coxb.util.NameUtil;
import com.legstar.coxb.util.PictureUtil;

/**
 * Root data items decorates regular LegStar COBOL data item to provide
 * additional methods made available to templates.
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

    /**
     * A prefix that can be used for generated program names.
     */
    private String programNamePrefix;

    /** The COBOL deta item we are decorating. */
    private final CobolDataItem cobolDataItem;

    /** Children are also decorated data items. */
    private List < ProtoCobolDataItem > children;

    public ProtoCobolDataItem(final CobolDataItem cobolDataItem) {
        this.cobolDataItem = cobolDataItem;
        decorateChildren(this);
        programNamePrefix = createProgramNamePrefix();
    }

    /**
     * Recursively decorate children.
     * 
     * @param protoCobolDataItem the item being decorated
     */
    protected void decorateChildren(ProtoCobolDataItem protoCobolDataItem) {
        List < ProtoCobolDataItem > decoratedChildren = new ArrayList < ProtoCobolDataItem >();
        for (CobolDataItem child : protoCobolDataItem.getCobolDataItem()
                .getChildren()) {
            ProtoCobolDataItem decoratedChild = new ProtoCobolDataItem(child);
            decoratedChildren.add(decoratedChild);
            decorateChildren(decoratedChild);
        }
        protoCobolDataItem.setChildren(decoratedChildren);
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
     *         childern
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
     * Retrieve all sub structures names whatever their depth in the hierarchy.
     * <p/>
     * TODO there is an implicit assumption that names are unique
     * 
     * @return the list of substructures COBOL names
     */
    public List < String > getSubStructuresCobolName() {
        return getSubStructuresCobolName(cobolDataItem);
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
     * 
     * @param cobolDataItem the current COBOL data item
     * @return the list of substructures COBOL names
     */
    protected List < String > getSubStructuresCobolName(
            CobolDataItem cobolDataItem) {

        List < String > subStructuresCobolName = new ArrayList < String >();
        for (CobolDataItem child : cobolDataItem.getChildren()) {
            if (child.getChildren().size() > 0) {
                subStructuresCobolName.add(child.getCobolName());
            }
            subStructuresCobolName.addAll(getSubStructuresCobolName(child));
        }
        return subStructuresCobolName;

    }

    /**
     * Collect all elementary data items which must be referred to using an
     * index.
     * 
     * @return the list of elementary data items which need to be indexed
     */
    public List < String > getIndexedCobolNames() {
        return getIndexedCobolNames(cobolDataItem, cobolDataItem.isArray());
    }

    /**
     * @return true if there is an indexed child or grand child.
     */
    public boolean isHasIndexedItems() {
        return getIndexedCobolNames().size() > 0;
    }

    /**
     * Recursively collects all elementary data items which must be referred to
     * using an index.
     * <p/>
     * Elementary data items are in this category if they have an OCCURS
     * attribute or if one of their ancestors has an OCCURS attribute.
     * <p/>
     * TODO this ignores multidimensional arrays
     * 
     * @param cobolDataItem the current COBOL data item
     * @param ancestorOccurs true if one ancestor at least has the OCCURS
     *            attribute
     * @return the list of elementary data items which need to be indexed
     */
    protected List < String > getIndexedCobolNames(CobolDataItem cobolDataItem,
            boolean ancestorOccurs) {
        List < String > indexedCobolNames = new ArrayList < String >();
        if (cobolDataItem.getChildren().size() == 0) {
            if (ancestorOccurs || cobolDataItem.isArray()) {
                indexedCobolNames.add(cobolDataItem.getCobolName());
            }
        } else {
            boolean occurs = ancestorOccurs || cobolDataItem.isArray();
            for (CobolDataItem child : cobolDataItem.getChildren()) {
                indexedCobolNames.addAll(getIndexedCobolNames(child, occurs));
            }
        }
        return indexedCobolNames;
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
        return (getChildren().size() > 0);
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
}
