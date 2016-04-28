package us.pente.graph.auth;

import com.google.inject.Inject;
import com.v5analytics.webster.Handler;
import org.visallo.core.bootstrap.InjectHelper;
import org.visallo.core.model.Name;
import org.visallo.web.AuthenticationHandler;
import org.visallo.web.WebApp;
import org.visallo.web.WebAppPlugin;

import javax.servlet.ServletContext;

@Name("Pente Graph Authentication Plugin")
public class PenteAuthenticationPlugin implements WebAppPlugin {
    private final Login login;

    @Inject
    public PenteAuthenticationPlugin(Login login) {
        this.login = login;
    }

    @Override
    public void init(WebApp app, ServletContext servletContext, Handler authenticationHandler) {
        app.registerBeforeAuthenticationJavaScript("/us/pente/graph/auth/plugin.js");
        app.registerJavaScript("/us/pente/graph/auth/authentication.js", false);
        app.registerJavaScriptTemplate("/us/pente/graph/auth/login.hbs");
        app.registerCss("/us/pente/graph/auth/login.css");
        app.registerResourceBundle("/us/pente/graph/auth/messages.properties");

        app.post(AuthenticationHandler.LOGIN_PATH, login);
    }
}
