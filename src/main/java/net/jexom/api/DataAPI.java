package net.jexom.api;

import net.jexom.util.MongoUtil;
import spark.ModelAndView;
import spark.Route;
import spark.template.velocity.VelocityTemplateEngine;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DataAPI {

    public static Route addData = (req, res) -> {
        if (req.queryParams().contains("token")){
            if (!req.queryParams("token").isEmpty()){
                if (req.queryParams().contains("type")) {
                    if (!req.queryParams("type").isEmpty()) {
                        if (!req.body().equals("")) {
                            switch (MongoUtil.addData(req.queryParams("token"), req.queryParams("type"), req.body())){
                                case 0:
                                    return "{\"success\":\"true\",\"err\":\"0\"}";
                                case -1:
                                    return "{\"success\":\"false\",\"err\":\"-1\"}";
                            };
                        }
                        return "{\"success\":\"false\",\"err\":\"1\"}";
                    }
                    return "{\"success\":\"false\",\"err\":\"2\"}";
                }
                return "{\"success\":\"false\",\"err\":\"3\"}";
            }
            return "{\"success\":\"false\",\"err\":\"4\"}";
        }
        return "{\"success\":\"false\",\"err\":\"5\"}";
    };

    public static Route deleteData = (req, res) -> {
        if (req.queryParams().contains("token")){
            if (!req.queryParams("token").isEmpty()){
                if (req.queryParams().contains("type")) {
                    if (!req.queryParams("type").isEmpty()) {
                            switch (MongoUtil.deleteData(req.queryParams("token"), req.queryParams("type"))){
                                case 0:
                                    return "Ok.";
                                case -1:
                                    return "No such device.";
                                case -2:
                                    return "Data write error.";
                            };
                        }
                    return "Header \"Type\" is empty";
                }
                return "Missing \"Type\" header";
            }
            return "Header \"Token\" is empty";
        }
        return "Missing \"Token\" header";
    };

    public static Route getData = (req, res) -> {
        if (req.queryParams().contains("token")){
            if (!req.queryParams("token").isEmpty()){
                if (req.queryParams().contains("type")) {
                    if (!req.queryParams("type").isEmpty()) {
                        int num = 10;
                        if (req.queryParams().contains("num") && !req.queryParams("num").isEmpty())
                            num = Integer.parseInt(req.queryParams("num"));
                        String data = MongoUtil.getData(req.queryParams("token"), req.queryParams("type"), num);
                        if (data == ""){
                            return "Error";
                        } else {
                            Logger lgr = Logger.getLogger(MongoUtil.class.getName());
                            lgr.log(Level.INFO, data);
                            return data;
                        }
                    }
                    return "Header \"Type\" is empty";
                }
                return "Missing \"Type\" header";
            }
            return "Header \"Token\" is empty";
        }
        return "Missing \"Token\" header";
    };

    public static Route getDataList = (req, res) -> {
        if (!req.queryParams().isEmpty() && !req.queryParams("device").isEmpty()) {
            return MongoUtil.getDataList(req.queryParams("device"));
        }
        return "Query is not correct";
    };

    public static Route showData = (req, res) -> {
        if (!req.queryParams().isEmpty() && !req.queryParams("device").isEmpty() && !req.queryParams("data").isEmpty()) {
            Map<String, Object> model = new HashMap<>();
            model.put("token", req.queryParams("device"));
            model.put("data", req.queryParams("data"));
            return new VelocityTemplateEngine().render(new ModelAndView(model, "templates/graph.vm"));
        }
        res.redirect("/404");
        return null;
    };
}
