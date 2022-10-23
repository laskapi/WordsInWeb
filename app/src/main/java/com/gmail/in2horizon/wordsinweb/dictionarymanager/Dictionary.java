package com.gmail.in2horizon.wordsinweb.dictionarymanager;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

public class Dictionary {
    private static final String TAG = Dictionary.class.getSimpleName();


    private final String fileName;
    private final String simpleName;
    private final String srcIso;
    private final String dstIso;
    private String srcName;
    private String dstName;

    static Map<String, String> isoToName;

    static {
        isoToName = new HashMap<>();
    }

    public Dictionary(String filename) throws InstantiationException {

        if (!isValidDictionaryName(filename)) {
            throw new InstantiationException("Cannot be instantiated");
        }
        this.fileName = filename;
        this.srcIso = filename.substring(0, 2);
        this.dstIso = filename.substring(3, 5);

        srcName = isoToName.get(srcIso);
        if (srcName == null) {
            srcName = new Locale(srcIso).getDisplayLanguage();
            isoToName.put(srcIso, srcName);
        }
        dstName = isoToName.get(dstIso);
        if (dstName == null) {
            dstName = new Locale(dstIso).getDisplayLanguage();
            isoToName.put(dstIso, dstName);
        }


        this.simpleName = srcName+"-"+dstName;


    }

    public String getFileName() {
        return fileName;
    }

    public String getSimpleName() {
        return simpleName;
    }

    public String getSrcIso() {
        return srcIso;
    }

    public String getDstISo() {
        return dstIso;
    }

    public String getSrcName() {
        return srcName;
    }

    public String getDstName() {
        return dstName;
    }

    private boolean isValidDictionaryName(String filename) {
        return filename.matches("^[a-z]{2}-[a-z]{2}\\.sqlite3");


    }
}
