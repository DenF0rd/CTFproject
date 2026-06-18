<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*, com.example.model.Contest, com.example.model.Team" %>
<%
    if (session.getAttribute("userId") == null) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }

    // ===== ПОЛУЧАЕМ ДАННЫЕ ИЗ ЗАПРОСА =====
    Contest contest = (Contest) request.getAttribute("contest");
    List<Map<String, Object>> tasks = (List<Map<String, Object>>) request.getAttribute("tasks");
    List<Map<String, Object>> leaderboard = (List<Map<String, Object>>) request.getAttribute("leaderboard");
    List<Map<String, Object>> teamLeaderboard = (List<Map<String, Object>>) request.getAttribute("teamLeaderboard");
    Team userTeam = (Team) request.getAttribute("userTeam");
    Boolean isJoinedObj = (Boolean) request.getAttribute("isJoined");
    boolean isJoined = isJoinedObj != null && isJoinedObj;
    int contestId = (int) request.getAttribute("contestId");
    String username = (String) session.getAttribute("username");
    Boolean isCompletedForUserObj = (Boolean) request.getAttribute("isCompletedForUser");
    boolean isCompletedForUser = isCompletedForUserObj != null && isCompletedForUserObj;
    boolean isAdmin = session.getAttribute("isAdmin") != null && (Boolean) session.getAttribute("isAdmin");
    int userContestPoints = (int) request.getAttribute("userContestPoints");

    // ===== СТАТУСЫ СОРЕВНОВАНИЯ =====
    Boolean isUpcomingObj = (Boolean) request.getAttribute("isUpcoming");
    boolean isUpcoming = isUpcomingObj != null && isUpcomingObj;

    Boolean isFinishedObj = (Boolean) request.getAttribute("isFinished");
    boolean isFinished = isFinishedObj != null && isFinishedObj;

    Boolean isActiveObj = (Boolean) request.getAttribute("isActive");
    boolean isActive = isActiveObj != null && isActiveObj;
    // ================================

    if (tasks == null) tasks = new ArrayList<>();
    if (leaderboard == null) leaderboard = new ArrayList<>();
    if (teamLeaderboard == null) teamLeaderboard = new ArrayList<>();

    String activeTab = request.getParameter("tab");
    if (activeTab == null) activeTab = "tasks";
