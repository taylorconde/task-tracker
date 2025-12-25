package br.com.taylor.controller;

import br.com.taylor.entity.Task;
import br.com.taylor.enums.TaskStatus;
import br.com.taylor.serializer.TaskSerializer;
import br.com.taylor.service.TaskService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskController implements HttpHandler {

    @FunctionalInterface
    interface RouteHandler {
        void handle(HttpExchange exchange, Long id) throws IOException;
    }

    private final TaskService service;
    private final Map<String, RouteHandler> routes = new HashMap<>();

    public TaskController(TaskService service) {
        this.service = service;

        // Rotas sem ID
        routes.put("GET:/tasks", this::handleFindAll);
        routes.put("POST:/tasks", this::handleCreate);

        // Rotas com ID (/tasks/{id})
        routes.put("GET:/tasks/:id", this::handleFindByID);
        routes.put("PATCH:/tasks/:id", this::handleUpdate);
        routes.put("DELETE:/tasks/:id", this::handleDelete);

    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        String key = method + ":" + path;

        // Caso 1: rota direta (/tasks)
        if (routes.containsKey(key)) {
            routes.get(key).handle(exchange, null);
            return;
        }

        // Caso 2: rota com id (/tasks/{id})
        if (path.startsWith("/tasks/")) {
            String idStr = path.substring("/tasks/".length());

            try {
                Long id = Long.parseLong(idStr);
                String keyId = method + ":/tasks/:id";

                RouteHandler handler = routes.get(keyId);
                if (handler != null) {
                    handler.handle(exchange, id);
                    return;
                }
            } catch (NumberFormatException e) {
                send(exchange, 400, "{\"error\":\"Invalid ID\"}");
                return;
            }
        }

        send(exchange, 404, "{\"error\":\"Not Found\"}");
    }

    private void handleFindAll(HttpExchange exchange, Long id) throws IOException {

        String query = exchange.getRequestURI().getQuery();

        // Se tiver filtro de status, delega para o método específico
        if (query != null && query.contains("status=")) {
            handleFindByStatus(exchange, query);
            return; // Encerra aqui para não listar tudo
        }

        // 3. Fluxo normal (sem filtro)
        var list = service.findAll();
        send(exchange, 200, TaskSerializer.toJson(list));
    }

    private void handleFindByStatus(HttpExchange exchange, String query) throws IOException {
        try {
            String statusParam = query.split("=")[1].toUpperCase();
            TaskStatus status = TaskStatus.valueOf(statusParam);

            List<Task> filteredTasks = service.findByStatus(status);

            send(exchange, 200, TaskSerializer.toJson(filteredTasks));
        } catch (Exception e) {
            send(exchange, 400, "Status inválido. Opções: TODO, IN_PROGRESS, DONE");
        }
    }

    private void handleFindByID(HttpExchange exchange, Long id) throws IOException{
        var result = service.findById(id);
        if (result == null) {
            send(exchange, 404, "{\"error\":\"Task not found\"}");
            return;
        }
        send(exchange, 200, TaskSerializer.toJson(result));
    }

    private void handleCreate(HttpExchange exchange, Long id) throws IOException{
        String body = readBody(exchange);
        List<Task> tasks = TaskSerializer.fromJson(body);

        if(tasks.isEmpty()) {
            send(exchange, 400, "{\"error\":\"Invalid body\"}");
            return;
        }
        if(tasks.getFirst().getStatus() == null) {
            send(exchange, 400, "{\"error\":\"Status is required\"}");
            return;
        }
        if(tasks.getFirst().getDescription().isBlank()) {
            send(exchange, 400, "{\"error\":\"Description is required\"}");
            return;
        }

        Task created = service.create(tasks.get(0));
        send(exchange, 201, TaskSerializer.toJson(created));
    }

    private void handleUpdate(HttpExchange exchange, Long id) throws IOException{
        String body = readBody(exchange);

        List<Task> newData = TaskSerializer.fromJson(body);
        if(newData.isEmpty()) {
            send(exchange, 400, "{\"error\":\"Invalid body\"}");
            return;
        }

        try {
            var updated = service.update(id, newData.get(0));
            send(exchange, 200, TaskSerializer.toJson(updated));

        } catch (RuntimeException e){
            send(exchange, 404, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    private void handleDelete(HttpExchange exchange, Long id) throws IOException{

        try {
            service.delete(id);
            send(exchange, 204, "");
        } catch (RuntimeException e) {
            send(exchange, 404, "{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    private String readBody(HttpExchange exchange) throws IOException{

        byte[] body = exchange.getRequestBody().readAllBytes();
        return new String(body, StandardCharsets.UTF_8);
    }

    private String normalizedPath(String path) {
        if (path.matches("/tasks/\\d+")) return "/tasks/:id";
        return path;
    }

    private void send(HttpExchange exchange, int status, String content) throws IOException {
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);

        if (bytes.length == 0) {
            exchange.sendResponseHeaders(status, -1);
        } else {
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
            exchange.sendResponseHeaders(status, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }
}
