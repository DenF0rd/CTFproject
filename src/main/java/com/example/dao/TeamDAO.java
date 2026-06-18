package com.example.dao;

import com.example.model.Team;
import com.example.util.DBConnection;
import com.example.util.RedisCache;
import com.fasterxml.jackson.core.type.TypeReference;

import java.sql.*;
import java.util.*;

public class TeamDAO {

    // ========== СОЗДАНИЕ КОМАНДЫ (с инвалидацией) ==========
    public boolean createTeam(String name, String description, int captainId, String inviteCode) {
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
                    addMemberWithRole(teamId, captainId, "captain");
                    System.out.println("Team created with ID: " + teamId);
                    RedisCache.remove("teams_all");
                    RedisCache.removeByPrefix("user_teams_");
                    RedisCache.removeByPrefix("team_user_");
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean addMemberWithRole(int teamId, int userId, String role) {
        String sql = "INSERT INTO team_members (team_id, user_id, role, joined_at) VALUES (?, ?, ?, NOW())";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, teamId);
            stmt.setInt(2, userId);
            stmt.setString(3, role);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ========== ДОБАВЛЕНИЕ УЧАСТНИКА (с инвалидацией) ==========
    public boolean addMember(int teamId, int userId) {
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

            if (result > 0) {
                RedisCache.remove("team_" + teamId);
                RedisCache.remove("team_members_" + teamId);
                RedisCache.removeByPrefix("team_user_");
                RedisCache.removeByPrefix("user_teams_");
                RedisCache.remove("teams_all");
            }
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean makeCaptain(int teamId, int userId) {
        String sql = "UPDATE teams SET captain_id = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, teamId);
            stmt.executeUpdate();
            RedisCache.remove("team_" + teamId);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ========== УДАЛЕНИЕ УЧАСТНИКА (с инвалидацией) ==========
    public boolean removeMember(int teamId, int userId) {
        String sql = "DELETE FROM team_members WHERE team_id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, teamId);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
            RedisCache.remove("team_" + teamId);
            RedisCache.remove("team_members_" + teamId);
            RedisCache.removeByPrefix("team_user_");
            RedisCache.removeByPrefix("user_teams_");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ========== ПОЛУЧЕНИЕ КОМАНДЫ ПО ID (с кэшем) ==========
    public Team getTeamById(int teamId) {
        String cacheKey = "team_" + teamId;

        Team cached = RedisCache.get(cacheKey, Team.class);
        if (cached != null) {
            System.out.println("Redis HIT: " + cacheKey);
            return cached;
        }

        System.out.println("Redis MISS: " + cacheKey + " - loading from DB");

        String sql = "SELECT t.*, COUNT(tm.user_id) as members_count FROM teams t " +
                "LEFT JOIN team_members tm ON t.id = tm.team_id WHERE t.id = ? GROUP BY t.id";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, teamId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Team team = extractTeamFromResultSet(rs);
                RedisCache.put(cacheKey, team, 60);
                return team;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ========== ПОЛУЧЕНИЕ КОМАНДЫ ПОЛЬЗОВАТЕЛЯ (с кэшем) ==========
    public Team getTeamByUser(int contestId, int userId) {
        // Для конкретного соревнования
        String cacheKey = "team_user_contest_" + contestId + "_" + userId;

        Team cached = RedisCache.get(cacheKey, Team.class);
        if (cached != null) {
            return cached;
        }

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
                Team team = extractTeamFromResultSet(rs);
                RedisCache.put(cacheKey, team, 30);
                return team;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ========== ПОЛУЧЕНИЕ УЧАСТНИКОВ КОМАНДЫ (с кэшем) ==========
    public List<Map<String, Object>> getTeamMembers(int teamId) {
        String cacheKey = "team_members_" + teamId;

        List<Map<String, Object>> cached = RedisCache.get(cacheKey, new TypeReference<List<Map<String, Object>>>() {});
        if (cached != null) {
            return cached;
        }

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

        RedisCache.put(cacheKey, members, 30);
        return members;
    }

    // ========== ПОЛУЧЕНИЕ ВСЕХ КОМАНД ПОЛЬЗОВАТЕЛЯ (с кэшем) ==========
    public List<Map<String, Object>> getUserTeams(int userId) {
        String cacheKey = "user_teams_" + userId;

        List<Map<String, Object>> cached = RedisCache.get(cacheKey, new TypeReference<List<Map<String, Object>>>() {});
        if (cached != null) {
            return cached;
        }

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

        RedisCache.put(cacheKey, teams, 30);
        return teams;
    }

    // ========== ПРОВЕРКА, СОСТОИТ ЛИ ПОЛЬЗОВАТЕЛЬ В КОМАНДЕ ==========
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

    // ========== УДАЛЕНИЕ КОМАНДЫ (с инвалидацией) ==========
    public boolean deleteTeam(int teamId) {
        String sql = "DELETE FROM teams WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, teamId);
            stmt.executeUpdate();
            RedisCache.remove("team_" + teamId);
            RedisCache.remove("team_members_" + teamId);
            RedisCache.remove("teams_all");
            RedisCache.removeByPrefix("team_user_");
            RedisCache.removeByPrefix("user_teams_");
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

    // ========== ЗАЯВКИ НА ВСТУПЛЕНИЕ ==========
    public boolean createJoinRequest(int teamId, int userId, String message) {
        if (hasPendingRequest(teamId, userId)) {
            System.out.println("User " + userId + " already has a pending request for team " + teamId);
            return false;
        }
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

    public List<Map<String, Object>> getJoinRequests(int teamId) {
        // Не кэшируем, так как это административный метод
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

    public boolean respondToRequest(int requestId, String status) {
        String sql = "UPDATE team_join_requests SET status = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, requestId);
            int updated = stmt.executeUpdate();
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

    // ========== ПОЛУЧЕНИЕ ВСЕХ КОМАНД (с кэшем) ==========
    public List<Map<String, Object>> getAllTeams() {
        String cacheKey = "teams_all";

        List<Map<String, Object>> cached = RedisCache.get(cacheKey, new TypeReference<List<Map<String, Object>>>() {});
        if (cached != null) {
            return cached;
        }

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

        RedisCache.put(cacheKey, teams, 60);
        return teams;
    }

    public List<Map<String, Object>> getAvailableTeams(int userId) {
        // Не кэшируем, так как зависит от userId
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

    public boolean sendJoinRequest(int teamId, int userId, String message) {
        return createJoinRequest(teamId, userId, message);
    }

    public boolean approveRequest(int requestId) {
        return respondToRequest(requestId, "approved");
    }

    public boolean declineRequest(int requestId) {
        return respondToRequest(requestId, "declined");
    }

    // ========== ПЕРЕДАЧА ЛИДЕРСТВА (с инвалидацией) ==========
    public boolean transferLeadership(int teamId, int currentCaptainId, int newCaptainId) {
        Team team = getTeamById(teamId);
        if (team == null || team.getCaptainId() != currentCaptainId) {
            System.out.println("User " + currentCaptainId + " is not captain of team " + teamId);
            return false;
        }
        if (!isUserInTeam(teamId, newCaptainId)) {
            System.out.println("User " + newCaptainId + " is not in team " + teamId);
            return false;
        }

        String sql = "UPDATE teams SET captain_id = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, newCaptainId);
            stmt.setInt(2, teamId);
            stmt.executeUpdate();

            String updateOldCaptainSql = "UPDATE team_members SET role = 'member' WHERE team_id = ? AND user_id = ?";
            try (PreparedStatement stmt2 = conn.prepareStatement(updateOldCaptainSql)) {
                stmt2.setInt(1, teamId);
                stmt2.setInt(2, currentCaptainId);
                stmt2.executeUpdate();
            }

            String updateNewCaptainSql = "UPDATE team_members SET role = 'captain' WHERE team_id = ? AND user_id = ?";
            try (PreparedStatement stmt3 = conn.prepareStatement(updateNewCaptainSql)) {
                stmt3.setInt(1, teamId);
                stmt3.setInt(2, newCaptainId);
                stmt3.executeUpdate();
            }

            System.out.println("Leadership transferred from " + currentCaptainId + " to " + newCaptainId);
            RedisCache.remove("team_" + teamId);
            RedisCache.remove("team_members_" + teamId);
            RedisCache.removeByPrefix("team_user_");
            RedisCache.removeByPrefix("user_teams_");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateTeam(int teamId, String name, String description) {
        String sql = "UPDATE teams SET name = ?, description = ?, updated_at = NOW() WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setString(2, description != null ? description : "");
            stmt.setInt(3, teamId);
            int updated = stmt.executeUpdate();
            if (updated > 0) {
                RedisCache.remove("team_" + teamId);
                RedisCache.remove("teams_all");
            }
            return updated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Team getTeamByUser(int userId) {
        String cacheKey = "team_user_" + userId;

        Team cached = RedisCache.get(cacheKey, Team.class);
        if (cached != null) {
            return cached;
        }

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
                Team team = extractTeamFromResultSet(rs);
                RedisCache.put(cacheKey, team, 30);
                return team;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean deleteTeam(int teamId, int captainId) {
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
            if (deleted > 0) {
                RedisCache.remove("team_" + teamId);
                RedisCache.remove("team_members_" + teamId);
                RedisCache.remove("teams_all");
                RedisCache.removeByPrefix("team_user_");
                RedisCache.removeByPrefix("user_teams_");
            }
            return deleted > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Map<String, Object>> getTeamMembersWithDetails(int teamId) {
        // Не кэшируем, так как содержит email и rating
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
                member.put("role", rs.getString("role"));
                members.add(member);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return members;
    }

    private static final int MAX_TEAM_MEMBERS = 5;

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

    public Team getCurrentUserTeam(int userId) {
        return getTeamByUser(userId);
    }

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

    public boolean hasTeamSpace(int teamId) {
        return getTeamMemberCount(teamId) < MAX_TEAM_MEMBERS;
    }

    public List<Map<String, Object>> searchTeams(String query, int userId) {
        // Не кэшируем, так как это поиск
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