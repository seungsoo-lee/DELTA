package org.deltaproject.webui.rest;

import org.deltaproject.manager.utils.AgentLogger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Get ChannelAgent log from channel.log.
 */
@Path("/text/getchannel")
public class ChannelLogResource {
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response getChannelLog() {

        BufferedReader log = null;
        String logstr = "";

        try {
            log = new BufferedReader(new InputStreamReader(new FileInputStream(AgentLogger.LOG_PATH + "channel.log")));

            String line;
            while ((line = log.readLine()) != null) {
                logstr = logstr + line + "\n";
            }

        } catch (FileNotFoundException e) {
            return Response.serverError().build();
        } catch (IOException e) {
            return Response.serverError().build();
        }

        return Response.ok(logstr).build();
    }
}
