package br.com.taylor.repository;

import br.com.taylor.entity.Task;
import br.com.taylor.enums.TaskStatus;
import br.com.taylor.infra.ConnectionFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.valueOf;

public class JdbcTaskRepository implements TaskRepository{
    @Override
    public List<Task> findAll() {
        String sql = "SELECT * FROM tasks";
        List<Task> tasks = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             Statement stmt = conn.createStatement()) {

            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {

                int id = rs.getInt("id");
                String description = rs.getString("description");
                TaskStatus status = TaskStatus.valueOf(rs.getString("status"));
                LocalDateTime createdAt = LocalDateTime.parse(rs.getString("created_at"));
                LocalDateTime updatedAt = LocalDateTime.parse(rs.getString("updated_at"));

                // cria task
                Task task = new Task(id, description, status);

                // injeta as datas via reflection
                try {
                    var f1 = Task.class.getDeclaredField("createdAt");
                    f1.setAccessible(true);
                    f1.set(task, createdAt);

                    var f2 = Task.class.getDeclaredField("updatedAt");
                    f2.setAccessible(true);
                    f2.set(task, updatedAt);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                tasks.add(task);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao acessar banco: " + e.getMessage());
        }

        return tasks;
    }

    @Override
    public Task findById(Long id) {
        return null;
    }

    @Override
    public Task save(Task task) {
        return null;
    }

    @Override
    public boolean update(Long id, Task updateTask) {
        return false;
    }

    @Override
    public boolean delete(Long id) {
        return false;
    }
}
