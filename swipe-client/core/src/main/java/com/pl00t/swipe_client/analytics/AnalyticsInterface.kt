package com.pl00t.swipe_client.analytics

interface AnalyticsInterface {

    fun trackEvent(event: String, data: Map<String, String>)

    fun trackEvent(event: String)
}
