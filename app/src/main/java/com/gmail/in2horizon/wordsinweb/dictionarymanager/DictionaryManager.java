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
import java.util.Observable;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class DictionaryManager extends Observable {
    private static final String TAG = DictionaryManager.class.getSimpleName();

    private static final String DICTIONARY_URL = "https://download.wikdict" +
            ".com/dictionaries/sqlite/2_2022-01/";

    private static final int MESSAGE_DICTIONARY_MANAGER_INSTANCE=1;

    private final Set<Dictionary> availableDictionaries;

    private final Set<Dictionary> uploadedDictionaries;
    private static DictionaryManager instance;
    private final RoomDbBuilder roomDbBuilder;

    /**
     * @param context - application context needed to read files in installed app folder.
     * @param handler - handler that will get message with constructed DictionaryManager instance
     * @return -true when instance will be created. As it is long running operation client should
     * use some kind of progressbar to indicate progress. False if instance is constructed
     * and will be returned immediately.
     */
    public static boolean build(Context context, Handler handler) {

        if (instance == null) {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                instance = new DictionaryManager(context);
                Message msg = Message.obtain(handler, MESSAGE_DICTIONARY_MANAGER_INSTANCE, instance);
                msg.sendToTarget();
            });
            return true;
        } else {
            Message msg = Message.obtain(handler, MESSAGE_DICTIONARY_MANAGER_INSTANCE, instance);
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
            String srcUrl=DICTIONARY_URL+dictionary.getFileName();
            roomDbBuilder.build(srcUrl, handler);
            if (availableDictionaries.remove(dictionary)) {
                uploadedDictionaries.add(dictionary);
                setChanged();
                notifyObservers();
                clearChanged();
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
            setChanged();
            notifyObservers();
            clearChanged();
            return true;
        }

        return false;

    }

    public Optional<Dictionary> getUploadedDictionary4name(String name) {
     return uploadedDictionaries.stream().filter(dict->dict.getSimpleName().equals(name)).findFirst();

    }

    private Set<Dictionary> readAvailableDictionaries() {

        Set<Dictionary> parsedDictionaries = new HashSet<>();

        try {
            final URLConnection conn = new URL(DICTIONARY_URL).openConnection();
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

        String dbPath = context.getDatabasePath("a").getAbsolutePath();
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


