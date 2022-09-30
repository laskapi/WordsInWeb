package com.gmail.in2horizon.wordsinweb;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
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

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class LanguageManagerDialogFragment extends DialogFragment {

    public static final String TAG = "LanguageManager";
    Map<String, Set<String>> dictionaries;
    private View view;
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

      AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        view = inflater.inflate(R.layout.language_manager, null);



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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        return view;
        }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onViewCreated");
        loadDictionariesList();
        super.onViewCreated(view, savedInstanceState);
    }

    private void loadDictionariesList() {
        dictionaries = new TreeMap<String, Set<String>>();
        final String url = "https://download.wikdict.com/dictionaries/sqlite/2_2022-01";
        Log.d(TAG, "loadDict");
        new LoadDictionariesTask(this).execute(url);
    }




class LoadDictionariesTask extends AsyncTask<String, Integer, Map<String, TreeSet<String>>> {

    private static final String TAG = "LoadDirectoriesTask";
    private final LanguageManagerDialogFragment fragment;

    LoadDictionariesTask(LanguageManagerDialogFragment fragment) {
        this.fragment = fragment;
    }

    @Override
    protected Map<String, TreeSet<String>> doInBackground(String... url) {
        try {
            final URLConnection conn = new URL(url[0]).openConnection();
            final InputStream is = conn.getInputStream();
            return parseHtml(is);

        } catch (IOException e) {
            e.printStackTrace();
        }
        cancel(true);
        return null;
    }

    @Override
    protected void onPostExecute(Map<String, TreeSet<String>> availableIsoDictionaries) {
        populateSpinners(availableIsoDictionaries);
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

    void populateSpinners(Map<String, TreeSet<String>> availableIsoDictionaries) {

        //initialize isoToNameBiMap
        BiMap<String, String> isoToNameBiMap = HashBiMap.create();
        Arrays.stream(Locale.getISOLanguages()).filter((s) -> availableIsoDictionaries.containsKey(s)).forEach((s) -> isoToNameBiMap.put(s,
                new Locale(s).getDisplayLanguage()));
        Log.d(TAG, isoToNameBiMap.toString());


        List<String> srcItems = availableIsoDictionaries
                .keySet().stream().map((s) -> isoToNameBiMap.get(s)).collect(Collectors.toCollection(ArrayList::new));

        Spinner srcSpinner = fragment.requireView().findViewById(R.id.src_spinner);
        ArrayAdapter<String> srcAdapter = new ArrayAdapter<>(fragment.getContext()
                , android.R.layout.simple_spinner_item, srcItems);
        srcAdapter.setDropDownViewResource(
                android.R.layout
                        .simple_spinner_dropdown_item);
        srcSpinner.setAdapter(srcAdapter);

        // srcSpinner.setSelection(0);

        Spinner dstSpinner = fragment.requireView().findViewById(R.id.dst_spinner);
        ArrayAdapter<String> dstAdapter = new ArrayAdapter<>(fragment.getContext()
                , android.R.layout.simple_spinner_item);
        dstAdapter.setDropDownViewResource(
                android.R.layout
                        .simple_spinner_dropdown_item);
        dstSpinner.setAdapter(dstAdapter);


        srcSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                ((ArrayAdapter) dstSpinner.getAdapter()).clear();

                String selectedIso=
                        isoToNameBiMap.inverse().get(parent.getAdapter().getItem(position));
                
                List<String>dstItems=
                        availableIsoDictionaries.get(selectedIso).stream().map(s->isoToNameBiMap.get(s)).collect(Collectors.toCollection(ArrayList::new));
                Log.d(TAG,
                        "selected" +dstItems);
                ((ArrayAdapter<String>) dstSpinner.getAdapter()).addAll(dstItems);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

}}