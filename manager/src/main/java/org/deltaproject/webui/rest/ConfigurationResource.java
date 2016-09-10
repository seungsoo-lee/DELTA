package org.deltaproject.webui.rest;

import org.deltaproject.manager.core.Configuration;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/text/getconfig")
public class ConfigurationResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response getConfiguration() {

        return Response.ok(Configuration.getInstance().showWEB()).build();
    }
}
