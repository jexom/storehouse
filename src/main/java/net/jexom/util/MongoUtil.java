package net.jexom.util;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import net.jexom.classes.Device;
import org.bson.Document;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MongoUtil {

    private static MongoCollection<Document> deviceCollection;
    private static Logger lgr;

    public static void connectDatabase(){ //метод подключения к базе данных
        lgr = Logger.getLogger(MongoUtil.class.getName());
        MongoClient mongo = new MongoClient("localhost", 27017);
        MongoDatabase database = mongo.getDatabase("iot");
        deviceCollection = database.getCollection("devices");
    }

//    Описание: удаляет устройство из базы данных.
//    Параметры
//    token – ключ доступа устройства
//    Возвращаемое значение – код выхода:
//        -1 – устройство не существует
//        0 – устройство успешно удалено
    public static int addDevice(String name){
        if(deviceCollection.find(Filters.eq("name", name)).iterator().hasNext()) //существует ли устройство
            return 1;
        deviceCollection.insertOne(new Device(name, UUID.randomUUID().toString()).toDocument()); //запись нового устройства в базу данных
        return 0;
    }
//    Описание: удаляет устройство из базы данных.
//    Параметры
//    name – имя устройства
//    Возвращаемое значение: ключ доступа устройства
    public static String getToken(String name){
        return deviceCollection.find(new Document("name", name)).iterator().next().getString("token");
    }

//    Описание: добавляет данные, отправленные устройством, в базу данных.
//    Параметры
//    token – ключ доступа устройства
//    type – вид данных
//    data – данные в виде JSON-объекта
//    Возвращаемое значение – код выхода:
//            -1 – устройство не существует
//            0 – данные успешно добавлены

    public static int addData(String token, String type, String data){
        Document device = getDevice(token);
        if(device == null)
            return -1;
        Document files = (Document) device.get("files");
        String val = (new JSONObject(data)).get("value").toString();

        Document value = new Document();
        value.put("value", val);
        DateTime time = new DateTime();
        DateTimeFormatter format = DateTimeFormat.forPattern("HH:mm:ss dd.MM.yyyy");
        String timestamp = format.print(time);
        value.put("timestamp", timestamp);
        if (!files.containsKey(type)){
            List<Document> file = new ArrayList<>();
            file.add(value);
            files.put(type, file);
        } else {
            ((ArrayList)files.get(type)).add(0,value);
        }
        deviceCollection.updateOne(Filters.eq("_id",device.getObjectId("_id")), Updates.set("files", files));
        return 0;
    }

//    Описание: удаляет устройство из базы данных.
//    Параметры
//    token – ключ доступа устройства
//    Возвращаемое значение – код выхода:
//            -1 – устройство не существует
//            0 – устройство успешно удалено
    public static int deleteDevice(String token){
        Document device = getDevice(token);
        if(device == null)
            return -1;
        deviceCollection.deleteOne(Filters.eq("_id", device.getObjectId("_id")));
        lgr.log(Level.INFO, "Succesfully dropped device");
        return 0;
    }

//    Описание: удаляет данные, хранимые устройством, из базы данных.
//    Параметры
//    token – ключ доступа устройства
//    type – вид данных
//    Возвращаемое значение – код выхода:
//            -1 – устройство не существует
//            0 – данные успешно добавлены
    public static int deleteData(String token, String dataType){
        Document deviceDoc = getDevice(token);
        if(deviceDoc == null)
            return -1;
        Document files = (Document) deviceDoc.get("files");
        files.remove(dataType);
        deviceCollection.updateOne(Filters.eq("token", token), Updates.set("files", files));
        return 0;
    }

//    Описание: возвращает устройство с заданным ключом доступа.
//    Параметры
//    token – ключ доступа устройства
//    Возвращаемое значение:
//    Документ, описывающий требуемое устройство, если оно есть в базе данных, null в противном случае.
    public static Document getDevice(String token){
        Iterator it = deviceCollection.find(Filters.eq("token",token)).iterator();
        Document device;
        if(it.hasNext()){
            device = (Document) it.next();
        } else {
            lgr.log(Level.WARNING, "No device");
            return null;
        }
        return device;
    }

//    Описание: запрашивает данные, хранимые устройством, из базы данных.
//    Параметры
//    token – ключ доступа устройства
//    type – вид данных
//    num – количество запрашиваемых данных
//    Возвращаемое значение – строка JSON-объект
    public static String getData(String token, String type, int num){
        Document device = getDevice(token);
        if (device == null)
            return "";
        Document files = (Document) device.get("files");
        ArrayList<Document> filename = (ArrayList) files.get(type);
        JSONArray jsonData = new JSONArray();

        ArrayList<Document> sublist = new ArrayList(filename.subList(0, (num < filename.size()) ? num : filename.size()));

        for(Document val : sublist){
            JSONObject obj = new JSONObject();
            obj.put("value", val.getString("value"));
            obj.put("timestamp", val.getString("timestamp"));
            jsonData.put(obj);
        }
        return jsonData.toString();
    }

//    Описание: возвращает список устройств с ключами доступа.
//    Возвращаемое значение – строка JSON-объект, содержащий массив имен устройств и их ключей доступа
    public static String getDeviceList(){
        Iterator it = deviceCollection.find().iterator();
        ArrayList<HashMap> devices = new ArrayList<>();
        while(it.hasNext()) {
            Device device = new Device((Document) it.next());
            HashMap<String, String> map = new HashMap<>();
            map.put("name", device.getName());
            map.put("link", device.getToken());
            if(!device.getFileList().isEmpty()) {
                map.put("data", (String) device.getFileList().keySet().toArray()[0]);
            } else {
                map.put("data", "");
            }
            devices.add(map);
        }
        Logger lgr = Logger.getLogger(MongoUtil.class.getName());
        lgr.log(Level.INFO, new JSONArray(devices).toString());
        return new JSONArray(devices).toString();
    }

//    Описание: запрашивает список видов данных, хранимых устройством, из базы данных.
//    Параметры
//    token – ключ доступа устройства
//    Возвращаемое значение – строка JSON-объект
    public static String getDataList(String token){
        Document device = getDevice(token);
        if(device == null)
            return "No device";
        JSONArray dataList = new JSONArray();
        HashMap<String, ArrayList<Document>> files = new HashMap<>();
        for (Map.Entry<String, Object> file : ((Document)device.get("files")).entrySet()){
            if(!(file.getValue() instanceof String)){
                files.put(file.getKey(), (ArrayList<Document>)file.getValue());
            }
        }
        for (Map.Entry<String, ArrayList<Document>> file: files.entrySet()){
            JSONObject obj = new JSONObject();
            obj.put("name", file.getKey());
            obj.put("value", file.getValue().get(0).getString("value"));
            dataList.put(obj);
        }
        return dataList.toString();
    }
}
