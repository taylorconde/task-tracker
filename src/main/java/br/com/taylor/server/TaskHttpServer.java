package br.com.taylor.server;

import br.com.taylor.controller.TaskController;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
public class TaskHttpServer {

    private final TaskController taskController;

    public TaskHttpServer(TaskController taskController) {
        this.taskController = taskController;
    }

    public void start(int port) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        // Mapear a rota raiz das tarefas para o nosso controller
        server.createContext("/tasks", taskController);

        server.setExecutor(null);
        server.start();
        System.out.println("Server started on port " + port + ".");
    }

}
