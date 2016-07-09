package org.deltaproject.webui.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static com.google.common.io.ByteStreams.toByteArray;

@Path("/")
public class MainResource {

    private static final String CSS_HEADER = "<style type=\"text/css\">";
    private static final String CSS_FOOTER = "</style>";

    private static final String INDEX_CSS1 = "<!-- {CSS1} -->";
    private static final String INDEX_JS1 = "<!-- {JS1} -->";

    @GET
    @Path("/")
    @Produces(MediaType.TEXT_HTML)
    public Response getMainPage() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream mainPage = classLoader.getResourceAsStream("index.html");
        InputStream headcss2 = classLoader.getResourceAsStream("css/keen-dashboards.css");

        String keenJS = "<script src=\"https://d26b395fwzu5fz.cloudfront.net/3.4.1/keen.min.js\" type=\"text/javascript\"></script>\n";

        String bootstrapCSS = "<link href=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css\" rel=\"stylesheet\" integrity=\"sha384-1q8mTJOASx8j1Au+a5WDVnPi2lkFfwwEAa8hDDdjZlpLegxhjVME1fgjWPGmkzs7\" crossorigin=\"anonymous\">";
        String bootstrapJS = "<script src=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/js/bootstrap.min.js\" integrity=\"sha384-0mSbJDEHialfmuBBQP6A4Qrprq5OVfW37PRR3j5ELqxss1yVqOtnepnHVP9aJ7xS\" crossorigin=\"anonymous\"></script>";

        String jquery = "<script src=\"https://ajax.googleapis.com/ajax/libs/jquery/2.2.4/jquery.min.js\"></script>";
        String datatable = "<script src=\"https://cdn.datatables.net/1.10.12/js/jquery.dataTables.min.js\"></script>";
        String dtSelect = "<script src=\"https://cdn.datatables.net/select/1.2.0/js/dataTables.select.min.js\"></script>";
        String dtButton = "<script src=\"https://cdn.datatables.net/buttons/1.2.1/js/dataTables.buttons.min.js\"></script>";

        String datatablecss = "<link rel=\"stylesheet\" type=\"text/css\" href=\"https://cdn.datatables.net/1.10.12/css/jquery.dataTables.css\">";
        String dtButtonCSS = "<link rel=\"stylesheet\" type=\"text/css\" href=\"https://cdn.datatables.net/buttons/1.2.1/css/buttons.dataTables.min.css\">";
        String dtSelectCSS = "<link rel=\"stylesheet\" type=\"text/css\" href=\"https://cdn.datatables.net/select/1.2.0/css/select.dataTables.min.css\">";

        String strMain = new String(toByteArray(mainPage));

        String strCss1 = bootstrapCSS +
                CSS_HEADER + new String(toByteArray(headcss2)) + CSS_FOOTER +
                datatablecss+dtButtonCSS+dtSelectCSS;

        String strJs1 = jquery +
                bootstrapJS + keenJS +
                datatable + dtSelect + dtButton;

        strMain = strMain.replace(INDEX_CSS1, strCss1);
        strMain = strMain.replace(INDEX_JS1, strJs1);

        InputStream result = new ByteArrayInputStream(strMain.getBytes(StandardCharsets.UTF_8));

        return Response.ok(result).build();
    }
}
