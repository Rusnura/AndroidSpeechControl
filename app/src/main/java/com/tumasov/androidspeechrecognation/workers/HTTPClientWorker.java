package com.tumasov.androidspeechrecognation.workers;

import android.os.AsyncTask;
import android.util.Log;
import com.tumasov.androidspeechrecognation.clients.HTTPClient;
import java.util.concurrent.ExecutionException;


public class HTTPClientWorker {
    private static class SenderRequest extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            try {
                return HTTPClient.sendGetRequest(strings[0]);
            } catch (Exception e) {
                return null;
            }
        }
    }

    public String sendGetRequest(String url) {
        try {
            return (new SenderRequest()).execute(url).get();
        } catch (InterruptedException | ExecutionException e) {
            Log.e("HTTPCLIENTWORKER", "Произошла ошибка выполнения: " + e);
            return null;
        }
    }
}
