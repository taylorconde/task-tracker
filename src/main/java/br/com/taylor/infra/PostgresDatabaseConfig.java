package br.com.taylor.infra;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class PostgresDatabaseConfig implements DatabaseConfig {

    public boolean createTables() {
        String sql = """
                CREATE TABLE IF NOT EXISTS tasks (
                    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY ,
                    description TEXT,
                    status TEXT,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );
        """;

//        try (Connection conn = ConnectionFactory.getSQLiteConnection();
        try (Connection conn = ConnectionFactory.getPostgresConnection();
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
