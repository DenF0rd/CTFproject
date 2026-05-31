package com.example.dao;

import com.example.model.Contest;
import com.example.util.DBConnection;
import java.sql.*;
import java.util.*;

public class ContestDAO {

    // ========== ПОЛУЧЕНИЕ ВСЕХ СОРЕВНОВАНИЙ ==========

    public List<Contest> getAllContests(int userId) {
        List<Contest> contests = new ArrayList<>();
        String sql = "SELECT c.*, " +
                "COALESCE(COUNT(DISTINCT t.id), 0) as tasks_count, " +
                "COALESCE(COUNT(DISTINCT cp_all.user_id), 0) as participants_count, " +
                "COALESCE(CASE WHEN cp.user_id IS NOT NULL THEN true ELSE false END, false) as user_joined, " +
                "COALESCE(cp.contest_points, 0) as user_points, " +
                "COALESCE(cp.solved_count, 0) as user_solved_count, " +
                "COALESCE((SELECT COUNT(*) FROM solved_tasks st WHERE st.user_id = ? AND st.task_id IN (SELECT id FROM tasks WHERE contest_id = c.id)), 0) as user_solved_in_contest " +
                "FROM contests c " +
                "LEFT JOIN tasks t ON c.id = t.contest_id AND t.is_active = true " +
                "LEFT JOIN contest_participants cp_all ON c.id = cp_all.contest_id " +
                "LEFT JOIN contest_participants cp ON c.id = cp.contest_id AND cp.user_id = ? " +
                "GROUP BY c.id, cp.user_id, cp.contest_points, cp.solved_count " +
                "ORDER BY c.start_time DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Contest contest = extractContestFromResultSet(rs);
                contest.setParticipantsCount(rs.getInt("participants_count"));
                contest.setUserJoined(rs.getBoolean("user_joined"));
                contest.setUserPoints(rs.getInt("user_points"));
                contest.setUserSolvedCount(rs.getInt("user_solved_in_contest"));
                contests.add(contest);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return contests;
    }

    // ========== ПОЛУЧЕНИЕ ОДНОГО СОРЕВНОВАНИЯ ==========

