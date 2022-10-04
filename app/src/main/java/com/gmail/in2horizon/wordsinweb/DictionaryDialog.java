package com.gmail.in2horizon.wordsinweb;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.gmail.in2horizon.wordsinweb.ui.MyProgressBar;
import com.google.common.collect.HashBiMap;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
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

public class DictionaryDialog extends DialogFragment {

    public static final String TAG = "DictionaryDialog";
    private static final String URL = "https://download.wikdict.com/dictionaries/sqlite/2_2022-01";

    private View view;
    private List<String> srcItems=new ArrayList<>();
    private List<String> dstItems=new ArrayList<>();
    private Map<String, TreeSet<String>> availableIsoDictionaries;
    private HashBiMap<String, String> isoToNameBiMap;
    private MyProgressBar myProgressBar;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        srcItems.add(getString(R.string.loading));
        dstItems.add(getString(R.string.loading));

        Runnable loadDictionaries = new Runnable() {
            @Override
            public void run() {
                if (savedInstanceState != null) {
                    availableIsoDictionaries =
                            (Map<String, TreeSet<String>>) savedInstanceState.getSerializable(
                                    "availableIsoDictionaries");
                    srcItems = savedInstanceState.getStringArrayList("srcItems");
                    isoToNameBiMap = (HashBiMap<String, String>) savedInstanceState.getSerializable(
                            "isoToNameBiMap");


                } else {
                    loadDictionaries();
                }
                handler.post(() -> updateSpinners());
            }
        };

        executor.execute(loadDictionaries);

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {


        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        view = inflater.inflate(R.layout.dictionaries_manager, null);

        if (savedInstanceState == null) {
            myProgressBar = view.findViewById(R.id.my_progress_bar);
            myProgressBar.show();
        }


        builder.setTitle(R.string.language_manager)
                .setView(view)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "button OK");
                    }
                });

        return builder.create();
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putStringArrayList("srcItems", (ArrayList<String>) srcItems);
        outState.putSerializable("availableIsoDictionaries",
                (Serializable) availableIsoDictionaries);
        outState.putSerializable("isoToNameBiMap", isoToNameBiMap);
        super.onSaveInstanceState(outState);
    }

    private void updateSpinners() {

        Spinner srcSpinner = view.findViewById(R.id.src_spinner);
        ArrayAdapter<String> srcAdapter = new ArrayAdapter<String>(getContext()
                , android.R.layout.simple_spinner_item,
                srcItems);
        srcAdapter.setDropDownViewResource(
                android.R.layout
                        .simple_spinner_dropdown_item);
        srcSpinner.setAdapter(srcAdapter);

        Spinner dstSpinner = view.findViewById(R.id.dst_spinner);
        ArrayAdapter<String> dstAdapter = new ArrayAdapter<String>(getContext()
                , android.R.layout.simple_spinner_item,dstItems);
        dstAdapter.setDropDownViewResource(
                android.R.layout
                        .simple_spinner_dropdown_item);
        dstSpinner.setAdapter(dstAdapter);

        srcSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                dstSpinner.getAdapter().clear();
                String selectedIso =
                        isoToNameBiMap.inverse().get(parent.getAdapter().getItem(position));

                dstItems =
                        availableIsoDictionaries.get(selectedIso).stream().map(s -> isoToNameBiMap.get(s)).collect(Collectors.toCollection(ArrayList::new));
                dstSpinner.getAdapter().addAll(dstItems);

                if (myProgressBar != null) {
                    myProgressBar.hide();
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


    }

    private void loadDictionaries() {

        final URLConnection conn;

        try {

            conn = new URL(URL).openConnection();
            final InputStream is = conn.getInputStream();
            availableIsoDictionaries = parseHtml(is);

            isoToNameBiMap = HashBiMap.create();
            Arrays.stream(Locale.getISOLanguages()).filter((s) -> availableIsoDictionaries.containsKey(s)).forEach((s) -> isoToNameBiMap.put(s,
                    new Locale(s).getDisplayLanguage()));

            srcItems = availableIsoDictionaries
                    .keySet().stream().map((s) -> isoToNameBiMap.get(s)).collect(Collectors.toCollection(ArrayList::new));

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private Map<String, TreeSet<String>> parseHtml(InputStream is) throws IOException {
        Map<String, TreeSet<String>> availableIsoDictionaries = new TreeMap<>();

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
                                FillDictionaries(availableIsoDictionaries, dictionaryFilename);
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
        return availableIsoDictionaries;
    }

    private boolean isValidDictionaryName(String filename) {
        return filename.matches("^[a-z]{2}-[a-z]{2}\\.(.*)");
    }


    private void FillDictionaries(Map<String, TreeSet<String>> availableIsoDictionaries,
                                  String filename) {
        String src = filename.substring(0, 2);
        String dst = filename.substring(3, 5);

        availableIsoDictionaries.computeIfAbsent(src, k -> new TreeSet<>()).add(dst);

    }


}
