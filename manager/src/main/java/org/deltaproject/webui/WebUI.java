package org.deltaproject.webui;

import com.owlike.genson.ext.jaxrs.GensonJsonConverter;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

/**
 * HTTP server for DELTA GUI.
 * Created by Changhoon on 7/4/16.
 */
public class WebUI {
    private static final String BASE_URI = "http://0.0.0.0:7070";
    private static final ResourceConfig RC = new ResourceConfig().packages("org.deltaproject.webui");
    private HttpServer server;
    private final Logger log = LoggerFactory.getLogger(getClass());

    public WebUI() {

    }

    public void activate() {
        RC.register(GensonJsonConverter.class);
        server = GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), RC);
        log.info("WebUI started @ " + BASE_URI);
    }

    public void deactivate() {
        server.shutdownNow();
    }

}
