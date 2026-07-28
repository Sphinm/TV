package com.fongmi.android.tv.utils;

import com.fongmi.android.tv.bean.Config;
import com.github.catvod.utils.Path;
import com.github.catvod.utils.Util;

import java.io.File;

public class ConfigCache {

    private ConfigCache() {
    }

    private static File file(Config config) {
        return Path.cache("vod_config_" + Util.md5(config.getUrl()) + ".json");
    }

    public static String get(Config config) {
        File file = file(config);
        return Path.exists(file) ? Path.read(file) : null;
    }

    public static void put(Config config, String json) {
        if (json == null || json.isEmpty()) return;
        Path.write(file(config), json.getBytes());
    }

    public static void clear(Config config) {
        Path.clear(file(config));
    }
}
