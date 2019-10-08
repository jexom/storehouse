// Выпускная работа бакалавра
// Группа А-08-14
// Чертенко Е.С.
// Листинг основного класса net.jexom.sparkPlayground
package net.jexom;
import net.jexom.api.*;
import net.jexom.util.DBUtil;
import spark.Filter;
import spark.Spark;

import java.util.HashMap;

import static spark.Spark.*;

public class SparkPlayground {
    public static void main(String[] args) {
        DBUtil.connectDatabase();                //подключение к базе данных
        staticFileLocation("/templates");    //установка расположения файлов-шаблонов веб-страниц
        CorsFilter.apply();                        //применение фильтров получения HTTP-запросов

        //API-пути устройств
        path("/device", () -> {
            post("/add", DeviceAPI.addDevice);  //добавление
            post("/delete", DeviceAPI.deleteDevice); //удаление
            get("/list", DeviceAPI.getDeviceList); //отправка списка клиенту
            get("", DeviceAPI.showDevice); //отправка страницы просмотра видов данных
        });

        //API-пути данных
        path("/data", () -> {
            post("/add", DataAPI.addData); //получение
            post("/delete", DataAPI.deleteData); //удаление
            get("/get", DataAPI.getData); //отправка клиенту
            get("/list", DataAPI.getDataList); //отправка списка клиенту
            get("", DataAPI.showData); //отправка страницы просмотра графика
        });
    }

    static class CorsFilter /*implements Apply*/{

        private static final HashMap<String, String> corsHeaders = new HashMap<>();

        public static void apply() {
            //установка заголовков необходимых для работы сервера
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
