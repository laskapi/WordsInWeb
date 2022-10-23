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
import java.util.List;


public class DictionaryDialog extends DialogFragment implements
        View.OnClickListener {


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


        List<String> initialSrcList = new ArrayList<>();
        initialSrcList.add(getString(R.string.from_language));

        List<String> initialDstList = new ArrayList<>();
        initialDstList.add(getString(R.string.to_language));

        ArrayAdapter<String> srcAdapter = new ArrayAdapter<>(getContext()
                , android.R.layout.simple_spinner_item,
                initialSrcList);
        srcAdapter.setDropDownViewResource(android.R.layout
                .simple_spinner_dropdown_item);
        binding.srcSpinner.setAdapter(srcAdapter);


        ArrayAdapter<String> dstAdapter = new ArrayAdapter<String>(getContext()
                , android.R.layout.simple_spinner_item, initialDstList);
        dstAdapter.setDropDownViewResource(android.R.layout
                .simple_spinner_dropdown_item);
        binding.dstSpinner.setAdapter(dstAdapter);

        Button uploadBtt = view.findViewById(R.id.upload_dict_btt);
        uploadBtt.setOnClickListener(this);
        Button deleteBtt = view.findViewById(R.id.delete_dict_btt);
        deleteBtt.setOnClickListener(this);

        Handler handler = new Handler(Looper.getMainLooper(), new DictionaryManagerCallback());
        if (DictionaryManager.build(getContext(), handler)) {
            binding.myProgressBar.show();
        }

        return builder.create();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.upload_dict_btt:
                //uploadDictionary();
                break;
            case R.id.delete_dict_btt:
                AlertDialog.Builder builder =
                        new AlertDialog.Builder(getActivity())
                                .setMessage(getString(R.string.delete_dictionary))
                                .setPositiveButton(R.string.delete_dict_btt, (dialog, which) -> {

                                });
                builder.show();
        }
    }


    private class DictionaryManagerCallback implements Handler.Callback {
        private DictionaryManager manager;
        private String dictionary2Delete;

        @Override
        public boolean handleMessage(@NonNull Message msg) {
            manager = (DictionaryManager) msg.obj;

            binding.myProgressBar.hide();
            ArrayAdapter<String> adapter = (ArrayAdapter<String>) binding.srcSpinner.getAdapter();
            ArrayList<String> srcNames = new ArrayList<>();
            srcNames.add(adapter.getItem(0));
            srcNames.addAll(manager.getAvailableDictionarySourceNames());
            adapter.clear();
            adapter.addAll(srcNames);
            binding.srcSpinner.setAdapter(adapter);

            ItemSelectedLister itemSelectedLister = new ItemSelectedLister(manager);
            binding.srcSpinner.setOnItemSelectedListener(itemSelectedLister);
            binding.dstSpinner.setOnItemSelectedListener(itemSelectedLister);


            RecyclerView recycler = binding.installedRecycler;
            recycler.setAdapter(new ItemAdapter(getContext(),
                    manager.getUploadedDictionaryNames()));
            recycler.getAdapter().registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                @Override
                public void onChanged() {
                    int selected = ((ItemAdapter) recycler.getAdapter()).getSelected();
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
                            ItemAdapter itemAdapter =
                                    (ItemAdapter) binding.installedRecycler.getAdapter();
                            itemAdapter.setDataSet(manager.getUploadedDictionaryNames());
                            itemAdapter.notifyDataSetChanged();

                            ArrayAdapter<String> arrayAdapter =
                                    (ArrayAdapter<String>) binding.srcSpinner.getAdapter();
                            ArrayList<String> srcNames = new ArrayList<>();
                            srcNames.add(arrayAdapter.getItem(0));
                            srcNames.addAll(manager.getAvailableDictionarySourceNames());
                            arrayAdapter.clear();
                            arrayAdapter.addAll(srcNames);
                            binding.srcSpinner.setAdapter(arrayAdapter);


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

                                                    ItemAdapter itemAdapter =
                                                            (ItemAdapter) binding.installedRecycler.getAdapter();
                                                        itemAdapter.deselect();
                                                    itemAdapter.setDataSet(manager.getUploadedDictionaryNames());
                                                    itemAdapter.notifyDataSetChanged();
                                                }

                                            });
                    builder.show();

                }
            });
            return false;
        }
    }

    private class ItemSelectedLister implements AdapterView.OnItemSelectedListener {

        private DictionaryManager manager;

        public ItemSelectedLister(DictionaryManager manager) {
            this.manager = manager;
        }

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            String value = (String) parent.getAdapter().getItem(position);
            Log.d(TAG, "" + parent.getId() + "::" + R.id.src_spinner + "::" + value);
            switch (parent.getId()) {
                case R.id.src_spinner: {

                    ArrayAdapter<String> adapter = (ArrayAdapter) binding.dstSpinner.getAdapter();
                    ArrayList<String> dstNames = new ArrayList<>();
                    dstNames.add(adapter.getItem(0));
                    dstNames.addAll(manager.getAvailableDictionaryDstNames(value));
                    adapter.clear();
                    adapter.addAll(dstNames);
                    binding.dstSpinner.setAdapter(adapter);
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
