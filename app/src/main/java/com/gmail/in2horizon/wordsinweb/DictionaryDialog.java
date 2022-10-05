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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class DictionaryDialog extends DialogFragment implements OnNamesReadyListener {

    public static final String TAG = "DictionaryDialog";

/*
    private static final String ISO_TO_NAME = "ISO_TO_NAME";
    private static final String ISO_TO_NAME_BIMAP = "ISO_TO_NAME_BIMAP";
    private static final String AVAILABLE_DIRECTORY_NAMES = "AVAILABLE_DIRECTORY_NAMES";
    private static final String AVAILABLE_DIRECTORY_ISOS = "AVAILABLE_DIRECTORY_ISOS";
    private static final String SRC_ITEMS = "SRC_ITEMS";
*/

    private MyProgressBar myProgressBar;
    private View view;
    private final List<String> srcItems = new ArrayList<>();
    private final List<String> dstItems = new ArrayList<>();

    private ArrayAdapter<String> srcAdapter;
    private ArrayAdapter<String> dstAdapter;
    private DictionaryNamesProvider.Names names;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        srcItems.add(getString(R.string.loading));
        dstItems.add(getString(R.string.loading));

   /*
                ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        Runnable loadDictionaries = new Runnable() {
            @Override
            public void run() {
                if (savedInstanceState != null) {
                    availableDirectoryIsos =
                            (Map<String, TreeSet<String>>) savedInstanceState.getSerializable(
                                    AVAILABLE_DIRECTORY_ISOS);
                    availableDictionaryNames =
                            (Map<String, TreeSet<String>>) savedInstanceState.getSerializable(
                                    AVAILABLE_DIRECTORY_NAMES);
                    srcItems = savedInstanceState.getStringArrayList(SRC_ITEMS);
                    isoToNameBiMap = (HashBiMap<String, String>) savedInstanceState.getSerializable(
                            ISO_TO_NAME_BIMAP);
                    isoToName = (HashMap<String, String>) savedInstanceState.getSerializable(
                            ISO_TO_NAME);

                } else {
                    loadDictionaries();
                    handler.post(() -> updateSpinners());
                }
            }
        };

        executor.execute(loadDictionaries);
*/
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        Log.d(TAG, "onCreateDialog");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        view = inflater.inflate(R.layout.dictionaries_manager, null);

        Spinner srcSpinner = view.findViewById(R.id.src_spinner);
        srcAdapter = new ArrayAdapter<String>(getContext()
                , android.R.layout.simple_spinner_item,
                srcItems);
        srcAdapter.setDropDownViewResource(android.R.layout
                .simple_spinner_dropdown_item);
        srcSpinner.setAdapter(srcAdapter);

        Spinner dstSpinner = view.findViewById(R.id.dst_spinner);
        dstAdapter = new ArrayAdapter<String>(getContext()
                , android.R.layout.simple_spinner_item, dstItems);
        dstAdapter.setDropDownViewResource(android.R.layout
                .simple_spinner_dropdown_item);
        dstSpinner.setAdapter(dstAdapter);


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
        DictionaryNamesProvider.registerOnNamesReadyListener(this);

        return builder.create();
    }


/*
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putStringArrayList(SRC_ITEMS, (ArrayList<String>) srcItems);
        outState.putSerializable(AVAILABLE_DIRECTORY_ISOS,
                (Serializable) availableDirectoryIsos);
        outState.putSerializable(AVAILABLE_DIRECTORY_NAMES,
                (Serializable) availableDictionaryNames);
        outState.putSerializable(ISO_TO_NAME_BIMAP, isoToNameBiMap);
        outState.putSerializable(ISO_TO_NAME, isoToName);
        super.onSaveInstanceState(outState);
    }
*/

    private void updateSpinners() {

        Spinner srcSpinner = view.findViewById(R.id.src_spinner);
        srcSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String key = (String) parent.getAdapter().getItem(position);
                if (names.availableDictionaryNames.containsKey(key)) {
                    Spinner dstSpinner = getDialog().findViewById(R.id.dst_spinner);
                    dstAdapter.clear();
                    dstAdapter.addAll(names.availableDictionaryNames.get(key));
                    dstSpinner.setSelection(0);
                }
                if (myProgressBar != null) {
                    myProgressBar.hide();
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        srcAdapter.clear();
        srcAdapter.addAll(names.availableDictionaryNames.keySet());


    }


    @Override
    public void onNamesReady(DictionaryNamesProvider.Names names) {
        this.names=names;
        updateSpinners();
    }
}
