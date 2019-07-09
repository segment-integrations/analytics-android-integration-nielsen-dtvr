package com.segment.analytics.android.integrations.nielsendtvr.sampleapp;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.VideoView;

public class AnalyticsVideoView extends VideoView {

    private AnalyticsController analytics;
    private boolean isResuming = false;

    public AnalyticsVideoView(Context context) {
        super(context);
    }

    public AnalyticsVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void start() {
        if (isResuming) {
            analytics.trackPlaybackResumed();
        } else {
            analytics.trackContentStart();
            isResuming = true;
        }

        super.start();
    }

    @Override
    public void pause() {
        analytics.trackPlaybackPaused();
        super.pause();
    }

    @Override
    public void seekTo(int msec) {
        analytics.trackSeekStarted();
        super.seekTo(msec);
    }

    public void setAnalytics(AnalyticsController analytics) {
        this.analytics = analytics;
    }

    public void clearState() {
        isResuming = false;
    }

    public void setResuming() {
        isResuming = true;
    }

    public boolean isResuming() {
        return isResuming;
    }

}
