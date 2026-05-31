<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*, com.example.model.Contest" %>
<%
    if (session.getAttribute("userId") == null) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }

    String username = (String) session.getAttribute("username");
    List<Contest> activeContests = (List<Contest>) request.getAttribute("activeContests");
    List<Contest> upcomingContests = (List<Contest>) request.getAttribute("upcomingContests");
    List<Contest> pastContests = (List<Contest>) request.getAttribute("pastContests");

    if (activeContests == null) activeContests = new ArrayList<>();
    if (upcomingContests == null) upcomingContests = new ArrayList<>();
    if (pastContests == null) pastContests = new ArrayList<>();
%>
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>CTF Platform - Соревнования</title>
    <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600;700;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: 'Poppins', sans-serif;
            background: linear-gradient(135deg, #0f0c29 0%, #302b63 50%, #24243e 100%);
            min-height: 100vh;
            color: #ffffff;
            position: relative;
            overflow-x: hidden;
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
            position: relative;
        }

        .nav-links a::after {
            content: '';
            position: absolute;
            bottom: -8px;
            left: 0;
            width: 0;
            height: 2px;
            background: linear-gradient(135deg, #8b5cf6 0%, #6366f1 100%);
            transition: width 0.3s;
        }

        .nav-links a:hover::after,
        .nav-links a.active::after {
            width: 100%;
        }

        .nav-links a:hover,
        .nav-links a.active {
            color: #ffffff;
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
            max-width: 1400px;
            margin: 2rem auto;
            padding: 0 2rem;
        }

        .welcome-card {
            background: linear-gradient(135deg, rgba(139, 92, 246, 0.2), rgba(99, 102, 241, 0.1));
            backdrop-filter: blur(20px);
            border-radius: 28px;
            padding: 1.5rem 2rem;
            margin-bottom: 2.5rem;
            border: 1px solid rgba(139, 92, 246, 0.3);
            animation: fadeInUp 0.5s ease forwards;
        }

        .welcome-text h2 {
            font-size: 1.5rem;
            margin-bottom: 0.25rem;
        }

        .welcome-text h2 span {
            background: linear-gradient(135deg, #c084fc, #60a5fa);
            -webkit-background-clip: text;
            background-clip: text;
            -webkit-text-fill-color: transparent;
        }

        .welcome-text p {
            color: rgba(255, 255, 255, 0.6);
            font-size: 0.85rem;
        }

        .section {
            margin-bottom: 2.5rem;
            animation: fadeInUp 0.5s ease forwards;
        }

        .section-title {
            font-size: 1.3rem;
            margin-bottom: 1.5rem;
            border-left: 4px solid #8b5cf6;
            padding-left: 1rem;
        }

        .contests-grid {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
            gap: 1.5rem;
        }

        .contest-card {
            background: rgba(255, 255, 255, 0.05);
            backdrop-filter: blur(20px);
            border-radius: 24px;
            padding: 1.5rem;
            transition: all 0.3s ease;
            border: 1px solid rgba(139, 92, 246, 0.2);
            cursor: pointer;
        }

        .contest-card:hover {
            transform: translateY(-5px);
            background: rgba(255, 255, 255, 0.08);
            border-color: rgba(139, 92, 246, 0.5);
        }

        .contest-title {
            font-size: 1.2rem;
            font-weight: 700;
            margin-bottom: 0.5rem;
            display: flex;
            justify-content: space-between;
            align-items: center;
            flex-wrap: wrap;
            gap: 0.5rem;
        }

        .contest-description {
            font-size: 0.8rem;
            color: rgba(255, 255, 255, 0.7);
            margin-bottom: 1rem;
            line-height: 1.4;
        }

        .contest-dates {
            font-size: 0.7rem;
            color: #a78bfa;
            margin-bottom: 1rem;
        }

        .contest-meta {
            display: flex;
            justify-content: space-between;
            align-items: center;
            flex-wrap: wrap;
            gap: 0.5rem;
            padding-top: 1rem;
            border-top: 1px solid rgba(255, 255, 255, 0.1);
        }

        .status-badge {
            padding: 4px 12px;
            border-radius: 20px;
            font-size: 0.7rem;
            font-weight: 600;
        }

        .status-active {
            background: #10b981;
            color: white;
        }

        .status-upcoming {
            background: #f59e0b;
            color: #1a1a2e;
        }

        .status-ended {
            background: #6b7280;
            color: white;
        }

        .btn-join {
            background: linear-gradient(135deg, #8b5cf6, #6366f1);
            border: none;
            padding: 6px 16px;
            border-radius: 20px;
            color: white;
            font-weight: 600;
            font-size: 0.7rem;
            cursor: pointer;
            transition: all 0.2s;
        }

        .btn-join:hover {
            transform: scale(1.02);
            box-shadow: 0 0 12px rgba(139, 92, 246, 0.5);
        }

        .btn-leave {
            background: rgba(239, 68, 68, 0.3);
            border: 1px solid #ef4444;
        }

        .btn-leave:hover {
            background: #ef4444;
        }

        .btn-view {
            background: rgba(255, 255, 255, 0.1);
            border: 1px solid rgba(139, 92, 246, 0.3);
        }

        .btn-view:hover {
            background: rgba(139, 92, 246, 0.3);
        }

        .completed-badge {
            background: #8b5cf6;
            padding: 2px 8px;
            border-radius: 12px;
            font-size: 0.6rem;
        }

        .user-points {
            font-size: 0.7rem;
            color: #c084fc;
            margin-top: 0.3rem;
        }

        .empty-state {
            text-align: center;
            padding: 3rem;
            background: rgba(255, 255, 255, 0.05);
            border-radius: 24px;
            border: 1px solid rgba(139, 92, 246, 0.2);
        }

        @keyframes fadeInUp {
            from {
                opacity: 0;
                transform: translateY(30px);
            }
            to {
                opacity: 1;
                transform: translateY(0);
            }
        }

        @media (max-width: 768px) {
            .navbar {
                flex-direction: column;
                height: auto;
                padding: 1rem;
                gap: 0.8rem;
            }
            .nav-links {
                flex-wrap: wrap;
                justify-content: center;
                gap: 1rem;
            }
            .container {
                padding: 0 1rem;
            }
            .welcome-card {
                text-align: center;
            }
            .contests-grid {
                grid-template-columns: 1fr;
            }
            .contest-meta {
                flex-direction: column;
                align-items: flex-start;
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
        <a href="${pageContext.request.contextPath}/contests" class="active">Соревнования</a>
        <a href="${pageContext.request.contextPath}/teams">Команды</a>
        <a href="${pageContext.request.contextPath}/scoreboard">Рейтинг</a>
        <a href="${pageContext.request.contextPath}/profile?id=<%= session.getAttribute("userId") %>">Профиль</a>
        <% if (session.getAttribute("isAdmin") != null && (Boolean) session.getAttribute("isAdmin")) { %>
        <a href="${pageContext.request.contextPath}/admin">Админ-панель</a>
        <% } %>
    </div>
    <a href="${pageContext.request.contextPath}/logout" class="logout-btn"><i class="fas fa-sign-out-alt"></i> Выйти</a>
</nav>

<div class="container">
    <div class="welcome-card">
        <div class="welcome-text">
            <h2>Привет, <span><%= username %></span>! 👋</h2>
            <p>Готов проверить свои навыки в кибербезопасности?</p>
        </div>
    </div>

    <!-- АКТИВНЫЕ СОРЕВНОВАНИЯ -->
    <% if (!activeContests.isEmpty()) { %>
    <div class="section">
        <div class="section-title">🔥 Активные соревнования</div>
        <div class="contests-grid">
            <% for (Contest contest : activeContests) { %>
            <div class="contest-card" onclick="location.href='${pageContext.request.contextPath}/contest?id=<%= contest.getId() %>'">
                <div class="contest-title">
                    <%= contest.getTitle() %>
                    <span class="status-badge status-active">● Активно</span>
                </div>
                <div class="contest-description"><%= contest.getDescription() %></div>
                <div class="contest-dates"><i class="far fa-calendar-alt"></i> До <%= contest.getEndTime() %></div>
                <div class="contest-meta">
                    <span><i class="fas fa-tasks"></i> <%= contest.getTasksCount() %> задач</span>
                    <span><i class="fas fa-users"></i> <%= contest.getParticipantsCount() %> уч.</span>
                    <% if (contest.isUserJoined()) { %>
                    <span><i class="fas fa-star" style="color: #f59e0b;"></i> Ваши очки: <strong><%= contest.getUserPoints() %></strong></span>
                    <% } %>
                </div>
                <% if (contest.isUserJoined()) { %>
                <div class="user-points">
                    <i class="fas fa-chart-line"></i> Прогресс: <%= contest.getUserSolvedCount() %> / <%= contest.getTasksCount() %>
                </div>
                <% } %>
                <div class="contest-actions" style="margin-top: 0.75rem;">
                    <% if (contest.isUserJoined()) { %>
                    <form action="${pageContext.request.contextPath}/contests" method="post" onclick="event.stopPropagation()">
                        <input type="hidden" name="action" value="leave">
                        <input type="hidden" name="contestId" value="<%= contest.getId() %>">
                        <button type="submit" class="btn-join btn-leave">Покинуть</button>
                    </form>
                    <% } else { %>
                    <form action="${pageContext.request.contextPath}/contests" method="post" onclick="event.stopPropagation()">
                        <input type="hidden" name="action" value="join">
                        <input type="hidden" name="contestId" value="<%= contest.getId() %>">
                        <button type="submit" class="btn-join">Присоединиться</button>
                    </form>
                    <% } %>
                </div>
            </div>
            <% } %>
        </div>
    </div>
    <% } %>

    <!-- БУДУЩИЕ СОРЕВНОВАНИЯ -->
    <% if (!upcomingContests.isEmpty()) { %>
    <div class="section">
        <div class="section-title">⏰ Скоро стартуют</div>
        <div class="contests-grid">
            <% for (Contest contest : upcomingContests) { %>
            <div class="contest-card" onclick="location.href='${pageContext.request.contextPath}/contest?id=<%= contest.getId() %>'">
                <div class="contest-title">
                    <%= contest.getTitle() %>
                    <span class="status-badge status-upcoming">● Скоро</span>
                </div>
                <div class="contest-description"><%= contest.getDescription() %></div>
                <div class="contest-dates"><i class="far fa-calendar-alt"></i> Старт: <%= contest.getStartTime() %></div>
                <div class="contest-meta">
                    <span><i class="fas fa-tasks"></i> <%= contest.getTasksCount() %> задач</span>
                    <% if (contest.isUserJoined()) { %>
                    <span style="color:#10b981;"><i class="fas fa-check-circle"></i> Вы зарегистрированы</span>
                    <form action="${pageContext.request.contextPath}/contests" method="post" onclick="event.stopPropagation()" style="display: inline;">
                        <input type="hidden" name="action" value="leave">
                        <input type="hidden" name="contestId" value="<%= contest.getId() %>">
                        <button type="submit" class="btn-join btn-leave">Отменить</button>
                    </form>
                    <% } else { %>
                    <form action="${pageContext.request.contextPath}/contests" method="post" onclick="event.stopPropagation()">
                        <input type="hidden" name="action" value="join">
                        <input type="hidden" name="contestId" value="<%= contest.getId() %>">
                        <button type="submit" class="btn-join">Предрегистрация</button>
                    </form>
                    <% } %>
                </div>
            </div>
            <% } %>
        </div>
    </div>
    <% } %>

    <!-- АРХИВ ЗАВЕРШЁННЫХ СОРЕВНОВАНИЙ -->
        <% if (!pastContests.isEmpty()) { %>
    <div class="section">
        <div class="section-title">📦 Архив соревнований</div>
        <div class="contests-grid">
            <% for (Contest contest : pastContests) { %>
            <div class="contest-card" style="opacity: 0.8;" onclick="location.href='${pageContext.request.contextPath}/contest?id=<%= contest.getId() %>&view=results'">
                <div class="contest-title">
                    <%= contest.getTitle() %>
                    <span class="completed-badge"><i class="fas fa-check-circle"></i> Завершено</span>
                </div>
                <div class="contest-description"><%= contest.getDescription() %></div>
                <div class="contest-dates"><i class="far fa-calendar-alt"></i> Завершено: <%= contest.getEndTime() %></div>
                <div class="contest-meta">
                    <span><i class="fas fa-tasks"></i> <%= contest.getTasksCount() %> задач</span>
                    <span><i class="fas fa-users"></i> <%= contest.getParticipantsCount() %> уч.</span>
                    <button class="btn-join btn-view" onclick="event.stopPropagation(); location.href='${pageContext.request.contextPath}/contest?id=<%= contest.getId() %>&view=results'">
                        <i class="fas fa-eye"></i> Результаты
                    </button>
                </div>
            </div>
            <% } %>
        </div>
    </div>
        <% } %>

<script>
    document.querySelectorAll('.circle').forEach((circle, i) => {
        circle.style.animationDuration = `${15 + i * 5}s`;
    });
</script>

</body>

<!--
Подсказка: Администратор оставил секретную страницу.
Попробуйте найти её. Может быть, поможет robots.txt?
-->
</html>