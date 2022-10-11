package com.gmail.in2horizon.wordsinweb.dictionarymanager;

import android.app.Application;
import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.common.collect.HashBiMap;

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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DictionaryNamesProvider {

    private static final String TAG = DictionaryNamesProvider.class.getSimpleName();
    private static Context context;

    static class Names {
        Map<String, TreeSet<String>> availableDictionaryNames;
        HashBiMap<String, String> isoToName;
        List<String> uploadedDictionaries;

        Names() {
            availableDictionaryNames = new TreeMap<>();
            isoToName = HashBiMap.create();
        }

    }

    static final String mUrl = "https://download.wikdict.com/dictionaries/sqlite/2_2022-01";
    private final List<OnNamesReadyListener> listeners;
    private static DictionaryNamesProvider instance;
    private final Names names;

    /**
     * @param listener callback interface gets initialized instance of Names class
     * @return true when parsing new instance from URL on background thread. Consumes lots of
     * time. Consider showing progress bar. False if callback gets called instantly.
     */
    static synchronized boolean registerOnNamesReadyListener(Context context,
                                                             OnNamesReadyListener listener) {
        DictionaryNamesProvider.context = context;
        if (instance == null) {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                instance = new DictionaryNamesProvider();
                instance.listeners.add(listener);
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(() -> instance.notifyListeners());
            });
            return true;
        } else {
            instance.listeners.add(listener);
            instance.notifyListeners();
            return false;
        }

    }

    private DictionaryNamesProvider() {
        names = new Names();
        listeners = new ArrayList<OnNamesReadyListener>();
        loadDictionaries();

    }

    ;

    private void notifyListeners() {
        listeners.forEach((l) -> l.onNamesReady(names));
    }


    private void loadDictionaries() {

        final URLConnection conn;

        try {

            conn = new URL(mUrl).openConnection();
            final InputStream is = conn.getInputStream();
            Map<String, TreeSet<String>> availableIsos = parseHtml(is);
            availableIsos.forEach((key, values) ->
            {
                names.isoToName.putIfAbsent(key, new Locale(key).getDisplayLanguage());
                values.forEach(value -> names.isoToName.putIfAbsent(value,
                        new Locale(value).getDisplayLanguage()));

            });

            availableIsos.forEach((key, value) -> {
                names.availableDictionaryNames.put(names.isoToName.get(key),
                        value.stream().map(iso -> names.isoToName.get(iso)).collect(Collectors.toCollection(TreeSet::new)));
            });

            names.uploadedDictionaries = getUploadedDictionaries();


        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private List<String> getUploadedDictionaries() {
        String dbPath = context.getDatabasePath("a").getAbsolutePath();
        dbPath = dbPath.substring(0, dbPath.length() - 1);
        File directory = new File(dbPath);
        File[] files = directory.listFiles();
        Arrays.stream(files).forEach(f->Log.d(TAG,f.getName()));

        return Arrays.stream(files).map(File::getName).filter(this::isValidDictionaryName).map(m->m.substring(0,5)).collect(Collectors.toCollection(ArrayList::new));
    }


    private Map<String, TreeSet<String>> parseHtml(InputStream is) throws IOException {
        Map<String, TreeSet<String>> availableIsos = new TreeMap<>();

        try {
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
                            if (isValidDictionaryName(dictionaryFilename)) {

                                String src = dictionaryFilename.substring(0, 2);
                                String dst = dictionaryFilename.substring(3, 5);
                                availableIsos.computeIfAbsent(src, k -> new TreeSet<>()).add(dst);

                            }
                            readTextFlag = false;
                        }
                        break;
                    default:
                        break;
                }

                eventType = parser.next();
            }

        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
        return availableIsos;

    }

    private boolean isValidDictionaryName(String filename) {
        return filename.matches("^[a-z]{2}-[a-z]{2}\\.sqlite3");
    }


}
