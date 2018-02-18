package net.jexom.util;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class TokenUtil {
    public static String getToken(String user, String device) {
        return DigestUtils.sha256Hex(user + device);
    }

    public static InputStream getTokenStream(String user, String device){
        return new ByteArrayInputStream(DigestUtils.sha256(user + device));
    }

    public static String getApiToken(String user, InputStream pass) {
        try {
            return DigestUtils.sha256Hex(user + IOUtils.toString(pass, "UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static InputStream getApiTokenStream(String user, InputStream pass){
        try {
            return new ByteArrayInputStream(DigestUtils.sha256(user + IOUtils.toString(pass, "UTF-8")));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getTokenByStream(InputStream stream){
        try {
            return IOUtils.toString(stream, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
