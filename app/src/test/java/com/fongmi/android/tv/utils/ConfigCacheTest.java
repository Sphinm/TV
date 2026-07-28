package com.fongmi.android.tv.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.fongmi.android.tv.bean.Config;
import com.github.catvod.Init;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 28)
public class ConfigCacheTest {

    @Before
    public void setUp() {
        Init.set(RuntimeEnvironment.getApplication());
    }

    @Test
    public void putAndGet_roundTrip() {
        Config config = Config.create(0, "http://example.com/vod.json");
        ConfigCache.put(config, "{\"sites\":[]}");
        assertEquals("{\"sites\":[]}", ConfigCache.get(config));
    }

    @Test
    public void clear_removesCachedValue() {
        Config config = Config.create(0, "http://example.com/clear.json");
        ConfigCache.put(config, "{\"sites\":[]}");
        ConfigCache.clear(config);
        assertNull(ConfigCache.get(config));
    }
}
