package com.gmail.in2horizon.wordsinweb;

import android.os.Handler;
import android.os.Looper;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class DictionaryNamesProvider {

    static class Names {
        Map<String, TreeSet<String>> availableDictionaryNames;
        HashMap<String, String> isoToName;
        //    HashBiMap<String, String> isoToNameBiMap;
        //        ArrayList<String> srcItems;

        Names() {
            availableDictionaryNames = new TreeMap<>();
            isoToName = new HashMap<>();
            //      isoToNameBiMap = HashBiMap.create();
        }

    }

    private static final String URL = "https://download.wikdict.com/dictionaries/sqlite/2_2022-01";

    private List<OnNamesReadyListener> listeners;

    private static DictionaryNamesProvider instance;

    private Names names;

    static synchronized void registerOnNamesReadyListener(OnNamesReadyListener listener) {
        if (instance == null) {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                instance = new DictionaryNamesProvider();
                instance.listeners.add(listener);
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(() -> instance.notifyListeners());
            });
        } else {
            instance.listeners.add(listener);
            instance.notifyListeners();
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

            conn = new URL(URL).openConnection();
            final InputStream is = conn.getInputStream();
            Map<String, TreeSet<String>> availableIsos = parseHtml(is);
            availableIsos.forEach((key, values) ->
            {
                names.isoToName.putIfAbsent(key, new Locale(key).getDisplayLanguage());
                values.forEach(value -> names.isoToName.putIfAbsent(value,
                        new Locale(value).getDisplayLanguage()));

            });
            //  names.isoToName.replaceAll((k, v) -> new Locale(k).getDisplayLanguage());

            availableIsos.forEach((key, value) -> {
                names.availableDictionaryNames.put(names.isoToName.get(key),
                        value.stream().map(iso -> names.isoToName.get(iso)).collect(Collectors.toCollection(TreeSet::new)));
            });
//            names.srcItems = new ArrayList<>(names.availableDictionaryNames.keySet());

            /*
            isoToNameBiMap = HashBiMap.create();

            Arrays.stream(Locale.getISOLanguages()).filter((s) -> availableIsoDictionaries
            .containsKey(s)).forEach((s) -> isoToNameBiMap.put(s,
                    new Locale(s).getDisplayLanguage()));
*/


            //         srcItems = availableIsoDictionaries
            //                .keySet().stream().map((s) -> isoToNameBiMap.get(s)).collect
            //                (Collectors
            //                .toCollection(ArrayList::new));

        } catch (IOException e) {
            e.printStackTrace();
        }

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
        return filename.matches("^[a-z]{2}-[a-z]{2}\\.(.*)");
    }


}
