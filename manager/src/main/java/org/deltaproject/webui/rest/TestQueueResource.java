package org.deltaproject.webui.rest;

import org.deltaproject.webui.TestCase;
import org.deltaproject.webui.TestCaseDirectory;
import org.deltaproject.webui.TestQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by changhoon on 7/7/16.
 */
@Path("/json/testqueue")
public class TestQueueResource {

    private final Logger log = LoggerFactory.getLogger(getClass());


    @GET
    @Path("/get")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTestQueue() {
        GenericEntity<ConcurrentLinkedQueue<TestCase>> queuelist =
                new GenericEntity<ConcurrentLinkedQueue<TestCase>>(TestQueue.getQueue()){};

        return Response.ok(queuelist).build();
    }

    @POST
    @Path("/post")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response addTestCases(String indexes) {

        int count = 0;
        String[] indexList = indexes.split(",");
        for (int i = 0; i < indexList.length; i ++) {
            if (TestCaseDirectory.getDirectory().containsKey(indexList[i].trim())) {
                TestCase testCase = new TestCase(indexList[i].trim());
                testCase.setStatus(TestCase.Status.QUEUED);
                TestQueue.getQueue().add(testCase);
                count++;
            }
        }
        if (count > 0) {
            log.info(count + " test case(s) queued.");
        }

        return Response.status(201).
                entity(count + " test(s) has been queued.").build();
    }
}
