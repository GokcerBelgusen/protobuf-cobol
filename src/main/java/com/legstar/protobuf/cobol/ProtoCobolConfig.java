package com.legstar.protobuf.cobol;

/**
 * This class is intended to hold parameters that influence the generation
 * process.
 * 
 */
public class ProtoCobolConfig {

    /** Default code page for COBOL. */
    public static final int DEFAULT_COBOL_CODE_PAGE = 1047;

    /** The code page to use for COBOL (CCSID number). */
    private int cobolCodePage = DEFAULT_COBOL_CODE_PAGE;

    /**
     * @return the code page (CCSID number) to use for COBOL
     */
    public int getCobolCodePage() {
        return cobolCodePage;
    }

    /**
     * @param cobolCodePage the code page (CCSID number) to set
     */
    public void setCobolCodePage(int cobolCodePage) {
        this.cobolCodePage = cobolCodePage;
    }

}
