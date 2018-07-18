// Выпускная работа бакалавра
// Группа А-08-14
// Чертенко Е.С.
// Листинг  класса net.jexom.api.DeviceAPI
package net.jexom.api;

import net.jexom.util.MongoUtil;
import spark.ModelAndView;
import spark.Route;
import spark.template.velocity.VelocityTemplateEngine;

import java.util.HashMap;
import java.util.Map;

public class DataAPI {

    public static Route addData = (req, res) -> { //запрос на добавление данных
        if (req.queryParams().contains("token")){ //проверка присутствия необходимых параметров
            if (!req.queryParams("token").isEmpty()){
                if (req.queryParams().contains("type")) {
                    if (!req.queryParams("type").isEmpty()) {
                        if (!req.body().equals("")) {
                            switch (MongoUtil.addData(req.queryParams("token"), req.queryParams("type"), req.body())){ //добавление данных в базу данных
                                case 0:
                                    return "{\"success\":\"true\",\"err\":\"0\"}"; //вывод сообщения об успехе добавления
                                case -1:
                                    return "{\"success\":\"false\",\"err\":\"-1\"}"; //данные не добавлены
                            };
                        }
                        return "{\"success\":\"false\",\"err\":\"1\"}"; //вывод ошибок об отсутствии необходимых параметров запроса
                    }
                    return "{\"success\":\"false\",\"err\":\"2\"}";
                }
                return "{\"success\":\"false\",\"err\":\"3\"}";
            }
            return "{\"success\":\"false\",\"err\":\"4\"}";
        }
        return "{\"success\":\"false\",\"err\":\"5\"}";
    };

    public static Route deleteData = (req, res) -> { //запрос на удаление данных
        if (req.queryParams().contains("token")){ //проверка присутствия необходимых параметров
            if (!req.queryParams("token").isEmpty()){
                if (req.queryParams().contains("type")) {
                    if (!req.queryParams("type").isEmpty()) {
                            switch (MongoUtil.deleteData(req.queryParams("token"), req.queryParams("type"))){ //удаление данных
                                case 0:
                                    return "Ok."; //данные удалены
                                case -1:
                                    return "No such device."; //устройство не существует
                            };
                        }
                    return "Header \"Type\" is empty"; //вывод ошибок об отсутствии необходимых параметров запроса
                }
                return "Missing \"Type\" header";
            }
            return "Header \"Token\" is empty";
        }
        return "Missing \"Token\" header";
    };

    public static Route getData = (req, res) -> { //запрос на вывод данных
        if (req.queryParams().contains("token")){ //проверка присутствия необходимых параметров
            if (!req.queryParams("token").isEmpty()){
                if (req.queryParams().contains("type")) {
                    if (!req.queryParams("type").isEmpty()) {
                        int num = 10; //установка количества выводимых данных по умолчанию
                        if (req.queryParams().contains("num") && !req.queryParams("num").isEmpty())
                            num = Integer.parseInt(req.queryParams("num")); //установка количества выводимых данных, если присутствует параметр запроса
                        String data = MongoUtil.getData(req.queryParams("token"), req.queryParams("type"), num); //запрос данных из базы данных
                        if (data == ""){
                            return "Error"; //данные отсутствуют
                        } else {
                            return data; //вывод данных
                        }
                    }
                    return "Header \"Type\" is empty"; //вывод ошибок об отсутствии необходимых параметров запроса
                }
                return "Missing \"Type\" header";
            }
            return "Header \"Token\" is empty";
        }
        return "Missing \"Token\" header";
    };

    public static Route getDataList = (req, res) -> { //запрос списка видов данных
        if (!req.queryParams().isEmpty() && !req.queryParams("device").isEmpty()) {
            return MongoUtil.getDataList(req.queryParams("device"));
        }
        return "Query is not correct";
    };

    public static Route showData = (req, res) -> { //запрос страницы графика данных
        //проверка присутствия необходимых параметров
        if (!req.queryParams().isEmpty() && !req.queryParams("device").isEmpty() && !req.queryParams("data").isEmpty()) {
            Map<String, Object> model = new HashMap<>();
            model.put("token", req.queryParams("device")); //вставка ключа доступа в шаблон страницы
            model.put("data", req.queryParams("data")); //вставка вида данных в шаблон страницы
            return new VelocityTemplateEngine().render(new ModelAndView(model, "templates/graph.html")); //вывод страницы клиенту
        }
        res.redirect("/404"); //перевод пользователя на страницу ошибки о том, что запрошенная страница не существует
        return null;
    };
}
