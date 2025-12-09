package br.com.taylor.utils;

import java.io.IOException;
import java.nio.file.StandardOpenOption;

public class JsonUtils {

    public static String readFile(String path) {
        try {
            byte[] bytes = java.nio.file.Files.readAllBytes(
                    java.nio.file.Paths.get(path)
            );
            return new String(bytes);
        } catch (IOException e) {
            return "";
        }
    }

    public static void writeFile(String path, String content) {
        try {
            java.nio.file.Files.write(
                    java.nio.file.Paths.get(path),
                    content.getBytes(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );
        } catch (Exception e) {
            System.err.println("Error writing file: " + e.getMessage());
        }
    }

    public static String escape(String text) {
        if (text == null) return "";
        return text.replace("\"", "\\\"");
    }
}
