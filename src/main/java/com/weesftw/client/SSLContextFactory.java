package com.weesftw.client;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Base64;

public class SSLContextFactory {

    private final String keystoreBase64, truststoreBase64;
    private final char[] keystorePassword, truststorePassword;

    SSLContextFactory(String keystoreBase64, String truststoreBase64, String keystorePassword, String truststorePassword) {
        this.keystoreBase64 = keystoreBase64;
        this.truststoreBase64 = truststoreBase64;
        this.keystorePassword = keystorePassword == null ?
                "".toCharArray() : keystorePassword.toCharArray();
        this.truststorePassword = truststorePassword == null ?
                "".toCharArray() : truststorePassword.toCharArray();
    }

    public SSLContext createSSLContext() {
        try {
            final var sslContext = SSLContext.getInstance("TLSv1.3");
            final var keystoreFactory = this.loadKeystore();
            if (truststoreBase64 == null) {
                sslContext.init(keystoreFactory.getKeyManagers(), null, new SecureRandom());
                return sslContext;
            }

            final var truststoreFactory = this.loadTruststore();
            sslContext.init(keystoreFactory.getKeyManagers(), truststoreFactory.getTrustManagers(), new SecureRandom());
            return sslContext;
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Load the keystore (Clients certificate)
     *
     * @return KeyManagerFactory instance
     */
    private KeyManagerFactory loadKeystore() {
        try (InputStream fis = new ByteArrayInputStream(Base64.getDecoder().decode(keystoreBase64))) {
            final var keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            final var keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(fis, keystorePassword);

            keyManagerFactory.init(keyStore, keystorePassword);
            return keyManagerFactory;
        } catch (IOException | NoSuchAlgorithmException | CertificateException | UnrecoverableKeyException | KeyStoreException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Load the truststore (CAs certificate)
     *
     * @return TrustManagerFactory instance
     */
    private TrustManagerFactory loadTruststore() {
        try (final var fis = new ByteArrayInputStream(Base64.getDecoder().decode(truststoreBase64))) {
            final var trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            final var trustStore = KeyStore.getInstance("PKCS12");
            trustStore.load(fis, truststorePassword);

            trustManagerFactory.init(trustStore);
            return trustManagerFactory;
        } catch (NoSuchAlgorithmException | CertificateException | KeyStoreException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String keystoreBase64, truststoreBase64, keystorePassword, truststorePassword;

        public Builder keystoreBase64(String keystoreBase64) {
            this.keystoreBase64 = keystoreBase64;
            return this;
        }

        public Builder truststoreBase64(String truststoreBase64) {
            this.truststoreBase64 = truststoreBase64;
            return this;
        }

        public Builder keystorePassword(String keystorePassword) {
            this.keystorePassword = keystorePassword;
            return this;
        }

        public Builder truststorePassword(String truststorePassword) {
            this.truststorePassword = truststorePassword;
            return this;
        }

        public SSLContext build() {
            return new SSLContextFactory(keystoreBase64, truststoreBase64, keystorePassword, truststorePassword).createSSLContext();
        }
    }
}
