package net.jexom.classes;

import org.apache.commons.codec.digest.DigestUtils;
import org.bson.Document;

import java.util.HashMap;
import java.util.Map;

public class Device {
    private String name;
    private String token;
    private Integer tokenCount;
    private HashMap<String, String> fileList;

    public Device(){
        this.name = "";
        this.tokenCount = 0;
        this.token = "";
        this.fileList = new HashMap<>();
    }

    public Device(String name){
        this.name = name;
        this.tokenCount = 0;
        this.token = "";
        this.fileList = new HashMap<>();
    }

    public Device(String name, String token){
        this.name = name;
        this.tokenCount = 1;
        this.token = token;
        this.fileList = new HashMap<>();
    }

    public Device(Document doc){
        Document files = (Document) doc.get("files");
        this.name = doc.getString("name");
        this.token = doc.getString("token");
        this.tokenCount = doc.getInteger("tokenCount");
        HashMap<String, String> fileList = new HashMap<>();
        for (Map.Entry<String, Object> file:
                files.entrySet()) {
            fileList.put(file.getKey(), (String)file.getValue());
        }
        this.fileList = fileList;
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

    public HashMap<String, String> getFileList() {
        return fileList;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setToken() {
        this.tokenCount++;
        this.token = DigestUtils.sha256Hex(this.name + this.tokenCount);
    }

    public void appendFileList(String type, String file) {
        this.fileList.put(type, file);
    }

    public boolean removeFileList(String type) {
        return this.fileList.remove(type) != null;
    }

    public void clearFileList() {
        this.fileList.clear();
    }

    public void setFileList(HashMap<String, String> fileList) {
        this.fileList = fileList;
    }

    public Document toDocument() {
        Document files = new Document();
        for (Map.Entry<String, String> file :
                fileList.entrySet()) {
            files.append(file.getKey(), file.getValue());
        }
        return new Document("name", this.name)
                .append("token", this.token)
                .append("tokenCount", this.tokenCount)
                .append("files",files);
    }


}