%>
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%= contest.getTitle() %> - CTF Platform</title>
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
        }

        .nav-links a:hover {
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
            max-width: 1200px;
            margin: 2rem auto;
            padding: 0 2rem;
        }

        .contest-header {
            background: linear-gradient(135deg, rgba(139, 92, 246, 0.2), rgba(99, 102, 241, 0.1));
            backdrop-filter: blur(20px);
            border-radius: 28px;
            padding: 2rem;
            margin-bottom: 2rem;
            border: 1px solid rgba(139, 92, 246, 0.3);
        }

        .contest-title {
            font-size: 2rem;
            font-weight: 700;
            margin-bottom: 0.5rem;
        }

        .contest-dates {
            display: flex;
            gap: 2rem;
            margin: 1rem 0;
            flex-wrap: wrap;
        }

        .contest-dates span {
            color: #a78bfa;
            font-size: 0.85rem;
        }

        .contest-description {
            color: rgba(255, 255, 255, 0.8);
            line-height: 1.6;
            margin: 1rem 0;
        }

        .contest-actions {
            display: flex;
            gap: 1rem;
            margin-top: 1.5rem;
            flex-wrap: wrap;
            align-items: center;
        }

        .btn {
            padding: 10px 24px;
            border-radius: 12px;
            font-weight: 600;
            text-decoration: none;
            border: none;
            cursor: pointer;
            transition: all 0.3s;
            font-size: 0.85rem;
        }

        .btn-primary {
            background: linear-gradient(135deg, #8b5cf6, #6366f1);
            color: white;
        }

        .btn-primary:hover {
            transform: translateY(-2px);
            box-shadow: 0 0 15px rgba(139, 92, 246, 0.5);
        }

        .btn-danger {
            background: rgba(239, 68, 68, 0.3);
            border: 1px solid #ef4444;
            color: white;
        }

        .btn-danger:hover {
            background: #ef4444;
        }

        .btn-outline {
            background: rgba(255, 255, 255, 0.1);
            border: 1px solid rgba(139, 92, 246, 0.5);
            color: white;
        }

        .btn-outline:hover {
            background: rgba(139, 92, 246, 0.3);
        }

        .btn-disabled {
            background: rgba(255, 255, 255, 0.05);
            border: 1px solid rgba(255, 255, 255, 0.1);
            color: rgba(255, 255, 255, 0.4);
            cursor: not-allowed;
        }

        .finished-banner {
            background: rgba(107, 114, 128, 0.3);
            border-radius: 16px;
            padding: 1rem;
            margin-top: 1rem;
            text-align: center;
        }

        .upcoming-banner {
            background: rgba(245, 158, 11, 0.15);
            border: 1px solid rgba(245, 158, 11, 0.3);
            border-radius: 16px;
            padding: 1rem;
            margin-top: 1rem;
            text-align: center;
            color: #fcd34d;
        }

        .tabs {
            display: flex;
            gap: 0.5rem;
            margin-bottom: 1.5rem;
            border-bottom: 1px solid rgba(255, 255, 255, 0.1);
            flex-wrap: wrap;
        }

        .tab-btn {
            background: none;
            border: none;
            padding: 0.8rem 1.5rem;
            font-size: 1rem;
            font-weight: 600;
            color: rgba(255, 255, 255, 0.6);
            cursor: pointer;
            transition: all 0.3s;
            position: relative;
        }

        .tab-btn.active {
            color: #c084fc;
        }

        .tab-btn.active::after {
            content: '';
            position: absolute;
            bottom: -1px;
            left: 0;
            width: 100%;
            height: 2px;
            background: linear-gradient(135deg, #8b5cf6, #6366f1);
        }

        .tab-btn:hover {
            color: #c084fc;
        }

        .tab-content {
            display: none;
            animation: fadeIn 0.3s ease;
        }

        .tab-content.active {
            display: block;
        }

        @keyframes fadeIn {
            from { opacity: 0; transform: translateY(10px); }
            to { opacity: 1; transform: translateY(0); }
        }

        .tasks-table {
            background: rgba(255, 255, 255, 0.05);
            border-radius: 20px;
            overflow: hidden;
        }

        .task-row {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 1rem 1.5rem;
            border-bottom: 1px solid rgba(255, 255, 255, 0.05);
            transition: background 0.2s;
        }

        .task-row.clickable {
            cursor: pointer;
        }

        .task-row.not-clickable {
            cursor: default;
            opacity: 0.7;
        }

        .task-row.not-clickable:hover {
            background: transparent;
        }

        .task-row.clickable:hover {
            background: rgba(139, 92, 246, 0.1);
        }

        .task-row.solved {
            background: rgba(16, 185, 129, 0.1);
        }

        .task-info {
            flex: 1;
        }

        .task-title {
            font-weight: 600;
            margin-bottom: 0.25rem;
        }

        .task-category {
            font-size: 0.7rem;
            color: #a78bfa;
        }

        .task-stats {
            display: flex;
            gap: 1.5rem;
            align-items: center;
        }

        .task-points {
            font-weight: 700;
            color: #c084fc;
        }

        .task-solved {
            font-size: 0.7rem;
            color: #10b981;
        }

        .solved-badge {
            background: #10b981;
            padding: 2px 8px;
            border-radius: 20px;
            font-size: 0.7rem;
        }

        .locked-badge {
            background: #f59e0b;
            padding: 2px 10px;
            border-radius: 20px;
            font-size: 0.6rem;
            color: #1a1a2e;
        }

        .archive-badge {
            background: #6b7280;
            padding: 2px 8px;
            border-radius: 12px;
            font-size: 0.6rem;
            margin-left: 0.5rem;
        }

        .finished-message {
            text-align: center;
            padding: 3rem;
            background: rgba(255, 255, 255, 0.05);
            border-radius: 20px;
        }

        .leaderboard-table {
            background: rgba(255, 255, 255, 0.05);
            border-radius: 20px;
            overflow: hidden;
        }

        .leaderboard-header {
            margin-bottom: 1rem;
            padding: 1rem;
            background: rgba(139, 92, 246, 0.1);
            border-radius: 16px;
        }

        .rank-row {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 0.8rem 1rem;
            border-bottom: 1px solid rgba(255, 255, 255, 0.05);
        }

        .rank-row.current-user {
            background: rgba(139, 92, 246, 0.2);
            border-left: 3px solid #8b5cf6;
            border-radius: 8px;
        }

        .rank-number {
            font-weight: 700;
            width: 60px;
        }

        .rank-username {
            flex: 1;
        }

        .rank-score {
            font-weight: 700;
            color: #c084fc;
        }

        .team-card {
            background: rgba(255, 255, 255, 0.05);
            border-radius: 20px;
            padding: 1.5rem;
            margin-bottom: 1rem;
        }

        .completed-message {
            background: linear-gradient(135deg, rgba(139, 92, 246, 0.3), rgba(99, 102, 241, 0.2));
            border: 1px solid #8b5cf6;
            border-radius: 16px;
            padding: 1rem;
            margin-bottom: 1.5rem;
            text-align: center;
        }

        .contest-stats {
            background: rgba(139, 92, 246, 0.1);
            border-radius: 16px;
            padding: 0.8rem 1rem;
            margin-top: 1rem;
            display: inline-block;
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
            .task-row {
                flex-direction: column;
                text-align: center;
                gap: 0.5rem;
            }
            .task-stats {
                justify-content: center;
            }
            .rank-row {
                flex-wrap: wrap;
                gap: 0.5rem;
            }
            .leaderboard-header div {
                flex-direction: column;
                gap: 0.5rem;
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
        <a href="<%= request.getContextPath() %>/contests">Соревнования</a>
        <a href="<%= request.getContextPath() %>/teams">Команды</a>
        <a href="<%= request.getContextPath() %>/scoreboard">Рейтинг</a>
        <a href="<%= request.getContextPath() %>/profile?id=<%= session.getAttribute("userId") %>">Профиль</a>
        <% if (isAdmin) { %>
        <a href="<%= request.getContextPath() %>/admin">Админ-панель</a>
        <% } %>
    </div>
    <a href="<%= request.getContextPath() %>/logout" class="logout-btn"><i class="fas fa-sign-out-alt"></i> Выйти</a>
</nav>

<div class="container">
    <%
        // Сообщения из сессии
        String taskError = (String) session.getAttribute("taskError");
        String taskMessage = (String) session.getAttribute("taskMessage");
        String success = (String) session.getAttribute("success");
        String error = (String) session.getAttribute("error");

        if (taskError != null) {
            session.removeAttribute("taskError");
    %>
    <div class="message-error">
        <i class="fas fa-exclamation-circle"></i> <%= taskError %>
    </div>
    <%
        }
        if (taskMessage != null) {
            session.removeAttribute("taskMessage");
    %>
    <div class="message-success">
        <i class="fas fa-check-circle"></i> <%= taskMessage %>
    </div>
    <%
        }
        if (success != null) {
            session.removeAttribute("success");
    %>
    <div class="message-success">
        <i class="fas fa-check-circle"></i> <%= success %>
    </div>
    <%
        }
        if (error != null) {
            session.removeAttribute("error");
    %>
    <div class="message-error">
        <i class="fas fa-exclamation-circle"></i> <%= error %>
    </div>
    <%
        }
    %>

    <!-- Информация о соревновании -->
    <div class="contest-header">
        <div class="contest-title">
            <%= contest.getTitle() %>
            <% if (isCompletedForUser) { %>
            <span style="font-size: 1rem; background: #8b5cf6; padding: 4px 12px; border-radius: 20px; margin-left: 1rem;">
                <i class="fas fa-check-circle"></i> Пройдено
            </span>
            <% } %>
            <% if (isFinished) { %>
            <span style="font-size: 1rem; background: #6b7280; padding: 4px 12px; border-radius: 20px; margin-left: 1rem;">
                <i class="fas fa-archive"></i> Завершено
            </span>
            <% } %>
            <% if (isUpcoming) { %>
            <span style="font-size: 1rem; background: #f59e0b; padding: 4px 12px; border-radius: 20px; margin-left: 1rem; color: #1a1a2e;">
                <i class="fas fa-clock"></i> Скоро
            </span>
            <% } %>
        </div>
        <div class="contest-dates">
            <span><i class="far fa-calendar-alt"></i> Начало: <%= contest.getStartTime() %></span>
            <span><i class="far fa-calendar-check"></i> Окончание: <%= contest.getEndTime() %></span>
        </div>
        <div class="contest-description"><%= contest.getDescription() %></div>

        <!-- Баннер для будущих соревнований -->
        <% if (isUpcoming) { %>
        <div class="upcoming-banner">
            <i class="fas fa-clock"></i>
            Соревнование начнётся <strong><%= contest.getStartTime() %></strong>
            <span style="display: block; font-size: 0.8rem; margin-top: 0.25rem; opacity: 0.7;">
                Задачи станут доступны после старта
            </span>
        </div>
        <% } %>

        <!-- Кнопки действий - показываем только для активных соревнований -->
        <% if (isActive && !isCompletedForUser) { %>
        <div class="contest-actions">
            <% if (isJoined) { %>
            <form action="<%= request.getContextPath() %>/contest" method="post" style="display:inline;">
                <input type="hidden" name="action" value="leave">
                <input type="hidden" name="contestId" value="<%= contestId %>">
                <button type="submit" class="btn btn-danger"><i class="fas fa-sign-out-alt"></i> Покинуть соревнование</button>
            </form>
            <% } else { %>
            <form action="<%= request.getContextPath() %>/contest" method="post" style="display:inline;">
                <input type="hidden" name="action" value="join">
                <input type="hidden" name="contestId" value="<%= contestId %>">
                <button type="submit" class="btn btn-primary"><i class="fas fa-user-plus"></i> Присоединиться</button>
            </form>
            <% } %>

            <% if (isJoined && userTeam == null) { %>
            <button class="btn btn-outline" onclick="location.href='<%= request.getContextPath() %>/create-team?contestId=<%= contestId %>'">
                <i class="fas fa-users"></i> Создать команду
            </button>
            <% } %>
        </div>
        <% } else if (isFinished) { %>
        <div class="finished-banner">
            <i class="fas fa-archive"></i> Это соревнование завершено. Доступен только просмотр результатов.
        </div>
        <% } else if (isUpcoming) { %>
        <div class="contest-actions">
            <% if (isJoined) { %>
            <form action="<%= request.getContextPath() %>/contest" method="post" style="display:inline;">
                <input type="hidden" name="action" value="leave">
                <input type="hidden" name="contestId" value="<%= contestId %>">
                <button type="submit" class="btn btn-danger"><i class="fas fa-sign-out-alt"></i> Отменить участие</button>
            </form>
            <% } else { %>
            <form action="<%= request.getContextPath() %>/contest" method="post" style="display:inline;">
                <input type="hidden" name="action" value="join">
                <input type="hidden" name="contestId" value="<%= contestId %>">
                <button type="submit" class="btn btn-primary"><i class="fas fa-user-plus"></i> Зарегистрироваться</button>
            </form>
            <% } %>
        </div>
        <% } %>

        <% if (isJoined && !isCompletedForUser && userContestPoints > 0 && isActive) { %>
        <div class="contest-stats">
            <i class="fas fa-star" style="color: #f59e0b;"></i>
            Ваши очки в этом соревновании: <strong><%= userContestPoints %></strong> pts
        </div>
        <% } %>
    </div>

    <!-- Табы -->
    <div class="tabs">
        <button class="tab-btn <%= "tasks".equals(activeTab) ? "active" : "" %>" data-tab="tasks">📋 Задачи</button>
        <button class="tab-btn <%= "leaderboard".equals(activeTab) ? "active" : "" %>" data-tab="leaderboard">🏆 Рейтинг</button>
        <button class="tab-btn <%= "team-leaderboard".equals(activeTab) ? "active" : "" %>" data-tab="team-leaderboard">👥 Команды</button>
        <% if (isActive && isJoined && !isCompletedForUser) { %>
        <button class="tab-btn <%= "team".equals(activeTab) ? "active" : "" %>" data-tab="team">👥 Моя команда</button>
        <% } %>
    </div>

    <!-- Вкладка: Задачи -->
    <div id="tab-tasks" class="tab-content <%= "tasks".equals(activeTab) ? "active" : "" %>">
        <% if (tasks.isEmpty()) { %>
        <div class="finished-message">
            <i class="fas fa-inbox" style="font-size: 3rem; opacity: 0.5;"></i>
            <p style="margin-top: 1rem;">Задачи пока не добавлены</p>
        </div>
        <% } else { %>
        <div class="tasks-table">
            <% for (Map<String, Object> task : tasks) {
                boolean isSolved = (boolean) task.get("is_solved");

                // Для будущих соревнований — показываем замок
                boolean isLocked = isUpcoming;

                // Определяем, можно ли кликнуть по задаче
                boolean isClickable = isActive && !isCompletedForUser && !isFinished && !isLocked;

                String clickableClass = isClickable ? "clickable" : "not-clickable";
                String onclickAttr = isClickable ?
                        "onclick=\"location.href='" + request.getContextPath() + "/task?id=" + task.get("id") + "&contestId=" + contestId + "'\"" : "";
            %>
            <div class="task-row <%= isSolved ? "solved" : "" %> <%= clickableClass %>" <%= onclickAttr %>>
                <div class="task-info">
                    <div class="task-title">
                        <%= task.get("title") %>

                        <% if (isSolved) { %>
                        <span class="solved-badge"><i class="fas fa-check"></i> Решено</span>
                        <% } %>

                        <% if (isLocked) { %>
                        <span class="locked-badge">
                                    <i class="fas fa-lock"></i> Скоро
                                </span>
                        <% } %>

                        <% if (isFinished && !isSolved) { %>
                        <span class="archive-badge">
                                    <i class="fas fa-archive"></i> Архив
                                </span>
                        <% } %>
                    </div>
                    <div class="task-category">
                        <i class="fas fa-tag"></i>
                        <%= task.get("category") != null ? task.get("category") : "Без категории" %>
                    </div>
                </div>
                <div class="task-stats">
                    <div class="task-points">
                        <%= isLocked ? "❓" : task.get("points") %>
                        <%= isLocked ? "" : "pts" %>
                    </div>
                    <div class="task-solved">
                        <i class="fas fa-users"></i>
                        <%= isLocked ? "—" : task.get("solves_count") + " решили" %>
                    </div>
                </div>
            </div>
            <% } %>
        </div>

        <%-- Если соревнование ещё не началось, показываем сообщение --%>
        <% if (isUpcoming) { %>
        <div style="text-align: center; margin-top: 1.5rem; padding: 1rem; background: rgba(245, 158, 11, 0.1); border-radius: 16px; border: 1px solid rgba(245, 158, 11, 0.3);">
            <i class="fas fa-clock" style="color: #f59e0b;"></i>
            <span style="color: rgba(255,255,255,0.7);">
                        Соревнование начнётся <strong><%= contest.getStartTime() %></strong>
                    </span>
            <span style="display: block; color: rgba(255,255,255,0.4); font-size: 0.8rem; margin-top: 0.25rem;">
                        Содержимое задач будет доступно после старта
                    </span>
        </div>
        <% } %>
        <% } %>
    </div>

    <!-- Вкладка: Рейтинг -->
    <div id="tab-leaderboard" class="tab-content <%= "leaderboard".equals(activeTab) ? "active" : "" %>">
        <% if (leaderboard.isEmpty()) { %>
        <div class="finished-message">
            <i class="fas fa-chart-line" style="font-size: 3rem; opacity: 0.5;"></i>
            <p style="margin-top: 1rem;">Пока нет участников</p>
            <p style="font-size: 0.8rem;">Присоединитесь к соревнованию и решайте задачи!</p>
        </div>
        <% } else { %>
        <div class="leaderboard-header">
            <div style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap;">
                <div>
                    <i class="fas fa-trophy" style="color: #f59e0b;"></i>
                    <strong>Индивидуальный рейтинг</strong>
                    <span style="font-size: 0.7rem; color: #a78bfa; margin-left: 0.5rem;">(только очки в этом соревновании)</span>
                </div>
                <div>
                    <span style="background: rgba(139,92,246,0.3); padding: 4px 12px; border-radius: 20px; font-size: 0.7rem;">
                        <i class="fas fa-users"></i> Участников: <%= leaderboard.size() %>
                    </span>
                </div>
            </div>
        </div>
        <div class="leaderboard-table">
            <div style="display: grid; grid-template-columns: 60px 1fr 100px 100px; padding: 0.8rem 1rem; background: rgba(139,92,246,0.2); border-radius: 12px; margin-bottom: 0.5rem;">
                <div>Место</div>
                <div>Участник</div>
                <div>Очки</div>
                <div>Решено</div>
            </div>
            <% for (Map<String, Object> entry : leaderboard) {
                boolean isCurrentUser = entry.get("username").equals(username);
            %>
            <div class="rank-row <%= isCurrentUser ? "current-user" : "" %>" style="display: grid; grid-template-columns: 60px 1fr 100px 100px; padding: 0.8rem 1rem; border-bottom: 1px solid rgba(255,255,255,0.05);">
                <div class="rank-number">#<%= entry.get("rank") %></div>
                <div class="rank-username"><%= entry.get("username") %><%= isCurrentUser ? " <span style='color:#c084fc;'>(Вы)</span>" : "" %></div>
                <div class="rank-score"><%= entry.get("score") %> pts</div>
                <div class="rank-solved"><i class="fas fa-check-circle"></i> <%= entry.get("solved") %> задач</div>
            </div>
            <% } %>
        </div>
        <% } %>
    </div>

    <!-- Вкладка: Командный рейтинг -->
    <div id="tab-team-leaderboard" class="tab-content <%= "team-leaderboard".equals(activeTab) ? "active" : "" %>">
        <%
            if (teamLeaderboard == null) teamLeaderboard = new ArrayList<>();
            if (teamLeaderboard.isEmpty()) {
        %>
        <div class="finished-message">
            <i class="fas fa-users" style="font-size: 3rem; opacity: 0.5;"></i>
            <p style="margin-top: 1rem;">Пока нет командных результатов</p>
            <p style="font-size: 0.8rem;">Участники должны быть в командах, чтобы появиться в этом рейтинге</p>
        </div>
        <% } else { %>
        <div class="leaderboard-header">
            <div style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap;">
                <div>
                    <i class="fas fa-trophy" style="color: #f59e0b;"></i>
                    <strong>Командный рейтинг</strong>
                    <span style="font-size: 0.7rem; color: #a78bfa; margin-left: 0.5rem;">
                            (сумма очков всех участников команды)
                        </span>
                </div>
                <div>
                        <span style="background: rgba(139,92,246,0.3); padding: 4px 12px; border-radius: 20px; font-size: 0.7rem;">
                            <i class="fas fa-users"></i> Команд: <%= teamLeaderboard.size() %>
                        </span>
                </div>
            </div>
        </div>
        <div class="leaderboard-table">
            <div style="display: grid; grid-template-columns: 60px 1fr 100px 100px 100px; padding: 0.8rem 1rem; background: rgba(139,92,246,0.2); border-radius: 12px; margin-bottom: 0.5rem;">
                <div>Место</div>
                <div>Команда</div>
                <div>Очки</div>
                <div>Решено</div>
                <div>Участников</div>
            </div>
            <%
                int userTeamId = -1;
                if (userTeam != null) userTeamId = userTeam.getId();
                for (Map<String, Object> entry : teamLeaderboard) {
                    boolean isUserTeam = (int) entry.get("teamId") == userTeamId;
            %>
            <div class="rank-row <%= isUserTeam ? "current-user" : "" %>"
                 style="display: grid; grid-template-columns: 60px 1fr 100px 100px 100px; padding: 0.8rem 1rem; border-bottom: 1px solid rgba(255,255,255,0.05);">
                <div class="rank-number">#<%= entry.get("rank") %></div>
                <div class="rank-username">
                    <%= entry.get("teamName") %>
                    <%= isUserTeam ? " <span style='color:#c084fc;'>(Ваша команда)</span>" : "" %>
                </div>
                <div class="rank-score"><%= entry.get("totalPoints") %> pts</div>
                <div class="rank-solved"><i class="fas fa-check-circle"></i> <%= entry.get("totalSolved") %></div>
                <div><i class="fas fa-users"></i> <%= entry.get("membersCount") %></div>
            </div>
            <% } %>
        </div>
        <% } %>
    </div>

    <!-- Вкладка: Моя команда -->
    <% if (isActive && isJoined && !isCompletedForUser) { %>
    <div id="tab-team" class="tab-content <%= "team".equals(activeTab) ? "active" : "" %>">
        <% if (userTeam == null) { %>
        <div class="team-card" style="text-align: center;">
            <i class="fas fa-users" style="font-size: 3rem; opacity: 0.5;"></i>
            <p style="margin: 1rem 0;">Вы пока не в команде</p>
            <button class="btn btn-primary" onclick="location.href='<%= request.getContextPath() %>/create-team?contestId=<%= contestId %>'">
                <i class="fas fa-plus"></i> Создать команду
            </button>
        </div>

        <div class="team-card">
            <h3><i class="fas fa-sign-in-alt"></i> Вступить в команду</h3>
            <form action="<%= request.getContextPath() %>/join-team" method="post">
                <input type="hidden" name="contestId" value="<%= contestId %>">
                <div style="display: flex; gap: 0.5rem; margin-top: 0.5rem;">
                    <input type="text" name="teamId" placeholder="ID команды" style="flex:1; padding: 10px; border-radius: 12px; background: rgba(255,255,255,0.1); border: 1px solid rgba(139,92,246,0.3); color: white;">
                    <button type="submit" class="btn btn-primary">Вступить</button>
                </div>
            </form>
        </div>
        <% } else { %>
        <div class="team-card">
            <h3><i class="fas fa-users"></i> <%= userTeam.getName() %></h3>
            <p><%= userTeam.getDescription() != null ? userTeam.getDescription() : "" %></p>

            <div class="team-actions" style="margin-top: 1rem; display: flex; gap: 0.5rem; flex-wrap: wrap;">
                <button class="btn btn-outline" onclick="location.href='<%= request.getContextPath() %>/team-members?teamId=<%= userTeam.getId() %>'">
                    <i class="fas fa-users"></i> Участники
                </button>
                <% if (userTeam.isCaptain()) { %>
                <button class="btn btn-danger" onclick="location.href='<%= request.getContextPath() %>/manage-team?teamId=<%= userTeam.getId() %>&contestId=<%= contestId %>'">
                    <i class="fas fa-cog"></i> Управление командой
                </button>
                <% } else { %>
                <button class="btn btn-danger" onclick="if(confirm('Вы уверены, что хотите покинуть команду?')) location.href='<%= request.getContextPath() %>/leave-team?teamId=<%= userTeam.getId() %>'">
                    <i class="fas fa-sign-out-alt"></i> Покинуть команду
                </button>
                <% } %>
            </div>
        </div>
        <% } %>
    </div>
    <% } %>
</div>

<script>
    document.querySelectorAll('.circle').forEach((circle, i) => {
        circle.style.animationDuration = `${15 + i * 5}s`;
    });

    // Переключение вкладок
    document.querySelectorAll('.tab-btn').forEach(btn => {
        btn.addEventListener('click', function(e) {
            const tabId = this.getAttribute('data-tab');

            document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
            this.classList.add('active');

            document.querySelectorAll('.tab-content').forEach(content => content.classList.remove('active'));
            const targetTab = document.getElementById('tab-' + tabId);
            if (targetTab) {
                targetTab.classList.add('active');
            }

            const url = new URL(window.location.href);
            url.searchParams.set('tab', tabId);
            window.history.pushState({}, '', url);
        });
    });
</script>

</body>
</html>