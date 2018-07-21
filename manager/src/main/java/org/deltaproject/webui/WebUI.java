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
        server = GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), RC, true);
        log.info("WebUI started @ " + BASE_URI);


//        for (NetworkListener listener : server.getListeners()) {
//            listener.registerAddOn(addon);
//        }
//        WebSocketEngine.getEngine().register("", "/new/echo", iframeApplication);
//        try {
//            server.start();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

//        HttpServer server = HttpServer.createSimpleServer();
//        server.addListener(new NetworkListener("delta", "0.0.0.0", 7070));
//        server.getServerConfiguration().addHttpHandler(new StaticHttpHandler(""));
//        try {
//            server.start();
//            System.out.println("Press any key to stop the server...");
//            System.in.read();
//        } catch (Exception e) {
//            System.err.println(e);
//        }

//        Server server = new Server();
//        ServerConnector connector = new ServerConnector(server);
//        connector.setPort(8080);
//        server.addConnector(connector);
//
//        // Setup the basic application "context" for this application at "/"
//        // This is also known as the handler tree (in jetty speak)
//        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
//        context.setContextPath("/");
//        server.setHandler(context);
//
//        try {
//            // Initialize javax.websocket layer
//            ServerContainer wscontainer = WebSocketServerContainerInitializer.configureContext(context);
//
//            // Add WebSocket endpoint to javax.websocket layer
//            wscontainer.addEndpoint(EventSocket.class);
//
//            server.start();
//            server.dump(System.err);
//            server.join();
//        } catch (Throwable t) {
//            t.printStackTrace(System.err);
//        }
    }

    public void deactivate() {
        server.shutdownNow();
    }

}
