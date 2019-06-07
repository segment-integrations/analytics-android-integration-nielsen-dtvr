package com.segment.analytics.android.integrations.nielsendtvr;

import android.content.Context;

import com.segment.analytics.Analytics;
import com.segment.analytics.ValueMap;
import com.segment.analytics.integrations.Integration;
import com.segment.analytics.integrations.Logger;
import com.nielsen.app.sdk.AppSdk;

import org.json.JSONException;
import org.json.JSONObject;

class NielsenDTVRIntegrationFactory implements Integration.Factory {

    private static final String NIELSEN_DTVR_KEY = "Nielsen DTVR";

    @Override
    public Integration<AppSdk> create(ValueMap settings, Analytics analytics) {
        Context appContext = analytics.getApplication();
        JSONObject appSdkConfig = new JSONObject();
        Logger logger = analytics.logger(NIELSEN_DTVR_KEY);

        try {
            appSdkConfig
                    .put("appid", settings.getString("appid"))
                    .put("sfcode", settings.getString("sfcode"));
        } catch (JSONException e) {
            logger.error(e, "Failed to initialize Nielsen SDK");
            return null;
        }

        AppSdk appSdk = createAppSdk(appContext, appSdkConfig);

        return new NielsenDTVRIntegration(appSdk, logger);
    }

    @Override
    public String key() {
        return NIELSEN_DTVR_KEY;
    }

    // object construction method to facilitate testing
    AppSdk createAppSdk(Context appContext, JSONObject appSdkConfig) {
        return new AppSdk(appContext, appSdkConfig, null);
    }
}
