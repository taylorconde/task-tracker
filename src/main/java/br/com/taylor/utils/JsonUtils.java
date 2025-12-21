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
            java.nio.file.Path filePath = java.nio.file.Paths.get(path);

            // Capturar diretório pai
            java.nio.file.Path parentDir = filePath.getParent();

            // Verifica se o diretório pai é nulo e se não existe, e o cria se não existir
            if(parentDir != null && !java.nio.file.Files.exists(parentDir))
            {
                java.nio.file.Files.createDirectories(parentDir);
            }
            java.nio.file.Files.write(
                    filePath,
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
