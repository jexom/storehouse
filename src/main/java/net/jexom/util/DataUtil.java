package net.jexom.util;

import net.jexom.sparkPlayground;
import org.apache.commons.io.FileUtils;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DataUtil {

    public static boolean addData(String newData, String filepath)  {
        String dataText = "";
        JSONArray jsonData;

        if (Files.exists(Paths.get(filepath))) {
            try {
                dataText = new String(Files.readAllBytes(Paths.get(filepath)), "UTF-8");
                jsonData = new JSONObject(dataText).getJSONArray("data");
            } catch (IOException ex) {
                Logger lgr = Logger.getLogger(sparkPlayground.class.getName());
                lgr.log(Level.SEVERE, ex.getMessage(), ex);
                return false;
            }
        } else {
            if(!Files.exists(Paths.get(filepath).getParent()))
                try {
                    Files.createDirectories(Paths.get(filepath).getParent());
                } catch (IOException ex) {
                    Logger lgr = Logger.getLogger(sparkPlayground.class.getName());
                    lgr.log(Level.SEVERE, ex.getMessage(), ex);
                    return false;
                }
            jsonData = new JSONArray();
        }

        JSONObject jsonNewData = new JSONObject(newData);
        DateTime time = new DateTime();
        DateTimeFormatter format = DateTimeFormat.forPattern("HH:mm:ss dd.MM.yyyy");
        String timestamp = format.print(time);
        jsonNewData.put("timestamp", timestamp);
        ArrayList j = new ArrayList<Map>();
        j.add(jsonNewData.toMap());
        j.addAll(jsonData.toList());
        jsonData = new JSONArray(j);

        JSONObject jsonOutData = new JSONObject().put("data", jsonData);

        try {
            Files.write(Paths.get(filepath), jsonOutData.toString(4).getBytes());
        } catch (IOException ex) {
            Logger lgr = Logger.getLogger(sparkPlayground.class.getName());
            lgr.log(Level.SEVERE, ex.getMessage(), ex);
            return false;
        }

        return true;
    }

    public static String getData(ArrayList<Document> doc, int num){
        String dataText;
        JSONArray jsonData = new JSONArray();
//        String filepath = "";
//        if (Files.exists(Paths.get(filepath))) {
//            try {
//                dataText = new String(Files.readAllBytes(Paths.get(filepath)), "UTF-8");
//                jsonData = new JSONObject(dataText).getJSONArray("data");
//            } catch (IOException ex) {
//                Logger lgr = Logger.getLogger(sparkPlayground.class.getName());
//                lgr.log(Level.SEVERE, ex.getMessage(), ex);
//                return "";
//            }
//        } else {
//            return "";
//        }

        ArrayList<Document> sublist = new ArrayList(doc.subList(0, (num < doc.size()) ? num : doc.size()));

        for(Document val : sublist){
            JSONObject obj = new JSONObject();
            obj.put("value", val.getString("value"));
            obj.put("timestamp", val.getString("timestamp"));
            jsonData.put(obj);
        }
        return jsonData.toString();
    }

    public static String getData(String filepath){
        String dataText;
        JSONArray jsonData;

        if (Files.exists(Paths.get(filepath))) {
            try {
                dataText = new String(Files.readAllBytes(Paths.get(filepath)), "UTF-8");
                jsonData = new JSONObject(dataText).getJSONArray("data");
            } catch (IOException ex) {
                Logger lgr = Logger.getLogger(sparkPlayground.class.getName());
                lgr.log(Level.SEVERE, ex.getMessage(), ex);
                return "";
            }
        } else {
            return "";
        }

        return jsonData.toString();
    }

//    public static HashMap<String, String> getData(String filepath){
//        String dataText;
//        JSONArray jsonData;
//
//        if (Files.exists(Paths.get(filepath))) {
//            try {
//                dataText = new String(Files.readAllBytes(Paths.get(filepath)), "UTF-8");
//                jsonData = new JSONObject(dataText).getJSONArray("data");
//            } catch (IOException ex) {
//                Logger lgr = Logger.getLogger(sparkPlayground.class.getName());
//                lgr.log(Level.SEVERE, ex.getMessage(), ex);
//                return null;
//            }
//        } else {
//            return null;
//        }
//
//        List list = jsonData.toList();
//        return (HashMap<String, String>) list.get(0);
//    }
}
