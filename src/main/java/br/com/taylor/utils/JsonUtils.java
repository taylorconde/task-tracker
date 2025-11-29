package br.com.taylor.utils;

import br.com.taylor.entity.Task;

import java.util.List;

public class JsonUtils {

    public static String toJson(List<Task> tasks) {
        return null;
    }

    public static List<Task> fromJson(String json){
        return null;
    }

    public static String readFile(String path){
        return null;
    }

    public static void writeFile(String path, String content){

    }

    public static String escape(String text) {
        if(text == null) return "";
        return text.replace("\"","\\\"");
    }
}
