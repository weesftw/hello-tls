package com.weesftw;

import io.javalin.Javalin;
import io.javalin.community.ssl.SslPlugin;

import static java.lang.System.getProperty;

public class Main {

    public static void main(String... args) {
        Javalin.create(config -> {
                    final var tlsPlugin = new SslPlugin(conf -> {
                        conf.sniHostCheck = false;
                        conf.pemFromPath(getProperty("server-crt"), getProperty("server-key"), getProperty("server-secret"));
                        if (mTLS()) {
                            conf.withTrustConfig(trustConfig -> trustConfig.trustStoreFromPath("cert/ca-truststore-empty.p12", ""));
                        }
                    });

                    config.registerPlugin(tlsPlugin);
                })
                .get("/", ctx -> ctx.result("Hello World"))
                .start(8080);
    }

    static boolean mTLS() {
        return Boolean.parseBoolean(getProperty("mtls"));
    }
}
