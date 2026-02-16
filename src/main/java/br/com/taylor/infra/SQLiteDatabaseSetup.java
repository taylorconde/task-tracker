package br.com.taylor.infra;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLiteDatabaseSetup implements DatabaseConfig {

    public boolean createTables() {
        String sql = """
                CREATE TABLE IF NOT EXISTS tasks (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    description TEXT,
                    status TEXT,
                    created_at TEXT,
                    updated_at TEXT
                );
        """;

        try (Connection conn = ConnectionFactory.getSQLiteConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(sql);
            System.out.println("Tabela 'tasks' verificada/criada com sucesso!");
            return true;
        } catch (SQLException e) {
            System.err.println("Erro ao configurar banco: " + e.getMessage());
            return false;
        }
    }
}
