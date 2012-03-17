package com.legstar.protobuf.cobol;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.google.protobuf.Descriptors.FieldDescriptor.Type;
import com.legstar.cobol.gen.CopybookGenerator;
import com.legstar.cobol.model.CobolDataItem;
import com.legstar.coxb.util.PictureUtil;

/**
 * Root data items decorates regular data item to provide additional methods
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

    /** The COBOL deta item we are decorating. */
    private final CobolDataItem cobolDataItem;

    /** Children are also decorated data items. */
    private List < ProtoCobolDataItem > children;
    
    public ProtoCobolDataItem(final CobolDataItem cobolDataItem) {
        this.cobolDataItem = cobolDataItem;
        decorateChildren(this);
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
     * Assuming PGMNAME(COMPAT) compiler option. From IBM Enterprise COBOL for
     * z/OSLanguage Reference Version 3 Release 3:
     * <ul>
     * <li>The name can be up to 30 characters in length</li>
     * <li>Only the hyphen, digits 0-9, and alphabetic characters are allowed.</li>
     * <li>At least one character must be alphabetic</li>
     * <li>The hyphen cannot be used as the first or last character</li>
     * </ul>
     * 
     * @return a valid COBOL program name prefix
     */
    protected String getProgramNamePrefix() {
        return StringUtils.substring(cobolDataItem.getCobolName(), 0, 29)
                .replace('_', '-');
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
    public boolean isMappedToDounble() {
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
}
