package net.jexom;
import spark.Filter;
import spark.Request;
import spark.Response;
import spark.Route;

public class Filters {
    public static Filter addTrailingSlashes = (Request req, Response res) -> {
        if(req.url().charAt(req.url().length()-1) != '/'){
            res.redirect(req.url() + "/");
        }
    };
}
