package com.fongmi.android.tv.server;

import java.util.HashMap;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

final class TestSession implements NanoHTTPD.IHTTPSession {

    private final NanoHTTPD.Method method;
    private final Map<String, String> params = new HashMap<>();
    private final Map<String, String> headers = new HashMap<>();

    TestSession(NanoHTTPD.Method method) {
        this.method = method;
    }

    @Override
    public void execute() {
    }

    @Override
    public Map<String, String> getHeaders() {
        return headers;
    }

    @Override
    public String getQueryParameterString() {
        return "";
    }

    @Override
    public Map<String, String> getParms() {
        return params;
    }

    @Override
    public String getRemoteIpAddress() {
        return "127.0.0.1";
    }

    @Override
    public String getRemoteHostName() {
        return "localhost";
    }

    @Override
    public String getUri() {
        return "/";
    }

    @Override
    public NanoHTTPD.Method getMethod() {
        return method;
    }

    @Override
    public java.io.InputStream getInputStream() {
        return null;
    }

    @Override
    public NanoHTTPD.CookieHandler getCookies() {
        return new NanoHTTPD.CookieHandler(getHeaders());
    }
}
