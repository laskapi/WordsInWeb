package com.gmail.in2horizon.wordsinweb;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
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
    private static final String BASE_URL = "https://www.google.com/search?q=";
    private ActivityMainBinding binding;
    private TransitionsMainViewModel viewModel;
    private String query;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.searchEdittext.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                binding.searchButton.performClick();
                return true;
            }
            return false;
        });

        binding.searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeSoftInput(binding.searchEdittext);
                query = binding.searchEdittext.getText().toString();
                String url = BASE_URL + query;
                binding.webView.loadUrl(url);

            }
        });

        viewModel = new ViewModelProvider(this).get(TransitionsMainViewModel.class);
        viewModel.getTranslation().observe(this,
                dst -> {
                    if (dst == null || dst.isEmpty()) {
                        return;
                    }
                    dst.replace("|", "\n");
                    binding.dstTextview.setText(dst);

           edw     });
        viewModel.getSourceWord().observe((this), src -> binding.srcTextview.setText(src));
        viewModel.getSearchText().observe(this, search -> binding.searchEdittext.setText(search));

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
                                viewModel.setDictionary(getApplicationContext(),
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

    private void closeSoftInput(View host) {
        InputMethodManager imm = (InputMethodManager)
                getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(host.getWindowToken(), 0);
    }

    @Override
    protected void onStart() {
        super.onStart();
        MyWebView webView = binding.webView;
        WebSettings webSetting = webView.getSettings();
        webSetting.setBuiltInZoomControls(true);
        webSetting.setJavaScriptEnabled(true);
        webView.setOnGetTranslationListener(new OnTranslateListener() {

            @Override
            public void translate(String src) {
                viewModel.setSourceWord(src);

            }
        });
        webView.setWebViewClient(new MyWebViewClient(viewModel));
        webView.loadUrl("https://google.com");

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