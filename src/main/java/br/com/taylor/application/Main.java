 package br.com.taylor.application;

 import br.com.taylor.controller.TaskController;
 import br.com.taylor.repository.TaskRepository;
 import br.com.taylor.server.TaskHttpServer;
 import br.com.taylor.service.TaskService;

 import java.io.IOException;

public class Main {
    public static void main(String[] args) {

        String path = "data/"+"task.json";
        int port = 8080;

        TaskRepository repository = new TaskRepository(path);
        TaskService service = new TaskService(repository);
        TaskController controller = new TaskController(service);
        TaskHttpServer server = new TaskHttpServer(controller);

        try {
            server.start(port);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
