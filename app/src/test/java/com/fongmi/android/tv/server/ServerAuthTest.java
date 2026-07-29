package com.fongmi.android.tv.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.github.catvod.Init;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import fi.iki.elonen.NanoHTTPD;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 28)
public class ServerAuthTest {

    @Before
    public void setUp() {
        Init.set(RuntimeEnvironment.getApplication());
    }

    @Test
    public void getToken_generatesSixDigits() {
        String token = ServerAuth.resetToken();
        assertEquals(6, token.length());
        assertTrue(token.matches("\\d{6}"));
    }

    @Test
    public void verify_acceptsMatchingToken() {
        String token = ServerAuth.resetToken();
        assertTrue(ServerAuth.verify(token));
        assertFalse(ServerAuth.verify("000000"));
    }

    @Test
    public void appendToken_addsQueryParam() {
        ServerAuth.resetToken();
        String url = ServerAuth.appendToken("http://192.168.1.10:9979");
        assertTrue(url.contains("token="));
        assertTrue(url.startsWith("http://192.168.1.10:9979?token="));
    }

    @Test
    public void appendToken_appendsWhenQueryExists() {
        ServerAuth.resetToken();
        String url = ServerAuth.appendToken("http://127.0.0.1:9979?tab=2");
        assertTrue(url.contains("&token="));
    }

    @Test
    public void isPublicPath_allowsStaticAssets() {
        NanoHTTPD.IHTTPSession session = new TestSession(NanoHTTPD.Method.GET);
        assertTrue(ServerAuth.isPublicPath(session, "/"));
        assertTrue(ServerAuth.isPublicPath(session, "/js/script.js"));
        assertFalse(ServerAuth.isPublicPath(session, "/action"));
    }
}
