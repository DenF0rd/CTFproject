package com.example.dao;

import com.example.model.Team;
import com.example.util.DBConnection;
import java.sql.*;
import java.util.*;

public class TeamDAO {

    // Создать команду
    public boolean createTeam(String name, String description, int captainId, String inviteCode) {
        // Проверяем, не состоит ли пользователь уже в команде
        if (isUserInAnyTeam(captainId)) {
            System.out.println("User " + captainId + " is already in a team");
            return false;
        }

        String sql = "INSERT INTO teams (name, description, captain_id, created_at, updated_at) VALUES (?, ?, ?, NOW(), NOW())";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, name);
            stmt.setString(2, description != null ? description : "");
            stmt.setInt(3, captainId);

            int affected = stmt.executeUpdate();

            if (affected > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    int teamId = rs.getInt(1);
                    // Добавляем капитана как участника с ролью captain
                    addMemberWithRole(teamId, captainId, "captain");
                    System.out.println("Team created with ID: " + teamId);
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Новый метод для добавления участника с указанной ролью
    public boolean addMemberWithRole(int teamId, int userId, String role) {
        String sql = "INSERT INTO team_members (team_id, user_id, role, joined_at) VALUES (?, ?, ?, NOW())";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, teamId);
            stmt.setInt(2, userId);
            stmt.setString(3, role);
            int result = stmt.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Добавить участника
    public boolean addMember(int teamId, int userId) {
        // Сначала проверяем, является ли пользователь капитаном команды
        Team team = getTeamById(teamId);
        boolean isCaptain = (team != null && team.getCaptainId() == userId);
        String role = isCaptain ? "captain" : "member";

        String sql = "INSERT INTO team_members (team_id, user_id, role, joined_at) VALUES (?, ?, ?, NOW())";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, teamId);
            stmt.setInt(2, userId);
            stmt.setString(3, role);
            int result = stmt.executeUpdate();
            System.out.println("Add member result: " + result + ", role: " + role);
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Сделать пользователя капитаном
    public boolean makeCaptain(int teamId, int userId) {
        String sql = "UPDATE teams SET captain_id = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, teamId);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Удалить участника
    public boolean removeMember(int teamId, int userId) {
        String sql = "DELETE FROM team_members WHERE team_id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, teamId);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Получить команду по ID
    public Team getTeamById(int teamId) {
        String sql = "SELECT t.*, COUNT(tm.user_id) as members_count FROM teams t " +
                "LEFT JOIN team_members tm ON t.id = tm.team_id WHERE t.id = ? GROUP BY t.id";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, teamId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return extractTeamFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Получить команду пользователя
    public Team getTeamByUser(int contestId, int userId) {
        String sql = "SELECT t.*, COUNT(tm.user_id) as members_count, " +
                "CASE WHEN t.captain_id = ? THEN true ELSE false END as is_captain " +
                "FROM contest_participants cp " +
                "JOIN teams t ON cp.team_id = t.id " +
                "LEFT JOIN team_members tm ON t.id = tm.team_id " +
                "WHERE cp.contest_id = ? AND cp.user_id = ? " +
                "GROUP BY t.id";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, contestId);
            stmt.setInt(3, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return extractTeamFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Получить участников команды
    public List<Map<String, Object>> getTeamMembers(int teamId) {
        List<Map<String, Object>> members = new ArrayList<>();
        String sql = "SELECT u.id, u.username, CASE WHEN t.captain_id = u.id THEN true ELSE false END as is_captain " +
                "FROM team_members tm " +
                "JOIN users u ON tm.user_id = u.id " +
                "JOIN teams t ON tm.team_id = t.id " +
                "WHERE tm.team_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, teamId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> member = new HashMap<>();
                member.put("id", rs.getInt("id"));
                member.put("username", rs.getString("username"));
                member.put("isCaptain", rs.getBoolean("is_captain"));
                members.add(member);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return members;
    }

    // Получить все команды пользователя
    public List<Map<String, Object>> getUserTeams(int userId) {
        List<Map<String, Object>> teams = new ArrayList<>();
        String sql = "SELECT t.*, " +
                "COUNT(DISTINCT tm2.user_id) as members_count, " +
                "CASE WHEN t.captain_id = ? THEN true ELSE false END as is_captain " +
                "FROM team_members tm " +
                "JOIN teams t ON tm.team_id = t.id " +
                "LEFT JOIN team_members tm2 ON t.id = tm2.team_id " +
                "WHERE tm.user_id = ? " +
                "GROUP BY t.id";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> team = new HashMap<>();
                team.put("id", rs.getInt("id"));
                team.put("name", rs.getString("name"));
                team.put("description", rs.getString("description"));
                team.put("members_count", rs.getInt("members_count"));
                team.put("total_points", rs.getInt("total_points"));
                team.put("is_captain", rs.getBoolean("is_captain"));
                teams.add(team);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return teams;
    }

    // Проверить, состоит ли пользователь в команде
    public boolean isUserInTeam(int teamId, int userId) {
        String sql = "SELECT 1 FROM team_members WHERE team_id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, teamId);
            stmt.setInt(2, userId);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Удалить команду
    public boolean deleteTeam(int teamId) {
        String sql = "DELETE FROM teams WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, teamId);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Team extractTeamFromResultSet(ResultSet rs) throws SQLException {
        Team team = new Team();
        team.setId(rs.getInt("id"));
        team.setName(rs.getString("name"));
        team.setDescription(rs.getString("description"));
        team.setCaptainId(rs.getInt("captain_id"));
        team.setCreatedAt(rs.getTimestamp("created_at"));
        team.setMembersCount(rs.getInt("members_count"));
        team.setTotalPoints(rs.getInt("total_points"));
        try {
            team.setCaptain(rs.getBoolean("is_captain"));
        } catch (SQLException e) {
            team.setCaptain(false);
        }
        return team;
    }

    // ========== МЕТОДЫ ДЛЯ ЗАЯВОК НА ВСТУПЛЕНИЕ ==========

    /**
     * Создать заявку на вступление в команду
     */
    public boolean createJoinRequest(int teamId, int userId, String message) {
        // Проверяем, не отправлял ли пользователь уже заявку
        if (hasPendingRequest(teamId, userId)) {
            System.out.println("User " + userId + " already has a pending request for team " + teamId);
            return false;
        }

        // Проверяем, не состоит ли уже пользователь в команде
        if (isUserInTeam(teamId, userId)) {
            System.out.println("User " + userId + " is already in team " + teamId);
            return false;
        }

        String sql = "INSERT INTO team_join_requests (team_id, user_id, message, status, created_at) VALUES (?, ?, ?, 'pending', NOW())";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, teamId);
            stmt.setInt(2, userId);
            stmt.setString(3, message != null ? message : "");
            stmt.executeUpdate();
            System.out.println("Join request created for user " + userId + " to team " + teamId);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Проверить, есть ли у пользователя ожидающая заявка
     */
    private boolean hasPendingRequest(int teamId, int userId) {
        String sql = "SELECT 1 FROM team_join_requests WHERE team_id = ? AND user_id = ? AND status = 'pending'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, teamId);
            stmt.setInt(2, userId);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Получить заявки на вступление в команду
     */
    public List<Map<String, Object>> getJoinRequests(int teamId) {
        List<Map<String, Object>> requests = new ArrayList<>();
        String sql = "SELECT tjr.*, u.username FROM team_join_requests tjr " +
                "JOIN users u ON tjr.user_id = u.id " +
                "WHERE tjr.team_id = ? AND tjr.status = 'pending' " +
                "ORDER BY tjr.created_at ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, teamId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> request = new HashMap<>();
                request.put("id", rs.getInt("id"));
                request.put("userId", rs.getInt("user_id"));
                request.put("username", rs.getString("username"));
                request.put("message", rs.getString("message"));
                request.put("createdAt", rs.getTimestamp("created_at"));
                requests.add(request);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return requests;
    }

    /**
     * Ответить на заявку (принять/отклонить)
     */
    public boolean respondToRequest(int requestId, String status) {
        String sql = "UPDATE team_join_requests SET status = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, requestId);
            int updated = stmt.executeUpdate();

            // Если приняли, добавляем пользователя в команду
            if ("approved".equals(status) && updated > 0) {
                String getInfoSql = "SELECT team_id, user_id FROM team_join_requests WHERE id = ?";
                try (PreparedStatement stmt2 = conn.prepareStatement(getInfoSql)) {
                    stmt2.setInt(1, requestId);
                    ResultSet rs = stmt2.executeQuery();
                    if (rs.next()) {
                        addMember(rs.getInt("team_id"), rs.getInt("user_id"));
                    }
                }
            }
            return updated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ========== МЕТОДЫ ДЛЯ ВСТУПЛЕНИЯ В КОМАНДУ ==========

    /**
     * Получить все команды (для вступления)
     */
    public List<Map<String, Object>> getAllTeams() {
        List<Map<String, Object>> teams = new ArrayList<>();
        String sql = "SELECT t.*, COUNT(tm.user_id) as members_count, u.username as captain_name " +
                "FROM teams t " +
                "LEFT JOIN team_members tm ON t.id = tm.team_id " +
                "LEFT JOIN users u ON t.captain_id = u.id " +
                "GROUP BY t.id, u.username " +
                "ORDER BY t.created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> team = new HashMap<>();
                team.put("id", rs.getInt("id"));
                team.put("name", rs.getString("name"));
                team.put("description", rs.getString("description"));
                team.put("captain_id", rs.getInt("captain_id"));
                team.put("captain_name", rs.getString("captain_name"));
                team.put("members_count", rs.getInt("members_count"));
                team.put("total_points", rs.getInt("total_points"));
                teams.add(team);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return teams;
    }

    /**
     * Получить доступные команды (в которые можно вступить)
     */
    public List<Map<String, Object>> getAvailableTeams(int userId) {
        List<Map<String, Object>> teams = new ArrayList<>();
        String sql = "SELECT t.*, COUNT(tm.user_id) as members_count, u.username as captain_name, " +
                "CASE WHEN tm2.user_id IS NOT NULL THEN true ELSE false END as is_member " +
                "FROM teams t " +
                "LEFT JOIN team_members tm ON t.id = tm.team_id " +
                "LEFT JOIN users u ON t.captain_id = u.id " +
                "LEFT JOIN team_members tm2 ON t.id = tm2.team_id AND tm2.user_id = ? " +
                "WHERE tm2.user_id IS NULL " +
                "GROUP BY t.id, u.username, tm2.user_id " +
                "ORDER BY t.created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> team = new HashMap<>();
                team.put("id", rs.getInt("id"));
                team.put("name", rs.getString("name"));
                team.put("description", rs.getString("description"));
                team.put("captain_id", rs.getInt("captain_id"));
                team.put("captain_name", rs.getString("captain_name"));
                team.put("members_count", rs.getInt("members_count"));
                team.put("total_points", rs.getInt("total_points"));
                teams.add(team);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return teams;
    }

    /**
     * Отправить заявку на вступление
     */
    public boolean sendJoinRequest(int teamId, int userId, String message) {
        if (hasPendingRequest(teamId, userId)) {
            return false;
        }
        if (isUserInTeam(teamId, userId)) {
            return false;
        }
        String sql = "INSERT INTO team_join_requests (team_id, user_id, message, status, created_at) VALUES (?, ?, ?, 'pending', NOW())";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, teamId);
            stmt.setInt(2, userId);
            stmt.setString(3, message != null ? message : "");
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Принять заявку
     */
    public boolean approveRequest(int requestId) {
        return respondToRequest(requestId, "approved");
    }

    /**
     * Отклонить заявку
     */
    public boolean declineRequest(int requestId) {
        return respondToRequest(requestId, "declined");
    }

// ========== МЕТОДЫ ДЛЯ РЕДАКТИРОВАНИЯ КОМАНДЫ ==========

    /**
     * Передать лидерство другому участнику
     */
    public boolean transferLeadership(int teamId, int currentCaptainId, int newCaptainId) {
        // Проверяем, что текущий пользователь - капитан
        Team team = getTeamById(teamId);
        if (team == null || team.getCaptainId() != currentCaptainId) {
            System.out.println("User " + currentCaptainId + " is not captain of team " + teamId);
            return false;
        }

        // Проверяем, что новый капитан состоит в команде
        if (!isUserInTeam(teamId, newCaptainId)) {
            System.out.println("User " + newCaptainId + " is not in team " + teamId);
            return false;
        }

        // Обновляем капитана в таблице teams
        String sql = "UPDATE teams SET captain_id = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, newCaptainId);
            stmt.setInt(2, teamId);
            stmt.executeUpdate();

            // Обновляем роли в team_members
            // Старому капитану ставим роль 'member'
            String updateOldCaptainSql = "UPDATE team_members SET role = 'member' WHERE team_id = ? AND user_id = ?";
            try (PreparedStatement stmt2 = conn.prepareStatement(updateOldCaptainSql)) {
                stmt2.setInt(1, teamId);
                stmt2.setInt(2, currentCaptainId);
                stmt2.executeUpdate();
            }

            // Новому капитану ставим роль 'captain'
            String updateNewCaptainSql = "UPDATE team_members SET role = 'captain' WHERE team_id = ? AND user_id = ?";
            try (PreparedStatement stmt3 = conn.prepareStatement(updateNewCaptainSql)) {
                stmt3.setInt(1, teamId);
                stmt3.setInt(2, newCaptainId);
                stmt3.executeUpdate();
            }

            System.out.println("Leadership transferred from " + currentCaptainId + " to " + newCaptainId);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Обновить информацию о команде
     */
    public boolean updateTeam(int teamId, String name, String description) {
        String sql = "UPDATE teams SET name = ?, description = ?, updated_at = NOW() WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setString(2, description != null ? description : "");
            stmt.setInt(3, teamId);
            int updated = stmt.executeUpdate();
            System.out.println("Team updated: " + teamId + ", rows affected: " + updated);
            return updated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Получить команду пользователя (без привязки к соревнованию)
     */
    public Team getTeamByUser(int userId) {
        String sql = "SELECT t.*, COUNT(tm.user_id) as members_count, " +
                "CASE WHEN t.captain_id = ? THEN true ELSE false END as is_captain " +
                "FROM teams t " +
                "LEFT JOIN team_members tm ON t.id = tm.team_id " +
                "WHERE tm.user_id = ? " +
                "GROUP BY t.id";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Team team = new Team();
                team.setId(rs.getInt("id"));
                team.setName(rs.getString("name"));
                team.setDescription(rs.getString("description"));
                team.setCaptainId(rs.getInt("captain_id"));
                team.setCreatedAt(rs.getTimestamp("created_at"));
                team.setMembersCount(rs.getInt("members_count"));
                team.setCaptain(rs.getBoolean("is_captain"));
                team.setTotalPoints(rs.getInt("total_points"));
                return team;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Удалить команду (только для капитана)
     * При удалении команды автоматически удаляются все связи из team_members
     * благодаря ON DELETE CASCADE в базе данных
     */
    public boolean deleteTeam(int teamId, int captainId) {
        // Сначала проверяем, что пользователь является капитаном
        Team team = getTeamById(teamId);
        if (team == null || team.getCaptainId() != captainId) {
            System.out.println("User " + captainId + " is not captain of team " + teamId);
            return false;
        }

        String sql = "DELETE FROM teams WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, teamId);
            int deleted = stmt.executeUpdate();
            System.out.println("Team deleted: " + teamId + ", rows affected: " + deleted);
            return deleted > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Получить участников команды с детальной информацией
     */
    public List<Map<String, Object>> getTeamMembersWithDetails(int teamId) {
        List<Map<String, Object>> members = new ArrayList<>();
        String sql = "SELECT u.id, u.username, u.email, u.rating, " +
                "CASE WHEN t.captain_id = u.id THEN true ELSE false END as is_captain, " +
                "tm.joined_at, tm.role " +
                "FROM team_members tm " +
                "JOIN users u ON tm.user_id = u.id " +
                "JOIN teams t ON tm.team_id = t.id " +
                "WHERE tm.team_id = ? " +
                "ORDER BY is_captain DESC, tm.joined_at ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, teamId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> member = new HashMap<>();
                member.put("id", rs.getInt("id"));
                member.put("username", rs.getString("username"));
                member.put("email", rs.getString("email"));
                member.put("rating", rs.getInt("rating"));
                member.put("isCaptain", rs.getBoolean("is_captain"));
                member.put("joinedAt", rs.getTimestamp("joined_at"));
                member.put("role", rs.getString("role"));  // Это должно быть 'captain' или 'member'
                members.add(member);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return members;
    }

    // Добавьте константу для максимального количества участников
    private static final int MAX_TEAM_MEMBERS = 5;

    /**
     * Проверить, состоит ли пользователь в какой-либо команде
     */
    public boolean isUserInAnyTeam(int userId) {
        String sql = "SELECT 1 FROM team_members WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Получить текущую команду пользователя
     */
    public Team getCurrentUserTeam(int userId) {
        String sql = "SELECT t.*, COUNT(tm.user_id) as members_count, " +
                "CASE WHEN t.captain_id = ? THEN true ELSE false END as is_captain " +
                "FROM teams t " +
                "JOIN team_members tm ON t.id = tm.team_id " +
                "WHERE tm.user_id = ? " +
                "GROUP BY t.id";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return extractTeamFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Получить количество участников в команде
     */
    public int getTeamMemberCount(int teamId) {
        String sql = "SELECT COUNT(*) FROM team_members WHERE team_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, teamId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Проверить, есть ли место в команде
     */
    public boolean hasTeamSpace(int teamId) {
        return getTeamMemberCount(teamId) < MAX_TEAM_MEMBERS;
    }

    /**
     * Поиск команд по названию
     */
    public List<Map<String, Object>> searchTeams(String query, int userId) {
        List<Map<String, Object>> teams = new ArrayList<>();
        String sql = "SELECT t.*, COUNT(tm.user_id) as members_count, u.username as captain_name, " +
                "CASE WHEN tm2.user_id IS NOT NULL THEN true ELSE false END as is_member " +
                "FROM teams t " +
                "LEFT JOIN team_members tm ON t.id = tm.team_id " +
                "LEFT JOIN users u ON t.captain_id = u.id " +
                "LEFT JOIN team_members tm2 ON t.id = tm2.team_id AND tm2.user_id = ? " +
                "WHERE t.name ILIKE ? AND tm2.user_id IS NULL " +
                "GROUP BY t.id, u.username, tm2.user_id " +
                "ORDER BY t.name ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, "%" + query + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> team = new HashMap<>();
                team.put("id", rs.getInt("id"));
                team.put("name", rs.getString("name"));
                team.put("description", rs.getString("description"));
                team.put("captain_id", rs.getInt("captain_id"));
                team.put("captain_name", rs.getString("captain_name"));
                team.put("members_count", rs.getInt("members_count"));
                team.put("total_points", rs.getInt("total_points"));
                teams.add(team);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return teams;
    }

}