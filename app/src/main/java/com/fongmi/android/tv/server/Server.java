package com.fongmi.android.tv.server;

import com.fongmi.android.tv.service.PlaybackService;
import com.fongmi.android.tv.utils.Task;
import com.github.catvod.Proxy;
import com.github.catvod.utils.Util;

public class Server {

    private volatile PlaybackService service;
    private volatile Nano nanoLocal;
    private volatile Nano nanoRemote;
    private int localPort = -1;
    private int remotePort = -1;

    private static class Loader {
        static volatile Server INSTANCE = new Server();
    }

    public static Server get() {
        return Loader.INSTANCE;
    }

    public PlaybackService getService() {
        return service;
    }

    public void setService(PlaybackService service) {
        this.service = service;
    }

    public int getLocalPort() {
        return localPort;
    }

    public int getRemotePort() {
        return remotePort;
    }

    public String getAddress() {
        return ServerAuth.appendToken(getPublicBase());
    }

    public String getAddress(int tab) {
        return getAddress() + "&tab=" + tab;
    }

    public String getAddress(String path) {
        return getAddress(true) + path;
    }

    public String getPublicBase() {
        if (remotePort < 0) return "";
        return "http://" + Util.getIp() + ":" + remotePort;
    }

    public String getAddress(boolean local) {
        int port = local ? localPort : remotePort;
        if (port < 0) return "";
        if (local) return "http://127.0.0.1:" + port;
        return ServerAuth.appendToken(getPublicBase());
    }

    public synchronized void start() {
        if (nanoLocal != null) return;
        ServerAuth.getToken();
        for (int i = 9978; i < 9998; i++) {
            try {
                nanoLocal = new Nano("127.0.0.1", i, false);
                nanoLocal.start(500);
                localPort = i;
                Proxy.set(i);
                break;
            } catch (Throwable e) {
                nanoLocal = null;
            }
        }
        int start = localPort > 0 ? localPort + 1 : 9979;
        for (int i = start; i < 9999; i++) {
            try {
                nanoRemote = new Nano(null, i, true);
                nanoRemote.start(500);
                remotePort = i;
                break;
            } catch (Throwable e) {
                nanoRemote = null;
            }
        }
    }

    public void stop() {
        Task.execute(() -> {
            if (nanoLocal != null) nanoLocal.stop();
            if (nanoRemote != null) nanoRemote.stop();
            service = null;
            nanoLocal = null;
            nanoRemote = null;
            localPort = -1;
            remotePort = -1;
        });
    }
}
