package com.gmail.in2horizon.wordsinweb;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.webkit.ValueCallback;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

interface OnTranslateListener {
    void translate(String src);
}

public class MyWebView extends WebView {
    private static final String TAG = MyWebView.class.getSimpleName();
    private OnTranslateListener onTranslateListener;

    public MyWebView(@NonNull Context context) {
        super(context);
    }

    public MyWebView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MyWebView(@NonNull Context context, @Nullable AttributeSet attrs,
                     int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    public boolean onTouchEvent(MotionEvent e) {
        Log.d(TAG, e.toString());
        if (e.getAction() == MotionEvent.ACTION_UP) {
            getSelection();
        }

        return super.onTouchEvent(e);


    }

    public void setOnGetTranslationListener(OnTranslateListener onTranslateListener) {
        this.onTranslateListener = onTranslateListener;
    }

    private void getSelection() {
        evaluateJavascript("(function(){return window.getSelection().toString" +
                        "()})()",
                new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String text) {
                        text = text.replace("\"", "");
                        if (text.isEmpty() || text.contains(" ")) {
                            return;
                        } else {
                            onTranslateListener.translate(text);
                        }
                    }
                });
    }


/*
    void translate(String text) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://libretranslate.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        TranslationApi service = retrofit.create(TranslationApi.class);
        RequestBody body = new RequestBody(text, "en", "es", "text", "");
        Call<Translation> call = service.getTranslation(body);
        call.enqueue(new Callback<Translation>() {
            @Override
            public void onResponse(Call<Translation> call, Response<Translation> response) {
Log.d("Success",
        response.message()+"::"+response.isSuccessful()+"::"+response.body()+"::"+response);
            }

            @Override
            public void onFailure(Call call, Throwable t) {

            }
        });

    }*/

    private void getElement(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        Log.d(TAG, "coords:" + x + " :: " + y);

        evaluateJavascript("(function(){return document.elementFromPoint(" + x + "," + y + ")" +
                        ".innerHTML })()",
                new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {

                        Log.d(TAG, "Webview selected text: " + value);
                    }
                });


    }


    void prepareDoc() {

        evaluateJavascript(



                "(function(){ var elems=document.getElementsByTagName(\"*\");" +
                        "for (var i = elems.length; i--;) {" +
                        "elems[i].html(elems[i].text().replace(/\\b(\\w+)" +
                        "\\b/g, \"<span>$1</span>\"));}}" +

                        "        $('span').hover(" +
                        "            function() { $('#word').text($(this).css" +
                        "('background-color','#ffff66').text()); }," +
                        "            function() { $('#word').text(''); $" +
                        "(this).css('background-color',''); }" +
                        "        );" +


                        ")()",
                new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {

                        Log.d(TAG, "Webview selected text: " + value);
                    }
                }


        );
    }

    @Override
    protected void finalize() throws Throwable {
        onTranslateListener = null;
        super.finalize();
    }
}
