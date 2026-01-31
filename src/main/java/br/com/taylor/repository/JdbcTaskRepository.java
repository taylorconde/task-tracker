package br.com.taylor.repository;

import br.com.taylor.entity.Task;
import br.com.taylor.enums.TaskStatus;
import br.com.taylor.infra.ConnectionFactory;
import br.com.taylor.utils.JdbcUtils;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
                LocalDateTime createdAt = JdbcUtils.toLocalDateTime(rs.getObject("created_at"));
                LocalDateTime updatedAt = JdbcUtils.toLocalDateTime(rs.getObject("updated_at"));

                // cria task
                Task task = new Task(id, description, status,createdAt,updatedAt);

                tasks.add(task);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao acessar banco: " + e.getMessage());
        }

        return tasks;
    }

    @Override
    public Task save(Task task) {
        String sql = "INSERT INTO tasks (description, status, created_at, updated_at) VALUES (?,?,?,?)";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, task.getDescription());
            stmt.setString(2, task.getStatus().name());
            stmt.setTimestamp(3, JdbcUtils.toTimestamp(task.getCreatedAt()));
            stmt.setTimestamp(4, JdbcUtils.toTimestamp(task.getUpdatedAt()));

            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            if(rs.next()) task.setId(rs.getInt(1));

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return task;
    }

    @Override
    public boolean update(Long id, Task updateTask) {
        String sql = "UPDATE tasks SET description=?, status=?, updated_at=? WHERE id=?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)){

            stmt.setString(1, updateTask.getDescription());
            stmt.setString(2, updateTask.getStatus().name());
            stmt.setTimestamp(3, JdbcUtils.toTimestamp(updateTask.getUpdatedAt()));
            stmt.setLong(4, id);

            return stmt.executeUpdate() == 1;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Task findById(Long id) {
        String sql = "SELECT * FROM tasks WHERE id=?";
        Task task = null;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();

            if(rs.next()) {
                int idInt = rs.getInt("id");
                String description = rs.getString("description");
                TaskStatus status = TaskStatus.valueOf(rs.getString("status"));
                LocalDateTime createdAt = JdbcUtils.toLocalDateTime(rs.getObject("created_at"));
                LocalDateTime updatedAt = JdbcUtils.toLocalDateTime(rs.getObject("updated_at"));

                // cria task
                task = new Task(idInt, description, status, createdAt, updatedAt);
            };
        }catch (Exception e) {
            throw new RuntimeException(e);
        }

        return task;
    }

    @Override
    public List<Task> findByStatus(List<TaskStatus> taskStatus) {

        List<Task> tasks = new ArrayList<>();
        String placeholders = String.join("," , Collections.nCopies(taskStatus.size(),"?"));

        String sql = "SELECT * FROM tasks WHERE status IN (" + placeholders + ")";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < taskStatus.size(); i++) {
                stmt.setString(i + 1, String.valueOf(taskStatus.get(i)).toUpperCase());
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {

                int id = rs.getInt("id");
                String description = rs.getString("description");
                TaskStatus status = TaskStatus.valueOf(rs.getString("status"));
                LocalDateTime createdAt = JdbcUtils.toLocalDateTime(rs.getObject("created_at"));
                LocalDateTime updatedAt = JdbcUtils.toLocalDateTime(rs.getObject("updated_at"));

                tasks.add(new Task(id, description, status, createdAt, updatedAt));
            };

        }catch (Exception e) {
            throw new RuntimeException(e);
        }

        return tasks;
    }






    @Override
    public boolean delete(Long id) {
        String sql = "DELETE FROM tasks WHERE id=?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            return stmt.executeUpdate() == 1;

        }catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
