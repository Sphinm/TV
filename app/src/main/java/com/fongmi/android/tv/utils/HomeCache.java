package com.fongmi.android.tv.utils;

import android.text.TextUtils;

import com.fongmi.android.tv.api.config.VodConfig;
import com.fongmi.android.tv.bean.Config;
import com.fongmi.android.tv.bean.Result;
import com.fongmi.android.tv.bean.Site;
import com.github.catvod.utils.Path;
import com.github.catvod.utils.Util;

import java.io.File;

public class HomeCache {

    private HomeCache() {
    }

    private static File file() {
        Config config = Config.vod();
        Site home = VodConfig.get().getHome();
        String key = config.getUrl() + "_" + (home != null ? home.getKey() : "");
        return Path.cache("home_" + Util.md5(key) + ".json");
    }

    public static Result get() {
        File file = file();
        if (!Path.exists(file)) return null;
        String json = Path.read(file);
        if (TextUtils.isEmpty(json)) return null;
        Result result = Result.fromJson(json);
        return result.getList().isEmpty() ? null : result;
    }

    public static void put(Result result) {
        if (result == null || result.getList().isEmpty()) return;
        Path.write(file(), result.toString().getBytes());
    }

    public static void clear() {
        Path.clear(file());
    }

    public static void clearAll() {
        File[] files = Path.cache().listFiles((dir, name) -> name.startsWith("home_"));
        if (files == null) return;
        for (File file : files) Path.clear(file);
    }
}
