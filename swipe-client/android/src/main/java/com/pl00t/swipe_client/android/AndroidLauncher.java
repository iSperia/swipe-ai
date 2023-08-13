package com.pl00t.swipe_client.android;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.pl00t.swipe_client.SwipeGame;
import com.pl00t.swipe_client.analytics.AnalyticsInterface;
import com.yandex.metrica.YandexMetrica;
import com.yandex.metrica.YandexMetricaConfig;

import java.util.HashMap;
import java.util.Map;

/** Launches the Android application. */
public class AndroidLauncher extends AndroidApplication implements AnalyticsInterface {

    private static String YMAK = "5c63eabc-0a52-4b2d-ae67-b30aec022e11";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Creating an extended library configuration.
        YandexMetricaConfig config = YandexMetricaConfig.newConfigBuilder(YMAK)
            .withSessionTimeout(15)
            .withLogs()
            .build();
        // Initializing the AppMetrica SDK.
        YandexMetrica.activate(getApplicationContext(), config);
        // Automatic tracking of user activity.
        YandexMetrica.enableActivityAutoTracking(getApplication());

        AndroidApplicationConfiguration configuration = new AndroidApplicationConfiguration();
        configuration.useImmersiveMode = true; // Recommended, but not required.
        configuration.numSamples = 2;
        initialize(new SwipeGame(this), configuration);
    }

    @Override
    protected void onPause() {
        super.onPause();
        YandexMetrica.getReporter(getApplicationContext(), YMAK).pauseSession();
    }

    @Override
    protected void onResume() {
        YandexMetrica.getReporter(getApplicationContext(), YMAK).resumeSession();
        super.onResume();
    }

    @Override
    public void trackEvent(String event, Map<String, String> data) {
        Map<String, Object> dataMapped = new HashMap<>();
        for (Map.Entry<String, String> entry : data.entrySet()) {
            dataMapped.put(entry.getKey(), entry.getValue());
        }
        YandexMetrica.getReporter(getApplicationContext(), YMAK).reportEvent(event, dataMapped);
    }

    @Override
    public void trackEvent(String event) {
        YandexMetrica.getReporter(getApplicationContext(), YMAK).reportEvent(event);
    }

}
