package com.example.dao;

import com.example.model.User;
import com.example.util.DBConnection;
import com.example.util.RedisCache;
import com.fasterxml.jackson.core.type.TypeReference;

import java.sql.*;
import java.util.*;

public class UserDAO {

    // ========== АУТЕНТИФИКАЦИЯ ==========
    public User authenticateUser(String email, String password) {
        // Не кэшируем результат аутентификации
        String sql = "SELECT id, username, email, password_hash, rating, is_verified, is_admin, is_active, bio, age, city, created_at, last_login " +
                "FROM users WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String storedHash = rs.getString("password_hash");
                if (com.example.util.PasswordUtil.checkPassword(password, storedHash)) {
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setUsername(rs.getString("username"));
                    user.setEmail(rs.getString("email"));
                    user.setPassword(storedHash);
                    user.setScore(rs.getInt("rating"));
                    user.setVerified(rs.getBoolean("is_verified"));
                    user.setAdmin(rs.getBoolean("is_admin"));
                    user.setActive(rs.getBoolean("is_active"));
                    user.setBio(rs.getString("bio"));
                    user.setCity(rs.getString("city"));
                    user.setAge(rs.getInt("age"));
                    user.setRegistrationDate(rs.getTimestamp("created_at"));
                    user.setLastLogin(rs.getTimestamp("last_login"));
                    return user;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ========== ПОЛУЧЕНИЕ ПОЛЬЗОВАТЕЛЯ ПО ID (с кэшем) ==========
    public User findById(int id) {
        String cacheKey = "user_" + id;

        User cached = RedisCache.get(cacheKey, User.class);
        if (cached != null) {
            System.out.println("Redis HIT: " + cacheKey);
            return cached;
        }

        System.out.println("Redis MISS: " + cacheKey + " - loading from DB");

        String sql = "SELECT u.id, u.username, u.email, u.password_hash, u.rating, " +
                "u.is_verified, u.is_admin, u.bio, u.age, u.city, u.created_at, u.last_login, " +
                "COALESCE((SELECT COUNT(*) FROM solved_tasks WHERE user_id = u.id), 0) as solved_count " +
                "FROM users u WHERE u.id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                User user = extractUserFromResultSet(rs);
                user.setSolvedCount(rs.getInt("solved_count"));
                RedisCache.put(cacheKey, user, 60);
                return user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ========== ПОЛУЧЕНИЕ ПОЛЬЗОВАТЕЛЯ ПО EMAIL (с кэшем) ==========
    public User findByEmail(String email) {
        String cacheKey = "user_email_" + email;

        User cached = RedisCache.get(cacheKey, User.class);
        if (cached != null) {
            return cached;
        }

        String sql = "SELECT u.id, u.username, u.email, u.password_hash, u.rating, " +
                "u.is_verified, u.is_admin, u.bio, u.age, u.city, u.created_at, u.last_login, " +
                "COALESCE((SELECT COUNT(*) FROM solved_tasks WHERE user_id = u.id), 0) as solved_count " +
                "FROM users u WHERE u.email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                User user = extractUserFromResultSet(rs);
                user.setSolvedCount(rs.getInt("solved_count"));
                RedisCache.put(cacheKey, user, 60);
                return user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean emailExists(String email) {
        // Простой запрос, не кэшируем
        String sql = "SELECT 1 FROM users WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ========== РЕГИСТРАЦИЯ (с инвалидацией) ==========
    public boolean registerUser(String username, String email, String passwordHash) {
        String sql = "INSERT INTO users (username, email, password_hash, is_verified) VALUES (?, ?, ?, false)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, email);
            stmt.setString(3, passwordHash);
            stmt.executeUpdate();
            RedisCache.remove("users_all");
            RedisCache.remove("users_all_with_details");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean verifyEmail(String email, String code) {
        String sql = "SELECT vc.id FROM verification_codes vc " +
                "JOIN users u ON vc.user_id = u.id " +
                "WHERE u.email = ? AND vc.code = ? AND vc.type = 'email_verification' " +
                "AND vc.expires_at > NOW() AND vc.is_used = false";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setString(2, code);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int codeId = rs.getInt("id");
                String updateSql = "UPDATE verification_codes SET is_used = true WHERE id = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setInt(1, codeId);
                    updateStmt.executeUpdate();
                }
                String verifySql = "UPDATE users SET is_verified = true WHERE email = ?";
                try (PreparedStatement verifyStmt = conn.prepareStatement(verifySql)) {
                    verifyStmt.setString(1, email);
                    verifyStmt.executeUpdate();
                }
                RedisCache.remove("user_email_" + email);
                RedisCache.removeByPrefix("user_");
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void verifyEmailSimple(String email) {
        String sql = "UPDATE users SET is_verified = true WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.executeUpdate();
            RedisCache.remove("user_email_" + email);
            RedisCache.removeByPrefix("user_");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateLastLogin(int userId) {
        String sql = "UPDATE users SET last_login = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
            RedisCache.remove("user_" + userId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ========== ПОЛУЧЕНИЕ ВСЕХ ПОЛЬЗОВАТЕЛЕЙ (с кэшем) ==========
    public List<User> getAllUsers() {
        String cacheKey = "users_all";

        List<User> cached = RedisCache.get(cacheKey, new TypeReference<List<User>>() {});
        if (cached != null) {
            System.out.println("Redis HIT: " + cacheKey);
            return cached;
        }

        System.out.println("Redis MISS: " + cacheKey + " - loading from DB");

        List<User> users = new ArrayList<>();
        String sql = "SELECT u.id, u.username, u.email, u.rating, u.is_verified, u.is_admin, " +
                "u.created_at, u.last_login, " +
                "COALESCE((SELECT COUNT(*) FROM solved_tasks WHERE user_id = u.id), 0) as solved_count " +
                "FROM users u WHERE u.is_active = true ORDER BY u.rating DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setEmail(rs.getString("email"));
                user.setScore(rs.getInt("rating"));
                user.setVerified(rs.getBoolean("is_verified"));
                user.setAdmin(rs.getBoolean("is_admin"));
                user.setRegistrationDate(rs.getTimestamp("created_at"));
                user.setLastLogin(rs.getTimestamp("last_login"));
                user.setSolvedCount(rs.getInt("solved_count"));
                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        RedisCache.put(cacheKey, users, 30);
        return users;
    }

    public int getUserScore(int userId) {
        // Простой запрос, не кэшируем
        String sql = "SELECT rating FROM users WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("rating");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getUserSolvedCount(int userId) {
        String sql = "SELECT COUNT(*) FROM solved_tasks WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getUserRank(int userId) {
        // Не кэшируем, так как рейтинг часто меняется
        String sql = "SELECT COUNT(*) + 1 as rank FROM users WHERE rating > (SELECT rating FROM users WHERE id = ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("rank");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 1;
    }

    public int getTotalUsersCount() {
        String sql = "SELECT COUNT(*) FROM users WHERE is_active = true";
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

    // ========== ПРОФИЛЬ (с инвалидацией) ==========
    public void updateProfile(int userId, String bio, String city, int age) {
        String sql = "UPDATE users SET bio = ?, city = ?, age = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, bio);
            stmt.setString(2, city);
            stmt.setInt(3, age);
            stmt.setInt(4, userId);
            stmt.executeUpdate();
            RedisCache.remove("user_" + userId);
            RedisCache.remove("users_all");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void makeAdmin(int userId) {
        String sql = "UPDATE users SET is_admin = true WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
            System.out.println("User " + userId + " is now admin");
            RedisCache.remove("user_" + userId);
            RedisCache.remove("users_all");
            RedisCache.remove("users_all_with_details");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ========== ИСТОРИЯ ==========
    public List<Map<String, Object>> getUserSolveHistory(int userId) {
        // Не кэшируем историю
        List<Map<String, Object>> history = new ArrayList<>();
        String sql = "SELECT st.*, t.title, t.points, t.category_id, c.name as category_name " +
                "FROM solved_tasks st " +
                "JOIN tasks t ON st.task_id = t.id " +
                "LEFT JOIN categories c ON t.category_id = c.id " +
                "WHERE st.user_id = ? " +
                "ORDER BY st.solved_at DESC LIMIT 20";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> solve = new HashMap<>();
                solve.put("task_title", rs.getString("title"));
                solve.put("points", rs.getInt("points"));
                solve.put("category", rs.getString("category_name"));
                solve.put("solved_at", rs.getTimestamp("solved_at"));
                history.add(solve);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return history;
    }

    public void updateUsername(int userId, String newUsername) {
        String sql = "UPDATE users SET username = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newUsername);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
            RedisCache.remove("user_" + userId);
            RedisCache.remove("users_all");
            RedisCache.remove("users_all_with_details");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateAvatar(int userId, String avatarPath) {
        String sql = "UPDATE users SET avatar_path = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, avatarPath);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
            RedisCache.remove("user_" + userId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Map<String, Object>> getUserContestHistory(int userId) {
        // Не кэшируем
        List<Map<String, Object>> contests = new ArrayList<>();
        String sql = "SELECT c.*, " +
                "COUNT(DISTINCT st.id) as solved_tasks, " +
                "COALESCE(SUM(st.points_earned), 0) as earned_points " +
                "FROM contest_participants cp " +
                "JOIN contests c ON cp.contest_id = c.id " +
                "LEFT JOIN tasks t ON c.id = t.contest_id " +
                "LEFT JOIN solved_tasks st ON t.id = st.task_id AND st.user_id = ? " +
                "WHERE cp.user_id = ? " +
                "GROUP BY c.id " +
                "ORDER BY c.end_time DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> contest = new HashMap<>();
                contest.put("id", rs.getInt("id"));
                contest.put("title", rs.getString("title"));
                contest.put("start_time", rs.getTimestamp("start_time"));
                contest.put("end_time", rs.getTimestamp("end_time"));
                contest.put("solved_tasks", rs.getInt("solved_tasks"));
                contest.put("earned_points", rs.getInt("earned_points"));
                contests.add(contest);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return contests;
    }

    // ========== ВСПОМОГАТЕЛЬНЫЕ ==========
    private User extractUserFromResultSet(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password_hash"));
        user.setScore(rs.getInt("rating"));
        user.setVerified(rs.getBoolean("is_verified"));
        user.setAdmin(rs.getBoolean("is_admin"));
        user.setRegistrationDate(rs.getTimestamp("created_at"));
        user.setLastLogin(rs.getTimestamp("last_login"));
        try { user.setBio(rs.getString("bio")); } catch (SQLException e) { user.setBio(""); }
        try { user.setCity(rs.getString("city")); } catch (SQLException e) { user.setCity(""); }
        try { user.setAge(rs.getInt("age")); } catch (SQLException e) { user.setAge(0); }

        // Пытаемся получить solved_count, если есть
        try { user.setSolvedCount(rs.getInt("solved_count")); } catch (SQLException e) {
            user.setSolvedCount(0);
        }

        return user;
    }

    public void updateUserRating(int userId, int pointsToAdd) {
        String sql = "UPDATE users SET rating = rating + ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, pointsToAdd);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
            System.out.println("Added " + pointsToAdd + " points to user " + userId);
            RedisCache.remove("user_" + userId);
            RedisCache.remove("users_all");
            RedisCache.remove("users_all_with_details");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Map<String, Object>> getUserTeams(int userId) {
        TeamDAO teamDAO = new TeamDAO();
        return teamDAO.getUserTeams(userId);
    }

    public List<User> getAllUsersWithDetails() {
        String cacheKey = "users_all_with_details";

        List<User> cached = RedisCache.get(cacheKey, new TypeReference<List<User>>() {});
        if (cached != null) {
            return cached;
        }

        List<User> users = new ArrayList<>();
        String sql = "SELECT u.id, u.username, u.email, u.rating, u.is_verified, u.is_admin, " +
                "u.is_active, u.created_at, u.last_login, " +
                "COALESCE((SELECT COUNT(*) FROM solved_tasks WHERE user_id = u.id), 0) as solved_count " +
                "FROM users u ORDER BY u.id";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setEmail(rs.getString("email"));
                user.setScore(rs.getInt("rating"));
                user.setVerified(rs.getBoolean("is_verified"));
                user.setAdmin(rs.getBoolean("is_admin"));
                user.setActive(rs.getBoolean("is_active"));
                user.setRegistrationDate(rs.getTimestamp("created_at"));
                user.setLastLogin(rs.getTimestamp("last_login"));
                user.setSolvedCount(rs.getInt("solved_count"));
                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        RedisCache.put(cacheKey, users, 60);
        return users;
    }

    public boolean toggleUserActive(int userId) {
        String sql = "UPDATE users SET is_active = NOT is_active WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            int updated = stmt.executeUpdate();
            System.out.println("Toggle user active. User ID: " + userId + ", Updated: " + updated);
            RedisCache.remove("user_" + userId);
            RedisCache.remove("users_all");
            RedisCache.remove("users_all_with_details");
            return updated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isUserBlocked(int userId) {
        String sql = "SELECT is_active FROM users WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return !rs.getBoolean("is_active");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public User authenticateUserWithStatus(String email, String password) {
        String sql = "SELECT u.id, u.username, u.email, u.password_hash, u.rating, " +
                "u.is_verified, u.is_admin, u.is_active, u.bio, u.age, u.city, u.created_at, u.last_login, " +
                "COALESCE((SELECT COUNT(*) FROM solved_tasks WHERE user_id = u.id), 0) as solved_count " +
                "FROM users u WHERE u.email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String storedHash = rs.getString("password_hash");
                if (com.example.util.PasswordUtil.checkPassword(password, storedHash)) {
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setUsername(rs.getString("username"));
                    user.setEmail(rs.getString("email"));
                    user.setPassword(storedHash);
                    user.setScore(rs.getInt("rating"));
                    user.setVerified(rs.getBoolean("is_verified"));
                    user.setAdmin(rs.getBoolean("is_admin"));
                    user.setActive(rs.getBoolean("is_active"));
                    user.setSolvedCount(rs.getInt("solved_count"));  // <--- ДОБАВЛЯЕМ
                    user.setBio(rs.getString("bio"));
                    user.setCity(rs.getString("city"));
                    user.setAge(rs.getInt("age"));
                    user.setRegistrationDate(rs.getTimestamp("created_at"));
                    user.setLastLogin(rs.getTimestamp("last_login"));
                    return user;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Сохранить код подтверждения для пользователя
     */
    public void saveVerificationCode(String email, String code) {
        String getUserIdSql = "SELECT id FROM users WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(getUserIdSql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int userId = rs.getInt("id");
                String insertSql = "INSERT INTO verification_codes (user_id, code, type, expires_at) VALUES (?, ?, 'email_verification', NOW() + INTERVAL '1 day')";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.setInt(1, userId);
                    insertStmt.setString(2, code);
                    insertStmt.executeUpdate();
                    System.out.println("Verification code saved for user: " + email);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Проверить код подтверждения и активировать аккаунт
     */
    public boolean verifyEmailByCode(String email, String code) {
        String sql = "SELECT vc.id FROM verification_codes vc " +
                "JOIN users u ON vc.user_id = u.id " +
                "WHERE u.email = ? AND vc.code = ? AND vc.type = 'email_verification' " +
                "AND vc.expires_at > NOW() AND vc.is_used = false";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setString(2, code);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int codeId = rs.getInt("id");
                String updateSql = "UPDATE verification_codes SET is_used = true WHERE id = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setInt(1, codeId);
                    updateStmt.executeUpdate();
                }

                String verifySql = "UPDATE users SET is_verified = true WHERE email = ?";
                try (PreparedStatement verifyStmt = conn.prepareStatement(verifySql)) {
                    verifyStmt.setString(1, email);
                    verifyStmt.executeUpdate();
                }

                // Инвалидируем кэш
                RedisCache.remove("user_email_" + email);
                RedisCache.removeByPrefix("user_");

                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Проверить, подтверждён ли email
     */
    public boolean isEmailVerified(String email) {
        String sql = "SELECT is_verified FROM users WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getBoolean("is_verified");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}