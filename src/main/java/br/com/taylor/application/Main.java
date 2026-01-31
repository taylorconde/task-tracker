 package br.com.taylor.application;

 import br.com.taylor.controller.TaskController;
 import br.com.taylor.infra.DatabaseConfig;
 import br.com.taylor.infra.PostgresDatabaseConfig;
 import br.com.taylor.infra.SQLiteDatabaseSetup;
 import br.com.taylor.repository.JdbcTaskRepository;
 import br.com.taylor.server.TaskHttpServer;
 import br.com.taylor.service.TaskService;
 import io.github.cdimascio.dotenv.Dotenv;

 import java.io.IOException;

 public class Main {
    public static void main(String[] args) {

        Dotenv dotenv = Dotenv.load();

        String path = "data/"+"task.json";
        int port = 8080;

        String DB_TYPE = dotenv.get("DB_TYPE");

        DatabaseConfig db = switch (DB_TYPE) {
            case "SQLite" -> new SQLiteDatabaseSetup();
            case "PostgresSQL" -> new PostgresDatabaseConfig();
            default -> throw new IllegalArgumentException("Invalid DB_TYPE");
        };

        db.createTables();

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
