// Выпускная работа бакалавра
// Группа А-08-14
// Чертенко Е.С.
// Листинг  класса net.jexom.classes.Device
package net.jexom.classes;

import org.apache.commons.codec.digest.DigestUtils;
import org.bson.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Device {
    private String name; //имя устройства
    private String token; //ключ доступа устройства
    private HashMap<String, ArrayList<Document>> fileList; //список видов данных устройства

    //Коструктор объекта класса
    //Входные параметры:
    //name - имя устройства
    //token - ключ доступа устройства
    public Device(String name, String token){
        this.name = name;
        this.token = token;
        this.fileList = new HashMap<>();
    }

    //Коструктор объекта класса
    //Входные параметры:
    //doc - документ базы данных
    public Device(Document doc){
        this.name = doc.getString("name");
        this.token = doc.getString("token");
        this.fileList = new HashMap<>();
        for(Map.Entry<String, Object> file : ((Document) doc.get("files")).entrySet()){
            if(!(file.getValue() instanceof String))
            this.fileList.put(file.getKey(), (ArrayList<Document>) file.getValue());
        }
    }

    public String getName() {
        return name;
    } //возвращает имя устройства

    public String getToken() {
        return token;
    } //возвращает ключ доступа устройства

    public HashMap<String, ArrayList<Document>> getFileList() {
        return fileList;
    } //возвращает список данных

    public void setName(String name) {
        this.name = name;
    } //устанавливает имя устройства

    //Метод преобразования объекта в документ базы данных
    //Выходные данные: документ, готовый к записи в базу данных
    public Document toDocument() {
        return new Document("name", this.name)
                .append("token", this.token)
                .append("files",fileList);
    }


}
