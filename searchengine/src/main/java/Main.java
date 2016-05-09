import java.sql.*;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;

import java.net.URI;
import java.net.URISyntaxException;

import static spark.Spark.*;
import spark.template.freemarker.FreeMarkerEngine;
import spark.ModelAndView;
import static spark.Spark.get;

import com.heroku.sdk.jdbc.DatabaseUrl;

import ir.SearchWeb;
import ir.PostingsList;
import ir.PostingsEntry;
import ir.Index;

public class Main {

    public static void main(String[] args) {

        port(Integer.valueOf(System.getenv("PORT")));
        staticFileLocation("/public");

        get("/", (request, response) -> {
            Map<String, Object> attributes = new HashMap<>();
            String q = request.queryParams("q");

            if(q != null) {
                SearchWeb sw = new SearchWeb(q);
                sw.index();
                System.err.println(sw.indexer.index.getSize());
                PostingsList pl = sw.indexer.index.getPostings("airplane");
                for(PostingsEntry pe: pl.getList()) {
                    System.err.println(pe.docID);
                }

                PostingsList res = sw.search();
                
                String message = "";
                for(PostingsEntry pe: res.getList()) {
                    message += Index.docIDs.get(pe.docID) + "<br>";
                }

                attributes.put("message", message);
                return new ModelAndView(attributes, "response.ftl");
            } else {
                return new ModelAndView(attributes, "index.ftl");
            }
        }, new FreeMarkerEngine());
    }

}
