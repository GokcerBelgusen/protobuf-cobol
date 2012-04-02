/**
 * Based on wave-protocol, Copyright 2010 Google Inc.
 * Original author kalman@google.com (Benjamin Kalman)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.legstar.protobuf.cobol;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.legstar.cobol.gen.CopybookGenerator;
import com.legstar.cobol.model.CobolDataItem;
import com.legstar.protobuf.cobol.ProtoCobolMapper.HasMaxSize;

/**
 * Translates protocol buffer protos files to COBOL artifacts.
 * <p/>
 * Generated artifacts are:
 * <ul>
 * <li>A copybook that represents the root protocol-buffer message structure</li>
 * <li>A parser subprogram that turns the protocol-buffer wire format to z/OS
 * data, conforming to the previous copybook</li>
 * <li>A writer subprogram that turns z/OS data, conforming to the previous
 * copybook, to the protocol-buffer wire format</li>
 * </ul>
 * <p/>
 * This is inspired and borrows code from the protobuf-stringtemplate (PST)
 * subproject in Google-Apache wave.
 * <p/>
 * There are 3 ways this generator can be invoked:
 * <ul>
 * <li>If you already have a protobuf FileDescriptor, then you can invoke the
 * run method directly</li>
 * <li>Otherwise, if you separately used protobuf to generate java code, then
 * you can refer to the java code which will be loaded from the classpath and a
 * FileDescriptor obtained by reflection</li>
 * <li>Otherwise you can pass a proto file and this will invoke the protoc
 * compiler (which must be on the Path) and then the java compiler to get to the
 * same point as the previous option</li>
 * </ul>
 * One a protoc-generated java class is available, this will map the descriptor
 * to COBOL structures.
 * 
 */
public class ProtoCobol {

    private static Log logger = LogFactory.getLog(ProtoCobol.class);

    /** Generated copybook file base name will have this suffix. */
    private static final String COBOL_MEMBER_COPYBOOK_SUFFIX = "C";

    /** Generated copybook file extension name. */
    private static final String COPYBOOK_FILE_EXTENSION = "cpy";

    /** Generated programs file extension name. */
    private static final String PROGRAM_FILE_EXTENSION = "cbl";

    /**
     * By default, don't wait more than this many seconds for a command to
     * execute.
     */
    private static final int DEFAULT_TIMEOUT = 10;

    private File outputDir;
    private String qualifiedClassName;
    private File protoFile;
    private File protoPath;
    private int timeout = DEFAULT_TIMEOUT;

    /** The protobuf to COBOL mapping logic. */
    private ProtoCobolMapper cobolMapper = new ProtoCobolMapper();

    /**
     * The main entry point if you don't have a FileDescriptor (@see
     * run(FileDescriptor) otherwise).
     * <p/>
     * Either qualifiedClassName or protoFile must be provided depending on if
     * you already ran the protoc compiler for java or you expect this tool to
     * run it for you.
     * 
     * @throws ProtoCobolException if generation fails.
     */
    public void run() throws ProtoCobolException {

        if (outputDir == null) {
            throw new ProtoCobolException("outputDir cannot be null");
        }
        if (qualifiedClassName != null) {
            run(toFileDescriptor(qualifiedClassName));
        } else if (protoFile != null) {
            if (!protoFile.exists()) {
                throw new ProtoCobolException("The specified proto file: "
                        + protoFile.getAbsolutePath() + ", does not exist");
            }
            run(toFileDescriptor(protoFile, protoPath, timeout));

        } else {
            throw new ProtoCobolException(
                    "You must specify either a proto file name or a protoc-generated java class name");
        }

    }

    /**
     * Run with a protobuf FileDescriptor.
     * <p/>
     * This will build a COBOL model by mapping the FileDescriptor content and
     * then uses StringTemplate to produce COBOL code.
     * 
     * @param fd the protobuf FileDescriptor
     * @throws ProtoCobolException if generation fails
     */
    public void run(FileDescriptor fd) throws ProtoCobolException {

        logger.info("ProtoCobol started with file: " + fd.getName()
                + ", output dir: " + outputDir.getAbsolutePath());

        List < ProtoCobolException > exceptions = new ArrayList < ProtoCobolException >();

        try {
            for (Descriptor messageDescriptor : fd.getMessageTypes()) {

                CobolDataItem cobolDataItem = cobolMapper
                        .toCobol(messageDescriptor);

                String copybookContent = CopybookGenerator
                        .generate(cobolDataItem);
                File copybookFile = writeCopybookFile(cobolDataItem,
                        copybookContent);
                if (logger.isDebugEnabled()) {
                    logger.debug("Generated copy book in file: "
                            + copybookFile.getPath());
                    logger.debug(copybookContent);
                }

                ProtoCobolDataItem protoCobolDataItem = new ProtoCobolDataItem(
                        cobolDataItem);

                String parserContent = ProtoCobolGenerator
                        .generateParser(protoCobolDataItem);
                File parserFile = writeParserFile(protoCobolDataItem,
                        parserContent);
                if (logger.isDebugEnabled()) {
                    logger.debug("Generated parser in file: "
                            + parserFile.getPath());
                    logger.debug(parserContent);
                }

            }
        } catch (Exception e) {
            exceptions
                    .add(new ProtoCobolException("COBOL generation failed", e));
        }

        for (ProtoCobolException e : exceptions) {
            logger.error("Generation error", e);
        }
        if (!exceptions.isEmpty()) {
            throw exceptions.get(0);
        }
    }

