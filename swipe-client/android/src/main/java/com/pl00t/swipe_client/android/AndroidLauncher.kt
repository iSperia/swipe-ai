package com.pl00t.swipe_client.android

import android.os.Bundle
import com.badlogic.gdx.backends.android.AndroidApplication
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
import com.pl00t.swipe_client.SwipeGame
import com.pl00t.swipe_client.analytics.AnalyticsInterface
import com.yandex.metrica.YandexMetrica
import com.yandex.metrica.YandexMetricaConfig
import java.util.*

/** Launches the Android application.  */
class AndroidLauncher : AndroidApplication(), AnalyticsInterface {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Creating an extended library configuration.
        val config = YandexMetricaConfig.newConfigBuilder(YMAK)
            .withSessionTimeout(15)
            .withLogs()
            .build()
        // Initializing the AppMetrica SDK.
        YandexMetrica.activate(applicationContext, config)
        // Automatic tracking of user activity.
        YandexMetrica.enableActivityAutoTracking(application)
        val sysDefaultSystemLanguage = Locale.getDefault().language.lowercase(Locale.getDefault())
        val configuration = AndroidApplicationConfiguration()
        configuration.useImmersiveMode = true // Recommended, but not required.
        configuration.numSamples = 2
        initialize(
            SwipeGame(
                sysDefaultSystemLanguage,
                this
            ), configuration
        )
    }

    override fun onPause() {
        super.onPause()
        YandexMetrica.getReporter(applicationContext, YMAK).pauseSession()
    }

    override fun onResume() {
        YandexMetrica.getReporter(applicationContext, YMAK).resumeSession()
        super.onResume()
    }

    override fun trackEvent(event: String, data: Map<String, String>) {
        val dataMapped: MutableMap<String, Any> = HashMap()
        for ((key, value) in data) {
            dataMapped[key] = value
        }
        YandexMetrica.getReporter(applicationContext, YMAK).reportEvent(event, dataMapped)
    }

    override fun trackEvent(event: String) {
        YandexMetrica.getReporter(applicationContext, YMAK).reportEvent(event)
    }

    companion object {
        private const val YMAK = "5c63eabc-0a52-4b2d-ae67-b30aec022e11"
    }
}
