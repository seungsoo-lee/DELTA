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

/**
 * Load external resources for index.html and log.html.
 */
@Path("/")
public class MainResource {

    private static final String CSS_HEADER = "<style type=\"text/css\">";
    private static final String CSS_FOOTER = "</style>";

    private static final String JS_HEADER = "<script>";
    private static final String JS_FOOTER = "</script>";

    private static final String INDEX_CSS1 = "<!-- {CSS1} -->";
    private static final String INDEX_JS1 = "<!-- {JS1} -->";

    @GET
    @Path("/")
    @Produces(MediaType.TEXT_HTML)
    public Response getMainPage() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream mainPage = classLoader.getResourceAsStream("index.html");
//        InputStream headcss2 = classLoader.getResourceAsStream("css/keen-dashboards.css");
//
//        String keenJS = "<script src=\"https://d26b395fwzu5fz.cloudfront.net/3.4.1/keen.min.js\" type=\"text/javascr" +
//                "ipt\"></script>\n";
//        String bootstrapCss = "<link href=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css\" " +
//                "rel=\"stylesheet\" " +
//                "integrity=\"sha384-1q8mTJOASx8j1Au+a5WDVnPi2lkFfwwEAa8hDDdjZlpLegxhjVME1fgjWPGmkzs7\" " +
//                "crossorigin=\"anonymous\">";
//        String bootstrapJs = "<script src=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/js/bootstrap.min.js\" " +
//                "integrity=\"sha384-0mSbJDEHialfmuBBQP6A4Qrprq5OVfW37PRR3j5ELqxss1yVqOtnepnHVP9aJ7xS\" " +
//                "crossorigin=\"anonymous\"></script>";
//        String jquery = "<script src=\"https://ajax.googleapis.com/ajax/libs/jquery/2.2.4/jquery.min.js\"></script>";
//        String dataTable = "<script src=\"https://cdn.datatables.net/1.10.12/js/jquery.dataTables.min.js\"></script>";
//        String dtSelect = "<script src=\"https://cdn.datatables.net/select/1.2.0/js/dataTables.select.min" +
//                ".js\"></script>";
//        String dtButton = "<script src=\"https://cdn.datatables.net/buttons/1.2.1/js/dataTables.buttons.min" +
//                ".js\"></script>";
//        String datatableCss = "<link rel=\"stylesheet\" type=\"text/css\" href=\"https://cdn.datatables" +
//                ".net/1.10.12/css/jquery.dataTables.css\">";
//        String dtButtonCss = "<link rel=\"stylesheet\" type=\"text/css\" href=\"https://cdn.datatables" +
//                ".net/buttons/1.2.1/css/buttons.dataTables.min.css\">";
//        String dtSelectCss = "<link rel=\"stylesheet\" type=\"text/css\" href=\"https://cdn.datatables" +
//                ".net/select/1.2.0/css/select.dataTables.min.css\">";
//
//        String btSelectorCss = "<link rel=\"stylesheet\" href=\"https://cdnjs.cloudflare.com/ajax/libs/bootstrap-select/1.12.3/css/bootstrap-select.min.css\">";
//
//        String btSelectorJs = "<script src=\"https://cdnjs.cloudflare.com/ajax/libs/bootstrap-select/1.12.3/js/bootstrap-select.min.js\"></script>";

//        String tempPath = classLoader.getResource("js/index.js").getPath();
//        String mainJs = "<script src=\"" + tempPath + "\"></script>";

        String strMain = new String(toByteArray(mainPage));
//        String strCss1 = bootstrapCss +
//                CSS_HEADER + new String(toByteArray(headcss2)) + CSS_FOOTER +
//                datatableCss + dtButtonCss + dtSelectCss + btSelectorCss;
//        String strJs1 = jquery +
//                bootstrapJs + keenJS +
//                dataTable + dtSelect + dtButton + btSelectorJs;
//        strMain = strMain.replace(INDEX_CSS1, strCss1);
//        strMain = strMain.replace(INDEX_JS1, strJs1);
        InputStream result = new ByteArrayInputStream(strMain.getBytes(StandardCharsets.UTF_8));

        return Response.ok(result).build();
    }

    @GET
    @Path("/log.html")
    @Produces(MediaType.TEXT_HTML)
    public Response getLogPage() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream logPage = classLoader.getResourceAsStream("log.html");
        InputStream headcss2 = classLoader.getResourceAsStream("css/keen-dashboards.css");

        String keenJS = "<script src=\"https://d26b395fwzu5fz.cloudfront.net/3.4.1/keen.min.js\" " +
                "type=\"text/javascript\"></script>\n";
        String bootstrapCss = "<link href=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css\" " +
                "rel=\"stylesheet\" " +
                "integrity=\"sha384-1q8mTJOASx8j1Au+a5WDVnPi2lkFfwwEAa8hDDdjZlpLegxhjVME1fgjWPGmkzs7\" " +
                "crossorigin=\"anonymous\">";
        String bootstrapJs = "<script src=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/js/bootstrap.min.js\" " +
                "integrity=\"sha384-0mSbJDEHialfmuBBQP6A4Qrprq5OVfW37PRR3j5ELqxss1yVqOtnepnHVP9aJ7xS\" " +
                "crossorigin=\"anonymous\"></script>";
        String jquery = "<script src=\"https://ajax.googleapis.com/ajax/libs/jquery/2.2.4/jquery.min.js\"></script>";
        String dataTable = "<script src=\"https://cdn.datatables.net/1.10.12/js/jquery.dataTables.min.js\"></script>";
        String dtSelect = "<script src=\"https://cdn.datatables.net/select/1.2.0/js/dataTables.select.min" +
                ".js\"></script>";
        String dtButton = "<script src=\"https://cdn.datatables.net/buttons/1.2.1/js/dataTables.buttons.min" +
                ".js\"></script>";
        String datatablecss = "<link rel=\"stylesheet\" type=\"text/css\" href=\"https://cdn.datatables" +
                ".net/1.10.12/css/jquery.dataTables.css\">";
        String dtButtonCss = "<link rel=\"stylesheet\" type=\"text/css\" href=\"https://cdn.datatables" +
                ".net/buttons/1.2.1/css/buttons.dataTables.min.css\">";
        String dtSelectCss = "<link rel=\"stylesheet\" type=\"text/css\" href=\"https://cdn.datatables" +
                ".net/select/1.2.0/css/select.dataTables.min.css\">";

        String strMain = new String(toByteArray(logPage));
        String strCss1 = bootstrapCss +
                CSS_HEADER + new String(toByteArray(headcss2)) + CSS_FOOTER +
                datatablecss + dtButtonCss + dtSelectCss;
        String strJs1 = jquery +
                bootstrapJs + keenJS +
                dataTable + dtSelect + dtButton;
        strMain = strMain.replace(INDEX_CSS1, strCss1);
        strMain = strMain.replace(INDEX_JS1, strJs1);

        InputStream result = new ByteArrayInputStream(strMain.getBytes(StandardCharsets.UTF_8));

        return Response.ok(result).build();
    }

    @GET
    @Path("/js/index.js")
    @Produces(MediaType.TEXT_HTML)
    public Response getJs() throws IOException {
        return Response.ok(new String(toByteArray(getClass().getClassLoader().getResourceAsStream("js/index.js")))).build();
    }

    @GET
    @Path("/css/keen-dashboards.css")
    @Produces("text/css")
    public Response getCss() throws IOException {
        return Response.ok(new String(toByteArray(getClass().getClassLoader().getResourceAsStream("css/keen-dashboards.css")))).build();
    }
}