    /**
     * Write the COBOL copybook to a file.
     * 
     * @param cobolDataItem the data item
     * @param copybookContent the copybook content
     * @return a file named after the protobuf message mapped to COBOL
     * @throws IOException if writing fails
     */
    protected File writeCopybookFile(CobolDataItem cobolDataItem,
            String copybookContent) throws IOException {
        File copybookFile = new File(outputDir, cobolDataItem.getCobolName()
                + COBOL_MEMBER_COPYBOOK_SUFFIX + "." + COPYBOOK_FILE_EXTENSION);
        FileUtils.writeStringToFile(copybookFile, copybookContent);
        return copybookFile;
    }

    /**
     * Write the COBOL parser to a file.
     * 
     * @param protoCobolDataItem the decorated data item
     * @param parserContent the parser content
     * @return a file named after the generated parser program name
     * @throws IOException if writing fails
     */
    protected File writeParserFile(ProtoCobolDataItem protoCobolDataItem,
            String parserContent) throws IOException {
        File parserFile = new File(outputDir,
                protoCobolDataItem.getParserProgramName() + "."
                        + PROGRAM_FILE_EXTENSION);
        FileUtils.writeStringToFile(parserFile, parserContent);
        return parserFile;
    }

    /**
     * Similar to wave PstFileDescriptor#asFileDescriptor.
     * <p/>
     * The class name is assumed to be available from the classpath.
     * 
     * @param qualifiedClassName the qualified class name
     * @return the File descriptor
     */
    public FileDescriptor toFileDescriptor(String qualifiedClassName) {
        try {
            Class < ? > clazz = loadClass(qualifiedClassName);
            return toFileDescriptor(clazz);
        } catch (Exception e) {
            throw new IllegalArgumentException("Class " + qualifiedClassName
                    + " cannot be found or was not generated by protobuf-java",
                    e);
        }
    }

    /**
     * Similar to wave PstFileDescriptor#asFileDescriptor.
     * <p/>
     * Retrieve de FileDescriptor by reflecting on the protoc-generated java
     * class.
     * 
     * @param clazz the protoc-generated java class
     * @return the File descriptor
     */
    public FileDescriptor toFileDescriptor(Class < ? > clazz) {
        try {
            Method method = clazz.getMethod("getDescriptor");
            return (FileDescriptor) method.invoke(null);
        } catch (Exception e) {
            throw new IllegalArgumentException("Class " + qualifiedClassName
                    + " cannot be found or was not generated by protobuf-java",
                    e);
        }
    }

    /**
     * Starting from a proto file, two steps are needed to produce the file
     * descriptor:
     * <ul>
     * <li>Invoke the protoc compiler to produce a java class source code</li>
     * <li>Invoke the java compiler to turn the source to binary</li>
     * </ul>
     * 
     * @param protoFile
     * @param protoPath
     * @param timeout
     * @return
     * @throws ProtoCobolException
     */
    public FileDescriptor toFileDescriptor(File protoFile, File protoPath,
            int timeout) throws ProtoCobolException {
        File javaOut = new File(FileUtils.getTempDirectory(), "ProtoCobol"
                + System.currentTimeMillis());
        javaOut.mkdirs();
        File javaSourceFile = runProtoJava(protoFile,
                (protoPath == null) ? protoFile.getParentFile() : protoPath,
                javaOut, timeout);
        Class < ? > clazz = runJavaCompiler(javaOut, javaSourceFile);
        return toFileDescriptor(clazz);
    }

