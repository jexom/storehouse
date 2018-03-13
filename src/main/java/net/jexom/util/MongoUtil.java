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
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.print.Doc;
import javax.swing.text.html.HTMLDocument;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
        deviceCollection.insertOne(new Device(name, DigestUtils.sha256Hex(name + 0)).toDocument());
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
        if (!files.containsKey(type)){
            lgr.log(Level.WARNING, "No file listed. ObjectId = " + device.getObjectId("_id").toHexString());
            files.append(type, "C:/Users/Jexom/IdeaProjects/sparkPlayground/src/main/resources/data/" + device.getObjectId("_id").toHexString()
                + "/" + type + ".json");
            deviceCollection.updateOne(Filters.eq("_id",device.getObjectId("_id")), Updates.set("files", files));
            lgr.log(Level.INFO, "File added to database");
        }
        if (!DataUtil.addData(data, files.getString(type)))
            return -2;
        lgr.log(Level.INFO, "Data added to file.");
        return 0;
    }

    public static int deleteDevice(String token){
        Document device = getDevice(token);
        if(device == null)
            return -1;
        String devId = device.getObjectId("_id").toHexString();
        try {
            FileUtils.deleteDirectory(new File("C:/Users/Jexom/IdeaProjects/sparkPlayground/src/main/resources/data/" + devId));
        } catch (IOException e) {
            lgr.log(Level.SEVERE, "Deletion error.");
            return -2;
        }
        deviceCollection.deleteOne(Filters.eq("_id", device.getObjectId("_id")));
        lgr.log(Level.INFO, "Succesfully dropped device");
        return 0;
    }

    public static int deleteData(String token, String dataType){
        Document deviceDoc = getDevice(token);
        if(deviceDoc == null)
            return -1;
        String path = "C:/Users/Jexom/IdeaProjects/sparkPlayground/src/main/resources/data/" +
                deviceDoc.getObjectId("_id").toHexString() + "/" + dataType + ".json";
        try {
            Files.delete(Paths.get(path));
        } catch (IOException e) {
            lgr.log(Level.SEVERE, "Deletion error.");
            return -2;
        }
        Document files = (Document) deviceDoc.get("files");
        files.remove(dataType);
        deviceCollection.updateOne(Filters.eq("token", token), Updates.set("files", files));
        return 0;
    }

    private static Document getDevice(String token){
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
        String filename = (String) files.get(type);
        return DataUtil.getLast(filename, num);
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
        return new JSONArray(devices).toString();
    }

    public static String getDataList(String token){
        Document doc = getDevice(token);
        if(doc == null)
            return "No device";
        ArrayList<HashMap<String,String>> dataList = new ArrayList<>();
        Device device = new Device(doc);
        for (String file : device.getFileList().keySet()) {
            HashMap<String, String> obj = new HashMap<>();
            obj.put("name", file);
            JSONObject last = new JSONObject();
            obj.put("value", DataUtil.getLast(device.getFileList().get(file)).get("value"));
            obj.put("time", DataUtil.getLast(device.getFileList().get(file)).get("timestamp"));
            dataList.add(obj);
        }

        return new JSONArray(dataList).toString();
    }
}
