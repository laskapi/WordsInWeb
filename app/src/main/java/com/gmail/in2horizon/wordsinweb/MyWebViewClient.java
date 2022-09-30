package com.gmail.in2horizon.wordsinweb;

import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MyWebViewClient extends WebViewClient {
    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        view.loadUrl("javascript:(function() {\n" +
                "        // wrap words in spans\n" +
                "        $('p').each(function() {\n" +
                "            var $this = $(this);\n" +
                "            $this.html($this.text().replace(/\\b(\\w+)\\b/g, \"<span>$1</span>\"));\n" +
                "        });\n" +
                "\n" +
                "        // bind to each span\n" +
                "        $('p span').hover(\n" +
                "            function() { $('#word').text($(this).css('background-color','#ffff66').text()); },\n" +
                "            function() { $('#word').text(''); $(this).css('background-color',''); }\n" +
                "        );\n" +
                "    })");
    }
}
