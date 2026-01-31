package br.com.taylor.infra;

import io.github.cdimascio.dotenv.Dotenv;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionFactory {

    static Dotenv dotenv = Dotenv.load();

    public static Connection getConnection() throws SQLException {
        final String DB_TYPE = dotenv.get("DB_TYPE");

        return switch (DB_TYPE) {
                case "SQLite" -> getSQLiteConnection();
                case "PostgresSQL" -> getPostgresConnection();
                default -> throw new IllegalStateException("Unexpected value: " + DB_TYPE);
        };
    }

    //SQLite
    private static final String POSTGRES_URL = dotenv.get("DB_POSTGRES_URL");
    private static final String SQLITE_URL = dotenv.get("DB_SQLITE_URL");

    public static Connection getSQLiteConnection() throws SQLException {
        return DriverManager.getConnection(SQLITE_URL);
    }

    //Postgres
    private static final String PASSWORD = dotenv.get("DB_PASSWORD");
    private static final String USER = dotenv.get("DB_USER");

    public static Connection getPostgresConnection() throws SQLException {
        return DriverManager.getConnection(
                POSTGRES_URL,
                USER,
                PASSWORD
        );
    }
}