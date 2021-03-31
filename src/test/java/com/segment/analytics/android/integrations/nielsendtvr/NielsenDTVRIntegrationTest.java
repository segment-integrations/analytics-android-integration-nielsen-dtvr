package com.segment.analytics.android.integrations.nielsendtvr;

import com.nielsen.app.sdk.AppSdk;
import com.segment.analytics.Analytics;
import com.segment.analytics.Properties;
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

import java.util.Arrays;
import java.util.List;

import static com.segment.analytics.android.integrations.nielsendtvr.NielsenDTVRIntegrationFactory.SETTING_ID3_PROPERTY_DEFAULT;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class NielsenDTVRIntegrationTest {
    @Mock AppSdk appSdk;

    private NielsenDTVRIntegration integration;
    private TrackPayload.Builder basePayloadBuilder;

    private JSONObject matchJSON(JSONObject expected) {
        return argThat(new JSONMatcher(expected));
    }

    private final List<String> id3EventNames = Arrays.asList("sendid3a", "sendid3b");

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

        integration = new NielsenDTVRIntegration(appSdk, Logger.with(Analytics.LogLevel.DEBUG), id3EventNames, SETTING_ID3_PROPERTY_DEFAULT);
        basePayloadBuilder = new TrackPayload.Builder().anonymousId("1");
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
                basePayloadBuilder
                        .event("Video Content Started")
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
                basePayloadBuilder
                        .event("Video Playback Resumed")
                        .properties(properties)
                        .build());

        verify(appSdk).play(matchJSON(expectedPlayConfig));
    }

    @Test
    public void videoPlaybackSeekCompleted() throws JSONException {
        Properties properties = new Properties().putValue("channel", "abc");
        JSONObject expectedPlayConfig = new JSONObject().put("channelName", "abc");

        integration.track(
                basePayloadBuilder
                        .event("Video Playback Seek Completed")
                        .properties(properties)
                        .build());

        verify(appSdk).play(matchJSON(expectedPlayConfig));
    }

    @Test
    public void videoPlaybackBufferCompleted() throws JSONException {
        Properties properties = new Properties().putValue("channel", "abcd");
        JSONObject expectedPlayConfig = new JSONObject().put("channelName", "abcd");

        integration.track(
                basePayloadBuilder
                        .event("Video Playback Buffer Completed")
                        .properties(properties)
                        .build());

        verify(appSdk).play(matchJSON(expectedPlayConfig));
    }

    @Test
    public void videoPlaybackPaused() {
        integration.track(
                basePayloadBuilder
                        .event("Video Playback Paused")
                        .build());

        verify(appSdk).stop();
    }

    @Test
    public void videoPlaybackInterrupted() {
        integration.track(
                basePayloadBuilder
                        .event("Video Playback Interrupted")
                        .build());

        verify(appSdk).stop();
    }

    @Test
    public void videoContentCompleted() {
        integration.track(
                basePayloadBuilder
                        .event("Video Content Completed")
                        .build());

        verify(appSdk).stop();
    }

    @Test
    public void videoPlaybackBufferStarted() {
        integration.track(
                basePayloadBuilder
                        .event("Video Playback Buffer Started")
                        .build());

        verify(appSdk).stop();
    }

    @Test
    public void videoPlaybackSeekStarted() {
        integration.track(
                basePayloadBuilder
                        .event("Video Playback Seek Started")
                        .build());

        verify(appSdk).stop();
    }

    @Test
    public void videoPlaybackExited() {
        integration.track(
                basePayloadBuilder
                        .event("Video Playback Exited")
                        .build());

        verify(appSdk).stop();
    }

    @Test
    public void videoPlaybackCompleted() {
        integration.track(
                basePayloadBuilder
                        .event("Video Playback Completed")
                        .build());

        verify(appSdk).stop();
    }

    @Test
    public void applicationBackgrounded() {
        integration.track(
                basePayloadBuilder
                        .event("Application Backgrounded")
                        .build());

        verify(appSdk).stop();
    }

    @Test
    public void sendID3() {
        String id3A = "testid3A";
        String id3B = "testid3b";
        Properties propertiesA = new Properties().putValue(SETTING_ID3_PROPERTY_DEFAULT, id3A);
        Properties propertiesB = new Properties().putValue(SETTING_ID3_PROPERTY_DEFAULT, id3B);
        TrackPayload payloadA = basePayloadBuilder
                .event("sendid3a")
                .properties(propertiesA)
                .build();

        TrackPayload payloadB = basePayloadBuilder
                .event("sendID3a")
                .properties(propertiesB)
                .build();

        integration.track(
                basePayloadBuilder
                        .event("not a sendID3")
                        .properties(propertiesA)
                        .build());
        verify(appSdk, never()).sendID3(anyString());

        integration.track(payloadA);
        integration.track(payloadA);
        verify(appSdk, times(1)).sendID3(id3A);

        integration.track(payloadB);
        verify(appSdk).sendID3(id3B);
        verify(appSdk, times(2)).sendID3(anyString());
    }
}