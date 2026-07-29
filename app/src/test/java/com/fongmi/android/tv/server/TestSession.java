package com.fongmi.android.tv.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

final class TestSession implements NanoHTTPD.IHTTPSession {

    private final NanoHTTPD server = new NanoHTTPD(8080) {};
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
    public Map<String, List<String>> getParameters() {
        return Collections.emptyMap();
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
    public InputStream getInputStream() {
        return null;
    }

    @Override
    public void parseBody(Map<String, String> files) throws IOException, NanoHTTPD.ResponseException {
    }

    @Override
    public NanoHTTPD.CookieHandler getCookies() {
        return server.new CookieHandler(headers);
    }
}
