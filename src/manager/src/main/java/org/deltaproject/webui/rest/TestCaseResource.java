package org.deltaproject.webui.rest;


import org.deltaproject.webui.TestCase;
import org.deltaproject.webui.TestCaseDirectory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

/**
 * Get test cases into the web table.
 */
@Path("/json/testcases")
public class TestCaseResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTestCases() {

        List<TestCase> testcases = new ArrayList<>();

        for (String index: TestCaseDirectory.getDirectory().keySet()) {
            TestCase test = TestCaseDirectory.getDirectory().get(index);
            test.setcasenum(index);
            testcases.add(test);
        }

        GenericEntity<List<TestCase>> genlist;
        genlist = new GenericEntity<List<TestCase>>(testcases) {

        };


        return Response.ok(genlist).build();
    }

}
