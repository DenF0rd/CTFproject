package com.example.dao;

import com.example.util.DBConnection;
import com.example.model.Task;
import java.sql.*;
import java.util.*;

public class TaskDAO {

    // Вызов хранимой функции check_flag()
    public Object[] checkFlag(int taskId, String submittedFlag, int userId, String ipAddress, String userAgent) {
        // Сначала проверяем, не решена ли уже задача
        if (isTaskSolved(userId, taskId)) {
            System.out.println("Task already solved: userId=" + userId + ", taskId=" + taskId);
            return new Object[]{false, 0, "Вы уже решили эту задачу!"};
        }

        String sql = "SELECT * FROM ctf_contest.check_flag(?, ?, ?, NULL, ?::inet, ?)";
        try (Connection conn = DBConnection.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {
            stmt.setInt(1, taskId);
            stmt.setString(2, submittedFlag);
            stmt.setInt(3, userId);
            stmt.setString(4, ipAddress != null ? ipAddress : "0.0.0.0");
            stmt.setString(5, userAgent != null ? userAgent : "");
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                boolean isCorrect = rs.getBoolean("is_correct");
                int pointsAwarded = rs.getInt("points_awarded");
                String message = rs.getString("message");
                System.out.println("check_flag result: isCorrect=" + isCorrect + ", points=" + pointsAwarded);
                return new Object[]{isCorrect, pointsAwarded, message};
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new Object[]{false, 0, "Ошибка проверки флага"};
    }

    // Получить задачу по ID
    public Map<String, Object> getTaskById(int taskId, int userId) {
        String sql = "SELECT t.*, c.name as category_name, " +
                "CASE WHEN st.user_id IS NOT NULL THEN true ELSE false END as is_solved " +
                "FROM tasks t " +
                "LEFT JOIN categories c ON t.category_id = c.id " +
                "LEFT JOIN solved_tasks st ON t.id = st.task_id AND st.user_id = ? " +
                "WHERE t.id = ? AND t.is_active = true";
        Map<String, Object> task = new HashMap<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, taskId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                task.put("id", rs.getInt("id"));
                task.put("contestId", rs.getInt("contest_id"));
                task.put("title", rs.getString("title"));
                task.put("description", rs.getString("description"));
                task.put("points", rs.getInt("points"));
                task.put("category", rs.getString("category_name"));
                task.put("hint", rs.getString("hint"));
                task.put("file_url", rs.getString("file_url"));
                task.put("solves_count", rs.getInt("solves_count"));
                task.put("is_solved", rs.getBoolean("is_solved"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return task;
    }

    // Проверить, решена ли задача
    public boolean isTaskSolved(int userId, int taskId) {
        String sql = "SELECT 1 FROM solved_tasks WHERE user_id = ? AND task_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, taskId);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Сохранить попытку сдачи
    public void saveSubmission(int taskId, int userId, String flag, boolean isCorrect) {
        String sql = "INSERT INTO submissions (task_id, user_id, submitted_flag, is_correct) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, taskId);
            stmt.setInt(2, userId);
            stmt.setString(3, flag);
            stmt.setBoolean(4, isCorrect);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // История попыток
    public List<Map<String, Object>> getSubmissionHistory(int taskId, int userId) {
        List<Map<String, Object>> history = new ArrayList<>();
        String sql = "SELECT submitted_flag, is_correct, submitted_at FROM submissions " +
                "WHERE task_id = ? AND user_id = ? ORDER BY submitted_at DESC LIMIT 10";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, taskId);
            stmt.setInt(2, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> attempt = new HashMap<>();
                attempt.put("flag", rs.getString("submitted_flag"));
                attempt.put("is_correct", rs.getBoolean("is_correct"));
                attempt.put("submitted_at", rs.getTimestamp("submitted_at"));
                history.add(attempt);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return history;
    }

    // Получить категории
    public List<Map<String, Object>> getAllCategories() {
        List<Map<String, Object>> categories = new ArrayList<>();
        String sql = "SELECT id, name, description, icon_url, color_code, sort_order FROM categories ORDER BY sort_order";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> cat = new HashMap<>();
                cat.put("id", rs.getInt("id"));
                cat.put("name", rs.getString("name"));
                cat.put("description", rs.getString("description"));
                cat.put("icon_url", rs.getString("icon_url"));
                cat.put("color_code", rs.getString("color_code"));
                categories.add(cat);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categories;
    }

    // Создать задачу
    public boolean createTask(int contestId, int categoryId, String title, String description, int points, String flagHash, String hint, String fileUrl) {
        String sql = "INSERT INTO tasks (contest_id, category_id, title, description, points, flag_hash, hint, file_url, is_active) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, true)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, contestId);
            stmt.setInt(2, categoryId);
            stmt.setString(3, title);
            stmt.setString(4, description);
            stmt.setInt(5, points);
            stmt.setString(6, flagHash);
            stmt.setString(7, hint);
            stmt.setString(8, fileUrl);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Обновить задачу
    public boolean updateTask(int id, String title, String description, int points, String flagHash, String hint, String fileUrl, boolean isActive) {
        String sql = "UPDATE tasks SET title = ?, description = ?, points = ?, flag_hash = ?, hint = ?, file_url = ?, is_active = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, title);
            stmt.setString(2, description);
            stmt.setInt(3, points);
            stmt.setString(4, flagHash);
            stmt.setString(5, hint);
            stmt.setString(6, fileUrl);
            stmt.setBoolean(7, isActive);
            stmt.setInt(8, id);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Удалить задачу
    public boolean deleteTask(int id) {
        String sql = "DELETE FROM tasks WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Получить все сабмишены (для админа)
    public List<Map<String, Object>> getAllSubmissions() {
        List<Map<String, Object>> submissions = new ArrayList<>();
        String sql = "SELECT s.*, u.username, t.title as task_title " +
                "FROM submissions s " +
                "JOIN users u ON s.user_id = u.id " +
                "JOIN tasks t ON s.task_id = t.id " +
                "ORDER BY s.submitted_at DESC LIMIT 100";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> sub = new HashMap<>();
                sub.put("id", rs.getInt("id"));
                sub.put("username", rs.getString("username"));
                sub.put("task_title", rs.getString("task_title"));
                sub.put("submitted_flag", rs.getString("submitted_flag"));
                sub.put("is_correct", rs.getBoolean("is_correct"));
                sub.put("points_awarded", rs.getInt("points_awarded"));
                sub.put("submitted_at", rs.getTimestamp("submitted_at"));
                submissions.add(sub);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return submissions;
    }

    // Получить все задачи
    public List<Task> getAllTasks() {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT t.*, c.title as contest_title FROM tasks t LEFT JOIN contests c ON t.contest_id = c.id ORDER BY t.created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Task task = new Task();
                task.setId(rs.getInt("id"));
                task.setContestId(rs.getInt("contest_id"));
                task.setContestTitle(rs.getString("contest_title"));
                task.setTitle(rs.getString("title"));
                task.setDescription(rs.getString("description"));
                task.setPoints(rs.getInt("points"));
                task.setFlag(rs.getString("flag_hash"));
                task.setHint(rs.getString("hint"));
                task.setActive(rs.getBoolean("is_active"));
                task.setCreatedAt(rs.getTimestamp("created_at"));
                tasks.add(task);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tasks;
    }

    // Получить задачи по ID соревнования
    public List<Task> getTasksByContestId(int contestId) {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM tasks WHERE contest_id = ? ORDER BY points ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, contestId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Task task = new Task();
                task.setId(rs.getInt("id"));
                task.setContestId(rs.getInt("contest_id"));
                task.setTitle(rs.getString("title"));
                task.setDescription(rs.getString("description"));
                task.setPoints(rs.getInt("points"));
                task.setFlag(rs.getString("flag_hash"));
                task.setHint(rs.getString("hint"));
                task.setActive(rs.getBoolean("is_active"));
                tasks.add(task);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tasks;
    }

    // Создать задачу
    public boolean createTask(int contestId, String title, String description, int points, String flag, String hint) {
        String sql = "INSERT INTO tasks (contest_id, title, description, points, flag_hash, hint, is_active, created_at) VALUES (?, ?, ?, ?, ?, ?, true, NOW())";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, contestId);
            stmt.setString(2, title);
            stmt.setString(3, description);
            stmt.setInt(4, points);
            stmt.setString(5, flag);
            stmt.setString(6, hint);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Обновить задачу
    public boolean updateTask(int id, int contestId, String title, String description, int points, String flag, String hint, boolean isActive) {
        String sql = "UPDATE tasks SET contest_id = ?, title = ?, description = ?, points = ?, flag_hash = ?, hint = ?, is_active = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, contestId);
            stmt.setString(2, title);
            stmt.setString(3, description);
            stmt.setInt(4, points);
            stmt.setString(5, flag);
            stmt.setString(6, hint);
            stmt.setBoolean(7, isActive);
            stmt.setInt(8, id);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Получить задачу по ID
     */
    public Task getTaskById(int taskId) {
        String sql = "SELECT t.*, c.title as contest_title FROM tasks t LEFT JOIN contests c ON t.contest_id = c.id WHERE t.id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, taskId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Task task = new Task();
                task.setId(rs.getInt("id"));
                task.setContestId(rs.getInt("contest_id"));
                task.setContestTitle(rs.getString("contest_title"));
                task.setTitle(rs.getString("title"));
                task.setDescription(rs.getString("description"));
                task.setPoints(rs.getInt("points"));
                task.setFlag(rs.getString("flag_hash"));
                task.setHint(rs.getString("hint"));
                task.setActive(rs.getBoolean("is_active"));
                task.setSolvesCount(rs.getInt("solves_count"));
                task.setCreatedAt(rs.getTimestamp("created_at"));
                return task;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}