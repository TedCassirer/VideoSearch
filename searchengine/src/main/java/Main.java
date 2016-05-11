import java.sql.*;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;
import java.util.LinkedList;
import java.util.Collections;

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

        get("/search", (request, response) -> {
            Map<String, Object> attributes = new HashMap<>();
            String q = request.queryParams("q");

            if(q != null && !q.equals("")) {
                SearchWeb sw = new SearchWeb(q);
                sw.index();
                PostingsList res = sw.search();

                List<Video> videos = new LinkedList<Video>();
                for(PostingsEntry pe: res.getList()) {
					//Finding the "BEST" time of the video if there are several 
					int offs = pe.getFirstPos();
					if (pe.getSizePos() > 1 ) {
						LinkedList<Integer> closeFrames = new LinkedList<Integer>();
						double lengthFrame = Index.docTimeFrame.get("" + pe.docID);
						
						for (int k = 0; k < pe.getSizePos() ; k++){
							int count = 0;
							int timePosition = 0;
							while (timePosition < pe.getSizePos() && pe.getPos(k) + sw.beingClose * lengthFrame > pe.getPos(timePosition)) {
								if (Math.abs(pe.getPos(k) - pe.getPos(timePosition)) < sw.beingClose * lengthFrame) {
									count++;
								}
								timePosition++;
							}
							closeFrames.add(count);
						}
						
						offs = pe.getPos(closeFrames.indexOf(Collections.max(closeFrames)));
					}
					
                    String videoPath = Index.docIDs.get(String.valueOf(pe.docID));
                    String videoName = videoPath.substring(8, videoPath.length() - 6);
                    Video v = new Video(videoName, offs);

                    videos.add(v);
                }

                attributes.put("videos", videos);
                attributes.put("query", q);
                return new ModelAndView(attributes, "response.ftl");
            } else {
                response.redirect("/");
                return null;
            }
        }, new FreeMarkerEngine());

        get("/", (req, res) -> {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("query", "");
            return new ModelAndView(attributes, "index.ftl");
        }, new FreeMarkerEngine());
    }
}
