package org.deltaproject.webui.rest;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static com.google.common.io.ByteStreams.toByteArray;

/**
 * Created by jinwookim on 2018. 7. 20..
 */
@Path("/new")
public class IframeResource {

    @GET
    @Path("/")
    @Produces(MediaType.TEXT_HTML)
    public Response getMainPage() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream mainPage = classLoader.getResourceAsStream("new.html");
        String strMain = new String(toByteArray(mainPage));
        InputStream result = new ByteArrayInputStream(strMain.getBytes(StandardCharsets.UTF_8));

        return Response.ok(result).build();
    }
}
