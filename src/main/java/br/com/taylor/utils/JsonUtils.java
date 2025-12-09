package br.com.taylor.utils;

import br.com.taylor.entity.Task;
import br.com.taylor.enums.TaskStatus;

import java.io.IOException;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class JsonUtils {

    public static String toJson(List<Task> tasks) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");

        for (int i = 0; i < tasks.size(); i++) {
            sb.append(tasks.get(i).toJson());
            if (i < tasks.size() - 1) {
                sb.append(",");
            }
        }

        sb.append("]");
        return sb.toString();
    }

    public static List<Task> fromJson(String json){
        List<Task> tasks = new ArrayList<>();

        if(json == null || json.trim().isEmpty()) {
            return tasks;
        }

        json = json.trim();

        // remover colchetes do array
        if (json.startsWith("[")) json = json.substring(1);
        if (json.endsWith("]")) json = json.substring(0, json.length() - 1);

        // separa objetos por "},{"
        String [] items = json.split("\\},\\{");

        for (String raw: items) {
            // limpa possíveis sobras
            String obj = raw.trim();
            if(!obj.startsWith("{")) obj = "{" + obj;
            if(!obj.endsWith("}")) obj = obj + "}";

            // remove chaves
            obj = obj.substring(1, obj.length() -1);

            String[] fields = obj.split(",");

            int id = 0;
            String description = "";
            TaskStatus status = TaskStatus.TODO;
            LocalDateTime createdAt = null;
            LocalDateTime updatedAt = null;

            for (String field : fields) {
                String[] parts = field.split(":", 2);
                String key = parts[0].replace("\"", "").trim();
                String value = parts[1].replace("\"", "").trim();

                switch (key) {
                    case "id":
                        id = Integer.parseInt(value);
                        break;
                    case "description":
                        description = value;
                        break;
                    case "status":
                        status = TaskStatus.valueOf(value);
                        break;
                    case "createdAt":
                        createdAt = LocalDateTime.parse(value);
                        break;
                    case "updatedAt":
                        updatedAt = LocalDateTime.parse(value);
                        break;
                }
            }

            Task task = new Task(id, description, status);

            // sobrescreve datas iniciais
            try {
                var createdField = Task.class.getDeclaredField("createdAt");
                createdField.setAccessible(true);
                createdField.set(task, createdAt);

                var updatedField = Task.class.getDeclaredField("updatedAt");
                updatedField.setAccessible(true);
                updatedField.set(task, updatedAt);
            } catch (Exception e) {}

            tasks.add(task);
        }

        return tasks;
    }

    public static String readFile(String path){
        try {
            byte[] bytes = java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(path));
            return new String(bytes);
        } catch (IOException e) {
            return "";
        }
    }

    public static void writeFile(String path, String content){
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
        if(text == null) return "";
        return text.replace("\"","\\\"");
    }
}
