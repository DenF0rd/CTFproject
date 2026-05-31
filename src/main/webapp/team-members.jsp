<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*, com.example.model.Team" %>
<%
    if (session.getAttribute("userId") == null) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }

    Team team = (Team) request.getAttribute("team");
    List<Map<String, Object>> members = (List<Map<String, Object>>) request.getAttribute("members");
    boolean isCaptain = (boolean) request.getAttribute("isCaptain");
    int currentUserId = (int) request.getAttribute("currentUserId");

    if (members == null) members = new ArrayList<>();
%>
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Участники команды - <%= team.getName() %> - CTF Platform</title>
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
            color: #c084fc;
        }
        .logout-btn {
            background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
            color: white;
            padding: 8px 18px;
            border-radius: 12px;
            text-decoration: none;
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
        }
        .page-header h1 {
            font-size: 1.8rem;
            margin-bottom: 0.5rem;
        }
        .page-header p {
            color: rgba(255, 255, 255, 0.6);
        }
        .back-link {
            display: inline-block;
            margin-bottom: 1rem;
            color: #a78bfa;
            text-decoration: none;
        }
        .back-link:hover {
            color: #c084fc;
        }
        .team-info {
            background: rgba(255, 255, 255, 0.05);
            backdrop-filter: blur(20px);
            border-radius: 20px;
            padding: 1.5rem;
            margin-bottom: 2rem;
            border: 1px solid rgba(139, 92, 246, 0.3);
        }
        .team-name {
            font-size: 1.5rem;
            font-weight: 700;
            margin-bottom: 0.5rem;
        }
        .team-description {
            color: rgba(255, 255, 255, 0.7);
            margin-bottom: 1rem;
        }
        .team-stats {
            display: flex;
            gap: 1.5rem;
            font-size: 0.8rem;
            color: #a78bfa;
        }
        .members-table {
            background: rgba(255, 255, 255, 0.05);
            backdrop-filter: blur(20px);
            border-radius: 20px;
            overflow: hidden;
        }
        table {
            width: 100%;
            border-collapse: collapse;
        }
        th, td {
            padding: 1rem;
            text-align: left;
            border-bottom: 1px solid rgba(255, 255, 255, 0.1);
        }
        th {
            color: #c084fc;
            font-weight: 600;
        }
        tr:hover td {
            background: rgba(139, 92, 246, 0.1);
        }
        .captain-badge {
            background: #f59e0b;
            color: #1a1a2e;
            padding: 2px 8px;
            border-radius: 12px;
            font-size: 0.6rem;
            font-weight: 600;
            margin-left: 0.5rem;
        }
        .btn {
            padding: 6px 12px;
            border-radius: 8px;
            font-size: 0.7rem;
            font-weight: 600;
            cursor: pointer;
            border: none;
            transition: all 0.3s;
        }
        .btn-primary {
            background: linear-gradient(135deg, #8b5cf6, #6366f1);
            color: white;
        }
        .btn-primary:hover {
            transform: translateY(-2px);
        }
        .btn-warning {
            background: #f59e0b;
            color: #1a1a2e;
        }
        .btn-warning:hover {
            background: #d97706;
        }
        .success-message {
            background: rgba(16, 185, 129, 0.2);
            border-left: 3px solid #10b981;
            padding: 0.75rem;
            border-radius: 12px;
            margin-bottom: 1rem;
            color: #6ee7b7;
        }
        .error-message {
            background: rgba(239, 68, 68, 0.2);
            border-left: 3px solid #ef4444;
            padding: 0.75rem;
            border-radius: 12px;
            margin-bottom: 1rem;
            color: #fca5a5;
        }
        .modal {
            display: none;
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: rgba(0, 0, 0, 0.8);
            z-index: 1000;
            align-items: center;
            justify-content: center;
        }
        .modal-content {
            background: rgba(20, 15, 45, 0.95);
            backdrop-filter: blur(20px);
            border-radius: 20px;
            padding: 2rem;
            max-width: 400px;
            width: 90%;
            border: 1px solid rgba(139, 92, 246, 0.3);
        }
        .modal-content h3 { margin-bottom: 1rem; }
        .modal-buttons { display: flex; gap: 1rem; justify-content: flex-end; margin-top: 1.5rem; }
        @media (max-width: 768px) {
            .navbar { flex-direction: column; height: auto; padding: 1rem; }
            .nav-links { flex-wrap: wrap; justify-content: center; }
            th, td { padding: 0.5rem; font-size: 0.8rem; }
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
        <a href="${pageContext.request.contextPath}/scoreboard">Рейтинг</a>
        <a href="${pageContext.request.contextPath}/profile?id=<%= session.getAttribute("userId") %>">Профиль</a>
        <% if (session.getAttribute("isAdmin") != null && (Boolean) session.getAttribute("isAdmin")) { %>
        <a href="${pageContext.request.contextPath}/admin">👑 Админ-панель</a>
        <% } %>
    </div>
    <a href="${pageContext.request.contextPath}/logout" class="logout-btn"><i class="fas fa-sign-out-alt"></i> Выйти</a>
</nav>

<div class="container">
    <a href="${pageContext.request.contextPath}/teams" class="back-link"><i class="fas fa-arrow-left"></i> Назад к командам</a>

    <div class="page-header">
        <h1><i class="fas fa-users"></i> Участники команды</h1>
        <p>Просмотр участников и управление командой</p>
    </div>

    <% if (session.getAttribute("success") != null) { %>
    <div class="success-message">✅ <%= session.getAttribute("success") %></div>
    <% session.removeAttribute("success"); %>
    <% } %>

    <% if (session.getAttribute("error") != null) { %>
    <div class="error-message">⚠️ <%= session.getAttribute("error") %></div>
    <% session.removeAttribute("error"); %>
    <% } %>

    <div class="team-info">
        <div class="team-name"><%= team.getName() %></div>
        <div class="team-description"><%= team.getDescription() != null ? team.getDescription() : "Нет описания" %></div>
        <div class="team-stats">
            <span><i class="fas fa-users"></i> Участников: <%= members.size() %> / 5</span>
            <span><i class="fas fa-trophy"></i> Очков: <%= team.getTotalPoints() %></span>
            <span><i class="fas fa-calendar"></i> Создана: <%= team.getCreatedAt() %></span>
        </div>
    </div>

    <div class="members-table">
        <table>
            <thead>
            <tr>
                <th>Участник</th>
                <th>Email</th>
                <th>Очки</th>
                <th>Роль</th>
                <th>Дата вступления</th>
                <% if (isCaptain) { %>
                <th>Действия</th>
                <% } %>
            </tr>
            </thead>
            <tbody>
            <% for (Map<String, Object> member : members) {
                boolean isCurrentUser = (int) member.get("id") == currentUserId;
            %>
            <tr>
                <td>
                    <%= member.get("username") %>
                    <% if ((boolean) member.get("isCaptain")) { %>
                    <span class="captain-badge"><i class="fas fa-crown"></i> Капитан</span>
                    <% } %>
                    <% if (isCurrentUser) { %>
                    <span style="color: #c084fc; font-size: 0.7rem;"> (Вы)</span>
                    <% } %>
                </td>
                <td><%= member.get("email") %></td>
                <td><%= member.get("rating") %></td>
                <td><%= member.get("role") != null ? member.get("role") : "member" %></td>
                <td><%= member.get("joinedAt") %></td>
                <% if (isCaptain && !(boolean) member.get("isCaptain") && !isCurrentUser) { %>
                <td>
                    <button class="btn btn-warning" onclick="openTransferModal(<%= team.getId() %>, <%= member.get("id") %>, '<%= member.get("username") %>')">
                        <i class="fas fa-exchange-alt"></i> Передать лидерство
                    </button>
                </td>
                <% } else if (isCaptain && !(boolean) member.get("isCaptain") && isCurrentUser) { %>
                <td><span style="color: rgba(255,255,255,0.4);">—</span></td>
                <% } else if (isCaptain && (boolean) member.get("isCaptain")) { %>
                <td><span style="color: rgba(255,255,255,0.4);">Вы капитан</span></td>
                <% } %>
            </tr>
            <% } %>
            </tbody>
        </table>
    </div>
</div>

<!-- Модальное окно передачи лидерства -->
<div id="transferModal" class="modal">
    <div class="modal-content">
        <h3><i class="fas fa-exchange-alt"></i> Передать лидерство</h3>
        <p>Вы уверены, что хотите передать лидерство участнику <strong id="transferUserName"></strong>?</p>
        <p style="font-size: 0.8rem; color: #f59e0b; margin-top: 0.5rem;">
            <i class="fas fa-info-circle"></i> После передачи вы станете обычным участником.
        </p>
        <form action="${pageContext.request.contextPath}/transfer-leadership" method="post">
            <input type="hidden" name="teamId" id="transferTeamId">
            <input type="hidden" name="newCaptainId" id="transferNewCaptainId">
            <div class="modal-buttons">
                <button type="submit" class="btn btn-primary">Да, передать</button>
                <button type="button" class="btn btn-secondary" onclick="closeTransferModal()">Отмена</button>
            </div>
        </form>
    </div>
</div>

<script>
    document.querySelectorAll('.circle').forEach((circle, i) => {
        circle.style.animationDuration = `${15 + i * 5}s`;
    });

    function openTransferModal(teamId, userId, username) {
        document.getElementById('transferTeamId').value = teamId;
        document.getElementById('transferNewCaptainId').value = userId;
        document.getElementById('transferUserName').innerText = username;
        document.getElementById('transferModal').style.display = 'flex';
    }

    function closeTransferModal() {
        document.getElementById('transferModal').style.display = 'none';
    }

    window.onclick = function(event) {
        if (event.target.classList.contains('modal')) {
            event.target.style.display = 'none';
        }
    }
</script>

</body>
</html>