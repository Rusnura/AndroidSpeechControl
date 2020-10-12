package com.tumasov.androidspeechrecognation.workers;

import android.content.Context;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.Locale;

public class VoiceHelper implements TextToSpeech.OnInitListener {
    private TextToSpeech tts;

    public VoiceHelper(Context appContext) {
        tts = new TextToSpeech(appContext, this);
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            Locale locale = new Locale("ru");
            int result = tts.setLanguage(locale);

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "Извините, Ваш язык не поддерживается");
            }
        } else {
            Log.e("TTS", "Неопознанная ошибка! [STATUS}: " + status);
        }
    }

    public void talk(String text) {
        Log.d("TALK", "Говорю: " + text);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
            } else {
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
            }
        } catch (Exception e) {
            Log.e("Synth error", "Ошибка синтазатора речи: " + e.getMessage());
        }
    }

    public boolean isSpeaking() {
        return tts.isSpeaking();
    }
}
