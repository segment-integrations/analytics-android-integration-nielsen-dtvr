package com.segment.analytics.android.integrations.nielsendtvr;

import com.segment.analytics.Properties;
import com.segment.analytics.integrations.Integration;
import com.segment.analytics.integrations.Logger;
import com.segment.analytics.integrations.TrackPayload;
import com.nielsen.app.sdk.AppSdk;

import org.json.JSONException;
import org.json.JSONObject;

public class NielsenDTVRIntegration extends Integration<AppSdk> {
    public static final Factory FACTORY = new NielsenDTVRIntegrationFactory();

    private final AppSdk appSdk;
    private final Logger logger;

    NielsenDTVRIntegration(AppSdk appSdk, Logger logger) {
        this.appSdk = appSdk;
        this.logger = logger;
    }

    @Override
    public void track(TrackPayload trackPayload) {
        String event = trackPayload.event();

        switch (event) {
            case "Video Content Started":
                play(trackPayload);
                loadMetadata(trackPayload);
                sendID3(trackPayload);
                break;
            case "Video Playback Resumed":
            case "Video Playback Seek Completed":
            case "Video Playback Buffer Completed":
                play(trackPayload);
                sendID3(trackPayload);
                break;
            case "Video Playback Paused":
            case "Video Playback Interrupted":
            case "Video Content Completed":
            case "Video Playback Buffer Started":
            case "Video Playback Seek Started":
                appSdk.stop();
                break;
            case "Video Playback Completed":
                appSdk.end();
                break;
            case "Application Backgrounded":
                appSdk.stop();
        }
    }

    private void play(TrackPayload trackPayload) {
        Properties properties = trackPayload.properties();
        JSONObject channelInfo = new JSONObject();

        try {
            if (properties.containsKey("channel"))
                channelInfo.put("channelName", properties.getString("channel"));

        } catch (JSONException e) {
            logger.error(e, "Failed to send play event");
        }

        appSdk.play(channelInfo);
    }

    private void loadMetadata(TrackPayload trackPayload) {
        Properties properties = trackPayload.properties();
        JSONObject jsonMetadata = new JSONObject();

        try {
            jsonMetadata.put("type", "content");

            if (properties.containsKey("channel"))
                jsonMetadata.put("channelName", properties.getString("channel"));

            if (properties.containsKey("load_type"))
                jsonMetadata.put("adModel", properties.getString("load_type").equals("linear") ? "1" : "2");

        } catch (JSONException e) {
            logger.error(e, "Failed to send loadMetadata event");
        }

        appSdk.loadMetadata(jsonMetadata);
    }

    // TODO: clarify ID3 behaviour
    private void sendID3(TrackPayload trackPayload) {
        Properties properties = trackPayload.properties();

        appSdk.sendID3(properties.getString("ID3"));
    }

    @Override
    public AppSdk getUnderlyingInstance() {
        return appSdk;
    }
}
