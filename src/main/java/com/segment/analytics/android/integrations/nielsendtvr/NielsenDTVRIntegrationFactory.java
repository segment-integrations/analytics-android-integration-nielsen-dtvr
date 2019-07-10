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

import static com.segment.analytics.internal.Utils.isNullOrEmpty;

class NielsenDTVRIntegrationFactory implements Integration.Factory {

  private static final String NIELSEN_DTVR_KEY = "Nielsen DTVR";
  private static final Map<String, List<AppSdk>> appSdkInstances = new HashMap<>();
  private static final int MAX_INSTANCES = 4;

  static final String SETTING_APP_ID_KEY = "appId";
  static final String SETTING_SF_CODE_KEY = "sfcode";
  static final String SETTING_ID3_EVENTS_KEY = "sendId3Events";
  static final String SETTING_ID3_PROPERTY_KEY = "id3Property";
  static final String SETTING_ID3_PROPERTY_DEFAULT = "id3";
  static final String SETTING_DEBUG_KEY = "debug";

  @Override
  public Integration<AppSdk> create(ValueMap settings, Analytics analytics) {
    Logger logger = analytics.logger(NIELSEN_DTVR_KEY);
    AppSdk appSdk;

    try {
      appSdk = fetchAppSdk(settings, analytics, appSdkInstances);
    } catch (JSONException e) {
      logger.error(e, "Failed to initialize Nielsen SDK");
      return null;
    }

    List<String> id3EventNames = parseId3EventNames(settings);
    String id3PropertyName = parseId3PropertyName(settings);

    return new NielsenDTVRIntegration(appSdk, logger, id3EventNames, id3PropertyName);
  }

  @Override
  public String key() {
    return NIELSEN_DTVR_KEY;
  }

  /**
   * reuse an existing AppSdk instance if {@link #MAX_INSTANCES} instances already exist, otherwise
   * creates a new AppSdk instance and saves it for future reference
   *
   * @param settings integration settings
   * @param analytics analytics object provided to the factory
   * @return AppSdk instance to use in integration
   */
  AppSdk fetchAppSdk(
      ValueMap settings, Analytics analytics, Map<String, List<AppSdk>> appSdkInstances)
      throws JSONException {
    String appId = settings.getString(SETTING_APP_ID_KEY);

    AppSdk appSdk = reuseAppSdk(appId, appSdkInstances);
    if (appSdk == null) {
      Context appContext = analytics.getApplication();

      JSONObject appSdkConfig = parseAppSdkConfig(settings);

      appSdk = new AppSdk(appContext, appSdkConfig, null);
      saveAppSdk(appId, appSdk, appSdkInstances);
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

    if (settings.getBoolean(SETTING_DEBUG_KEY, false)) {
      appSdkConfig.put("nol_devDebug", "DEBUG");
    }

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
   * retrieves lowercase list of id3 event names from settings
   *
   * @param settings integration settings
   * @return list of lower case id3 event names
   */
  List<String> parseId3EventNames(ValueMap settings) {
    @SuppressWarnings("unchecked")
    List<String> id3EventNames = (List<String>) settings.get(SETTING_ID3_EVENTS_KEY);

    for (int i = 0; i < id3EventNames.size(); ++i) {
      id3EventNames.set(i, id3EventNames.get(i).toLowerCase());
    }

    return id3EventNames;
  }

  /**
   * retrieves id3 property name from settings, otherwise uses default value
   *
   * @param settings integration settings
   * @return custom id3 property name, otherwise {@link #SETTING_ID3_PROPERTY_DEFAULT}
   */
  String parseId3PropertyName(ValueMap settings) {
    String id3Property = settings.getString(SETTING_ID3_PROPERTY_KEY);

    return isNullOrEmpty(id3Property) ? SETTING_ID3_PROPERTY_DEFAULT : id3Property;
  }
}