    public Contest getContestById(int contestId, int userId) {
        String sql = "SELECT c.*, " +
                "COALESCE(COUNT(DISTINCT t.id), 0) as tasks_count, " +
                "COALESCE(CASE WHEN cp.user_id IS NOT NULL THEN true ELSE false END, false) as user_joined, " +
                "COALESCE(CASE WHEN COUNT(DISTINCT t.id) > 0 AND COUNT(DISTINCT st.id) >= COUNT(DISTINCT t.id) THEN true ELSE false END, false) as user_completed " +
                "FROM contests c " +
                "LEFT JOIN tasks t ON c.id = t.contest_id AND t.is_active = true " +
                "LEFT JOIN contest_participants cp ON c.id = cp.contest_id AND cp.user_id = ? " +
                "LEFT JOIN solved_tasks st ON t.id = st.task_id AND st.user_id = ? " +
                "WHERE c.id = ? " +
                "GROUP BY c.id, cp.user_id";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, userId);
            stmt.setInt(3, contestId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Contest contest = extractContestFromResultSet(rs);
                contest.setUserCompleted(rs.getBoolean("user_completed"));
                contest.setUserFinished(false);
                return contest;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ========== АКТИВНЫЕ СОРЕВНОВАНИЯ ==========

    public List<Contest> getActiveContests(int userId) {
        List<Contest> contests = new ArrayList<>();
        String sql = "SELECT c.*, " +
                "COUNT(DISTINCT t.id) as tasks_count, " +
                "COUNT(DISTINCT cp_all.user_id) as participants_count, " +
                "CASE WHEN cp.user_id IS NOT NULL THEN true ELSE false END as user_joined, " +
                "COALESCE(cp.contest_points, 0) as user_points, " +
                "COALESCE(cp.solved_count, 0) as user_solved_count " +
                "FROM contests c " +
                "LEFT JOIN tasks t ON c.id = t.contest_id AND t.is_active = true " +
                "LEFT JOIN contest_participants cp_all ON c.id = cp_all.contest_id " +
                "LEFT JOIN contest_participants cp ON c.id = cp.contest_id AND cp.user_id = ? " +
                "WHERE c.start_time <= NOW() AND c.end_time >= NOW() AND c.is_active = true " +
                "GROUP BY c.id, cp.user_id, cp.contest_points, cp.solved_count " +
                "ORDER BY c.end_time ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Contest contest = extractContestFromResultSet(rs);
                contest.setParticipantsCount(rs.getInt("participants_count"));
                contest.setUserJoined(rs.getBoolean("user_joined"));
                contest.setUserPoints(rs.getInt("user_points"));
                contest.setUserSolvedCount(rs.getInt("user_solved_count"));
                contests.add(contest);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return contests;
    }

    // ========== БУДУЩИЕ СОРЕВНОВАНИЯ ==========

    public List<Contest> getUpcomingContests(int userId) {
        List<Contest> contests = new ArrayList<>();
        String sql = "SELECT c.*, " +
                "COUNT(DISTINCT t.id) as tasks_count, " +
                "CASE WHEN cp.user_id IS NOT NULL THEN true ELSE false END as user_joined " +
                "FROM contests c " +
                "LEFT JOIN tasks t ON c.id = t.contest_id AND t.is_active = true " +
                "LEFT JOIN contest_participants cp ON c.id = cp.contest_id AND cp.user_id = ? " +
                "WHERE c.start_time > NOW() AND c.is_active = true " +
                "GROUP BY c.id, cp.user_id " +
                "ORDER BY c.start_time ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Contest contest = extractContestFromResultSet(rs);
                contest.setParticipantsCount(0);
                contest.setUserJoined(rs.getBoolean("user_joined"));
                contest.setUserPoints(0);
                contest.setUserSolvedCount(0);
                contests.add(contest);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return contests;
    }

    // ========== ПРОШЕДШИЕ СОРЕВНОВАНИЯ ==========

    public List<Contest> getPastContests(int userId) {
        List<Contest> contests = new ArrayList<>();
        String sql = "SELECT c.*, " +
                "COUNT(DISTINCT t.id) as tasks_count, " +
                "COUNT(DISTINCT cp_all.user_id) as participants_count " +
                "FROM contests c " +
                "LEFT JOIN tasks t ON c.id = t.contest_id AND t.is_active = true " +
                "LEFT JOIN contest_participants cp_all ON c.id = cp_all.contest_id " +
                "WHERE c.end_time < NOW() " +
                "GROUP BY c.id " +
                "ORDER BY c.end_time DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Contest contest = extractContestFromResultSet(rs);
                contest.setParticipantsCount(rs.getInt("participants_count"));
                contest.setUserJoined(false);
                contest.setUserPoints(0);
                contest.setUserSolvedCount(0);
                contests.add(contest);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return contests;
    }

    // ========== ЗАДАЧИ СОРЕВНОВАНИЯ ==========

    public List<Map<String, Object>> getContestTasks(int contestId, int userId) {
        List<Map<String, Object>> tasks = new ArrayList<>();
        String sql = "SELECT t.*, c.name as category_name, " +
                "CASE WHEN st.user_id IS NOT NULL THEN true ELSE false END as is_solved " +
                "FROM tasks t " +
                "LEFT JOIN categories c ON t.category_id = c.id " +
                "LEFT JOIN solved_tasks st ON t.id = st.task_id AND st.user_id = ? " +
                "WHERE t.contest_id = ? AND t.is_active = true " +
                "ORDER BY t.points ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, contestId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> task = new HashMap<>();
                task.put("id", rs.getInt("id"));
                task.put("title", rs.getString("title"));
                task.put("description", rs.getString("description"));
                task.put("category", rs.getString("category_name"));
                task.put("points", rs.getInt("points"));
                task.put("solves_count", rs.getInt("solves_count"));
                task.put("is_solved", rs.getBoolean("is_solved"));
                tasks.add(task);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tasks;
    }

    // ========== РЕЙТИНГ УЧАСТНИКОВ ==========

    /**
     * Получить рейтинг участников конкретного соревнования
     */
    public List<Map<String, Object>> getContestLeaderboard(int contestId) {
        List<Map<String, Object>> leaderboard = new ArrayList<>();
        String sql = "SELECT u.id, u.username, cp.contest_points as total_score, cp.solved_count " +
                "FROM contest_participants cp " +
                "JOIN users u ON cp.user_id = u.id " +
                "WHERE cp.contest_id = ? " +
                "ORDER BY cp.contest_points DESC, cp.solved_count DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, contestId);
            ResultSet rs = stmt.executeQuery();
            int rank = 1;
            while (rs.next()) {
                Map<String, Object> entry = new HashMap<>();
                entry.put("rank", rank++);
                entry.put("userId", rs.getInt("id"));
                entry.put("username", rs.getString("username"));
                entry.put("score", rs.getInt("total_score"));
                entry.put("solved", rs.getInt("solved_count"));
                leaderboard.add(entry);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return leaderboard;
    }

    // ========== УЧАСТИЕ В СОРЕВНОВАНИЯХ ==========

    public boolean isUserJoined(int contestId, int userId) {
        String sql = "SELECT 1 FROM contest_participants WHERE contest_id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, contestId);
            stmt.setInt(2, userId);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean joinContest(int contestId, int userId) {
        String sql = "INSERT INTO contest_participants (contest_id, user_id, joined_at) VALUES (?, ?, NOW())";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, contestId);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean leaveContest(int contestId, int userId) {
        String sql = "DELETE FROM contest_participants WHERE contest_id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, contestId);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Удаляем метод finishContest, так как нет поля finished
    // public boolean finishContest(int contestId, int userId) { ... }

    // ========== АДМИНИСТРИРОВАНИЕ ==========

    public boolean createContest(String title, String description, Timestamp startTime, Timestamp endTime) {
        String sql = "INSERT INTO contests (title, description, start_time, end_time, is_active, created_at) VALUES (?, ?, ?, ?, true, NOW())";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, title);
            stmt.setString(2, description);
            stmt.setTimestamp(3, startTime);
            stmt.setTimestamp(4, endTime);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteContest(int id) {
        String sql = "DELETE FROM contests WHERE id = ?";
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

    // ========== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ==========

    private Contest extractContestFromResultSet(ResultSet rs) throws SQLException {
        Contest contest = new Contest();
        contest.setId(rs.getInt("id"));
        contest.setTitle(rs.getString("title"));
        contest.setDescription(rs.getString("description"));
        contest.setStartTime(rs.getTimestamp("start_time"));
        contest.setEndTime(rs.getTimestamp("end_time"));
        contest.setActive(rs.getBoolean("is_active"));
        contest.setCreatedAt(rs.getTimestamp("created_at"));
        contest.setTasksCount(rs.getInt("tasks_count"));
        return contest;
    }

    // Создать соревнование с наградой
    public boolean createContest(String title, String description, String reward, Timestamp startTime, Timestamp endTime) {
        String sql = "INSERT INTO contests (title, description, reward, start_time, end_time, is_active, created_at) VALUES (?, ?, ?, ?, ?, true, NOW())";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, title);
            stmt.setString(2, description);
            stmt.setString(3, reward);
            stmt.setTimestamp(4, startTime);
            stmt.setTimestamp(5, endTime);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Обновить соревнование
    public boolean updateContest(int id, String title, String description, String reward, Timestamp startTime, Timestamp endTime) {
        String sql = "UPDATE contests SET title = ?, description = ?, reward = ?, start_time = ?, end_time = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, title);
            stmt.setString(2, description);
            stmt.setString(3, reward);
            stmt.setTimestamp(4, startTime);
            stmt.setTimestamp(5, endTime);
            stmt.setInt(6, id);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Получить все соревнования для админа
    public List<Contest> getAllContestsForAdmin() {
        List<Contest> contests = new ArrayList<>();
        String sql = "SELECT * FROM contests ORDER BY created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Contest contest = new Contest();
                contest.setId(rs.getInt("id"));
                contest.setTitle(rs.getString("title"));
                contest.setDescription(rs.getString("description"));
                try { contest.setReward(rs.getString("reward")); } catch (SQLException e) { contest.setReward(""); }
                contest.setStartTime(rs.getTimestamp("start_time"));
                contest.setEndTime(rs.getTimestamp("end_time"));
                contest.setActive(rs.getBoolean("is_active"));
                contest.setCreatedAt(rs.getTimestamp("created_at"));
                contests.add(contest);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return contests;
    }



    /**
     * Обновить очки пользователя в соревновании при решении задачи
     */
    public void updateUserContestPoints(int userId, int contestId, int pointsEarned) {
        String sql = "INSERT INTO contest_participants (contest_id, user_id, contest_points, solved_count, joined_at) " +
                "VALUES (?, ?, ?, 1, NOW()) " +
                "ON CONFLICT (contest_id, user_id) DO UPDATE SET " +
                "contest_points = contest_participants.contest_points + EXCLUDED.contest_points, " +
                "solved_count = contest_participants.solved_count + 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, contestId);
            stmt.setInt(2, userId);
            stmt.setInt(3, pointsEarned);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Получить очки пользователя в соревновании
     */
    public int getUserContestPoints(int userId, int contestId) {
        String sql = "SELECT contest_points FROM contest_participants WHERE contest_id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, contestId);
            stmt.setInt(2, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("contest_points");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }



}