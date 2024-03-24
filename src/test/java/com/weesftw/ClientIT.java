package com.weesftw;

import com.weesftw.client.SSLContextFactory;
import org.junit.jupiter.api.Test;

import javax.net.ssl.SSLParameters;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static java.lang.System.getProperty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ClientIT {

    @Test
    void mtls_client() throws Exception {
        final var sslContext = SSLContextFactory.builder()
                .keystoreBase64(getProperty("keystore"))
                .truststoreBase64(getProperty("truststore"))
                .keystorePassword(getProperty("keystore_password"))
                .truststorePassword(getProperty("truststore_password"))
                .build();

        try (var httpClient = HttpClient.newBuilder().sslParameters(getSslParameters()).sslContext(sslContext).build()) {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create("https://localhost:443"))
                    .GET()
                    .build();
            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            assertNotNull(response);
            assertEquals(200, response.statusCode());
            assertEquals("Hello World", response.body());
        }
    }

    private static SSLParameters getSslParameters() {
        final var sslParams = new SSLParameters();
        sslParams.setProtocols(new String[]{"TLSv1.2", "TLSv1.3"});
        sslParams.setCipherSuites(new String[]{"TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384",
                "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256"}); // priorizar suites que ofereçam proteção contra ataques, como suites com Diffie-Hellman Ephemeral (DHE) ou Elliptic Curve Diffie-Hellman (ECDHE)
        sslParams.setNeedClientAuth(true); // para autenticação mútua de cliente e servidor
        return sslParams;
    }
}