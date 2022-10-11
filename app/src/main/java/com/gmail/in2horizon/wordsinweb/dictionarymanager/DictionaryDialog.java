package com.gmail.in2horizon.wordsinweb.dictionarymanager;

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
import android.widget.Button;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.gmail.in2horizon.wordsinweb.R;
import com.gmail.in2horizon.wordsinweb.databinding.DictionaryDialogBinding;
import com.gmail.in2horizon.wordsinweb.ui.MyProgressBar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class DictionaryDialog extends DialogFragment implements OnNamesReadyListener,
        View.OnClickListener, AdapterView.OnItemSelectedListener {


    public static final String TAG = "DictionaryDialog";


    private ArrayAdapter<String> srcAdapter;
    private DictionaryNamesProvider.Names names;
    private String srcName, dstName;
    private DictionaryDialogBinding binding;


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {


        binding=DictionaryDialogBinding.inflate(getLayoutInflater());

        View view = binding.getRoot();//inflater.inflate(R.layout.dictionary_dialog, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.language_manager)
                .setView(view)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.d(TAG, "button OK");
                            }
                        });


        List<String> initialSrcList =
                new ArrayList<String>(Arrays.asList(getString(R.string.loading)));
        List<String> initialDstList = new ArrayList<String>(initialSrcList);

        Spinner srcSpinner = view.findViewById(R.id.src_spinner);

        srcAdapter = new ArrayAdapter<String>(getContext()
                , android.R.layout.simple_spinner_item,
                initialSrcList);
        srcAdapter.setDropDownViewResource(android.R.layout
                .simple_spinner_dropdown_item);
        srcSpinner.setAdapter(srcAdapter);


        Spinner dstSpinner = view.findViewById(R.id.dst_spinner);
        ArrayAdapter<String> dstAdapter = new ArrayAdapter<String>(getContext()
                , android.R.layout.simple_spinner_item, initialDstList);
        dstAdapter.setDropDownViewResource(android.R.layout
                .simple_spinner_dropdown_item);
        dstSpinner.setAdapter(dstAdapter);

        srcSpinner.setOnItemSelectedListener(this);
        dstSpinner.setOnItemSelectedListener(this);

        Button uploadBtt = view.findViewById(R.id.upload_dict_btt);
        uploadBtt.setOnClickListener(this);
        Button deleteBtt = view.findViewById(R.id.delete_dict_btt);
        deleteBtt.setOnClickListener(this);



        if (DictionaryNamesProvider.registerOnNamesReadyListener(getContext(),this)) {
            MyProgressBar myProgressBar = view.findViewById(R.id.my_progress_bar);
            myProgressBar.show();

        }

        return builder.create();
    }


    private void updateUiComponents() {
        srcAdapter.clear();
        srcAdapter.addAll(names.availableDictionaryNames.keySet());
        RecyclerView recycler = binding.installedRecycler;
        recycler.setAdapter(new ItemAdapter(getContext(), names.uploadedDictionaries));
        recycler.getAdapter().registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                int selected = ((ItemAdapter) recycler.getAdapter()).getSelected();
                if (selected == -1) {
                    binding.deleteDictBtt.setEnabled(false);
                } else {
                    binding.deleteDictBtt.setEnabled(true);
                }
            }
        });

    }

    @Override
    public void onNamesReady(DictionaryNamesProvider.Names names) {
        this.names = names;
        updateUiComponents();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.upload_dict_btt:
                uploadDictionary();
                break;
            case R.id.delete_dict_btt:
                AlertDialog.Builder builder =
                        new AlertDialog.Builder(getActivity()).setMessage(getString(R.string.delete_dictionary));
                builder.show();
        }
    }

    private void uploadDictionary() {
        String srcIso = names.isoToName.inverse().get(srcName);
        String dstIso = names.isoToName.inverse().get(dstName);
        String filename = srcIso + "-" + dstIso + ".sqlite3";
        MyProgressBar myProgressBar = binding.myProgressBar;//
        myProgressBar.show();

        Handler handler = new Handler(Looper.getMainLooper(), msg -> {
            myProgressBar.setProgress(msg.obj);
            return false;
        });

        RoomDbBuilder.build(getContext(), filename, handler);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position,
                               long id) {
        String value = (String) parent.getAdapter().getItem(position);
        Log.d(TAG, "" + parent.getId() + "::" + R.id.src_spinner + "::" + value);
        switch (parent.getId()) {
            case R.id.src_spinner: {

                if (names != null && names.availableDictionaryNames.containsKey(value)) {
                    Spinner dstSpinner = getDialog().findViewById(R.id.dst_spinner);
                    ArrayAdapter dstAdapter = (ArrayAdapter) dstSpinner.getAdapter();
                    dstAdapter.clear();
                    dstAdapter.addAll(names.availableDictionaryNames.get(value));
                    dstSpinner.setAdapter(dstAdapter);

                }
                MyProgressBar myProgressBar = requireDialog().findViewById(R.id
                        .my_progress_bar);
                myProgressBar.hide();

                srcName = value;
                break;
            }
            case R.id.dst_spinner: {
                dstName = value;
                break;
            }
        }
        Log.d(TAG, srcName + "-" + dstName);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }


}
