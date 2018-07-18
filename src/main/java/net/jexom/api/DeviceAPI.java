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

public class DeviceAPI {
    public static Route addDevice = (req, res) -> { //запрос на создание нового устройства
                if (req.queryParams().contains("name")) {
                    if (!req.queryParams("name").isEmpty()) { //проверка присутствия имени в параметрах запроса
                        switch (MongoUtil.addDevice(req.queryParams("name"))) { //создание нового устройства
                            case 0:
                                return "\"token\":\"" + MongoUtil.getToken(req.queryParams("name")) + "\""; //возврат ключа доступа нового устройства
                            case 1:
                                return "Device name already taken"; //устройство уже занято
                        }
                    }
                    return "\"Name\" header is empty."; //Имя нового устройства отсутствует в параметрах запроса
                }
                return "Missing \"Name\" header.";
    };

    public static Route deleteDevice = (req, res) -> { //запрос на удаление устройства
        if (req.queryParams().contains("token")) {
            if (!req.queryParams("token").isEmpty()) { //проверка присутствия ключа доступа в параметрах запроса
                        switch (MongoUtil.deleteDevice(req.queryParams("token"))){ //удаление устройства
                            case 0:
                                return "Ok."; //устройство удалено
                            case -1:
                                return "No device."; //запрошенное устройство не существует
                        };
                    }
                    return "\"Token\" header is empty."; //ключ доступа устройства отсутствует в параметрах запроса
                }
                return "Missing \"Token\" header.";
    };

    public static Route getDeviceList = (req, res) -> MongoUtil.getDeviceList(); //запрос списка устройств и его вывод

    public static Route showDevice = (req, res) -> { //запрос страницы видов данных для устройства
        if (!req.queryParams().isEmpty() &&
                !req.queryParams("device").isEmpty() &&
                MongoUtil.getDevice(req.queryParams("device")) != null) { //проверка присутствия ключа доступа в параметрах запроса
            Map<String, Object> model = new HashMap<>();
            model.put("token", req.queryParams("device")); //вставка ключа доступа в шаблон страницы
            return new VelocityTemplateEngine().render(new ModelAndView(model, "templates/device.html")); //вывод страницы клиенту
        }
        res.redirect("/404"); //перевод пользователя на страницу ошибки о том, что запрошенная страница не существует
        return null;
    };
}
