 package br.com.taylor.application;

 import br.com.taylor.controller.TaskController;
 import br.com.taylor.infra.DatabaseSetup;
 import br.com.taylor.repository.JdbcTaskRepository;
 import br.com.taylor.repository.JsonTaskRepository;
 import br.com.taylor.server.TaskHttpServer;
 import br.com.taylor.service.TaskService;

 import java.io.IOException;

public class Main {
    public static void main(String[] args) {

        String path = "data/"+"task.json";
        int port = 8080;

        DatabaseSetup.createTables();

//        JsonTaskRepository JsonRepository = new JsonTaskRepository(path);
        JdbcTaskRepository JdbcRepository = new JdbcTaskRepository();
        TaskService service = new TaskService(JdbcRepository);
        TaskController controller = new TaskController(service);
        TaskHttpServer server = new TaskHttpServer(controller);

        try {
            server.start(port);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
