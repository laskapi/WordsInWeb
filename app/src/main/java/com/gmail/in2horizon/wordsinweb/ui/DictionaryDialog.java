package com.gmail.in2horizon.wordsinweb.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gmail.in2horizon.wordsinweb.R;
import com.gmail.in2horizon.wordsinweb.databinding.DictionaryDialogBinding;
import com.gmail.in2horizon.wordsinweb.dictionarymanager.DictionaryManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Observable;
import java.util.Observer;


public class DictionaryDialog extends DialogFragment implements Observer {

    public static final String TAG = "DictionaryDialog";

    private String srcName, dstName;
    private DictionaryDialogBinding binding;
    private AlertDialog dialog;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        binding = DictionaryDialogBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();


        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view)
                .setPositiveButton(R.string.close,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.d(TAG, "button OK");
                            }
                        })
                .setCancelable(false);

        Handler handler = new Handler(Looper.getMainLooper(), new DictionaryManagerCallback());
        if (DictionaryManager.build(getContext(), handler)) {
            binding.myProgressBar.show();
        }
        dialog = builder.create();

        return dialog;
    }

    @Override
    public void show(@NonNull FragmentManager manager, @Nullable String tag) {
        super.show(manager, tag);
        manager.executePendingTransactions();
        Rect displayRectangle = new Rect();

        int height = (int) (getResources().getDisplayMetrics().heightPixels * 0.9);

        if (getDialog() != null) {
            getDialog().getWindow().setLayout(
                    getDialog().getWindow().getAttributes().width, height);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        dialog.getButton(Dialog.BUTTON_POSITIVE).setEnabled(false);
    }

    @Override
    public void update(Observable o, Object arg) {
        DictionaryManager manager = (DictionaryManager) o;
        if (manager.getUploadedDictionaryNames().isEmpty()) {

            dialog.getButton(Dialog.BUTTON_POSITIVE).setEnabled(false);
            binding.deleteDictBtt.setVisibility(View.INVISIBLE);
            binding.youHaveToUpload.setVisibility(View.VISIBLE);
        } else {
            dialog.getButton(Dialog.BUTTON_POSITIVE).setEnabled(true);
            binding.deleteDictBtt.setVisibility(View.VISIBLE);
            binding.youHaveToUpload.setVisibility(View.GONE);

        }
    }

    private class DictionaryManagerCallback implements Handler.Callback {
        private DictionaryManager manager;
        private String dictionary2Delete;

        @Override
        public boolean handleMessage(@NonNull Message msg) {

            manager = (DictionaryManager) msg.obj;
            manager.addObserver(DictionaryDialog.this);
            update(manager, null);
            SpinnerItemSelectedListener spinnerItemSelectedListener =
                    new SpinnerItemSelectedListener(manager);

            ArrayList<String> srcNames =
                    new ArrayList<>(manager.getAvailableDictionarySourceNames());
            GravitatedSpinnerAdapter<String> srcAdapter =
                    new GravitatedSpinnerAdapter<>(getContext()
                            , android.R.layout.simple_spinner_item,
                            srcNames);
            srcAdapter.setDropDownViewResource(android.R.layout
                    .simple_spinner_dropdown_item);
            binding.srcSpinner.setAdapter(srcAdapter);
            binding.srcSpinner.setOnItemSelectedListener(spinnerItemSelectedListener);

            ArrayList<String> initialItem =
                    new ArrayList<String>(Collections.singletonList(getString(R.string.loading)));
            GravitatedSpinnerAdapter<String> dstAdapter =
                    new GravitatedSpinnerAdapter<>(getContext()
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

            GravitatedSpinnerAdapter<String> arrayAdapter =
                    (GravitatedSpinnerAdapter<String>) binding.srcSpinner.getAdapter();
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
                    GravitatedSpinnerAdapter<String> dstAdapter =
                            (GravitatedSpinnerAdapter<String>) binding.dstSpinner.getAdapter();
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
