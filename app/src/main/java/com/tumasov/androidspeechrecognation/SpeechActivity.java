package com.tumasov.androidspeechrecognation;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.tumasov.androidspeechrecognation.workers.HTTPClientWorker;
import com.tumasov.androidspeechrecognation.workers.VoiceHelper;
import java.util.ArrayList;
import github.com.vikramezhil.dks.speech.Dks;
import github.com.vikramezhil.dks.speech.DksListener;

public class SpeechActivity extends AppCompatActivity implements View.OnClickListener, DksListener {
    private final String TAG = "SP_RECOGNIZER";
    private final String RELAY_SERVER = "192.168.1.247"; // IP адрес ESP8266, который управляет реле
    private Dks droidSpeech;
    private TextView finalSpeechResult;
    private Button start, stop;
    private VoiceHelper voiceHelper;
    private HTTPClientWorker httpClientWorker;
    private final int MY_PERMISSIONS_RECORD_AUDIO = 17;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialization of recognize
        droidSpeech = new Dks(getApplication(), getSupportFragmentManager(), this);
        // droidSpeech.setOnDroidSpeechListener(this);
        // droidSpeech.setShowRecognitionProgressView(true);
        droidSpeech.setOneStepResultVerify(true);
        droidSpeech.setContinuousSpeechRecognition(true);
        // droidSpeech.setOfflineSpeechRecognition(true);
        // droidSpeech.setRecognitionProgressMsgColor(Color.WHITE);
        // droidSpeech.setOneStepVerifyConfirmTextColor(Color.WHITE);
        // droidSpeech.setOneStepVerifyRetryTextColor(Color.WHITE);

        finalSpeechResult = findViewById(R.id.finalSpeechResult);
        start = findViewById(R.id.start);
        start.setOnClickListener(this);
        stop = findViewById(R.id.stop);
        stop.setOnClickListener(this);
        stop.setVisibility(View.GONE);

        // Initialization of voice assistant
        voiceHelper = new VoiceHelper(getApplicationContext());

        // Initialization of http client
        httpClientWorker = new HTTPClientWorker();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (stop.getVisibility() == View.VISIBLE) {
            stop.performClick();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (stop.getVisibility() == View.VISIBLE) {
            stop.performClick();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.start: {
                requestAudioPermissions();
                break;
            }

            case R.id.stop: {
                droidSpeech.closeSpeechOperations();
                stop.setVisibility(View.GONE);
                start.setVisibility(View.VISIBLE);
                break;
            }
        }
    }

    // Данный метод работает после того, как android распознал целую фразу
    @Override
    public void onDksFinalSpeechResult(String finalSpeechResult) {
        if (finalSpeechResult == null || finalSpeechResult.trim().equals("")) return;
        finalSpeechResult = finalSpeechResult.toLowerCase();
        Log.i(TAG, "Live FINAL speech result = " + finalSpeechResult);
        String result = null;
        if (finalSpeechResult.contains("свет")) {
            if (finalSpeechResult.contains("вкл")) {
                result = httpClientWorker.sendGetRequest(RELAY_SERVER + "/on");
            } else if (finalSpeechResult.contains("выкл")) {
                result = httpClientWorker.sendGetRequest(RELAY_SERVER + "/off");
            }
        }

        if (result != null) {
            Log.i(TAG, "RESULT: " + result);
            voiceHelper.talk(result);
            while (voiceHelper.isSpeaking()) {
                // ждём, пока ассистент договорит
            }
        }
        this.finalSpeechResult.setText(finalSpeechResult);
    }

    @Override
    public void onDksLanguagesAvailable(String currentSpeechLanguage, ArrayList<String> supportedSpeechLanguages) {
        Log.i(TAG, "Current language = " + currentSpeechLanguage);
        Log.i(TAG, "List of supporting languages = " + supportedSpeechLanguages.toString());

        // Try to set Russian language
        if (supportedSpeechLanguages.contains("ru-RU"))  {
            droidSpeech.setCurrentSpeechLanguage("ru-RU");
        } else {
            displayToastMessageIfPresent("Русский язык не поддерживается в вашей системе :(");
        }
    }

    @Override
    public void onDksLiveSpeechFrequency(float v) {
        // FIXME
    }

    // Данный метод работает после того, как android распознал слова, но ещё не полностью фразу
    @Override
    public void onDksLiveSpeechResult(String liveSpeechResult) {
        Log.i(TAG, "Результат распознавания в режиме OnLine = " + liveSpeechResult);
    }

    @Override
    public void onDksSpeechError(String message) {
        // Ошибка распознавания - останавливаем прослущивание
        displayToastMessageIfPresent(message);
        stop.post(() -> stop.performClick());
    }

    private void displayToastMessageIfPresent(String message) {
        if (message != null && !"".equals(message.trim()))
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void requestAudioPermissions() {
        boolean accessToMicroHasGranted = true;
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) {
                    Toast.makeText(this, "Пожалуйста, разрешите доступ к микрофону!", Toast.LENGTH_LONG).show();
                }
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, MY_PERMISSIONS_RECORD_AUDIO);
            }

            accessToMicroHasGranted = (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED);
        }

        if (accessToMicroHasGranted) {
            droidSpeech.startSpeechRecognition();
            start.setVisibility(View.GONE);
            stop.setVisibility(View.VISIBLE);
        }
    }
}