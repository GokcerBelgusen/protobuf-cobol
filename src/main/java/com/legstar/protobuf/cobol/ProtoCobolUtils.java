package com.legstar.protobuf.cobol;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

/**
 * When the input is a proto file, this class does some crude parsing to detect
 * the proposed Java package and class name (if any).
 * 
 */
public class ProtoCobolUtils {

    /** Regex that matches the protobuf 'package' option. */
    private static final Pattern PACKAGE_NAME_PATTERN = Pattern.compile(
            "package\\s*([a-z_\\.]+)\\s*;", Pattern.CASE_INSENSITIVE);

    /** Regex that matches the protobuf 'java_package' option. */
    private static final Pattern JAVA_PACKAGE_NAME_PATTERN = Pattern.compile(
            "option\\s+java_package\\s*=\\s*\"([a-z_\\.]+)\"\\s*;",
            Pattern.CASE_INSENSITIVE);

    /** Regex that matches the protobuf 'java_outer_classname' option. */
    private static final Pattern JAVA_OUTER_CLASS_NAME_PATTERN = Pattern
            .compile(
                    "option\\s+java_outer_classname\\s*=\\s*\"([a-z_\\.]+)\"\\s*;",
                    Pattern.CASE_INSENSITIVE);

    /**
     * A utility class.
     */
    private ProtoCobolUtils() {

    }

    /**
     * Find java specific properties in a proto file.
     * <p/>
     * The java package name can be explicitly specified in the proto file or it
     * defaults to to the proto file package (if any).
     * <p/>
     * The java outer class name can be explicitly specified in the proto file
     * or inferred from the file name.
     * 
     * @param protoFile the proto file to look at
     * @return the java properties extracted from the file
     * @throws IOException if file cannot be read
     */
    public static ProtoFileJavaProperties getJavaProperties(File protoFile)
            throws IOException {

        String javaPackageName = null;
        String javaClassName = null;

        String content = FileUtils.readFileToString(protoFile);
        Matcher matcher = JAVA_PACKAGE_NAME_PATTERN.matcher(content);
        if (matcher.find()) {
            javaPackageName = matcher.group(1);
        } else {
            matcher = PACKAGE_NAME_PATTERN.matcher(content);
            if (matcher.find()) {
                javaPackageName = matcher.group(1);
            }
        }

        matcher = JAVA_OUTER_CLASS_NAME_PATTERN.matcher(content);
        if (matcher.find()) {
            javaClassName = matcher.group(1) + ".java";
        } else {
            javaClassName = getDefaultJavaClassName(protoFile);
        }
        return new ProtoFileJavaProperties(javaPackageName, javaClassName);

    }

    /**
     * The java class name derived from the proto file name.
     * 
     * @param protoFile the proto file
     * @return the java class name
     */
    public static String getDefaultJavaClassName(File protoFile) {
        return StringUtils.capitalize(FilenameUtils.getBaseName(protoFile
                .getName())) + ".java";
    }

    /**
     * Transforms a package name to a relative location on the file system.
     * 
     * @param packageName the java package name
     * @return the relative location on file system (with ending file
     *         separator). Empty string if package name is null.
     */
    public static String packageToLocation(String packageName) {
        if (packageName == null || packageName.trim().length() == 0) {
            return "";
        }
        String location = packageName.replace('.', File.separatorChar);
        if (location.charAt(location.length() - 1) != File.separatorChar) {
            location += File.separatorChar;
        }
        return location;
    }

    /**
     * Java optional properties in the proto file.
     * 
     */
    public static class ProtoFileJavaProperties {

        private final String javaPackageName;

        private final String javaClassName;

        public ProtoFileJavaProperties(final String javaPackageName,
                final String javaClassName) {
            this.javaPackageName = javaPackageName;
            this.javaClassName = javaClassName;
        }

        /**
         * @return the java Class Name
         */
        public String getJavaClassName() {
            return javaClassName;
        }

        /**
         * @return the java Package Name
         */
        public String getJavaPackageName() {
            return javaPackageName;
        }

    }

}
