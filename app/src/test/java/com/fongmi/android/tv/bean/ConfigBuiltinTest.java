package com.fongmi.android.tv.bean;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ConfigBuiltinTest {

    @Test
    public void builtinVodUrl_usesAssetsScheme() {
        assertEquals("assets://config/vod.json", Config.BUILTIN_VOD_URL);
    }

    @Test
    public void builtinVodName_isChineseLabel() {
        assertEquals("内置配置", Config.BUILTIN_VOD_NAME);
    }
}
