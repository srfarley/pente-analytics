package us.pente.graph.auth;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.List;

public final class SiteLogin {
    private SiteLogin() {
    }

    private static final SSLSocketFactory sslSocketFactory = createPenteOrgSSLSocketFactory();

    public static List<String> login(String username, String password) throws IOException {
        HttpsURLConnection connection = null;
        try {
            URL url = new URL("https://www.pente.org/gameServer/index.jsp");
            connection = (HttpsURLConnection) url.openConnection();
            connection.setSSLSocketFactory(sslSocketFactory);
            connection.setConnectTimeout(5000);
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            try (OutputStream out = connection.getOutputStream()) {
                out.write(String.format("name2=%s&password2=%s", username, password).getBytes("UTF-8"));
            }
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException("Unable to authenticate user " + username);
            }
            List<String> cookieHeaders = connection.getHeaderFields().get("Set-Cookie");
            connection.disconnect();
            return cookieHeaders;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public static SSLSocketFactory createPenteOrgSSLSocketFactory() {
        // This is necessary because the root CA used by pente.org is not present in the default trust store that
        // ships with Oracle Java 8.
        // http://stackoverflow.com/questions/34110426/does-java-support-lets-encrypt-certificates
        // https://community.letsencrypt.org/t/will-the-cross-root-cover-trust-by-the-default-list-in-the-jdk-jre/134/40
        try {
            // Certificates downloaded from https://letsencrypt.org/certs/lets-encrypt-x{1-3}-cross-signed.der
            final String[] certResources = new String[]{
                    "lets-encrypt-x1-cross-signed.der",
                    "lets-encrypt-x2-cross-signed.der",
                    "lets-encrypt-x3-cross-signed.der"
            };
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, null);
            int entryNumber = 1;
            for (String certResource : certResources) {
                InputStream in = SiteLogin.class.getResourceAsStream("/" + certResource);
                Certificate cert = CertificateFactory.getInstance("X.509").generateCertificate(in);
                keyStore.setCertificateEntry(Integer.toString(entryNumber++), cert);
            }
            TrustManagerFactory trustManagerFactory =
                    TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustManagerFactory.getTrustManagers(), null);
            return sslContext.getSocketFactory();
        } catch (Exception ex) {
            throw new RuntimeException("Unable to create SSLSocketFactory", ex);
        }
    }
}
