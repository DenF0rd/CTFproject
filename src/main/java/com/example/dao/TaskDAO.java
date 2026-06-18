package com.example.dao;

import com.example.util.DBConnection;
import com.example.util.RedisCache;
import com.example.model.Task;
import com.fasterxml.jackson.core.type.TypeReference;

import java.sql.*;
import java.util.*;

public class TaskDAO {

    // ========== ПРОВЕРКА ФЛАГА (с инвалидацией кэша) ==========
    public Object[] checkFlag(int taskId, String submittedFlag, int userId, String ipAddress, String userAgent) {
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

                if (isCorrect) {
                    // Инвалидируем кэш после успешного решения
                    RedisCache.remove("task_" + taskId + "_" + userId);
                    RedisCache.remove("points_earned_" + userId + "_" + taskId);
                    RedisCache.removeByPrefix("contest_tasks_");
                    RedisCache.removeByPrefix("contest_leaderboard_");
                    RedisCache.removeByPrefix("team_leaderboard_");
                    RedisCache.remove("users_all");
                }

                return new Object[]{isCorrect, pointsAwarded, message};
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new Object[]{false, 0, "Ошибка проверки флага"};
    }

    // ========== ПОЛУЧЕНИЕ ЗАДАЧИ ПО ID (с кэшем) ==========
    public Map<String, Object> getTaskById(int taskId, int userId) {
        String cacheKey = "task_" + taskId + "_" + userId;

        Map<String, Object> cached = RedisCache.get(cacheKey, new TypeReference<Map<String, Object>>() {});
        if (cached != null) {
            System.out.println("Redis HIT: " + cacheKey);
            return cached;
        }

        System.out.println("Redis MISS: " + cacheKey + " - loading from DB");

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
                task.put("base_points", rs.getInt("base_points"));
                task.put("min_points", rs.getInt("min_points"));

                RedisCache.put(cacheKey, task, 30);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return task;
    }

