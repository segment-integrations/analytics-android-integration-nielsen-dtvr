package com.segment.analytics.android.integrations.nielsendtvr;

import android.content.Context;

import com.nielsen.app.sdk.AppSdk;
import com.segment.analytics.Analytics;
import com.segment.analytics.Properties;
import com.segment.analytics.ValueMap;
import com.segment.analytics.integrations.Logger;
import com.segment.analytics.integrations.TrackPayload;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class NielsenDTVRIntegrationTest {
    @Mock AppSdk appSdk;
    @Mock Analytics analytics;
    @Mock NielsenDTVRIntegrationFactory factory;

    private NielsenDTVRIntegration integration;

    private JSONObject matchJSON(JSONObject expected) {
        return argThat(new JSONMatcher(expected));
    }

    class JSONMatcher implements ArgumentMatcher<JSONObject> {
        JSONObject expected;

        JSONMatcher(JSONObject expected) {
            this.expected = expected;
        }

        @Override
        public boolean matches(JSONObject argument) {
            try {
                JSONAssert.assertEquals(expected, argument, JSONCompareMode.STRICT);
                return true;
            } catch (JSONException e) {
                return false;
            }
        }
    }

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        integration = new NielsenDTVRIntegration(appSdk, Logger.with(Analytics.LogLevel.DEBUG));
    }

    @Test
    public void factory() throws Exception {
        when(analytics.getApplication()).thenReturn(null);
        when(factory.create((ValueMap) any(), (Analytics) any())).thenCallRealMethod();

        ValueMap settings = new ValueMap();
        settings.put("appid", "123");
        settings.put("sfcode", "1234");

        factory.create(settings, analytics);

        JSONObject expectedConfig = new JSONObject()
                .put("appid", "123")
                .put("sfcode", "1234");

        verify(factory).createAppSdk((Context) isNull(), matchJSON(expectedConfig));
    }

    @Test
    public void videoContentStarted() throws JSONException {
        Properties properties = new Properties().putValue("channel", "a").putValue("load_type", "linear");
        JSONObject expectedPlayConfig = new JSONObject().put("channelName", "a");
        JSONObject expectedMetadataConfig = new JSONObject()
                .put("channelName", "a")
                .put("adModel", "1")
                .put("type", "content");

        integration.track(
                new TrackPayload.Builder()
                        .event("Video Content Started")
                        .anonymousId("1")
                        .properties(properties)
                        .build());

        verify(appSdk).play(matchJSON(expectedPlayConfig));
        verify(appSdk).loadMetadata(matchJSON(expectedMetadataConfig));
    }

    @Test
    public void videoPlaybackResumed() throws JSONException {
        Properties properties = new Properties().putValue("channel", "ab");
        JSONObject expectedPlayConfig = new JSONObject().put("channelName", "ab");

        integration.track(
                new TrackPayload.Builder()
                        .event("Video Playback Resumed")
                        .anonymousId("1")
                        .properties(properties)
                        .build());

        verify(appSdk).play(matchJSON(expectedPlayConfig));
    }

    @Test
    public void videoPlaybackSeekCompleted() throws JSONException {
        Properties properties = new Properties().putValue("channel", "abc");
        JSONObject expectedPlayConfig = new JSONObject().put("channelName", "abc");

        integration.track(
                new TrackPayload.Builder()
                        .event("Video Playback Seek Completed")
                        .anonymousId("1")
                        .properties(properties)
                        .build());

        verify(appSdk).play(matchJSON(expectedPlayConfig));
    }

    @Test
    public void videoPlaybackBufferCompleted() throws JSONException {
        Properties properties = new Properties().putValue("channel", "abcd");
        JSONObject expectedPlayConfig = new JSONObject().put("channelName", "abcd");

        integration.track(
                new TrackPayload.Builder()
                        .event("Video Playback Buffer Completed")
                        .anonymousId("1")
                        .properties(properties)
                        .build());

        verify(appSdk).play(matchJSON(expectedPlayConfig));
    }

    @Test
    public void videoPlaybackPaused() {
        integration.track(
                new TrackPayload.Builder()
                        .event("Video Playback Paused")
                        .anonymousId("1")
                        .build());

        verify(appSdk).stop();
    }

    @Test
    public void videoPlaybackInterrupted() {
        integration.track(
                new TrackPayload.Builder()
                        .event("Video Playback Interrupted")
                        .anonymousId("1")
                        .build());

        verify(appSdk).stop();
    }

    @Test
    public void videoContentCompleted() {
        integration.track(
                new TrackPayload.Builder()
                        .event("Video Content Completed")
                        .anonymousId("1")
                        .build());

        verify(appSdk).stop();
    }

    @Test
    public void videoPlaybackBufferStarted() {
        integration.track(
                new TrackPayload.Builder()
                        .event("Video Playback Buffer Started")
                        .anonymousId("1")
                        .build());

        verify(appSdk).stop();
    }

    @Test
    public void videoPlaybackSeekStarted() {
        integration.track(
                new TrackPayload.Builder()
                        .event("Video Playback Seek Started")
                        .anonymousId("1")
                        .build());

        verify(appSdk).stop();
    }

    @Test
    public void videoPlaybackCompleted() {
        integration.track(
                new TrackPayload.Builder()
                        .event("Video Playback Completed")
                        .anonymousId("1")
                        .build());

        verify(appSdk).end();
    }

    @Test
    public void applicationBackgrounded() {
        integration.track(
                new TrackPayload.Builder()
                        .event("Application Backgrounded")
                        .anonymousId("1")
                        .build());

        verify(appSdk).stop();
    }
}