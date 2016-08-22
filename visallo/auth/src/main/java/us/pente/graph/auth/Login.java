package us.pente.graph.auth;

import com.google.common.base.Throwables;
import com.google.inject.Inject;
import com.v5analytics.webster.ParameterizedHandler;
import com.v5analytics.webster.annotations.Handle;
import com.v5analytics.webster.annotations.Required;
import org.json.JSONObject;
import org.visallo.core.exception.VisalloAccessDeniedException;
import org.visallo.core.model.user.UserNameAuthorizationContext;
import org.visallo.core.model.user.UserRepository;
import org.visallo.core.user.User;
import org.visallo.web.AuthenticationHandler;
import org.visallo.web.CurrentUser;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.List;

public class Login implements ParameterizedHandler {

    private final UserRepository userRepository;
    private final SSLSocketFactory sslSocketFactory;

    @Inject
    public Login(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.sslSocketFactory = createPenteOrgSSLSocketFactory();
    }

    @Handle
    public JSONObject handle(
            HttpServletRequest request,
            @Required(name = "username") String username,
            @Required(name = "password") String password
    ) throws Exception {
        username = username.trim();
        password = password.trim();

        if (isValidPenteOrgUser(username, password)) {
            User user = findOrCreateUser(username);
            UserNameAuthorizationContext authorizationContext =
                    new UserNameAuthorizationContext(username, AuthenticationHandler.getRemoteAddr(request));
            userRepository.updateUser(user, authorizationContext);
            CurrentUser.set(request, user.getUserId(), user.getUsername());
            JSONObject json = new JSONObject();
            json.put("status", "OK");
            return json;
        } else {
            throw new VisalloAccessDeniedException("", null, null);
        }
    }

    private User findOrCreateUser(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            // For form based authentication, username and displayName will be the same
            String randomPassword = UserRepository.createRandomPassword();
            user = userRepository.findOrAddUser(username, username, null, randomPassword);
        }
        return user;
    }

    private boolean isValidPenteOrgUser(String username, String password) {
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
                return false;
            }
            List<String> cookieHeaders = connection.getHeaderFields().get("Set-Cookie");
            connection.disconnect();
            return cookieHeaders.stream().anyMatch(header -> header.contains(String.format("name2=\"%s\"",username)));
        } catch (IOException e) {
            throw Throwables.propagate(e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static SSLSocketFactory createPenteOrgSSLSocketFactory() {
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
                InputStream in = Login.class.getResourceAsStream(certResource);
                Certificate cert = CertificateFactory.getInstance("X.509").generateCertificate(in);
                keyStore.setCertificateEntry(Integer.toString(entryNumber++), cert);
            }
            TrustManagerFactory trustManagerFactory =
                    TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustManagerFactory.getTrustManagers(), null);
            return sslContext.getSocketFactory();
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }
}
