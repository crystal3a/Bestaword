package com.besta.bestaword;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

public class MainActivity extends AppCompatActivity {

    private Button btnRecon;
    private TextView textView2/*, textViewOut*/;
    private SpeechRecognizer recognizer;
    private static final String TAG = "fedword";
    private  QueryRequest qrreq;
    private WebView webView_out;
    private Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
    /** TTS 物件 */
    private TextToSpeech textToSpeech ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnRecon = (Button)findViewById(R.id.btn_query);
        btnRecon.setVisibility(View.INVISIBLE);
        textView2 = (TextView)findViewById(R.id.text_input_word);
        //textViewOut = (TextView)findViewById(R.id.text_output);
        textToSpeech = new TextToSpeech(MainActivity.this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    textToSpeech.setLanguage(Locale.ENGLISH);
                    textToSpeech.setSpeechRate(0.9f);
                }
            }
        });
        qrreq = new QueryRequest();
        webView_out = (WebView)findViewById(R.id.WebView_output);
        webView_out.setWebChromeClient(new WebChromeClient());
        webView_out.getSettings().setJavaScriptEnabled(true);
        webView_out.addJavascriptInterface(new MyJavaScriptInterface(this), "HtmlViewer");
        webView_out.setWebViewClient(new WebViewClient()
        {
            private int running = 0; // Could be public if you want a timer to check.

            @Override
            public boolean shouldOverrideUrlLoading(WebView webView, String urlNewString) {
                running++;
                webView.loadUrl(urlNewString);
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                running = Math.max(running, 1); // First request move it to 1.
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if(--running == 0) { // just "running--;" if you add a timer.
                    // TODO: finished... if you want to fire a method.
                    webView_out.loadUrl("javascript:window.HtmlViewer.showHTML(document.getElementsByTagName('body')[0].innerText);");
                }
            }
        });


        recognizer = SpeechRecognizer.createSpeechRecognizer(this);
        recognizer.setRecognitionListener(new MyRecognizerListener());

        //Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        //設定辨識語言(這邊設定的是繁體中文)
        //intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "zh-TW");
        //intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
        //intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ja-jp");




    }

    protected void onStart(){
        super.onStart();
        recognizer.startListening(intent);
        Log.d("reconizer","Auto Start");

/*
        //按 Button 時，呼叫 SpeechRecognizer 的 startListening()
        //Intent 為傳遞給 SpeechRecognizer 的參數
        btnRecon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ttsGreater21("Please Speaking");
                } else {
                    ttsUnder20("Please Speaking");
                }
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        });
*/
    }

    protected void onDestroy(){
        super.onDestroy();
        recognizer.stopListening();
        recognizer.destroy();
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        recognizer.stopListening();
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }

    }


    class MyJavaScriptInterface
    {
        private Context ctx;
        MyJavaScriptInterface(Context ctx)
        {
            this.ctx = ctx;
        }
        @JavascriptInterface
        public void showHTML(String html)
        {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ttsGreater21(html);
            } else {
                ttsUnder20(html);
            }
            Log.d("HTML",html);
        }
    }

    @SuppressWarnings("deprecation")
    private void ttsUnder20(String text) {
        HashMap<String, String> map = new HashMap<>();
        map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "MessageId");

        StringTokenizer st = new StringTokenizer(text); //切割中英文頻繁切換聲音會糊成一團，JAVA的斷詞也不正確
        String token;
        boolean ischinese = false;

        // TODO Auto-generated method stub
        MainActivity.this.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                Log.d("reconizer","Start");
                recognizer.stopListening();
            }
        });

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        while(st.hasMoreTokens()){
            token = st.nextToken().toString();
            if (token.substring(0,1).matches("[\\u4E00-\\u9FA5]+")){
                if (ischinese == false) {
                    textToSpeech.setLanguage(Locale.CHINESE);
                    ischinese = true;
                    Log.d("Language","Chinese "+token);
                }
            }
            else{
                if(ischinese == true){
                    ischinese = false;
                    textToSpeech.setLanguage(Locale.ENGLISH);
                    Log.d("Language","English "+token);
                }
            }
            textToSpeech.speak(token, TextToSpeech.QUEUE_FLUSH, map);
            while(textToSpeech.isSpeaking()){}
        }

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }



        // TODO Auto-generated method stub
        MainActivity.this.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                Log.d("reconizer","Start");
                recognizer.startListening(intent);

            }
        });
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void ttsGreater21(String text) {
        String utteranceId=this.hashCode() + "";
        StringTokenizer st = new StringTokenizer(text); //切割中英文頻繁切換聲音會糊成一團，JAVA的斷詞也不正確
        String token;
        boolean ischinese = false;

        // TODO Auto-generated method stub
        MainActivity.this.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                Log.d("reconizer","Stop");
                recognizer.stopListening();

            }
        });


        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        while(st.hasMoreTokens()){
            token = st.nextToken().toString();
            if (token.substring(0,1).matches("[\\u4E00-\\u9FA5]+")){
                if (ischinese == false) {
                    textToSpeech.setLanguage(Locale.CHINESE);
                    ischinese = true;
                    Log.d("Language","Chinese "+token);
                }
            }
            else{
                if(ischinese == true){
                    ischinese = false;
                    textToSpeech.setLanguage(Locale.ENGLISH);
                    Log.d("Language","English "+token);
                }
            }
            textToSpeech.speak(token, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
            while(textToSpeech.isSpeaking()){}
        }




        // TODO Auto-generated method stub
        MainActivity.this.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                Log.d("reconizer","Start");
                recognizer.startListening(intent);

            }
        });
    }


    private class MyRecognizerListener implements RecognitionListener {

        @Override
        public void onResults(Bundle results) {
            List<String> resList = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            StringBuffer sb = new StringBuffer();
            String url = "";
            for(String res: resList) {
                sb.append(res.replace(" ", ""));
                Log.d("Res",res);
                if (res.length() > 1) break;
            }
            //url = url + resList.get(0);
            textView2.setText("onResults: " + sb.toString());
            Log.d("RECOGNIZER", "onResults: " + sb.toString());
            url = qrreq.Query(sb.toString(), getApplicationContext()/*, textViewOut*/);
            webView_out.loadUrl(url);
        }

        @Override
        public void onError(int error) {
            Log.d("RECOGNIZER", "Error Code: " + error);
            if(error==7) {
               recognizer.startListening(intent);
            }
        }

        @Override
        public void onReadyForSpeech(Bundle params) {
            Log.d(TAG, "onReadyForSpeech");
        }

        @Override
        public void onBeginningOfSpeech() {
            Log.d(TAG, "onBeginningOfSpeech");
        }

        @Override
        public void onRmsChanged(float rmsdB) {
            Log.d(TAG, "onRmsChanged");
        }

        @Override
        public void onBufferReceived(byte[] buffer) {
            Log.d(TAG, "onBufferReceived");
        }

        @Override
        public void onEndOfSpeech() {
            Log.d(TAG, "onEndofSpeech");
        }

        @Override
        public void onPartialResults(Bundle partialResults) {
            Log.d(TAG, "onPartialResults");
        }

        @Override
        public void onEvent(int eventType, Bundle params) {
            Log.d(TAG, "onEvent " + eventType);
        }
    }
}
