package net.jexom.util;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.*;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import net.jexom.SparkPlayground;
import org.bson.Document;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DBUtil {

    private static CollectionReference deviceCollection;
    private static Logger lgr;

    public static void connectDatabase(){ //метод подключения к базе данных
        lgr = Logger.getLogger(DBUtil.class.getName());

        try {
            InputStream serviceAccount = SparkPlayground.class.getResourceAsStream("/sak.json");

            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl("dburl")
                    .build();

            FirebaseApp.initializeApp(options);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Firestore db = FirestoreClient.getFirestore();
         deviceCollection = db.collection("devices");
    }

//    Описание: удаляет устройство из базы данных.
//    Параметры
//    token – ключ доступа устройства
//    Возвращаемое значение – код выхода:
//        -1 – устройство не существует
//        0 – устройство успешно удалено
    public static int addDevice(String name) throws ExecutionException, InterruptedException {
        if(deviceCollection.document(name).get().get().exists()) //существует ли устройство
            return 1;
        HashMap device = new HashMap();
        device.put("name", name);
        device.put("token", UUID.randomUUID().toString());
        deviceCollection.document(name).set(device);
        return 0;
    }
//    Описание: удаляет устройство из базы данных.
//    Параметры
//    name – имя устройства
//    Возвращаемое значение: ключ доступа устройства
    public static String getToken(String name) throws ExecutionException, InterruptedException {
        return deviceCollection.document(name).get().get().get("token").toString();
        //return deviceCollection.find(new Document("name", name)).iterator().next().getString("token");
    }

//    Описание: добавляет данные, отправленные устройством, в базу данных.
//    Параметры
//    token – ключ доступа устройства
//    type – вид данных
//    data – данные в виде JSON-объекта
//    Возвращаемое значение – код выхода:
//            -1 – устройство не существует
//            0 – данные успешно добавлены

    public static int addData(String token, String type, String data) throws ExecutionException, InterruptedException {
        String device = getDevice(token);
        if(device == null)
            return -1;
        String val = (new JSONObject(data)).get("value").toString();
        DateTime time = new DateTime();
        DateTimeFormatter format = DateTimeFormat.forPattern("HH:mm:ss dd.MM.yyyy");
        String timestamp = format.print(time);
        format = DateTimeFormat.forPattern("HHmmssddMMyyyy");
        HashMap entry = new HashMap();
        entry.put("val", val);
        entry.put("time", timestamp);
        deviceCollection.document(device)
                .collection("data").document(type)
                .collection("values").document(format.print(time))
                .set(entry).get();
        return 0;
    }

//    Описание: удаляет устройство из базы данных.
//    Параметры
//    token – ключ доступа устройства
//    Возвращаемое значение – код выхода:
//            -1 – устройство не существует
//            0 – устройство успешно удалено
    public static int deleteDevice(String token) throws ExecutionException, InterruptedException {
        String device = getDevice(token);
        if(device == null)
            return -1;

        Iterable<DocumentReference> docRef = deviceCollection.document(device).collection("data").listDocuments();
        for (DocumentReference doc : docRef) {
            deleteCollection(doc.collection("values"), 1000);
            doc.delete().get();
        }
        deleteCollection(deviceCollection.document(device).collection("data"), 1000);
        deviceCollection.document(device).delete().get();
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
    public static int deleteData(String token, String dataType) throws ExecutionException, InterruptedException {
        String deviceDoc = getDevice(token);
        if(deviceDoc == null)
            return -1;
        deleteCollection(deviceCollection.document(deviceDoc)
                .collection("data").document(dataType).collection("values"), 1000);
        deviceCollection.document(deviceDoc)
                .collection("data").document(dataType)
                .delete().get();
        return 0;
    }

//    Описание: возвращает устройство с заданным ключом доступа.
//    Параметры
//    token – ключ доступа устройства
//    Возвращаемое значение:
//    Документ, описывающий требуемое устройство, если оно есть в базе данных, null в противном случае.
    public static String getDevice(String token) throws ExecutionException, InterruptedException {
        Iterator it = deviceCollection.whereEqualTo("token", token).get().get().iterator();
        QueryDocumentSnapshot device;
        if(it.hasNext()){
            device = (QueryDocumentSnapshot) it.next();
        } else {
            lgr.log(Level.WARNING, "No device");
            return null;
        }
        return device.getString("name");
    }

//    Описание: запрашивает данные, хранимые устройством, из базы данных.
//    Параметры
//    token – ключ доступа устройства
//    type – вид данных
//    num – количество запрашиваемых данных
//    Возвращаемое значение – строка JSON-объект
    public static String getData(String token, String type, int num) throws ExecutionException, InterruptedException {
        String device = getDevice(token);
        if (device == null)
            return "";
        List<QueryDocumentSnapshot> query = deviceCollection.document(device)
                .collection("data").document(type)
                .collection("values").orderBy("time", Query.Direction.DESCENDING ).limit(num).get().get().getDocuments();
        JSONArray jsonData = new JSONArray();

        for(QueryDocumentSnapshot doc : query){
            JSONObject obj = new JSONObject();
            obj.put("value", doc.getString("val"));
            obj.put("timestamp", doc.getString("time"));
            jsonData.put(obj);
        }
        return jsonData.toString();
    }

//    Описание: возвращает список устройств с ключами доступа.
//    Возвращаемое значение – строка JSON-объект, содержащий массив имен устройств и их ключей доступа
    public static String getDeviceList() throws ExecutionException, InterruptedException {
        Iterator it = deviceCollection.listDocuments().iterator();
        ArrayList<HashMap> devices = new ArrayList<>();
        while(it.hasNext()) {
            DocumentSnapshot device = ((DocumentReference) it.next()).get().get();
            HashMap<String, String> map = new HashMap<>();
            map.put("name", device.getString("name"));
            map.put("link", device.getString("token"));
            Iterator<DocumentReference> fileIterator = device.getReference().collection("data").listDocuments().iterator();
            List<String> files = new ArrayList<>();
            while (fileIterator.hasNext()){
                files.add(fileIterator.next().getId());
            }
            if(!files.isEmpty()) {
                map.put("data", files.toString());
            } else {
                map.put("data", "");
            }
            devices.add(map);
        }
        Logger lgr = Logger.getLogger(DBUtil.class.getName());
        lgr.log(Level.INFO, new JSONArray(devices).toString());
        return new JSONArray(devices).toString();
    }

//    Описание: запрашивает список видов данных, хранимых устройством, из базы данных.
//    Параметры
//    token – ключ доступа устройства
//    Возвращаемое значение – строка JSON-объект
    public static String getDataList(String token) throws ExecutionException, InterruptedException {
        String device = getDevice(token);
        if(device == null)
            return "No device";
        JSONArray dataList = new JSONArray();

        HashMap<String, ArrayList<Document>> files = new HashMap<>();
        for (DocumentReference doc : deviceCollection.document(device).collection("data").listDocuments()) {
            String value = doc.collection("values")
                    .orderBy("time", Query.Direction.DESCENDING).limit(1).get().get()
                    .getDocuments().get(0).getString("val");
            JSONObject obj = new JSONObject();
            obj.put("name", doc.getId());
            obj.put("value", value);
            dataList.put(obj);
        }
        return dataList.toString();
    }

    private static void deleteCollection(CollectionReference collection, int batchSize) {
        try {
            // retrieve a small batch of documents to avoid out-of-memory errors
            ApiFuture<QuerySnapshot> future = collection.limit(batchSize).get();
            int deleted = 0;
            // future.get() blocks on document retrieval
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            for (QueryDocumentSnapshot document : documents) {
                document.getReference().delete();
                ++deleted;
            }
            if (deleted >= batchSize) {
                // retrieve and delete another batch
                deleteCollection(collection, batchSize);
            }
        } catch (Exception e) {
            System.err.println("Error deleting collection : " + e.getMessage());
        }
    }
}
