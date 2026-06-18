<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*, com.example.model.User" %>
<%
    if (session.getAttribute("userId") == null) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }

    List<User> users = (List<User>) request.getAttribute("users");
    User currentUser = (User) request.getAttribute("currentUser");
    int currentRank = (int) request.getAttribute("currentRank");
    int currentUserId = (int) session.getAttribute("userId");

    if (users == null) users = new ArrayList<>();
%>
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Рейтинг - CTF Platform</title>
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

        .bg-animation {
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            z-index: 0;
            overflow: hidden;
            pointer-events: none;
        }

        .circle {
            position: absolute;
            background: rgba(99, 102, 241, 0.15);
            border-radius: 50%;
            animation: float 20s infinite ease-in-out;
        }

        @keyframes float {
            0%, 100% { transform: translateY(0) translateX(0); }
            50% { transform: translateY(-50px) translateX(50px); }
        }

        .navbar {
            position: relative;
            z-index: 10;
            background: rgba(15, 12, 41, 0.95);
            backdrop-filter: blur(20px);
            border-bottom: 1px solid rgba(139, 92, 246, 0.3);
            padding: 0 2rem;
            height: 70px;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }

        .logo {
            font-size: 1.5rem;
            font-weight: 700;
            background: linear-gradient(135deg, #fff 0%, #a78bfa 100%);
            -webkit-background-clip: text;
            background-clip: text;
            -webkit-text-fill-color: transparent;
        }

        .nav-links {
            display: flex;
            gap: 2rem;
        }

        .nav-links a {
            text-decoration: none;
            color: rgba(255, 255, 255, 0.7);
            font-weight: 500;
            transition: color 0.3s;
        }

        .nav-links a:hover, .nav-links a.active {
            color: #a78bfa;
        }

        .logout-btn {
            background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
            color: white;
            padding: 8px 18px;
            border-radius: 12px;
            text-decoration: none;
            transition: all 0.3s;
        }

        .logout-btn:hover {
            transform: translateY(-2px);
            opacity: 0.9;
        }

        .container {
            position: relative;
            z-index: 1;
            max-width: 1000px;
            margin: 2rem auto;
            padding: 0 2rem;
        }

        .page-header {
            margin-bottom: 2rem;
            text-align: center;
        }

        .page-header h1 {
            font-size: 2rem;
            margin-bottom: 0.5rem;
        }

        .page-header p {
            color: rgba(255, 255, 255, 0.6);
        }

        .user-stats {
            background: rgba(139, 92, 246, 0.2);
            backdrop-filter: blur(20px);
            border-radius: 20px;
            padding: 1.5rem;
            margin-bottom: 2rem;
            display: flex;
            justify-content: space-around;
            flex-wrap: wrap;
            gap: 1rem;
            text-align: center;
        }

        .stat-item {
            min-width: 120px;
        }

        .stat-value {
            font-size: 1.8rem;
            font-weight: 700;
            color: #c084fc;
        }

        .stat-label {
            font-size: 0.8rem;
            color: rgba(255, 255, 255, 0.6);
        }

        .scoreboard-table {
            background: rgba(255, 255, 255, 0.05);
            backdrop-filter: blur(20px);
            border-radius: 20px;
            overflow: hidden;
        }

        .table-header {
            display: grid;
            grid-template-columns: 80px 1fr 120px 120px;
            background: rgba(139, 92, 246, 0.3);
            padding: 1rem;
            font-weight: 600;
        }

        .scoreboard-row {
            display: grid;
            grid-template-columns: 80px 1fr 120px 120px;
            padding: 0.8rem 1rem;
            border-bottom: 1px solid rgba(255, 255, 255, 0.05);
            transition: background 0.2s;
        }

        .scoreboard-row:hover {
            background: rgba(139, 92, 246, 0.1);
        }

        .scoreboard-row.current-user {
            background: rgba(139, 92, 246, 0.2);
            border-left: 3px solid #8b5cf6;
        }

        .rank {
            font-weight: 700;
        }

        .rank-1 { color: #ffd700; }
        .rank-2 { color: #c0c0c0; }
        .rank-3 { color: #cd7f32; }

        .username {
            display: flex;
            align-items: center;
            gap: 0.5rem;
        }

        .current-badge {
            background: #8b5cf6;
            padding: 2px 8px;
            border-radius: 20px;
            font-size: 0.7rem;
        }

        .score, .solved {
            font-weight: 600;
        }

        .empty-state {
            text-align: center;
            padding: 3rem;
            color: rgba(255, 255, 255, 0.5);
        }

        @media (max-width: 768px) {
            .navbar {
                flex-direction: column;
                height: auto;
                padding: 1rem;
            }
            .nav-links {
                flex-wrap: wrap;
                justify-content: center;
            }
            .container {
                padding: 0 1rem;
            }
            .table-header, .scoreboard-row {
                grid-template-columns: 60px 1fr 80px 80px;
                font-size: 0.8rem;
            }
        }
    </style>
</head>
<body>

<div class="bg-animation">
    <div class="circle" style="width: 300px; height: 300px; top: -100px; left: -100px;"></div>
    <div class="circle" style="width: 200px; height: 200px; bottom: -50px; right: -50px;"></div>
    <div class="circle" style="width: 150px; height: 150px; top: 50%; left: 10%;"></div>
    <div class="circle" style="width: 250px; height: 250px; bottom: 20%; right: 10%;"></div>
    <div class="circle" style="width: 100px; height: 100px; top: 20%; right: 20%;"></div>
</div>

<nav class="navbar">
    <div class="logo">🏆 CTF Platform</div>
    <div class="nav-links">
        <a href="${pageContext.request.contextPath}/contests">Соревнования</a>
        <a href="${pageContext.request.contextPath}/teams">Команды</a>
        <a href="${pageContext.request.contextPath}/scoreboard" class="active">Рейтинг</a>
        <a href="${pageContext.request.contextPath}/profile?id=<%= session.getAttribute("userId") %>">Профиль</a>
        <% if (session.getAttribute("isAdmin") != null && (Boolean) session.getAttribute("isAdmin")) { %>
        <a href="${pageContext.request.contextPath}/admin">Админ-панель</a>
        <% } %>
    </div>
    <a href="${pageContext.request.contextPath}/logout" class="logout-btn"><i class="fas fa-sign-out-alt"></i> Выйти</a>
</nav>

<div class="container">
    <div class="page-header">
        <h1><i class="fas fa-trophy"></i> Рейтинг участников</h1>
        <p>Таблица лидеров CTF-платформы</p>
    </div>

    <!-- Статистика текущего пользователя -->
    <% if (currentUser != null) { %>
    <div class="user-stats">
        <div class="stat-item">
            <div class="stat-value">#<%= currentRank %></div>
            <div class="stat-label">Ваше место</div>
        </div>
        <div class="stat-item">
            <div class="stat-value"><%= currentUser.getScore() %></div>
            <div class="stat-label">Очки</div>
        </div>
        <div class="stat-item">
            <div class="stat-value"><%= currentUser.getSolvedCount() %></div>
            <div class="stat-label">Решено задач</div>
        </div>
    </div>
    <% } %>

    <% if (users != null && !users.isEmpty()) { %>
    <div class="scoreboard-table">
        <div class="table-header">
            <div>Место</div>
            <div>Участник</div>
            <div>Очки</div>
            <div>Решено</div>
        </div>

        <% int rank = 1;
            for (User user : users) { %>
        <div class="scoreboard-row <%= user.getId() == currentUserId ? "current-user" : "" %>"
             onclick="location.href='${pageContext.request.contextPath}/profile?id=<%= user.getId() %>'">
            <div class="rank <%= rank == 1 ? "rank-1" : (rank == 2 ? "rank-2" : (rank == 3 ? "rank-3" : "")) %>">
                #<%= rank++ %>
            </div>
            <div class="username">
                <%= user.getUsername() %>
                <% if (user.getId() == currentUserId) { %>
                <span class="current-badge">Вы</span>
                <% } %>
            </div>
            <div class="score"><%= user.getScore() %></div>
            <div class="solved"><%= user.getSolvedCount() %></div>
        </div>
        <% } %>
    </div>
    <% } else { %>
    <div class="empty-state">
        <i class="fas fa-chart-line" style="font-size: 3rem; opacity: 0.5;"></i>
        <p>Пока нет участников</p>
        <p style="font-size: 0.8rem;">Станьте первым!</p>
    </div>
    <% } %>
</div>

<script>
    document.querySelectorAll('.circle').forEach((circle, i) => {
        circle.style.animationDuration = `${15 + i * 5}s`;
    });
</script>

</body>
</html>