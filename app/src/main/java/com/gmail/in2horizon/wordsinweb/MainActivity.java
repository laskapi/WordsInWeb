package com.gmail.in2horizon.wordsinweb;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.WebSettings;

import com.gmail.in2horizon.wordsinweb.ui.DictionaryDialog;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "WiW";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firstInit();
    }

    private boolean firstInit() {

        //--check if first time
        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        String key = getString(R.string.app_name);
        if (prefs.contains(key)) {
            //        return false;
        }
        prefs.edit().putBoolean(key, true).apply();
//        dbInit();

        //     loadList();
        return true;
    }


    private void dbInit() {

   /*     WiwDatabase db = Room.databaseBuilder(getApplicationContext(),
                WiwDatabase.class, "languages")
                .createFromAsset("db/3.sqlite3")
                .build();
        TranslationDao td=db.translationDao();
        AsyncTask.execute(()->{
            Translation result=td.translate("work");
            Log.i(TAG,result.pl);

        });*/

        /*
    BufferedInputStream is= new BufferedInputStream(getResources().openRawResource(R.raw.isolang));
        InputStreamReader isr= new InputStreamReader(is);
    BufferedReader br=new BufferedReader(isr);
    br.lines().map(x->new Language(x))
        try {
            Log.d(TAG,br.readLine());
        } catch (IOException e) {
            e.printStackTrace();
        }
*/

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

    @Override
    protected void onStart() {
        super.onStart();

        runDictionaryManagerForTest();
        home();
    }

    private void runDictionaryManagerForTest() {
        DictionaryDialog dialog = new DictionaryDialog();
        dialog.show(getSupportFragmentManager(), DictionaryDialog.TAG);
    }


    public void home() {
        MyWebView webView = findViewById(R.id.webView1);
        WebSettings webSetting = webView.getSettings();
        webSetting.setBuiltInZoomControls(true);
        webSetting.setJavaScriptEnabled(true);

        webView.setWebViewClient(new MyWebViewClient());
        webView.loadUrl("https://www.onet.pl");


    }

}