package com.legstar.protobuf.cobol;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Protocol Buffer for COBOL standalone executable.
 * <p/>
 * This is the main class for the executable jar. It takes options from the
 * command line and calls the {@link ProtoCobol} API.
 * <p/>
 * Usage: <code>
 * java -jar protobuf-cobol-x.y.z-exe.jar
 *      -i&lt;i protocol buffers proto file&gt;
 *      -o&lt;o COBOL folder for generated artifacts&gt;
 * </code>
 * 
 */
public class ProtoCobolMain {

    /** The version properties file name. */
    private static final String VERSION_FILE_NAME = "/version.properties";

    /** The default COBOL output folder. */
    private static final File DEFAULT_PROTOCOB_OUTPUT_FOLDER = new File("cobol");

    /** Logger. */
    private final Log _log = LogFactory.getLog(getClass());

    /** Input protocol buffers file. */
    private File inputProtoFile;

    /** COBOL artifacts will be generated in this folder. */
    private File outputFolder;

    public ProtoCobolMain() {
    }

    /**
     * @param args translator options. Provides help if no arguments passed.
     */
    public static void main(final String[] args) {
        ProtoCobolMain main = new ProtoCobolMain();
        main.execute(args);
    }

    /**
     * Process command line options and run generator.
     * <p/>
     * If no options are passed, prints the help. Help is also printed if the
     * command line options are invalid.
     * 
     * @param args generator options
     */
    public void execute(final String[] args) {
        Options options = createOptions();
        try {
            if (collectOptions(options, args)) {
                setDefaults();
                execute();
            }
        } catch (Exception e) {
            _log.error("Protocol buffers COBOL failure", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * @return the command line options
     */
    protected Options createOptions() {
        Options options = new Options();

        Option version = new Option("v", "version", false,
                "print the version information and exit");
        options.addOption(version);

        Option help = new Option("h", "help", false,
                "print the options available");
        options.addOption(help);

        Option inputProtoFile = new Option("i", "inputProtoFile", true,
                "Input protocol buffers file");
        options.addOption(inputProtoFile);

        Option targetCobolFolder = new Option("o", "outputFolder", true,
                "Output COBOL folder");
        options.addOption(targetCobolFolder);

        return options;
    }

    /**
     * Take arguments received on the command line and setup corresponding
     * options.
     * <p/>
     * 
     * @param options the expected options
     * @param args the actual arguments received on the command line
     * @return true if arguments were valid
     * @throws Exception if something goes wrong while parsing arguments
     */
    protected boolean collectOptions(final Options options, final String[] args)
            throws Exception {
        CommandLineParser parser = new PosixParser();
        CommandLine line = parser.parse(options, args);
        return processLine(line, options);
    }

    /**
     * Process the command line options selected.
     * 
     * @param line the parsed command line
     * @param options available
     * @return false if processing needs to stop, true if its ok to continue
     * @throws Exception if line cannot be processed
     */
    protected boolean processLine(final CommandLine line, final Options options)
            throws Exception {
        if (line.hasOption("version")) {
            System.out.println("version " + getVersion());
            return false;
        }
        if (line.hasOption("help")) {
            produceHelp(options);
            return false;
        }
        if (line.hasOption("inputProtoFile")) {
            inputProtoFile = new File(
                    (line.getOptionValue("inputProtoFile").trim()));
            if (!inputProtoFile.canRead()) {
                System.out
                        .println("Input file "
                                + inputProtoFile
                                + " cannot be read."
                                + " Check that the file exists and that you have permission to read it.");
                return false;
            }
        } else {
            System.out
                    .println("You must provide an input file protocol buffers file.");

            return false;
        }
        if (line.hasOption("outputFolder")) {
            outputFolder = new File(
                    (line.getOptionValue("outputFolder").trim()));
        }

        return true;
    }

    /**
     * Make sure mandatory parameters have default values.
     */
    protected void setDefaults() {
        if (outputFolder == null) {
            outputFolder = DEFAULT_PROTOCOB_OUTPUT_FOLDER;
        }

    }

    /**
     * @param options options available
     */
    protected void produceHelp(final Options options) {
        HelpFormatter formatter = new HelpFormatter();
        String version = getVersion();
        formatter.printHelp("java -jar protobuf-cobol-" + version
                + "-exe.jar followed by:", options);
    }

    /**
     * Execute the protocol buffer COBOL translator.
     * 
     * @throws IOException if basic read/write operation fails
     * @throws ProtoCobolException if execution fails
     * 
     */
    protected void execute() throws IOException, ProtoCobolException {

        new ProtoCobol().setOutputDir(outputFolder)
                .setProtoFile(inputProtoFile).run();

    }

    /**
     * Pick up the version from the properties file.
     * 
     * @return the product version
     */
    protected String getVersion() {
        InputStream stream = null;
        try {
            Properties version = new Properties();
            stream = ProtoCobolMain.class
                    .getResourceAsStream(VERSION_FILE_NAME);
            version.load(stream);
            return version.getProperty("version");
        } catch (IOException e) {
            return "?";
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    // just ignore
                }
            }
        }
    }

}
