package net.jexom.util;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import net.jexom.classes.Device;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.bson.Document;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.print.Doc;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MongoUtil {

    private static MongoCollection<Document> deviceCollection;
    private static Logger lgr;

    public static void connectDatabase(){
        lgr = Logger.getLogger(MongoUtil.class.getName());
        MongoClient mongo = new MongoClient("localhost", 27017);
        MongoDatabase database = mongo.getDatabase("iot");
        deviceCollection = database.getCollection("devices");
    }

    public static int addDevice(String name){
        if(deviceCollection.find(Filters.eq("name", name)).iterator().hasNext())
            return 1;
        deviceCollection.insertOne(new Device(name, UUID.randomUUID().toString()).toDocument());
        return 0;
    }

    public static String getToken(String name){
        return deviceCollection.find(new Document("name", name)).iterator().next().getString("token");
    }

    public static String updateToken(String token){
        Document deviceDoc = getDevice(token);
        if(deviceDoc == null)
            return "";
        Device device = new Device(deviceDoc);

        device.setToken();
        deviceCollection.updateOne(Filters.eq("token", token),
                Updates.combine(Updates.set("token", device.getToken()), Updates.set("tokenCount", device.getTokenCount())));

        return device.getToken();
    }

    public static int addData(String token, String type, String data){
        lgr.log(Level.INFO, "Start data write");
        Document device = getDevice(token);
        if(device == null)
            return -1;
        Document files = (Document) device.get("files");
        String val = (new JSONObject(data)).get("value").toString();

        if (!files.containsKey(type)){
            Document value = new Document();
            value.put("value", val);
            DateTime time = new DateTime();
            DateTimeFormatter format = DateTimeFormat.forPattern("HH:mm:ss dd.MM.yyyy");
            String timestamp = format.print(time);
            value.put("timestamp", timestamp);
            List<Document> file = new ArrayList<>();
            file.add(value);
            files.put(type, file);
            deviceCollection.updateOne(Filters.eq("_id",device.getObjectId("_id")), Updates.set("files", files));
        } else {
            Document value = new Document();
            value.put("value", val);
            DateTime time = new DateTime();
            DateTimeFormatter format = DateTimeFormat.forPattern("HH:mm:ss dd.MM.yyyy");
            String timestamp = format.print(time);
            value.put("timestamp", timestamp);
            ((ArrayList)files.get(type)).add(0,value);
            deviceCollection.updateOne(Filters.eq("_id",device.getObjectId("_id")), Updates.set("files", files));
        }
        lgr.log(Level.INFO, "Data added to file.");
        return 0;
    }

    public static int deleteDevice(String token){
        Document device = getDevice(token);
        if(device == null)
            return -1;
        deviceCollection.deleteOne(Filters.eq("_id", device.getObjectId("_id")));
        lgr.log(Level.INFO, "Succesfully dropped device");
        return 0;
    }

    public static int deleteData(String token, String dataType){
        Document deviceDoc = getDevice(token);
        if(deviceDoc == null)
            return -1;
//        String path = "C:/Users/Jexom/IdeaProjects/sparkPlayground/src/main/resources/data/" +
//                deviceDoc.getObjectId("_id").toHexString() + "/" + dataType + ".json";
//        try {
//            Files.delete(Paths.get(path));
//        } catch (IOException e) {
//            lgr.log(Level.SEVERE, "Deletion error.");
//            return -2;
//        }
        Document files = (Document) deviceDoc.get("files");
        files.remove(dataType);
        deviceCollection.updateOne(Filters.eq("token", token), Updates.set("files", files));
        return 0;
    }

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

    public static String getDataList(String token){
        Document device = getDevice(token);
        if(device == null)
            return "No device";
//        ArrayList<HashMap<String,String>> dataList = new ArrayList<>();
//        Device device = new Device(doc);
//        for (String file : device.getFileList().keySet()) {
//            HashMap<String, String> obj = new HashMap<>();
//            obj.put("name", file);
//            JSONObject last = new JSONObject();
//            obj.put("value", DataUtil.getData(device.getFileList().get(file)).get("value"));
//            obj.put("time", DataUtil.getData(device.getFileList().get(file)).get("timestamp"));
//            dataList.add(obj);
//        }
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
