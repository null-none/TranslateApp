package com.lib.libs.mobileappspec;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.StrictMode;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;


public class MobileAppSpec extends AppCompatActivity {

    public TextToSpeech tts;
    public EditText text;
    public TextView result;
    public OkHttpClient client = new OkHttpClient();
    public String firstLang = "en";
    public String secondLang = "ru";
    public Button langs;
    protected static final int REQ_CODE_SPEECH_INPUT = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobile_app_spec);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        String textButton = String.format("%s ↔ %s", firstLang, secondLang);
        langs = (Button) findViewById(R.id.langs);
        langs.setText(textButton);

        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    tts.setLanguage(Locale.getDefault());
                }
            }
        });
    }

    public void swapLang(View view) {
        String temp = firstLang;
        firstLang = secondLang;
        secondLang = temp;
        String textButton = String.format("%s ↔ %s", firstLang, secondLang);
        langs = (Button) findViewById(R.id.langs);
        langs.setText(textButton);
    }

    public void textToSpeech(View view) {
        text = (EditText) findViewById(R.id.translateText);
        Toast.makeText(getApplicationContext(), text.getText().toString(), Toast.LENGTH_SHORT).show();
        tts.speak(text.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
    }

    public void translateText(View view) {
        text = (EditText) findViewById(R.id.translateText);
        result = (TextView) findViewById(R.id.result);

        String url = String.format("http://mymemory.translated.net/api/get?q=%s!&langpair=%s|%s", text.getText().toString(), firstLang, secondLang);
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = null;
        try {
            response = client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            JSONObject jObject = null;
            jObject = new JSONObject(response.body().string());
            JSONObject data = jObject.getJSONObject("responseData");
            result.setText(data.getString("translatedText"));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void speechToText(View view) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (data != null) {
                    ArrayList<String> resultArray = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    text = (EditText) findViewById(R.id.translateText);
                    text.setText(resultArray.get(0).toString());
                }
                break;
            }

        }
    }
}