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

import org.antlr.stringtemplate.CommonGroupLoader;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateErrorListener;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.legstar.cobol.gen.Copybook72ColWriter;

/**
 * Generates a COBOL protocol buffer Parser (protobuf to COBOL) and a Writer
 * (COBOL to protobuf) for a structure.
 * <p/>
 * 
 */
public class ProtoCobolGenerator {

    /** Location where string templates resources are found. */
    public static final String TEMPLATE_BASE = "templates";

    /** The stringtemplate group for protobuf-cobol templates. */
    public static final String PROTOCOB_TEMPLATE_GROUP_NAME = "protocob-group";

    /** Template that generates a protocol-cobol parser. */
    public static final String PARSER_TEMPLATE_NAME = "toProtocobParser";

    /** Template that generates a protocol-cobol writer. */
    public static final String WRITER_TEMPLATE_NAME = "toProtocobWriter";

    private static Log logger = LogFactory.getLog(ProtoCobolGenerator.class);

    /** Utility class. */
    private ProtoCobolGenerator() {

    }

    /**
     * Fetches templates from the classpath.
     */
    public static final CommonGroupLoader GROUP_LOADER = new CommonGroupLoader(
            TEMPLATE_BASE, new StringTemplateErrorListener() {
                public void error(String msg, Throwable e) {
                    logger.error(msg, e);
                }

                public void warning(String msg) {
                    logger.warn(msg);
                }
            });

    /**
     * Generates a COBOL protocol buffer parser as a string.
     * 
     * @param protoCobolDataItem the COBOL data item
     * @return the COBOL protocol buffer parser content
     */
    public static String generateParser(ProtoCobolDataItem protoCobolDataItem) {
        return generate(protoCobolDataItem, PARSER_TEMPLATE_NAME);
    }

    /**
     * Generates a COBOL protocol buffer writer as a string.
     * 
     * @param protoCobolDataItem the COBOL data item
     * @return the COBOL protocol buffer writer content
     */
    public static String generateWriter(ProtoCobolDataItem protoCobolDataItem) {
        return generate(protoCobolDataItem, WRITER_TEMPLATE_NAME);
    }

    /**
     * Generates a COBOL artifact as a string.
     * 
     * @param protoCobolDataItem the COBOL data item
     * @param templateName the template name
     * @return the COBOL artifact content
     */
    public static String generate(ProtoCobolDataItem protoCobolDataItem,
            final String templateName) {
        StringTemplate template = getTemplate(PROTOCOB_TEMPLATE_GROUP_NAME,
                templateName);
        template.setAttribute("protoCobolDataItem", protoCobolDataItem);
        return template.toString();
    }

    /**
     * Retrieve a template from the classpath.
     * 
     * @param templateGroupName the template group name
     * @param templateName the template name
     * @return the template instance
     */
    public static StringTemplate getTemplate(final String templateGroupName,
            final String templateName) {
        StringTemplateGroup.registerGroupLoader(GROUP_LOADER);
        StringTemplateGroup group = StringTemplateGroup.loadGroup(
                templateGroupName, DefaultTemplateLexer.class, null);
        group.setStringTemplateWriter(Copybook72ColWriter.class);
        return group.getInstanceOf(templateName);
    }

}
