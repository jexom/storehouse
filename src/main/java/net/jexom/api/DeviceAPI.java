package net.jexom.api;

import net.jexom.util.MongoUtil;
import spark.Route;

public class DeviceAPI {
    public static Route addDevice = (req, res) -> {
                if (req.headers().contains("name")) {
                    if (!req.headers("name").isEmpty()) {
                        switch (MongoUtil.addDevice(req.headers("name"))) {
                            case 0:
                                return "Ok. Token: " + MongoUtil.getToken(req.headers("name"));
                            case 1:
                                return "Device name already taken";
                        }
                    }
                    return "\"Name\" header is empty.";
                }
                return "Missing \"Name\" header.";
    };

    public static Route deleteDevice = (req, res) -> {
        if (req.headers().contains("token")) {
            if (!req.headers("token").isEmpty()) {
                        switch (MongoUtil.deleteDevice(req.headers("token"))){
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
        if (req.headers().contains("token")) {
            if (!req.headers("token").isEmpty()) {
                String token = MongoUtil.updateToken(req.headers("token"));
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
