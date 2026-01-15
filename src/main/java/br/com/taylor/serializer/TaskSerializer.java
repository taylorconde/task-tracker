package br.com.taylor.serializer;

import br.com.taylor.entity.Task;
import br.com.taylor.enums.TaskStatus;
import br.com.taylor.repository.JdbcTaskRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static br.com.taylor.utils.JsonUtils.escape;

public class TaskSerializer {

//    public static String toJson(Task task) {
//        return "{"
//                + "\"id\":" + task.getId() + ","
//                + "\"description\":\"" + escape(task.getDescription()) + "\","
//                + "\"status\":\"" + task.getStatus().name() + "\","
//                + "\"createdAt\":\"" + task.getCreatedAt() + "\","
//                + "\"updatedAt\":\"" + task.getUpdatedAt() + "\""
//                + "}";
//    }
    public static String toJson(Task task) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"id\":").append(task.getId()).append(",");
        sb.append("\"description\":\"").append(escape(task.getDescription())).append("\",");
        sb.append("\"status\":\"").append(task.getStatus()).append("\",");
        sb.append("\"createdAt\":\"").append(task.getCreatedAt()).append("\",");
        sb.append("\"updatedAt\":\"").append(task.getUpdatedAt()).append("\"");
        sb.append("}");
        return sb.toString();
    }

    public static String toJson(List<Task> tasks) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");

        for (int i = 0; i < tasks.size(); i++) {
            sb.append(toJson(tasks.get(i)));
            if (i < tasks.size() - 1) sb.append(",");
        }

        sb.append("]");
        return sb.toString();
    }

    public static List<Task> fromJson(String json) {
        List<Task> tasks = new ArrayList<>();

        if (json == null || json.trim().isEmpty()) {
            return tasks;
        }

        json = json.trim();

        // remove colchetes
        if (json.startsWith("[")) json = json.substring(1);
        if (json.endsWith("]")) json = json.substring(0, json.length() - 1);

        if(json.isBlank()) return tasks;
        // separa objetos
        String[] items = json.split("\\},\\{");

        for (String raw : items) {

            String obj = raw.trim();

            if (!obj.startsWith("{")) obj = "{" + obj;
            if (!obj.endsWith("}")) obj = obj + "}";

            // remove chaves
            obj = obj.substring(1, obj.length() - 1);

            StringBuilder palavraAtual = new StringBuilder();
            List<String> fields = new ArrayList<>();

            char[] chars = obj.toCharArray();
            boolean dentroDeAspas = false;

            for (char c : chars) {
                if(c == '\"') dentroDeAspas = !dentroDeAspas;

                if(c == ',' && !dentroDeAspas) {
                    fields.add(palavraAtual.toString());
                    palavraAtual.setLength(0);
                }
                else {
                    palavraAtual.append(c);
                }
            }
            if(!palavraAtual.isEmpty()){
                fields.add(palavraAtual.toString());
                palavraAtual.setLength(0);
            }

            int id = 0;
            String description = null;
            TaskStatus status = null;
            LocalDateTime createdAt = null;
            LocalDateTime updatedAt = null;

            for (String f : fields) {


                String[] kv = f.split(":", 2);

                String key = kv[0].trim();
                String value = kv[1].trim();

                key = (key.startsWith("\"") && key.endsWith("\"")) ? key.substring(1, key.length() - 1) : key;
                value = (value.startsWith("\"") && value.endsWith("\"")) ? value.substring(1, value.length() - 1) : value;

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

            // cria task
            Task task = new Task(id, description, status);

            tasks.add(task);
        }
        return tasks;
    }
}