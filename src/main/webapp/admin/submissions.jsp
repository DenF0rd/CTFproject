<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*" %>
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

    List<Map<String, Object>> submissions = (List<Map<String, Object>>) request.getAttribute("submissions");
    if (submissions == null) submissions = new ArrayList<>();
%>
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Попытки сдачи - Админ-панель</title>
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
        .nav-links a:hover { color: #c084fc; }
        .logout-btn { background: linear-gradient(135deg, #ef4444, #dc2626); color: white; padding: 8px 18px; border-radius: 12px; text-decoration: none; }
        .container { max-width: 1400px; margin: 2rem auto; padding: 0 2rem; }
        .page-header { margin-bottom: 2rem; }
        .page-header h1 { font-size: 1.8rem; margin-bottom: 0.5rem; }
        .back-link { color: #a78bfa; text-decoration: none; transition: color 0.3s; }
        .back-link:hover { color: #c084fc; }
        .submissions-table { background: rgba(255,255,255,0.05); backdrop-filter: blur(20px); border-radius: 20px; overflow-x: auto; }
        table { width: 100%; border-collapse: collapse; }
        th, td { padding: 1rem; text-align: left; border-bottom: 1px solid rgba(255,255,255,0.1); }
        th { color: #c084fc; font-weight: 600; }
        tr:hover td { background: rgba(139,92,246,0.1); }
        .correct { color: #10b981; }
        .wrong { color: #ef4444; }
        .badge-correct { background: rgba(16,185,129,0.2); color: #10b981; padding: 2px 8px; border-radius: 12px; font-size: 0.7rem; }
        .badge-wrong { background: rgba(239,68,68,0.2); color: #ef4444; padding: 2px 8px; border-radius: 12px; font-size: 0.7rem; }
        .flag-text { font-family: monospace; font-size: 0.8rem; max-width: 250px; overflow-x: auto; white-space: nowrap; }
        .empty-state {
            text-align: center;
            padding: 3rem;
            color: rgba(255,255,255,0.4);
        }
        @media (max-width: 768px) {
            .navbar { flex-direction: column; height: auto; padding: 1rem; gap: 0.8rem; }
            .nav-links { flex-wrap: wrap; justify-content: center; }
            .container { padding: 0 1rem; }
            th, td { padding: 0.5rem; font-size: 0.7rem; }
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
        <a href="${pageContext.request.contextPath}/admin">Админ-панель</a>
    </div>
    <a href="${pageContext.request.contextPath}/logout" class="logout-btn"><i class="fas fa-sign-out-alt"></i> Выйти</a>
</nav>

<div class="container">
    <div class="page-header">
        <a href="${pageContext.request.contextPath}/admin" class="back-link"><i class="fas fa-arrow-left"></i> Назад</a>
        <h1><i class="fas fa-flag-checkered"></i> Попытки сдачи флагов</h1>
        <p style="color: rgba(255,255,255,0.6);">Последние 200 попыток</p>
    </div>

    <div class="submissions-table">
        <% if (submissions.isEmpty()) { %>
        <div class="empty-state">
            <i class="fas fa-inbox" style="font-size: 3rem; opacity: 0.3;"></i>
            <p style="margin-top: 1rem;">Пока нет попыток сдачи флагов</p>
        </div>
        <% } else { %>
        <table>
            <thead>
            <tr>
                <th>ID</th>
                <th>Пользователь</th>
                <th>Задача</th>
                <th>Флаг</th>
                <th>Результат</th>
                <th>Очки</th>
                <th>Время</th>
            </tr>
            </thead>
            <tbody>
            <% for (Map<String, Object> sub : submissions) {
                boolean isCorrect = (boolean) sub.get("is_correct");
            %>
            <tr>
                <td><%= sub.get("id") %></td>
                <td><%= sub.get("username") %></td>
                <td><%= sub.get("task_title") %></td>
                <td class="flag-text"><code><%= sub.get("submitted_flag") %></code></td>
                <td>
                    <% if (isCorrect) { %>
                    <span class="badge-correct"><i class="fas fa-check"></i> Правильно</span>
                    <% } else { %>
                    <span class="badge-wrong"><i class="fas fa-times"></i> Неправильно</span>
                    <% } %>
                </td>
                <td class="<%= isCorrect ? "correct" : "wrong" %>">
                    <%= sub.get("points_awarded") %> pts
                </td>
                <td><%= sub.get("submitted_at") %></td>
            </tr>
            <% } %>
            </tbody>
        </table>
        <% } %>
    </div>
</div>

</body>
</html>