    // ========== ПРОВЕРКА, РЕШЕНА ЛИ ЗАДАЧА ==========
    public boolean isTaskSolved(int userId, int taskId) {
        // Простой запрос, кэшируем результат
        String cacheKey = "is_solved_" + userId + "_" + taskId;

        Boolean cached = RedisCache.get(cacheKey, Boolean.class);
        if (cached != null) {
            return cached;
        }

        String sql = "SELECT 1 FROM solved_tasks WHERE user_id = ? AND task_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, taskId);
            ResultSet rs = stmt.executeQuery();
            boolean result = rs.next();
            RedisCache.put(cacheKey, result, 60); // 60 секунд
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ========== ПОЛУЧЕНИЕ ОЧКОВ ПОЛЬЗОВАТЕЛЯ ЗА ЗАДАЧУ (с кэшем) ==========
    public int getPointsEarnedForTask(int userId, int taskId) {
        String cacheKey = "points_earned_" + userId + "_" + taskId;

        Integer cached = RedisCache.get(cacheKey, Integer.class);
        if (cached != null) {
            System.out.println("Redis HIT: " + cacheKey);
            return cached;
        }

        System.out.println("Redis MISS: " + cacheKey + " - loading from DB");

        String sql = "SELECT points_earned FROM solved_tasks WHERE user_id = ? AND task_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, taskId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int points = rs.getInt("points_earned");
                RedisCache.put(cacheKey, points, 60);
                return points;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // ========== СОХРАНИТЬ ПОПЫТКУ ==========
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

    // ========== ИСТОРИЯ ПОПЫТОК ==========
    public List<Map<String, Object>> getSubmissionHistory(int taskId, int userId) {
        // Не кэшируем историю, так как она часто меняется
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

    // ========== ПОЛУЧИТЬ КАТЕГОРИИ (с кэшем) ==========
    public List<Map<String, Object>> getAllCategories() {
        String cacheKey = "categories_all";

        List<Map<String, Object>> cached = RedisCache.get(cacheKey, new TypeReference<List<Map<String, Object>>>() {});
        if (cached != null) {
            return cached;
        }

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

        RedisCache.put(cacheKey, categories, 300); // 5 минут
        return categories;
    }

    // ========== ПОЛУЧИТЬ ВСЕ ЗАДАЧИ (с кэшем) ==========
    public List<Task> getAllTasks() {
        String cacheKey = "tasks_all";

        List<Task> cached = RedisCache.get(cacheKey, new TypeReference<List<Task>>() {});
        if (cached != null) {
            return cached;
        }

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

        RedisCache.put(cacheKey, tasks, 60);
        return tasks;
    }

    // ========== ПОЛУЧИТЬ ЗАДАЧИ ПО ID СОРЕВНОВАНИЯ (с кэшем) ==========
    public List<Task> getTasksByContestId(int contestId) {
        String cacheKey = "tasks_by_contest_" + contestId;

        List<Task> cached = RedisCache.get(cacheKey, new TypeReference<List<Task>>() {});
        if (cached != null) {
            return cached;
        }

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

        RedisCache.put(cacheKey, tasks, 30);
        return tasks;
    }

    // ========== ПОЛУЧИТЬ ВСЕ САБМИШЕНЫ (ДЛЯ АДМИНА) ==========
    /**
     * Получить все сабмишены (для админа)
     * Теперь показывает ВСЕ попытки, включая успешные
     */
    public List<Map<String, Object>> getAllSubmissions() {
        List<Map<String, Object>> submissions = new ArrayList<>();
        String sql = "SELECT s.id, s.user_id, s.task_id, s.submitted_flag, s.is_correct, " +
                "s.points_awarded, s.submitted_at, " +
                "u.username, t.title as task_title " +
                "FROM submissions s " +
                "JOIN users u ON s.user_id = u.id " +
                "JOIN tasks t ON s.task_id = t.id " +
                "ORDER BY s.submitted_at DESC LIMIT 200";  // LIMIT 200 для производительности
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> sub = new HashMap<>();
                sub.put("id", rs.getInt("id"));
                sub.put("userId", rs.getInt("user_id"));
                sub.put("taskId", rs.getInt("task_id"));
                sub.put("username", rs.getString("username"));
                sub.put("task_title", rs.getString("task_title"));
                sub.put("submitted_flag", rs.getString("submitted_flag"));
                sub.put("is_correct", rs.getBoolean("is_correct"));

                // points_awarded может быть NULL для неудачных попыток
                int points = rs.getInt("points_awarded");
                sub.put("points_awarded", rs.wasNull() ? 0 : points);

                sub.put("submitted_at", rs.getTimestamp("submitted_at"));
                submissions.add(sub);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return submissions;
    }

    // ========== ПОЛУЧИТЬ ЗАДАЧУ ПО ID (ДЛЯ АДМИНА) ==========
    public Task getTaskById(int taskId) {
        String cacheKey = "task_admin_" + taskId;

        Task cached = RedisCache.get(cacheKey, Task.class);
        if (cached != null) {
            return cached;
        }

        String sql = "SELECT t.*, c.title as contest_title, " +
                "t.base_points, t.min_points " +
                "FROM tasks t LEFT JOIN contests c ON t.contest_id = c.id WHERE t.id = ?";
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
                task.setBasePoints(rs.getInt("base_points"));
                task.setMinPoints(rs.getInt("min_points"));

                RedisCache.put(cacheKey, task, 60);
                return task;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ========== ПЕРЕСЧЁТ СТОИМОСТИ ЗАДАЧИ (с инвалидацией) ==========
    public void recalculateTaskPoints(int taskId) {
        String sql = "WITH task_data AS (" +
                "   SELECT base_points, min_points, solves_count FROM tasks WHERE id = ?" +
                ") " +
                "UPDATE tasks SET points = GREATEST(" +
                "   (SELECT min_points FROM task_data), " +
                "   (SELECT base_points FROM task_data) * POWER(0.9, (SELECT solves_count FROM task_data))" +
                ") WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, taskId);
            stmt.setInt(2, taskId);
            stmt.executeUpdate();
            // Инвалидируем кэш задачи
            RedisCache.removeByPrefix("task_" + taskId + "_");
            RedisCache.remove("task_admin_" + taskId);
            RedisCache.removeByPrefix("contest_tasks_");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ========== СОЗДАНИЕ ЗАДАЧИ (с инвалидацией) ==========
    public boolean createTask(int contestId, String title, String description, int points,
                              String flag, String hint, int basePoints, int minPoints) {
        String sql = "INSERT INTO tasks (contest_id, title, description, points, base_points, min_points, flag_hash, hint, is_active, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, true, NOW())";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, contestId);
            stmt.setString(2, title);
            stmt.setString(3, description);
            stmt.setInt(4, points);
            stmt.setInt(5, basePoints);
            stmt.setInt(6, minPoints);
            stmt.setString(7, flag);
            stmt.setString(8, hint);
            stmt.executeUpdate();
            RedisCache.remove("tasks_all");
            RedisCache.remove("tasks_by_contest_" + contestId);
            RedisCache.removeByPrefix("contest_tasks_" + contestId + "_");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ========== ОБНОВЛЕНИЕ ЗАДАЧИ (с инвалидацией) ==========
    public boolean updateTask(int id, int contestId, String title, String description,
                              int points, String flag, String hint, boolean isActive,
                              int basePoints, int minPoints) {
        String sql = "UPDATE tasks SET contest_id = ?, title = ?, description = ?, points = ?, " +
                "base_points = ?, min_points = ?, flag_hash = ?, hint = ?, is_active = ? " +
                "WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, contestId);
            stmt.setString(2, title);
            stmt.setString(3, description);
            stmt.setInt(4, points);
            stmt.setInt(5, basePoints);
            stmt.setInt(6, minPoints);
            stmt.setString(7, flag);
            stmt.setString(8, hint);
            stmt.setBoolean(9, isActive);
            stmt.setInt(10, id);
            stmt.executeUpdate();
            RedisCache.removeByPrefix("task_" + id + "_");
            RedisCache.remove("task_admin_" + id);
            RedisCache.remove("tasks_all");
            RedisCache.remove("tasks_by_contest_" + contestId);
            RedisCache.removeByPrefix("contest_tasks_" + contestId + "_");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ========== УДАЛЕНИЕ ЗАДАЧИ (с инвалидацией) ==========
    public boolean deleteTask(int id) {
        String sql = "DELETE FROM tasks WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
            RedisCache.removeByPrefix("task_" + id + "_");
            RedisCache.remove("task_admin_" + id);
            RedisCache.remove("tasks_all");
            RedisCache.removeByPrefix("contest_tasks_");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ========== СОЗДАНИЕ ЗАДАЧИ (БЕЗ ДИНАМИЧЕСКОЙ СТОИМОСТИ) ==========
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
            RedisCache.remove("tasks_all");
            RedisCache.remove("tasks_by_contest_" + contestId);
            RedisCache.removeByPrefix("contest_tasks_" + contestId + "_");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ========== ОБНОВЛЕНИЕ ЗАДАЧИ (БЕЗ ДИНАМИЧЕСКОЙ СТОИМОСТИ) ==========
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
            RedisCache.removeByPrefix("task_" + id + "_");
            RedisCache.remove("task_admin_" + id);
            RedisCache.remove("tasks_all");
            RedisCache.remove("tasks_by_contest_" + contestId);
            RedisCache.removeByPrefix("contest_tasks_" + contestId + "_");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Получить общее количество сабмишенов
     */
    public int getTotalSubmissionsCount() {
        String sql = "SELECT COUNT(*) FROM submissions";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}