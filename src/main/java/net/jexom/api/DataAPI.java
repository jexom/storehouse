package net.jexom.api;

import net.jexom.util.MongoUtil;
import spark.ModelAndView;
import spark.Route;
import spark.template.velocity.VelocityTemplateEngine;

import java.util.HashMap;
import java.util.Map;

public class DataAPI {

    public static Route addData = (req, res) -> {
        if (req.headers().contains("token")){
            if (!req.headers("token").isEmpty()){
                if (req.headers().contains("type")) {
                    if (!req.headers("type").isEmpty()) {
                        if (!req.body().equals("")) {
                            switch (MongoUtil.addData(req.headers("token"), req.headers("type"), req.body())){
                                case 0:
                                    return "Ok.";
                                case -1:
                                    return "No such device.";
                                case -2:
                                    return "Data write error.";
                            };
                        }
                        return "No data to add.";
                    }
                    return "Header \"Type\" is empty";
                }
                return "Missing \"Type\" header";
            }
            return "Header \"Token\" is empty";
        }
        return "Missing \"Token\" header";
    };

    public static Route deleteData = (req, res) -> {
        if (req.headers().contains("token")){
            if (!req.headers("token").isEmpty()){
                if (req.headers().contains("type")) {
                    if (!req.headers("type").isEmpty()) {
                            switch (MongoUtil.deleteData(req.headers("token"), req.headers("type"))){
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
        if (req.headers().contains("token")){
            if (!req.headers("token").isEmpty()){
                if (req.headers().contains("type")) {
                    if (!req.headers("type").isEmpty()) {
                        int num = 10;
                        if (req.headers().contains("num") && !req.headers("num").isEmpty())
                            num = Integer.parseInt(req.headers("num"));
                        String data = MongoUtil.getData(req.headers("token"), req.headers("type"), num);
                        if (data == ""){
                            return "Error";
                        } else {
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
            return new VelocityTemplateEngine().render(new ModelAndView(model, "templates/data.vm"));
        }
        res.redirect("/404");
        return null;
    };
}
