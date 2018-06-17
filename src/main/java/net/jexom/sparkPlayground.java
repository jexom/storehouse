package net.jexom;
import net.jexom.api.*;
import net.jexom.util.MongoUtil;
import spark.Filter;
import spark.Spark;

import java.util.HashMap;

import static spark.Spark.*;

public class sparkPlayground {
    public static void main(String[] args) {
        MongoUtil.connectDatabase();
        staticFileLocation("/templates");
        CorsFilter.apply();
        //Device routes
        path("/device", () -> {
            post("/add", DeviceAPI.addDevice);
            delete("/delete", DeviceAPI.deleteDevice);
            //post("/token/update", DeviceAPI.updateToken);
            get("/list", DeviceAPI.getDeviceList);
            get("", DeviceAPI.showDevice);
        });

        /*//Userland
        path("/user", () -> {
            post("/add", DeviceAPI.addUser);
            //post("/updatepass", DeviceAPI.updateUserPass);
            //post("/delete", DeviceAPI.deleteUser);
        });
        */


        /*//API access
        path( "/api", () -> {
            post("/generate", DeviceAPI.addApiToken);
        });
        */

        //Data routes
        path("/data", () -> {
            post("/add", DataAPI.addData);
            post("/delete", DataAPI.deleteData);
            get("/get", DataAPI.getData);
            get("/list", DataAPI.getDataList);
            get("", DataAPI.showData);
        });
    }

    static class CorsFilter /*implements Apply*/{

        private static final HashMap<String, String> corsHeaders = new HashMap<>();

        public static void apply() {
            corsHeaders.put("Access-Control-Allow-Methods", "GET,PUT,POST,DELETE,OPTIONS");
            corsHeaders.put("Access-Control-Allow-Origin", "*");
            corsHeaders.put("Access-Control-Allow-Headers", "Content-Type,Authorization,X-Requested-With,Content-Length,Accept,Origin,");
            corsHeaders.put("Access-Control-Allow-Credentials", "true");
            Filter filter = (request, response) -> corsHeaders.forEach((key, value) -> {
                response.header(key, value);
            });
            Spark.after(filter);
        }
    }
}
