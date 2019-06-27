package com.segment.analytics.android.integrations.nielsendtvr;

import com.nielsen.app.sdk.AppSdk;
import com.segment.analytics.Analytics;
import com.segment.analytics.ValueMap;

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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class NielsenDTVRIntegrationFactoryTest {
    @Mock AppSdk appSdk;
    @Mock Analytics analytics;
    @Mock NielsenDTVRIntegrationFactory factoryMock;

    private ValueMap settings;
    private Map<String, List<AppSdk>> emptyInstances;
    private Map<String, List<AppSdk>> maxInstances;
    private NielsenDTVRIntegrationFactory factory;

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

        factory = new NielsenDTVRIntegrationFactory();
    }

    @Test
    public void reuseAppSdk() {
        assertNull(factory.reuseAppSdk(appid, emptyInstances));
        assertEquals(appSdk, factory.reuseAppSdk(appid, maxInstances));
    }

    @Test
    public void parseAppSdkConfig() throws JSONException {
        JSONObject expectedConfig = new JSONObject()
                .put("appid", appid)
                .put("sfcode", sfcode);

        JSONAssert.assertEquals(factory.parseAppSdkConfig(settings), expectedConfig, JSONCompareMode.STRICT);
    }

    @Test
    public void saveAppSdk() {
        List<AppSdk> instancesForId = emptyInstances.get(appid);
        assertNull(instancesForId);

        factory.saveAppSdk(appid, appSdk, emptyInstances);
        instancesForId = emptyInstances.get(appid);
        assertEquals(1, instancesForId.size());

        factory.saveAppSdk(appid, appSdk, maxInstances);
        instancesForId = maxInstances.get(appid);
        assertEquals(5, instancesForId.size());
    }

    @Test
    public void fetchAppSdk() throws JSONException {
        when(factoryMock.fetchAppSdk((ValueMap) any(), (Analytics) any(), ArgumentMatchers.<String, List<AppSdk>>anyMap())).thenCallRealMethod();
        when(factoryMock.reuseAppSdk(anyString(), eq(emptyInstances))).thenReturn(null);
        when(factoryMock.reuseAppSdk(anyString(), eq(maxInstances))).thenReturn(appSdk);

        factoryMock.fetchAppSdk(settings, analytics, emptyInstances);
        verify(factoryMock).saveAppSdk(anyString(), (AppSdk) any(), ArgumentMatchers.<String, List<AppSdk>>anyMap());

        reset(factoryMock);
        factoryMock.fetchAppSdk(settings, analytics, maxInstances);
        verify(factoryMock, never()).saveAppSdk(anyString(), (AppSdk) any(), ArgumentMatchers.<String, List<AppSdk>>anyMap());
    }

    @Test
    public void parseId3EventNames() {
        ValueMap settings = new ValueMap();
        settings.put(SETTING_ID3_EVENTS_KEY, Arrays.asList("sendID3a", "sendID3b"));

        List<String> expected = Arrays.asList("sendid3a", "sendid3b");
        assertEquals(expected, factory.parseId3EventNames(settings));
    }

    @Test
    public void parseId3PropertyName() {
        ValueMap settings = new ValueMap();

        assertEquals(SETTING_ID3_PROPERTY_DEFAULT, factory.parseId3PropertyName(settings));

        settings.put(SETTING_ID3_PROPERTY_KEY, "");
        assertEquals(SETTING_ID3_PROPERTY_DEFAULT, factory.parseId3PropertyName(settings));

        String expected = "testid";
        settings.put(SETTING_ID3_PROPERTY_KEY, expected);
        assertEquals(expected, factory.parseId3PropertyName(settings));
    }
}