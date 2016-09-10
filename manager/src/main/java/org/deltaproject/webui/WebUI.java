package org.deltaproject.webui;

import com.owlike.genson.ext.jaxrs.GensonJsonConverter;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

/**
 * Created by changhoon on 7/4/16.
 */
public class WebUI {
    //private static final String BASE_URI = "http://localhost:7070";
    private static final String BASE_URI = "http://143.248.55.28:7070";
    private static final ResourceConfig rc = new ResourceConfig().packages("org.deltaproject.webui");
    private HttpServer server;
    private final Logger log = LoggerFactory.getLogger(getClass());

    public WebUI() {

    }

    public void activate() {
        rc.register(GensonJsonConverter.class);
        server = GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
        log.info("WebUI started @ localhost:7070");
    }

    public void deactivate() {
        server.stop();
    }

}
