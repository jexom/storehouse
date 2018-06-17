package net.jexom.classes;

import org.apache.commons.codec.digest.DigestUtils;
import org.bson.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Device {
    private String name;
    private String token;
    private Integer tokenCount;
    private HashMap<String, ArrayList<Document>> fileList;

    public Device(String name, String token){
        this.name = name;
        this.tokenCount = 1;
        this.token = token;
        this.fileList = new HashMap<>();
    }

    public Device(Document doc){
        this.name = doc.getString("name");
        this.token = doc.getString("token");
        this.tokenCount = doc.getInteger("tokenCount");
        this.fileList = new HashMap<>();
        for(Map.Entry<String, Object> file : ((Document) doc.get("files")).entrySet()){
            if(!(file.getValue() instanceof String))
            this.fileList.put(file.getKey(), (ArrayList<Document>) file.getValue());
        }
    }

    public String getName() {
        return name;
    }

    public String getToken() {
        return token;
    }

    public Integer getTokenCount() {
        return tokenCount;
    }

    public HashMap<String, ArrayList<Document>> getFileList() {
        return fileList;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setToken() {
        this.tokenCount++;
        this.token = DigestUtils.sha256Hex(this.name + this.tokenCount);
    }

    public void appendFileList(String type, ArrayList<Document> file) {
        this.fileList.put(type, file);
    }

    public boolean removeFileList(String type) {
        return this.fileList.remove(type) != null;
    }

    public void clearFileList() {
        this.fileList.clear();
    }

    public void setFileList(HashMap<String, ArrayList<Document>> fileList) {
        this.fileList = fileList;
    }

    public Document toDocument() {
        return new Document("name", this.name)
                .append("token", this.token)
                .append("tokenCount", this.tokenCount)
                .append("files",fileList);
    }


}