    /**
     * From Google's org.waveprotocol.pst.PstFileDescriptor.
     * <p/>
     * Call the protoc compiler, waiting a limited amount of time for
     * completion.
     * 
     * @param protoFile the protocol buffer file (proto file)
     * @param protoPath additional imported protocol buffer files will be picked
     *            up from this location
     * @param javaOut where, on the file system, the generated java class will
     *            be produced
     * @param timeout how long to wait before canceling (in seconds)
     * @return the generated java source file
     * @throws ProtoCobolException if invokation fails
     */
    public File runProtoJava(File protoFile, File protoPath, File javaOut,
            int timeout) throws ProtoCobolException {
        try {
            String[] protocCommand = new String[] { "protoc",
                    protoFile.getAbsolutePath(),
                    "-I" + protoPath.getAbsolutePath(), "--java_out",
                    javaOut.getAbsolutePath() };
            logger.info("About to execute: "
                    + StringUtils.join(protocCommand, ' '));

            Process protoc = Runtime.getRuntime().exec(protocCommand);
            killProcessAfter(timeout, TimeUnit.SECONDS, protoc);
            int exitCode = protoc.waitFor();
            if (exitCode != 0) {
                throw new ProtoCobolException("Command failed. "
                        + IOUtils.toString(protoc.getErrorStream()));

            }
            logger.info("Command succeeded. "
                    + IOUtils.toString(protoc.getInputStream()));
            return locateJavaSourceFile(javaOut);
        } catch (IOException e) {
            throw new ProtoCobolException(e);
        } catch (InterruptedException e) {
            throw new ProtoCobolException(e);
        }
    }

    /**
     * After a java source file is generated, we need to locate it in the output
     * folder. The class probably belongs to a package and is located several
     * subdirectories below the output dir.
     * 
     * @param javaOut the output directory
     * @return the generated java source file
     * @throws ProtoCobolException if file cannot be located
     */
    protected File locateJavaSourceFile(File javaOut)
            throws ProtoCobolException {

        String javaClassName = StringUtils.capitalize(FilenameUtils
                .getBaseName(protoFile.getName())) + ".java";
        logger.info("Looking for " + javaClassName + " in "
                + javaOut.getAbsolutePath());

        Collection < File > files = FileUtils.listFiles(javaOut,
                new NameFileFilter(javaClassName), TrueFileFilter.INSTANCE);
        if (files.size() == 0) {
            throw new ProtoCobolException(
                    "Unable to locate the generated java source "
                            + javaClassName + " in "
                            + javaOut.getAbsolutePath());
        }
        if (files.size() > 1) {
            logger.warn("There are more than one file named " + javaClassName
                    + " in " + javaOut.getAbsolutePath());
        }
        return files.iterator().next();
    }

    /**
     * Compile a java source file.
     * 
     * @param javaOut where, on the file system, the generated java class will
     *            be produced
     * @param javaSourceFile the java source file
     * @return the java class loaded
     * @throws ProtoCobolException if compilation fails
     */
    public Class < ? > runJavaCompiler(File javaOut, File javaSourceFile)
            throws ProtoCobolException {

        logger.info("About to compile " + javaSourceFile.getAbsolutePath());

        JavaCompiler javaCompiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager manager = javaCompiler.getStandardFileManager(
                null, null, null);
        Iterable < ? extends JavaFileObject > units = manager
                .getJavaFileObjects(javaSourceFile.getAbsolutePath());
        String[] opts = new String[] { "-d", javaOut.getAbsolutePath() };
        CompilationTask task = javaCompiler.getTask(null, manager, null,
                Arrays.asList(opts), null, units);
        boolean status = task.call();
        if (status) {
            logger.info("Compilation successful");
        } else {
            throw new ProtoCobolException("Compilation failed for "
                    + javaSourceFile.getAbsolutePath());
        }
        return loadClass(javaOut, getRelativeClassName(javaOut, javaSourceFile));
    }

    /**
     * Retrieve the java class file name relative to the base directory where it
     * was generated.
     * 
     * @param javaOut the base directory
     * @param javaSourceFile the java source file (the binary file is expected
     *            at the same location)
     * @return the java binary class name relative to the base directory
     */
    protected String getRelativeClassName(File javaOut, File javaSourceFile) {
        return javaSourceFile.getAbsolutePath()
                .substring(javaOut.getAbsolutePath().length() + 1)
                .replace(".java", ".class");
    }

