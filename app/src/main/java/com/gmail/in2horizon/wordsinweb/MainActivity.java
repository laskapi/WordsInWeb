package com.gmail.in2horizon.wordsinweb;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.widget.AdapterView;


import com.gmail.in2horizon.wordsinweb.databinding.ActivityMainBinding;
import com.gmail.in2horizon.wordsinweb.dictionarymanager.Dictionary;
import com.gmail.in2horizon.wordsinweb.dictionarymanager.DictionaryManager;
import com.gmail.in2horizon.wordsinweb.ui.DictionaryDialog;
import com.gmail.in2horizon.wordsinweb.ui.CenteredSpinnerObserverAdapter;

import java.util.Arrays;
import java.util.List;
import java.util.Observable;
import java.util.Optional;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private ActivityMainBinding binding;

    private TransitionsMainViewModel mainViewModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mainViewModel = new ViewModelProvider(this).get(TransitionsMainViewModel.class);

        mainViewModel.getTranslation().observe(this,
                dst -> {

                    if (dst != null && !dst.isEmpty()) {
                        Log.d(TAG, "jestem");
                        binding.translationTextview.setText(dst.get(0));
                    }

                });


        DictionaryManager.build(this, new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message msg) {

                DictionaryManager manager = (DictionaryManager) msg.obj;

                List<String> items = prepareDictionarySpinnerData(manager);

                CenteredSpinnerObserverAdapter<String> adapter =
                        new CenteredSpinnerObserverAdapter<String>(MainActivity.this,
                                android.R.layout.simple_spinner_item, items) {

                            @Override
                            public void update(Observable manager, Object arg) {
                                clear();
                                addAll(prepareDictionarySpinnerData((DictionaryManager) manager));
                                notifyDataSetChanged();
                            }
                        };
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                manager.addObserver(adapter);

                binding.dictionariesSpinner.setAdapter(adapter);

                binding.dictionariesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position,
                                               long id) {
                        String name = (String) parent.getAdapter().getItem(position);

                        Optional<Dictionary> optDictionary =
                                manager.getUploadedDictionary4name(name);

                        optDictionary.ifPresent(dictionary ->
                                mainViewModel.setDictionary(getApplicationContext(),
                                        dictionary.getFileName()));
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

                return false;
            }

            private List<String> prepareDictionarySpinnerData(DictionaryManager manager) {
                return manager.getUploadedDictionaryNames().isEmpty() ?
                        Arrays.asList(getString(R.string.upload_dictionary_first)) :
                        manager.getUploadedDictionaryNames();
            }


        }));
    }

    @Override
    protected void onStart() {
        super.onStart();
        home();
    }


    public void home() {
        MyWebView webView = findViewById(R.id.webView);
        WebSettings webSetting = webView.getSettings();
        webSetting.setBuiltInZoomControls(true);
        webSetting.setJavaScriptEnabled(true);
        webView.setOnGetTranslationListener(new OnTranslateListener() {

            @Override
            public void translate(String src) {
                mainViewModel.setSourceWord(src);

            }
        });
        webView.setWebViewClient(new MyWebViewClient());
        webView.loadUrl("https://www.onet.pl");

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {

            case R.id.settings:
                Log.d(TAG, "settings ");
                break;

            case R.id.language_manager:
                DictionaryDialog dialog = new DictionaryDialog();
                dialog.show(getSupportFragmentManager(), DictionaryDialog.TAG);
                break;
            default:
                Log.d(TAG, "No item resolved");
                break;
        }
        return true;

    }


}