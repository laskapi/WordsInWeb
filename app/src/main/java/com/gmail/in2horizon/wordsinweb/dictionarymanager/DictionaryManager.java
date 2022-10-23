package com.gmail.in2horizon.wordsinweb.dictionarymanager;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class DictionaryManager {
    private static final String DICT_URL = "https://download.wikdict" +
            ".com/dictionaries/sqlite/2_2022-01";
    private static final String TAG = DictionaryManager.class.getSimpleName();

    private final Set<Dictionary> availableDictionaries;
    private final Set<Dictionary> uploadedDictionaries;
    private static DictionaryManager instance;
    private String dbPath;
    private RoomDbBuilder roomDbBuilder;

    public static boolean build(Context context, Handler handler) {

        if (instance == null) {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                instance = new DictionaryManager(context);
                Message msg = Message.obtain(handler, 0, instance);
                msg.sendToTarget();
            });
            return true;
        } else {
            Message msg = Message.obtain(handler, 0, instance);
            msg.sendToTarget();
            return false;
        }

    }


    protected DictionaryManager(Context context) {
        availableDictionaries = readAvailableDictionaries();
        uploadedDictionaries = readUploadedDictionaries(context);
        roomDbBuilder = new RoomDbBuilder(context);
    }

    public ArrayList<String> getAvailableDictionarySourceNames() {
        return availableDictionaries.stream().map(Dictionary::getSrcName)
                .distinct().sorted().collect(Collectors.toCollection(ArrayList::new));
    }

    public ArrayList<String> getAvailableDictionaryDstNames(String srcName) {
        return availableDictionaries.stream().filter(n -> n.getSrcName().equals(srcName)).map(Dictionary::getDstName)
                .distinct().sorted().collect(Collectors.toCollection(ArrayList::new));
    }

    public List<String> getUploadedDictionaryNames() {
        return uploadedDictionaries.stream().map(Dictionary::getSimpleName)
                .distinct().sorted().collect(Collectors.toCollection(ArrayList::new));
    }


    public void uploadDictionary(String srcName, String dstName,
                                 Handler handler) throws RuntimeException {
        Dictionary dictionary =
                availableDictionaries.parallelStream()
                        .filter(dict -> dict.getSrcName().equals(srcName) && dict.getDstName().equals(dstName))
                        .findAny().orElseThrow(() -> new RuntimeException("No such element"));
        try {

            roomDbBuilder.build(dictionary.getFileName(), handler);
            if (availableDictionaries.remove(dictionary)) {
                uploadedDictionaries.add(dictionary);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public boolean deleteDictionary(String dictionaryName) {

        Optional<Dictionary> dictionaryOptional =
                uploadedDictionaries.stream()
                        .filter(dictionary -> dictionary.getSimpleName().equals(dictionaryName))
                        .findAny();
        if (dictionaryOptional.isPresent()) {
            Dictionary dictionary = dictionaryOptional.get();
            roomDbBuilder.deleteDb(dictionary.getFileName());
            uploadedDictionaries.remove(dictionary);
            availableDictionaries.add(dictionary);
            return true;
        }

        return false;

    }

    private Set<Dictionary> readAvailableDictionaries() {

        Set<Dictionary> parsedDictionaries = new HashSet<>();

        try {
            final URLConnection conn = new URL(DICT_URL).openConnection();
            final InputStream is = conn.getInputStream();

            XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);

            parser.setInput(is, "UTF-8");

            int eventType = parser.getEventType();
            boolean readTextFlag = false;
            while (eventType != XmlPullParser.END_DOCUMENT) {

                String tagName = parser.getName();

                switch (eventType) {
                    case XmlPullParser.START_TAG:

                        if (tagName.equals("a")) {
                            readTextFlag = true;
                        }
                        break;
                    case XmlPullParser.TEXT:
                        if (readTextFlag) {
                            String dictionaryFilename = parser.getText();
                            try {
                                parsedDictionaries.add(new Dictionary(dictionaryFilename));
                            } catch (InstantiationException e) {
                                e.printStackTrace();
                            }


                            readTextFlag = false;
                        }
                        break;
                    default:
                        break;
                }

                eventType = parser.next();
            }

        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }
        return parsedDictionaries;
    }

    private Set<Dictionary> readUploadedDictionaries(Context context) {
        Set<Dictionary> readDictionaries = new HashSet<>();

        dbPath = context.getDatabasePath("a").getAbsolutePath();
        dbPath = dbPath.substring(0, dbPath.length() - 1);
        File directory = new File(dbPath);
        String[] filenames = directory.list();

        if (filenames != null) {
            HashSet<String> files =
                    (HashSet<String>) Arrays.stream(filenames).collect(Collectors.toSet());

            availableDictionaries.stream().filter(d -> files.
                    contains(d.getFileName())).collect(Collectors.toCollection(() -> readDictionaries));
            Log.d(TAG, "available dicts: " + availableDictionaries.size());
            Log.d(TAG, "files in db folder: " + files.size());
        }

        Log.d(TAG, "uploaded dicts in db folder: " + readDictionaries.size());
        availableDictionaries.removeAll(readDictionaries);
        Log.d(TAG, "resulting available dicts: " + availableDictionaries.size());

        return readDictionaries;
    }


}


