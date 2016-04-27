package us.pente.graph.web;

import com.v5analytics.webster.Handler;
import org.visallo.core.model.Name;
import org.visallo.web.WebApp;
import org.visallo.web.WebAppPlugin;

import javax.servlet.ServletContext;

@Name("Pente Graph Web App Plugin")
public class PenteWebAppPlugin implements WebAppPlugin {
    @Override
    public void init(WebApp app, ServletContext servletContext, Handler authenticationHandler) {
        app.registerJavaScript("/us/pente/graph/web/plugin.js", true);
        app.registerResourceBundle("/us/pente/graph/web/messages.properties");
    }
}
