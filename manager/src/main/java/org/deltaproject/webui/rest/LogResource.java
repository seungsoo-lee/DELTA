package org.deltaproject.webui.rest;

import org.deltaproject.manager.utils.AgentLogger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

@Path("/text/getlog")
public class LogResource {
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response getLog() {

        RandomAccessFile log = null;
        String logstr = "";

        try {
            log = new RandomAccessFile(AgentLogger.LOG_PATH + "manager.log", "r");

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
