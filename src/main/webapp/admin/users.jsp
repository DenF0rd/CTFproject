<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*, com.example.model.User" %>
<%
    if (session.getAttribute("userId") == null) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }
    Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");
    if (isAdmin == null || !isAdmin) {
        response.sendRedirect(request.getContextPath() + "/contests");
        return;
    }

    List<User> users = (List<User>) request.getAttribute("users");
    int currentUserId = (int) session.getAttribute("userId");
    if (users == null) users = new ArrayList<>();
%>
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Управление пользователями - Админ-панель</title>
    <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600;700;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body {
            font-family: 'Poppins', sans-serif;
            background: linear-gradient(135deg, #0f0c29 0%, #302b63 50%, #24243e 100%);
            min-height: 100vh;
            color: #ffffff;
        }
        .navbar {
            background: rgba(15, 12, 41, 0.95);
            backdrop-filter: blur(20px);
            border-bottom: 1px solid rgba(139, 92, 246, 0.3);
            padding: 0 2rem;
            height: 70px;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        .logo { font-size: 1.5rem; font-weight: 700; background: linear-gradient(135deg, #fff, #a78bfa); -webkit-background-clip: text; background-clip: text; -webkit-text-fill-color: transparent; }
        .nav-links { display: flex; gap: 2rem; }
        .nav-links a { text-decoration: none; color: rgba(255,255,255,0.7); font-weight: 500; transition: color 0.3s; }
        .nav-links a:hover, .nav-links a.active { color: #c084fc; }
        .logout-btn { background: linear-gradient(135deg, #ef4444, #dc2626); color: white; padding: 8px 18px; border-radius: 12px; text-decoration: none; }
        .container { max-width: 1400px; margin: 2rem auto; padding: 0 2rem; }
        .page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 2rem; flex-wrap: wrap; gap: 1rem; }
        .page-header h1 { font-size: 1.8rem; }
        .back-link { color: #a78bfa; text-decoration: none; transition: color 0.3s; }
        .back-link:hover { color: #c084fc; }
        .users-table { background: rgba(255,255,255,0.05); backdrop-filter: blur(20px); border-radius: 20px; overflow-x: auto; }
        table { width: 100%; border-collapse: collapse; }
        th, td { padding: 1rem; text-align: left; border-bottom: 1px solid rgba(255,255,255,0.1); }
        th { color: #c084fc; font-weight: 600; }
        tr:hover td { background: rgba(139,92,246,0.1); }
        .status-active { color: #10b981; }
        .status-inactive { color: #ef4444; }
        .admin-badge { background: #8b5cf6; padding: 2px 8px; border-radius: 12px; font-size: 0.7rem; }
        .btn { padding: 6px 12px; border-radius: 8px; font-size: 0.75rem; font-weight: 600; cursor: pointer; border: none; transition: all 0.3s; }
        .btn-warning { background: #f59e0b; color: #1a1a2e; }
        .btn-warning:hover { background: #d97706; }
        .btn-success { background: #10b981; color: white; }
        .btn-success:hover { background: #059669; }
        .btn-sm { padding: 4px 10px; font-size: 0.7rem; }
        .disabled-row { opacity: 0.6; }
        .disabled-btn { background: #6b7280; cursor: not-allowed; opacity: 0.5; }
        @media (max-width: 768px) {
            .navbar { flex-direction: column; height: auto; padding: 1rem; gap: 0.8rem; }
            .nav-links { flex-wrap: wrap; justify-content: center; }
            .container { padding: 0 1rem; }
            th, td { padding: 0.5rem; font-size: 0.8rem; }
        }
    </style>
</head>
<body>

<nav class="navbar">
    <div class="logo">🏆 CTF Platform</div>
    <div class="nav-links">
        <a href="${pageContext.request.contextPath}/contests">Соревнования</a>
        <a href="${pageContext.request.contextPath}/teams">Команды</a>
        <a href="${pageContext.request.contextPath}/scoreboard">Рейтинг</a>
        <a href="${pageContext.request.contextPath}/profile?id=<%= session.getAttribute("userId") %>">Профиль</a>
        <a href="${pageContext.request.contextPath}/admin" class="active">Админ-панель</a>
    </div>
    <a href="${pageContext.request.contextPath}/logout" class="logout-btn"><i class="fas fa-sign-out-alt"></i> Выйти</a>
</nav>

<div class="container">
    <div class="page-header">
        <div>
            <a href="${pageContext.request.contextPath}/admin" class="back-link"><i class="fas fa-arrow-left"></i> Назад</a>
            <h1 style="margin-top: 0.5rem;"><i class="fas fa-users"></i> Управление пользователями</h1>
        </div>
        <div>
            <span class="admin-badge"><i class="fas fa-shield-alt"></i> Всего: <%= users.size() %> пользователей</span>
        </div>
    </div>

    <div class="users-table">
        <table>
            <thead>
            <tr>
                <th>ID</th>
                <th>Имя пользователя</th>
                <th>Email</th>
                <th>Очки</th>
                <th>Статус</th>
                <th>Роль</th>
                <th>Регистрация</th>
                <th>Действия</th>
            </tr>
            </thead>
            <tbody>
            <% for (User user : users) {
                boolean isCurrentUser = user.getId() == currentUserId;
                boolean isAdminUser = user.isAdmin();
            %>
            <tr class="<%= isAdminUser && !isCurrentUser ? "disabled-row" : "" %>">
                <td><%= user.getId() %></td>
                <td><%= user.getUsername() %><%= isCurrentUser ? " <span style='color:#c084fc;'>(Вы)</span>" : "" %></td>
                <td><%= user.getEmail() %></td>
                <td><%= user.getScore() %></td>
                <td class="<%= user.isActive() ? "status-active" : "status-inactive" %>">
                    <%= user.isActive() ? "✅ Активен" : "⛔ Заблокирован" %>
                </td>
                <td>
                    <% if (isAdminUser) { %>
                    <span class="admin-badge"><i class="fas fa-crown"></i> Администратор</span>
                    <% } else { %>
                    <span style="color: rgba(255,255,255,0.6);">Участник</span>
                    <% } %>
                </td>
                <td><%= user.getRegistrationDate() != null ? user.getRegistrationDate() : "—" %></td>
                <td>
                    <% if (!isCurrentUser) { %>
                    <%-- Только блокировка/разблокировка для обычных пользователей --%>
                    <% if (!isAdminUser) { %>
                    <form action="${pageContext.request.contextPath}/admin/users" method="post" style="display: inline;">
                        <input type="hidden" name="action" value="toggleActive">
                        <input type="hidden" name="userId" value="<%= user.getId() %>">
                        <button type="submit" class="btn <%= user.isActive() ? "btn-warning" : "btn-success" %> btn-sm">
                            <%= user.isActive() ? "🔒 Заблокировать" : "🔓 Разблокировать" %>
                        </button>
                    </form>
                    <% } else { %>
                    <span style="color: rgba(255,255,255,0.4); font-size: 0.7rem;">
                                    <i class="fas fa-lock"></i> Недоступно
                                </span>
                    <% } %>
                    <% } else { %>
                    <span style="color: rgba(255,255,255,0.4); font-size: 0.7rem;">—</span>
                    <% } %>
                </td>
            </tr>
            <% } %>
            </tbody>
        </table>
    </div>
</div>

</body>
</html>