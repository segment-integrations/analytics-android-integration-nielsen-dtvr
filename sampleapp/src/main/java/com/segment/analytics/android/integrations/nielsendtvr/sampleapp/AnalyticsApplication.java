package com.segment.analytics.android.integrations.nielsendtvr.sampleapp;

import android.app.Application;

import com.segment.analytics.android.integrations.nielsendtvr.sampleapp.BuildConfig;
import com.segment.analytics.Analytics;
import com.segment.analytics.android.integrations.nielsendtvr.NielsenDTVRIntegration;

public class AnalyticsApplication extends Application {
    private final static String WRITE_KEY = BuildConfig.WRITE_KEY;
    private AnalyticsController analyticsController;

    @Override
    public void onCreate() {
        super.onCreate();
        initAnalytics();
    }

    private void initAnalytics() {
        analyticsController = new AnalyticsController(
                new Analytics.Builder(this, WRITE_KEY)
                        .logLevel(Analytics.LogLevel.VERBOSE)
                        .use(NielsenDTVRIntegration.FACTORY)
                        .build());
    }

    public AnalyticsController getAnalytics() {
        return analyticsController;
    }
}
