<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.example.model.User" %>
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
%>
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Админ-панель - CTF Platform</title>
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
        .nav-links a:hover { color: #a78bfa; }
        .logout-btn { background: linear-gradient(135deg, #ef4444, #dc2626); color: white; padding: 8px 18px; border-radius: 12px; text-decoration: none; }
        .container { max-width: 1200px; margin: 2rem auto; padding: 0 2rem; }
        .admin-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 2rem; flex-wrap: wrap; gap: 1rem; }
        .admin-header h1 { font-size: 1.8rem; }
        .admin-badge { background: #ef4444; padding: 4px 12px; border-radius: 20px; font-size: 0.8rem; }
        .stats-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(250px, 1fr)); gap: 1.5rem; margin-bottom: 2rem; }
        .stat-card { background: rgba(255,255,255,0.05); backdrop-filter: blur(20px); border-radius: 20px; padding: 1.5rem; text-align: center; transition: transform 0.3s; }
        .stat-card:hover { transform: translateY(-5px); background: rgba(255,255,255,0.08); }
        .stat-value { font-size: 2.5rem; font-weight: 700; color: #c084fc; }
        .stat-label { color: rgba(255,255,255,0.6); margin-top: 0.5rem; }
        .admin-links { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 1rem; margin-top: 1rem; }
        .admin-card { background: rgba(255,255,255,0.05); backdrop-filter: blur(20px); border-radius: 20px; padding: 1.5rem; text-align: center; text-decoration: none; color: white; transition: all 0.3s; }
        .admin-card:hover { transform: translateY(-5px); background: rgba(139,92,246,0.2); }
        .admin-card i { font-size: 2rem; margin-bottom: 0.5rem; color: #c084fc; }
        .admin-card h3 { font-size: 1.2rem; margin-bottom: 0.5rem; }
        .admin-card p { font-size: 0.8rem; color: rgba(255,255,255,0.6); }
        @media (max-width: 768px) {
            .navbar { flex-direction: column; height: auto; padding: 1rem; gap: 0.8rem; }
            .nav-links { flex-wrap: wrap; justify-content: center; }
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
        <a href="${pageContext.request.contextPath}/admin" style="color: #c084fc;">Админ-панель</a>
    </div>
    <a href="${pageContext.request.contextPath}/logout" class="logout-btn"><i class="fas fa-sign-out-alt"></i> Выйти</a>
</nav>

<div class="container">
    <div class="admin-header">
        <h1><i class="fas fa-crown" style="color: #f59e0b;"></i> Админ-панель</h1>
        <span class="admin-badge"><i class="fas fa-shield-alt"></i> Администратор</span>
    </div>

    <div class="stats-grid">
        <div class="stat-card">
            <div class="stat-value"><%= request.getAttribute("totalUsers") != null ? request.getAttribute("totalUsers") : 0 %></div>
            <div class="stat-label"><i class="fas fa-users"></i> Пользователей</div>
        </div>
        <div class="stat-card">
            <div class="stat-value"><%= request.getAttribute("totalContests") != null ? request.getAttribute("totalContests") : 0 %></div>
            <div class="stat-label"><i class="fas fa-trophy"></i> Соревнований</div>
        </div>
        <div class="stat-card">
            <div class="stat-value"><%= request.getAttribute("totalSubmissions") != null ? request.getAttribute("totalSubmissions") : 0 %></div>
            <div class="stat-label"><i class="fas fa-flag-checkered"></i> Попыток сдачи</div>
        </div>
    </div>

    <div class="admin-links">
        <a href="${pageContext.request.contextPath}/admin/users" class="admin-card">
            <i class="fas fa-users"></i>
            <h3>Пользователи</h3>
            <p>Управление пользователями, блокировка</p>
        </a>
        <a href="${pageContext.request.contextPath}/admin/contests" class="admin-card">
            <i class="fas fa-trophy"></i>
            <h3>Соревнования</h3>
            <p>Создание, редактирование, удаление соревнований</p>
        </a>
        <a href="${pageContext.request.contextPath}/admin/submissions" class="admin-card">
            <i class="fas fa-flag-checkered"></i>
            <h3>Сабмишены</h3>
            <p>Просмотр всех попыток сдачи флагов</p>
        </a>
    </div>
</div>

</body>
</html>