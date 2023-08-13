package com.pl00t.swipe_client.android;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.gameanalytics.sdk.GameAnalytics;
import com.pl00t.swipe_client.SwipeGame;
import com.pl00t.swipe_client.analytics.AnalyticsInterface;

import java.util.Map;

/** Launches the Android application. */
public class AndroidLauncher extends AndroidApplication implements AnalyticsInterface {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PackageManager pm = getPackageManager();
        try {
            PackageInfo info = pm.getPackageInfo(getPackageName(), 0);
            GameAnalytics.setEnabledInfoLog(true);
            GameAnalytics.setEnabledVerboseLog(true);
            GameAnalytics.configureBuild(info.versionName);
            GameAnalytics.initializeWithGameKey("7e317788e1340f0d6eaa3e65b4d42cc2", "1b95a324841fd91773967d880b4bc98442c8f242");
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }



        AndroidApplicationConfiguration configuration = new AndroidApplicationConfiguration();
        configuration.useImmersiveMode = true; // Recommended, but not required.
        configuration.numSamples = 2;
        initialize(new SwipeGame(this), configuration);
    }

    @Override
    public void trackEvent(String event, Map<String, String> data) {
        GameAnalytics.addDesignEventWithEventId(event);
    }

    @Override
    public void trackEvent(String event) {
        GameAnalytics.addDesignEventWithEventId(event);
    }

}
