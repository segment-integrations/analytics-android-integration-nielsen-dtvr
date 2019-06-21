package com.segment.analytics.android.integrations.nielsendtvr;

import android.content.Context;

import com.nielsen.app.sdk.AppSdk;
import com.segment.analytics.Analytics;
import com.segment.analytics.ValueMap;
import com.segment.analytics.integrations.Logger;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.segment.analytics.android.integrations.nielsendtvr.NielsenDTVRIntegrationFactory.SETTING_APP_ID_KEY;
import static com.segment.analytics.android.integrations.nielsendtvr.NielsenDTVRIntegrationFactory.SETTING_ID3_EVENTS_KEY;
import static com.segment.analytics.android.integrations.nielsendtvr.NielsenDTVRIntegrationFactory.SETTING_ID3_PROPERTY_DEFAULT;
import static com.segment.analytics.android.integrations.nielsendtvr.NielsenDTVRIntegrationFactory.SETTING_ID3_PROPERTY_KEY;
import static com.segment.analytics.android.integrations.nielsendtvr.NielsenDTVRIntegrationFactory.SETTING_SF_CODE_KEY;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class NielsenDTVRIntegrationFactoryTest {
    @Mock AppSdk appSdk;
    @Mock Analytics analytics;
    @Mock NielsenDTVRIntegrationFactory factory;

    private ValueMap settings;
    private Map<String, List<AppSdk>> emptyInstances;
    private Map<String, List<AppSdk>> maxInstances;

    private final List<String> id3EventNames = Arrays.asList("sendID3a", "sendID3b");
    private final String appid = "testappid";
    private final String sfcode = "testsfcode";

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        emptyInstances = new HashMap<>();
        maxInstances = new HashMap<>();
        maxInstances.put(appid, new ArrayList<>(Arrays.asList(appSdk, appSdk, appSdk, appSdk)));

        settings = new ValueMap();
        settings.put(SETTING_APP_ID_KEY, appid);
        settings.put(SETTING_SF_CODE_KEY, sfcode);
        settings.put(SETTING_ID3_EVENTS_KEY, id3EventNames);
    }

    @Test
    public void create() {
        when(analytics.getApplication()).thenReturn(null);
        when(factory.create((ValueMap) any(), (Analytics) any())).thenCallRealMethod();

        factory.create(settings, analytics);
        verify(factory).createNielsenIntegration((AppSdk) any(), (Logger) any(), eq(id3EventNames), eq(SETTING_ID3_PROPERTY_DEFAULT));

        settings.put(SETTING_ID3_PROPERTY_KEY, "");
        factory.create(settings, analytics);
        verify(factory, times(2)).createNielsenIntegration((AppSdk) any(), (Logger) any(), eq(id3EventNames), eq(SETTING_ID3_PROPERTY_DEFAULT));

        settings.put(SETTING_ID3_PROPERTY_KEY, null);
        factory.create(settings, analytics);
        verify(factory, times(3)).createNielsenIntegration((AppSdk) any(), (Logger) any(), eq(id3EventNames), eq(SETTING_ID3_PROPERTY_DEFAULT));

        String id3Property = "ID33";
        settings.put(SETTING_ID3_PROPERTY_KEY, id3Property);
        factory.create(settings, analytics);
        verify(factory).createNielsenIntegration((AppSdk) any(), (Logger) any(), eq(id3EventNames), eq(id3Property));
    }

    @Test
    public void reuseAppSdk() {
        NielsenDTVRIntegrationFactory factory = new NielsenDTVRIntegrationFactory();

        assert(factory.reuseAppSdk(appid, emptyInstances) == null);
        assert(factory.reuseAppSdk(appid, maxInstances) == appSdk);
    }

    @Test
    public void parseAppSdkConfig() throws JSONException {
        NielsenDTVRIntegrationFactory factory = new NielsenDTVRIntegrationFactory();
        JSONObject expectedConfig = new JSONObject()
                .put("appid", appid)
                .put("sfcode", sfcode);

        JSONAssert.assertEquals(factory.parseAppSdkConfig(settings), expectedConfig, JSONCompareMode.STRICT);
    }

    @Test
    public void saveAppSdk() {
        NielsenDTVRIntegrationFactory factory = new NielsenDTVRIntegrationFactory();
        List<AppSdk> instancesForId = emptyInstances.get(appid);

        assert(instancesForId == null);

        factory.saveAppSdk(appid, appSdk, emptyInstances);
        instancesForId = emptyInstances.get(appid);
        assert(instancesForId != null && instancesForId.size() == 1);

        factory.saveAppSdk(appid, appSdk, maxInstances);
        instancesForId = maxInstances.get(appid);
        assert(instancesForId != null && instancesForId.size() == 5);
    }

    @Test
    public void fetchAppSdk() {
        when(factory.fetchAppSdk((ValueMap) any(), (Analytics) any(), ArgumentMatchers.<String, List<AppSdk>>anyMap())).thenCallRealMethod();
        when(factory.reuseAppSdk(anyString(), eq(emptyInstances))).thenReturn(null);
        when(factory.reuseAppSdk(anyString(), eq(maxInstances))).thenReturn(appSdk);

        factory.fetchAppSdk(settings, analytics, emptyInstances);

        verify(factory).createAppSdk((Context) any(), (JSONObject) any());
        verify(factory).saveAppSdk(anyString(), (AppSdk) any(), ArgumentMatchers.<String, List<AppSdk>>anyMap());

        reset(factory);
        factory.fetchAppSdk(settings, analytics, maxInstances);
        verify(factory, never()).createAppSdk((Context) any(), (JSONObject) any());
        verify(factory, never()).saveAppSdk(anyString(), (AppSdk) any(), ArgumentMatchers.<String, List<AppSdk>>anyMap());
    }
}