    /**
     * From Google's org.waveprotocol.pst.PstFileDescriptor.
     * <p/>
     * Will kill a process if it takes too long.
     * 
     * @param delay how long to wait (
     * @param unit the unit of time delay is expressed in
     * @param process the process to kill
     */
    protected void killProcessAfter(final long delay, final TimeUnit unit,
            final Process process) {
        Thread processKiller = new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(unit.toMillis(delay));
                    process.destroy();
                } catch (InterruptedException e) {
                }
            }
        };
        processKiller.setDaemon(true);
        processKiller.start();
    }

    /**
     * From Google's org.waveprotocol.pst.PstFileDescriptor.
     * 
     * @param baseDir the base directory where compiled java binaries are
     *            located
     * @param path the path to the java binary file relative to the base
     *            directory
     * @return the java class loaded
     * @throws ProtoCobolException if class cannot be loaded
     */
    protected Class < ? > loadClass(File baseDir, String path)
            throws ProtoCobolException {
        try {
            ClassLoader classLoader = new URLClassLoader(new URL[] { baseDir
                    .toURI().toURL() });
            return classLoader.loadClass(getBinaryName(path));
        } catch (Exception e) {
            throw new ProtoCobolException(e);
        }
    }

    /**
     * Rather than using the Class.forName mechanism first, this uses
     * Thread.getContextClassLoader instead. In a Servlet context such as
     * Tomcat, this allows JAXB classes for instance to be loaded from the web
     * application (webapp) location while this code might have been loaded from
     * shared/lib. If Thread.getContextClassLoader fails to locate the class
     * then we give a last chance to Class.forName.
     * 
     * @param qualifiedClassName the class name to load
     * @return the class
     * @throws ClassNotFoundException if class is not accessible from any class
     *             loader
     */
    public static Class < ? > loadClass(final String qualifiedClassName)
            throws ClassNotFoundException {
        ClassLoader contextClassLoader = Thread.currentThread()
                .getContextClassLoader();
        if (contextClassLoader == null) {
            return Class.forName(qualifiedClassName);
        }
        try {
            return contextClassLoader.loadClass(qualifiedClassName);
        } catch (ClassNotFoundException e) {
            return Class.forName(qualifiedClassName);
        }
    }

    /**
     * From Google's org.waveprotocol.pst.PstFileDescriptor.
     * 
     * @param path the path to the java binary file relative to the base
     *            directory
     * @return the qualified java class name
     */
    protected String getBinaryName(String path) {
        return path.replace(File.separatorChar, '.').substring(0,
                path.length() - ".class".length());
    }

    /**
     * Location of file system where COBOL files are generated.
     * 
     * @return the outputDir a location of file system where COBOL files are
     *         generated
     */
    public File getOutputDir() {
        return outputDir;
    }

    /**
     * Location of file system where COBOL files are generated.
     * 
     * @param outputDir the location of file system where COBOL files are
     *            generated to set
     */
    public ProtoCobol setOutputDir(File outputDir) {
        this.outputDir = outputDir;
        return this;
    }

    /**
     * The protoc generated java qualified class name.
     * 
     * @return the qualifiedClassName the protoc generated java qualified class
     *         name
     */
    public String getQualifiedClassName() {
        return qualifiedClassName;
    }

    /**
     * The protoc generated java qualified class name.
     * 
     * @param qualifiedClassName the the protoc generated java qualified class
     *            name to set
     * @return this instance for chaining
     */
    public ProtoCobol setQualifiedClassName(String qualifiedClassName) {
        this.qualifiedClassName = qualifiedClassName;
        return this;
    }

    /**
     * The input proto file.
     * 
     * @return the input proto file
     */
    public File getProtoFile() {
        return protoFile;
    }

    /**
     * input proto file.
     * 
     * @param protoFileName the input proto file to set
     * @return this instance for chaining
     */
    public ProtoCobol setProtoFile(File protoFile) {
        this.protoFile = protoFile;
        return this;
    }

    /**
     * Imported protocol buffer files will be picked up from this location
     * 
     * @return the location where imported proto files are found
     */
    public File getProtoPath() {
        return protoPath;
    }

    /**
     * Imported protocol buffer files will be picked up from this location
     * 
     * @param protoPath the location where imported proto files are found to set
     * @return this instance for chaining
     */
    public ProtoCobol setProtoPath(File protoPath) {
        this.protoPath = protoPath;
        return this;
    }

    /**
     * Maximum time to wait for a command to execute (seconds).
     * 
     * @return the maximum time to wait for a command to execute (seconds)
     */
    public int getTimeout() {
        return timeout;
    }

    /**
     * Maximum time to wait for a command to execute (seconds).
     * 
     * @param timeout the maximum time to wait for a command to execute
     *            (seconds) to set
     * @return this instance for chaining
     */
    public ProtoCobol setTimeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    /**
     * Add a new size provider. This is a user provider code that will provide
     * values for string maximum sizes and arrays maximum number of items.
     * 
     * @param provider a size provider
     * @return this instance for chaining
     */
    public ProtoCobol addSizeProvider(HasMaxSize provider) {
        cobolMapper.addSizeProvider(provider);
        return this;
    }

}
