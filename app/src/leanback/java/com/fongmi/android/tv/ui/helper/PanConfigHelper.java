package com.fongmi.android.tv.ui.helper;

import android.text.TextUtils;

import com.fongmi.android.tv.App;
import com.fongmi.android.tv.R;
import com.fongmi.android.tv.api.config.VodConfig;
import com.fongmi.android.tv.bean.Site;
import com.fongmi.android.tv.bean.Vod;
import com.fongmi.android.tv.utils.Notify;
import com.fongmi.android.tv.utils.Task;
import com.google.gson.JsonParser;

import java.util.Collections;

public class PanConfigHelper {

    public static final String CONFIG_SITE_KEY = "配置";
    private static final String CONFIG_SITE_API = "csp_Config";

    private PanConfigHelper() {
    }

    public static boolean isConfigSite(Site site) {
        return site != null && (CONFIG_SITE_API.equals(site.getApi()) || CONFIG_SITE_KEY.equals(site.getKey()));
    }

    public static boolean isConfigSite(String key) {
        return CONFIG_SITE_KEY.equals(key);
    }

    public static Site resolveSite(String key) {
        Site site = VodConfig.get().getSite(key);
        if (!TextUtils.isEmpty(site.getKey()) && !TextUtils.isEmpty(site.getApi())) return site;
        if (!CONFIG_SITE_KEY.equals(key)) return site;
        Site ref = VodConfig.get().getSite("夸克");
        if (TextUtils.isEmpty(ref.getKey())) ref = VodConfig.get().getHome();
        return Site.objectFrom(JsonParser.parseString("{\"key\":\"配置\",\"name\":\"配置中心\",\"type\":3,\"api\":\"csp_Config\"}"), ref.getJar());
    }

    public static void openItem(String key, Vod item) {
        if (item == null || TextUtils.isEmpty(item.getId())) return;
        Task.execute(() -> {
            try {
                resolveSite(key).recent().spider().detailContent(Collections.singletonList(item.getId()));
            } catch (Exception e) {
                e.printStackTrace();
                App.post(() -> Notify.show(R.string.vod_pan_config_open_error));
            }
        });
    }
}
