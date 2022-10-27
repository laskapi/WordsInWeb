package com.gmail.in2horizon.wordsinweb.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.gmail.in2horizon.wordsinweb.R;
import com.gmail.in2horizon.wordsinweb.databinding.DictionaryDialogBinding;
import com.gmail.in2horizon.wordsinweb.dictionarymanager.DictionaryManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;


public class DictionaryDialog extends DialogFragment {

    public static final String TAG = "DictionaryDialog";

    private String srcName, dstName;
    private DictionaryDialogBinding binding;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        binding = DictionaryDialogBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();

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

        Handler handler = new Handler(Looper.getMainLooper(), new DictionaryManagerCallback());
        if (DictionaryManager.build(getContext(), handler)) {
            binding.myProgressBar.show();
        }

        return builder.create();
    }


    private class DictionaryManagerCallback implements Handler.Callback {
        private DictionaryManager manager;
        private String dictionary2Delete;

        @Override
        public boolean handleMessage(@NonNull Message msg) {

            manager = (DictionaryManager) msg.obj;

            SpinnerItemSelectedListener spinnerItemSelectedListener =
                    new SpinnerItemSelectedListener(manager);

            ArrayList<String> srcNames =
                    new ArrayList<>(manager.getAvailableDictionarySourceNames());
            CenteredSpinnerAdapter<String> srcAdapter =
                    new CenteredSpinnerAdapter<>(getContext()
                            , android.R.layout.simple_spinner_item,
                            srcNames);
            srcAdapter.setDropDownViewResource(android.R.layout
                    .simple_spinner_dropdown_item);
            binding.srcSpinner.setAdapter(srcAdapter);
            binding.srcSpinner.setOnItemSelectedListener(spinnerItemSelectedListener);

            ArrayList<String> initialItem =
                    new ArrayList<String>(Collections.singletonList(getString(R.string.loading)));
            CenteredSpinnerAdapter<String> dstAdapter =
                    new CenteredSpinnerAdapter<>(getContext()
                            , android.R.layout.simple_spinner_item, initialItem);
            dstAdapter.setDropDownViewResource(android.R.layout
                    .simple_spinner_dropdown_item);
            binding.dstSpinner.setAdapter(dstAdapter);
            binding.dstSpinner.setOnItemSelectedListener(spinnerItemSelectedListener);


            RecyclerView recycler = binding.uploadedDictionariesRecycler;
            recycler.setAdapter(new RecyclerItemAdapter(getContext(),
                    manager.getUploadedDictionaryNames()));
            recycler.getAdapter().registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                @Override
                public void onChanged() {
                    int selected = ((RecyclerItemAdapter) recycler.getAdapter()).getSelected();
                    if (selected == -1) {
                        binding.deleteDictBtt.setEnabled(false);
                    } else {
                        dictionary2Delete =
                                manager.getUploadedDictionaryNames().get(selected);
                        binding.deleteDictBtt.setEnabled(true);
                    }
                }
            });

            binding.uploadDictBtt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Handler handler = new Handler(Looper.getMainLooper(), msg -> {
                        binding.myProgressBar.setProgress(msg.obj);
                        if (msg.obj == null) {
                            updateAvailableAndUploadedUiListComponents();
                        }
                        return true;
                    });
                    manager.uploadDictionary(srcName, dstName, handler);
                    binding.myProgressBar.show();

                }
            });

            binding.deleteDictBtt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder =
                            new AlertDialog.Builder(getActivity())
                                    .setMessage(getString(R.string.delete_dictionary))
                                    .setPositiveButton(android.R.string.ok,
                                            (dialog, which) -> {
                                                if (manager.deleteDictionary(dictionary2Delete)) {
                                                    updateAvailableAndUploadedUiListComponents();
                                                }
                                            })
                                    .setNegativeButton(android.R.string.cancel,
                                            (dialog, which) -> dialog.dismiss());
                    builder.show();

                }
            });

            binding.myProgressBar.hide();
            return false;
        }

        private void updateAvailableAndUploadedUiListComponents() {
            RecyclerItemAdapter recyclerItemAdapter =
                    (RecyclerItemAdapter) binding.uploadedDictionariesRecycler.getAdapter();
            recyclerItemAdapter.setDataSet(manager.getUploadedDictionaryNames());
            recyclerItemAdapter.notifyDataSetChanged();

            ArrayList<String> srcNames =
                    manager.getAvailableDictionarySourceNames();
            CenteredSpinnerAdapter<String> arrayAdapter =
                    (CenteredSpinnerAdapter<String>) binding.srcSpinner.getAdapter();
            arrayAdapter.clear();
            arrayAdapter.addAll(srcNames);
            binding.srcSpinner.setAdapter(arrayAdapter);

        }

    }

    private class SpinnerItemSelectedListener implements AdapterView.OnItemSelectedListener {

        private final DictionaryManager manager;

        public SpinnerItemSelectedListener(DictionaryManager manager) {
            this.manager = manager;
        }

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            String value = (String) parent.getAdapter().getItem(position);

            switch (parent.getId()) {
                case R.id.src_spinner: {

                    ArrayList<String> dstNames = manager.getAvailableDictionaryDstNames(value);
                    CenteredSpinnerAdapter<String> dstAdapter =
                            (CenteredSpinnerAdapter<String>) binding.dstSpinner.getAdapter();
                    dstAdapter.clear();
                    dstAdapter.addAll(dstNames);
                    binding.dstSpinner.setAdapter(dstAdapter);
                    srcName = value;

                    break;
                }
                case R.id.dst_spinner: {
                    dstName = value;
                    break;
                }
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    }
}
