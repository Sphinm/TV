package com.fongmi.android.tv.server;

import android.text.TextUtils;

import com.github.catvod.utils.Prefers;

import java.util.Map;
import java.util.Random;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;

public final class ServerAuth {

    private static final String KEY = "server_token";

    private ServerAuth() {
    }

    public static String getToken() {
        String token = Prefers.getString(KEY, "");
        if (token.length() != 6) token = resetToken();
        return token;
    }

    public static String resetToken() {
        int code = 100000 + new Random().nextInt(900000);
        String token = String.valueOf(code);
        Prefers.put(KEY, token);
        return token;
    }

    public static boolean verify(String token) {
        return !TextUtils.isEmpty(token) && token.equals(getToken());
    }

    public static String fromSession(IHTTPSession session) {
        Map<String, String> params = session.getParms();
        String token = params.get("token");
        if (TextUtils.isEmpty(token)) token = session.getHeaders().get("x-auth-token");
        return token == null ? "" : token;
    }

    public static boolean isAuthorized(IHTTPSession session) {
        return verify(fromSession(session));
    }

    public static boolean isLocalhost(IHTTPSession session) {
        String remote = session.getRemoteIpAddress();
        if (remote == null) return false;
        return remote.equals("127.0.0.1") || remote.equals("::1") || remote.startsWith("127.");
    }

    public static String appendToken(String url) {
        String token = getToken();
        return url.contains("?") ? url + "&token=" + token : url + "?token=" + token;
    }

    public static boolean isPublicPath(IHTTPSession session, String url) {
        if (!"GET".equals(session.getMethod().name())) return false;
        if (url.equals("/") || url.equals("/index.html")) return true;
        return url.endsWith(".css") || url.endsWith(".js") || url.endsWith(".html") || url.startsWith("/css/") || url.startsWith("/js/");
    }
}
