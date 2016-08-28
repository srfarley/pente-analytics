package us.pente.analytics.fetch;

import org.springframework.http.client.SimpleClientHttpRequestFactory;
import us.pente.graph.auth.SiteLogin;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.HttpURLConnection;

public class PenteOrgClientHttpRequestFactory  extends SimpleClientHttpRequestFactory {
    private static final SSLSocketFactory sslSocketFactory = SiteLogin.createPenteOrgSSLSocketFactory();

    @Override
    protected void prepareConnection(HttpURLConnection connection, String httpMethod) throws IOException {
        if (connection instanceof HttpsURLConnection) {
            ((HttpsURLConnection) connection).setSSLSocketFactory(sslSocketFactory);
        }
        super.prepareConnection(connection, httpMethod);
    }
}
