package br.com.taylor.infra;

import io.github.cdimascio.dotenv.Dotenv;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionFactory {

    static Dotenv dotenv = Dotenv.load();
    private static String activeDatabase = "";

    public static String getActiveDatabase() {
        return activeDatabase;
    }

    public static DatabaseConfig getDatabaseConfig(String dbType) {
        DatabaseConfig config;

        if (dbType.equals("auto")) {
            config = getAutoDatabase();
        } else {
            config = switch (dbType) {
                case "SQLite" -> {
                    activeDatabase = "SQLite";
                    yield new SQLiteDatabaseSetup();
                }
                case "PostgreSQL" -> {
                    activeDatabase = "PostgreSQL";
                    yield new PostgresDatabaseConfig();
                }
                default -> throw new IllegalStateException("Unexpected value: " + dbType);
            };
            config.createTables();
        }
        return config;
    }

    public static DatabaseConfig getAutoDatabase() {
        System.out.println("DB_TYPE set to 'auto'. Attempting to connect to PostgreSQL first...");
        DatabaseConfig postgresDb = new PostgresDatabaseConfig();
        boolean postgresReady = postgresDb.createTables();

        System.out.println("[AUTO] PostgreSQL ready? " + postgresReady);

        if (postgresReady) {
            activeDatabase = "PostgreSQL";
            System.out.println("PostgreSQL is ready. Using PostgreSQL as the active database.");
            return postgresDb;
        }

        System.out.println("[AUTO] PostgreSQL not available. Falling back to SQLite...");
        DatabaseConfig sqliteDb = new SQLiteDatabaseSetup();
        boolean sqliteReady = sqliteDb.createTables();
        System.out.println("[AUTO] SQLite ready? " + sqliteReady);
        activeDatabase = "SQLite";
        System.out.println("PostgreSQL is " + (postgresReady ? "ready" : "not available") + ". Using " + activeDatabase + " instead.");
        return sqliteDb;
    }

    public static Connection getConnection() throws SQLException {
        final String DB_TYPE = dotenv.get("DB_TYPE");

        String dbToUse = DB_TYPE.equals("auto") ? getActiveDatabase() : DB_TYPE;

        return switch (dbToUse) {
                case "SQLite" -> getSQLiteConnection();
                case "PostgreSQL" -> getPostgresConnection();
                default -> throw new IllegalStateException("Unexpected value: " + dbToUse);
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