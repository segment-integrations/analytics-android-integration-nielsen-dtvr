package com.segment.analytics.android.integrations.nielsendtvr.sampleapp;

import com.segment.analytics.Analytics;
import com.segment.analytics.Properties;

class AnalyticsController {
    private Analytics analytics;

    AnalyticsController(Analytics analytics) {
        this.analytics = analytics;
    }

    void trackContentStart() {
        analytics.track("Video Content Started",
                new Properties()
                        .putValue("channel", "a")
                        .putValue("load_type", "linear"));
    }

    void trackPlaybackResumed() {
        trackPlay("Video Playback Resumed");
    }

    void trackSeekCompleted() {
        trackPlay("Video Playback Seek Completed");
    }

    void trackBufferCompleted() {
        trackPlay("Video Playback Buffer Completed");
    }

    private void trackPlay(String event) {
        analytics.track(event,
                new Properties()
                        .putValue("channel", "a"));
    }

    void trackPlaybackPaused() {
        analytics.track("Video Playback Paused");
    }

    void trackContentCompleted() {
        analytics.track("Video Content Completed");
    }

    void trackBufferStarted() {
        analytics.track("Video Playback Buffer Started");
    }

    void trackSeekStarted() {
        analytics.track("Video Playback Seek Started");
    }

    void trackApplicationBackgrounded() {
        analytics.track("Application Backgrounded");
    }

    void trackEnd() {
        analytics.track("Video Playback Completed");
    }

    void trackID3(String id3) {
        analytics.track("sendID3",
                new Properties()
                        .putValue("Id3", id3));
    }
}
