package net.jexom.api;

import net.jexom.util.MongoUtil;
import spark.ModelAndView;
import spark.Route;
import spark.template.velocity.VelocityTemplateEngine;

import java.util.HashMap;
import java.util.Map;

public class DeviceAPI {
    public static Route addDevice = (req, res) -> {
                if (req.queryParams().contains("name")) {
                    if (!req.queryParams("name").isEmpty()) {
                        switch (MongoUtil.addDevice(req.queryParams("name"))) {
                            case 0:
                                return "\"token\":\"" + MongoUtil.getToken(req.queryParams("name")) + "\"";
                            case 1:
                                return "Device name already taken";
                        }
                    }
                    return "\"Name\" header is empty.";
                }
                return "Missing \"Name\" header.";
    };

    public static Route deleteDevice = (req, res) -> {
        if (req.queryParams().contains("token")) {
            if (!req.queryParams("token").isEmpty()) {
                        switch (MongoUtil.deleteDevice(req.queryParams("token"))){
                            case 0:
                                return "Ok.";
                            case -1:
                                return "No device.";
                            case -2:
                                return "FS Error";
                        };
                    }
                    return "\"Token\" header is empty.";
                }
                return "Missing \"Token\" header.";
    };

    public static Route updateToken = (req, res) -> {
        if (req.queryParams().contains("token")) {
            if (!req.queryParams("token").isEmpty()) {
                String token = MongoUtil.updateToken(req.queryParams("token"));
                if (!token.equals("")){
                    return "New token: " + token;
                } else {
                    return "Error.";
                }
            }
            return "\"Token\" header is empty.";
        }
        return "Missing \"Token\" header.";
    };

    public static Route getDeviceList = (req, res) -> MongoUtil.getDeviceList();

    public static Route showDevice = (req, res) -> {
        if (!req.queryParams().isEmpty() && !req.queryParams("device").isEmpty() && MongoUtil.getDevice(req.queryParams("device")) != null) {
            Map<String, Object> model = new HashMap<>();
            model.put("token", req.queryParams("device"));
            return new VelocityTemplateEngine().render(new ModelAndView(model, "templates/device.vm"));
        }
        res.redirect("/404");
        return null;
    };

    /*
    public static Route updateUserPass = (req, res) -> {
        if (req.headers().contains("username")){
            if (!req.headers("username").isEmpty()){
                if (req.headers().contains("pass")) {
                    if (!req.headers("pass").isEmpty()) {
                        if (req.headers().contains("oldpass")) {
                            if (!req.headers("oldpass").isEmpty()) {
                                InputStream pass = new ByteArrayInputStream(DigestUtils.sha256(req.headers("pass")));
                                InputStream oldpass = new ByteArrayInputStream(DigestUtils.sha256(req.headers("pass")));
                                return DBUtil.updateUserPass(req.headers("username"), pass, oldpass) ? "Ok." : "Something went wrong.";
                            }
                            return "Header \"OldPass\" is empty";
                        }
                        return "Missing \"OldPass\" header";
                    }
                    return "Header \"Pass\" is empty";
                }
                return "Missing \"Pass\" header";
            }
            return "Header \"Username\" is empty";
        }
        return "Missing \"Username\" header";
    };
    */

    /*
    public static Route addUser = (req, res) ->{
        if (req.headers().contains("username")){
            if (!req.headers("username").isEmpty()){
                if (req.headers().contains("pass")) {
                    if (!req.headers("pass").isEmpty()) {
                        InputStream pass = new ByteArrayInputStream(DigestUtils.sha256(req.headers("pass") + req.headers("username")));
                        switch (DBUtil.addUser(req.headers("username"), pass)){
                            case 0:
                                return "Ok.";
                            case 1:
                                return "Username taken.";
                            case -1:
                                return "Something went wrong.";
                        }
                    }
                    return "Header \"Pass\" is empty";
                }
                return "Missing \"Pass\" header";
            }
            return "Header \"Username\" is empty";
        }
        return "Missing \"Username\" header";
    };
    */

    /*
    public static Route addApiToken = (req, res) -> {
        if (req.headers().contains("username")){
            if (!req.headers("username").isEmpty()){
                if (req.headers().contains("pass")) {
                    if (!req.headers("pass").isEmpty()) {
                        InputStream pass = new ByteArrayInputStream(DigestUtils.sha256(req.headers("pass")));
                        return DBUtil.addApiToken(req.headers("username"), pass) ? "Ok." : "Something went wrong.";
                    }
                    return "Header \"Pass\" is empty";
                }
                return "Missing \"Pass\" header";
            }
            return "Header \"Username\" is empty";
        }
        return "Missing \"Username\" header";
    };
     */
}
