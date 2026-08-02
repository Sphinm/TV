package com.fongmi.android.tv.ui.helper;

import android.os.Environment;

import androidx.fragment.app.FragmentActivity;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.utils.Notify;
import com.fongmi.android.tv.utils.PermissionUtil;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;

public class PanStorageHelper {

    private static final String TVBOX_DIR = "TVBox";
    private static final String QUARK_COOKIE_FILE = ".quark_cookie.txt";

    private PanStorageHelper() {
    }

    public static void runWithStorage(FragmentActivity activity, Runnable action) {
        PermissionUtil.requestFile(activity, granted -> {
            if (granted) action.run();
            else Notify.show(R.string.vod_pan_storage_denied);
        });
    }

    public static boolean hasQuarkCookie() {
        File file = getQuarkCookieFile();
        if (!file.exists() || file.length() == 0) return false;
        try (FileInputStream in = new FileInputStream(file)) {
            byte[] data = new byte[(int) Math.min(file.length(), 4096)];
            int read = in.read(data);
            if (read <= 0) return false;
            String content = new String(data, 0, read, StandardCharsets.UTF_8).trim();
            return !content.isEmpty() && !content.equals("{}");
        } catch (Exception e) {
            return false;
        }
    }

    public static File getQuarkCookieFile() {
        return new File(new File(Environment.getExternalStorageDirectory(), TVBOX_DIR), QUARK_COOKIE_FILE);
    }
}
