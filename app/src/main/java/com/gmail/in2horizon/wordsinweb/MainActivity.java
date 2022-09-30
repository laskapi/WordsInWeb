package com.gmail.in2horizon.wordsinweb;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.content.AsyncQueryHandler;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.WebSettings;

import com.gmail.in2horizon.wordsinweb.database.Translation;
import com.gmail.in2horizon.wordsinweb.database.TranslationDao;
import com.gmail.in2horizon.wordsinweb.database.WiwDatabase;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import retrofit2.http.HTTP;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "WiW";
    private MyWebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        webView = findViewById(R.id.webView1);

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
                LanguageManagerDialogFragment lm= new LanguageManagerDialogFragment();
                lm.show(getSupportFragmentManager(),LanguageManagerDialogFragment.TAG);
            default:
                Log.d(TAG, "No item resolved");
                break;
        }
        return true;

    }

    @Override
    protected void onStart() {
        super.onStart();
        home();
    }


    public void home() {
        WebSettings webSetting = webView.getSettings();
        webSetting.setBuiltInZoomControls(true);
        webSetting.setJavaScriptEnabled(true);

        webView.setWebViewClient(new MyWebViewClient());
        webView.loadUrl("https://www.onet.pl");


    }

}