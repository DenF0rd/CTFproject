<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*, com.example.model.Team" %>
<%
    if (session.getAttribute("userId") == null) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }

    Team team = (Team) request.getAttribute("team");
    List<Map<String, Object>> members = (List<Map<String, Object>>) request.getAttribute("members");
    List<Map<String, Object>> requests = (List<Map<String, Object>>) request.getAttribute("requests");
    int contestId = (int) request.getAttribute("contestId");

    if (members == null) members = new ArrayList<>();
    if (requests == null) requests = new ArrayList<>();
%>
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Управление командой - CTF Platform</title>
    <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600;700;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }

        body {
            font-family: 'Poppins', sans-serif;
            background: linear-gradient(135deg, #0f0c29 0%, #302b63 50%, #24243e 100%);
            min-height: 100vh;
            color: #ffffff;
            position: relative;
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
            max-width: 800px;
            margin: 2rem auto;
            padding: 0 2rem;
        }

        .card {
            background: rgba(255, 255, 255, 0.05);
            backdrop-filter: blur(20px);
            border-radius: 28px;
            padding: 2rem;
            border: 1px solid rgba(139, 92, 246, 0.3);
            margin-bottom: 1.5rem;
        }

        h2 {
            margin-bottom: 1rem;
        }

        h3 {
            margin: 1.5rem 0 1rem;
            font-size: 1.2rem;
        }

        .invite-code {
            background: rgba(0, 0, 0, 0.3);
            padding: 0.75rem;
            border-radius: 12px;
            font-family: monospace;
            font-size: 1.1rem;
            text-align: center;
            margin: 0.5rem 0;
        }

        .member-list, .request-list {
            list-style: none;
        }

        .member-item, .request-item {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 0.75rem 0;
            border-bottom: 1px solid rgba(255, 255, 255, 0.1);
        }

        .captain-badge {
            background: #8b5cf6;
            padding: 2px 8px;
            border-radius: 12px;
            font-size: 0.7rem;
            margin-left: 0.5rem;
        }

        .btn {
            padding: 8px 16px;
            border-radius: 12px;
            font-weight: 600;
            border: none;
            cursor: pointer;
            transition: all 0.3s;
            font-size: 0.8rem;
        }

        .btn-danger {
            background: rgba(239, 68, 68, 0.3);
            border: 1px solid #ef4444;
            color: white;
        }

        .btn-danger:hover {
            background: #ef4444;
        }

        .btn-success {
            background: #10b981;
            color: white;
        }

        .btn-warning {
            background: #f59e0b;
            color: white;
        }

        .btn-primary {
            background: linear-gradient(135deg, #8b5cf6, #6366f1);
            color: white;
        }

        .btn-sm {
            padding: 4px 12px;
            font-size: 0.7rem;
        }

        .btn-group {
            display: flex;
            gap: 0.5rem;
        }

        .btn-back {
            background: rgba(255, 255, 255, 0.1);
            color: white;
            margin-top: 1rem;
            width: 100%;
        }

        @keyframes fadeInUp {
            from { opacity: 0; transform: translateY(30px); }
            to { opacity: 1; transform: translateY(0); }
        }

        .animate-in {
            animation: fadeInUp 0.5s ease forwards;
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
            .member-item, .request-item {
                flex-direction: column;
                gap: 0.5rem;
                text-align: center;
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
        <a href="#">Рейтинг</a>
        <a href="${pageContext.request.contextPath}/profile?id=<%= session.getAttribute("userId") %>">Профиль</a>
    </div>
    <a href="${pageContext.request.contextPath}/logout" class="logout-btn"><i class="fas fa-sign-out-alt"></i> Выйти</a>
</nav>

<div class="container">
    <div class="card animate-in">
        <h2><i class="fas fa-users"></i> Управление командой: <%= team.getName() %></h2>

        <div class="invite-code">
            <i class="fas fa-link"></i> Код приглашения: <strong><%= team.getInviteCode() %></strong>
            <button class="btn btn-primary btn-sm" onclick="copyInviteCode()" style="margin-left: 0.5rem;">Копировать</button>
        </div>

        <!-- Участники -->
        <h3><i class="fas fa-user-friends"></i> Участники (<%= members.size() %>)</h3>
        <ul class="member-list">
            <% for (Map<String, Object> member : members) { %>
            <li class="member-item">
                <span>
                    <%= member.get("username") %>
                    <% if ((boolean) member.get("isCaptain")) { %>
                    <span class="captain-badge"><i class="fas fa-crown"></i> Капитан</span>
                    <% } %>
                </span>
                <% if (!(boolean) member.get("isCaptain")) { %>
                <form action="${pageContext.request.contextPath}/manage-team" method="post">
                    <input type="hidden" name="action" value="remove">
                    <input type="hidden" name="teamId" value="<%= team.getId() %>">
                    <input type="hidden" name="contestId" value="<%= contestId %>">
                    <input type="hidden" name="memberId" value="<%= member.get("id") %>">
                    <button type="submit" class="btn btn-danger btn-sm"><i class="fas fa-trash"></i> Исключить</button>
                </form>
                <% } %>
            </li>
            <% } %>
        </ul>

        <!-- Заявки -->
        <% if (!requests.isEmpty()) { %>
        <h3><i class="fas fa-envelope"></i> Заявки на вступление (<%= requests.size() %>)</h3>
        <ul class="request-list">
            <% for (Map<String, Object> requestItem : requests) { %>
            <li class="request-item">
                <div>
                    <strong><%= requestItem.get("username") %></strong>
                    <% if (requestItem.get("message") != null && !((String)requestItem.get("message")).isEmpty()) { %>
                    <div style="font-size: 0.7rem; opacity: 0.7;">Сообщение: <%= requestItem.get("message") %></div>
                    <% } %>
                </div>
                <div class="btn-group">
                    <form action="${pageContext.request.contextPath}/manage-team" method="post">
                        <input type="hidden" name="action" value="approve">
                        <input type="hidden" name="teamId" value="<%= team.getId() %>">
                        <input type="hidden" name="contestId" value="<%= contestId %>">
                        <input type="hidden" name="requestId" value="<%= requestItem.get("id") %>">
                        <button type="submit" class="btn btn-success btn-sm"><i class="fas fa-check"></i> Принять</button>
                    </form>
                    <form action="${pageContext.request.contextPath}/manage-team" method="post">
                        <input type="hidden" name="action" value="decline">
                        <input type="hidden" name="teamId" value="<%= team.getId() %>">
                        <input type="hidden" name="contestId" value="<%= contestId %>">
                        <input type="hidden" name="requestId" value="<%= requestItem.get("id") %>">
                        <button type="submit" class="btn btn-warning btn-sm"><i class="fas fa-times"></i> Отклонить</button>
                    </form>
                </div>
            </li>
            <% } %>
        </ul>
        <% } %>

        <!-- Опасная зона -->
        <hr style="margin: 1.5rem 0; border-color: rgba(255,255,255,0.1);">
        <form action="${pageContext.request.contextPath}/manage-team" method="post" onsubmit="return confirm('Вы уверены? Это действие необратимо!');">
            <input type="hidden" name="action" value="delete">
            <input type="hidden" name="teamId" value="<%= team.getId() %>">
            <input type="hidden" name="contestId" value="<%= contestId %>">
            <button type="submit" class="btn btn-danger" style="width: 100%;"><i class="fas fa-exclamation-triangle"></i> Удалить команду</button>
        </form>
    </div>

    <button class="btn btn-back" onclick="location.href='${pageContext.request.contextPath}/contest?id=<%= contestId %>&tab=team'">
        <i class="fas fa-arrow-left"></i> Назад к соревнованию
    </button>
</div>

<script>
    document.querySelectorAll('.circle').forEach((circle, i) => {
        circle.style.animationDuration = `${15 + i * 5}s`;
    });

    function copyInviteCode() {
        const code = '<%= team.getInviteCode() %>';
        navigator.clipboard.writeText(code);
        alert('Код скопирован: ' + code);
    }
</script>

</body>
</html>