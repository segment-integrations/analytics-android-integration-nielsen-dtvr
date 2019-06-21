package com.segment.analytics.android.integrations.nielsendtvr;

import android.content.Context;

import com.segment.analytics.Analytics;
import com.segment.analytics.ValueMap;
import com.segment.analytics.integrations.Integration;
import com.segment.analytics.integrations.Logger;
import com.nielsen.app.sdk.AppSdk;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class NielsenDTVRIntegrationFactory implements Integration.Factory {

    private static final String NIELSEN_DTVR_KEY = "Nielsen DTVR";
    private static final Map<String, List<AppSdk>> appSdkInstances = new HashMap<>();
    private static final int MAX_INSTANCES = 4;

    static final String SETTING_APP_ID_KEY = "appId";
    static final String SETTING_SF_CODE_KEY = "sfcode";
    static final String SETTING_ID3_EVENTS_KEY = "sendId3Events";
    static final String SETTING_ID3_PROPERTY_KEY = "id3Property";
    static final String SETTING_ID3_PROPERTY_DEFAULT = "Id3";

    @Override
    public Integration<AppSdk> create(ValueMap settings, Analytics analytics) {
        Logger logger = analytics.logger(NIELSEN_DTVR_KEY);
        AppSdk appSdk = fetchAppSdk(settings, analytics, appSdkInstances);

        @SuppressWarnings("unchecked")
        List<String> id3EventNames = (List<String>) settings.get(SETTING_ID3_EVENTS_KEY);
        String id3PropertyName = settings.containsKey(SETTING_ID3_PROPERTY_KEY) ? settings.getString(SETTING_ID3_PROPERTY_KEY) : SETTING_ID3_PROPERTY_DEFAULT;

        return createNielsenIntegration(appSdk, logger, id3EventNames, id3PropertyName);
    }

    @Override
    public String key() {
        return NIELSEN_DTVR_KEY;
    }

    /**
     * reuse an existing AppSdk instance if {@link #MAX_INSTANCES} instances already exist,
     * otherwise creates a new AppSdk instance and saves it for future reference
     *
     * @param settings integration settings
     * @param analytics analytics object provided to the factory
     * @return AppSdk instance to use in integration
     */
    AppSdk fetchAppSdk(ValueMap settings, Analytics analytics, Map<String, List<AppSdk>> appSdkInstances) {
        String appId = settings.getString(SETTING_APP_ID_KEY);

        AppSdk appSdk = reuseAppSdk(appId, appSdkInstances);
        if (appSdk == null) {
            Context appContext = analytics.getApplication();

            try {
                JSONObject appSdkConfig = parseAppSdkConfig(settings);

                appSdk = createAppSdk(appContext, appSdkConfig);
                saveAppSdk(appId, appSdk, appSdkInstances);
            } catch (JSONException e) {
                Logger.with(Analytics.LogLevel.DEBUG).error(e, "Failed to initialize Nielsen SDK");
                return null;
            }
        }

        return appSdk;
    }

    /**
     * reuse an existing AppSdk instance if {@link #MAX_INSTANCES} instances already exist
     *
     * @param appId fetch instances for the given app id
     * @param appSdkInstances a map of app ids and their list of AppSdk instances
     * @return the first existing AppSdk instance or null if max instances has not been reached
     */
    AppSdk reuseAppSdk(String appId, Map<String, List<AppSdk>> appSdkInstances) {
        List<AppSdk> instancesForId = appSdkInstances.get(appId);

        if (instancesForId != null && instancesForId.size() >= MAX_INSTANCES) {
            return instancesForId.get(0);
        }

        return null;
    }

    /**
     * object construction method to facilitate testing;
     * creates a new AppSdk instance
     *
     * @param appContext application context
     * @param appSdkConfig parameters for initializing the Nielsen App SDK
     * @return new instance of an AppSdk
     */
    AppSdk createAppSdk(Context appContext, JSONObject appSdkConfig) {
        return new AppSdk(appContext, appSdkConfig, null);
    }

    /**
     * parses integration settings into a JSON config for Nielsen App SDK initialization
     *
     * @param settings integration settings
     * @throws JSONException if error trying to parse settings
     * @return JSON config object for initializing the Nielsen App SDK
     */
    JSONObject parseAppSdkConfig(ValueMap settings) throws JSONException {
        JSONObject appSdkConfig = new JSONObject();
        appSdkConfig
                .put("appid", settings.getString(SETTING_APP_ID_KEY))
                .put("sfcode", settings.getString(SETTING_SF_CODE_KEY));

        return appSdkConfig;
    }

    /**
     * save a newly created AppSdk instance
     *
     * @param appId save AppSdk instance to the given app id
     * @param appSdk the AppSdk instance to save
     * @param appSdkInstances a map of app ids and their list of AppSdk instances
     */
    void saveAppSdk(String appId, AppSdk appSdk, Map<String, List<AppSdk>> appSdkInstances) {
        List<AppSdk> instancesForId = appSdkInstances.get(appId);

        if (instancesForId == null) {
            instancesForId = new ArrayList<>();
            appSdkInstances.put(appId, instancesForId);
        }

        instancesForId.add(appSdk);
    }

    /**
     * object construction method to facilitate testing;
     *
     * @param appSdk Nielsen App Sdk instance to be used in integration
     * @param logger logger for debugging
     * @param id3EventNames list of event names that should map to a sendID3 event
     * @param id3PropertyName property name to fetch the id3 value from
     * @return new instance of an Nielsen DTVR Integration
     */
    Integration<AppSdk> createNielsenIntegration(AppSdk appSdk, Logger logger, List<String> id3EventNames, String id3PropertyName) {
        return new NielsenDTVRIntegration(appSdk, logger, id3EventNames, id3PropertyName);
    }
}
