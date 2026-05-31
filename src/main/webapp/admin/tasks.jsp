<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*, com.example.model.Task, com.example.model.Contest" %>
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

    List<Task> tasks = (List<Task>) request.getAttribute("tasks");
    List<Contest> contests = (List<Contest>) request.getAttribute("contests");
    Integer filterContestId = (Integer) request.getAttribute("filterContestId");

    if (tasks == null) tasks = new ArrayList<>();
    if (contests == null) contests = new ArrayList<>();
%>
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Управление задачами - Админ-панель</title>
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
        .container { max-width: 1400px; margin: 2rem auto; padding: 0 2rem; }
        .page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 2rem; flex-wrap: wrap; gap: 1rem; }
        .page-header h1 { font-size: 1.8rem; }
        .back-link { color: #a78bfa; text-decoration: none; transition: color 0.3s; }
        .back-link:hover { color: #c084fc; }
        .btn-primary { background: linear-gradient(135deg, #8b5cf6, #6366f1); color: white; padding: 10px 20px; border-radius: 12px; border: none; cursor: pointer; font-weight: 600; transition: all 0.3s; }
        .btn-primary:hover { transform: translateY(-2px); box-shadow: 0 0 15px rgba(139,92,246,0.5); }
        .tasks-table { background: rgba(255,255,255,0.05); backdrop-filter: blur(20px); border-radius: 20px; overflow-x: auto; margin-top: 2rem; }
        table { width: 100%; border-collapse: collapse; }
        th, td { padding: 1rem; text-align: left; border-bottom: 1px solid rgba(255,255,255,0.1); }
        th { color: #c084fc; font-weight: 600; }
        tr:hover td { background: rgba(139,92,246,0.1); }
        .btn { padding: 6px 12px; border-radius: 8px; font-size: 0.75rem; font-weight: 600; cursor: pointer; border: none; transition: all 0.3s; text-decoration: none; display: inline-block; margin: 0 2px; }
        .btn-warning { background: #f59e0b; color: #1a1a2e; }
        .btn-warning:hover { background: #d97706; }
        .btn-danger { background: #ef4444; color: white; }
        .btn-danger:hover { background: #dc2626; }
        .btn-sm { padding: 4px 10px; font-size: 0.7rem; }
        .filter-bar { background: rgba(255,255,255,0.05); border-radius: 16px; padding: 1rem; margin-bottom: 1rem; display: flex; gap: 1rem; align-items: center; flex-wrap: wrap; }
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
        <a href="${pageContext.request.contextPath}/scoreboard">Рейтинг</a>
        <a href="${pageContext.request.contextPath}/profile?id=<%= session.getAttribute("userId") %>">Профиль</a>
        <a href="${pageContext.request.contextPath}/admin" style="color: #c084fc;">Админ-панель</a>
    </div>
    <a href="${pageContext.request.contextPath}/logout" class="logout-btn"><i class="fas fa-sign-out-alt"></i> Выйти</a>
</nav>

<div class="container">
    <div class="page-header">
        <div>
            <a href="${pageContext.request.contextPath}/admin" class="back-link"><i class="fas fa-arrow-left"></i> Назад</a>
            <h1 style="margin-top: 0.5rem;"><i class="fas fa-tasks"></i> Управление задачами</h1>
        </div>
        <a href="${pageContext.request.contextPath}/admin/edit-task" class="btn-primary"><i class="fas fa-plus"></i> Создать задачу</a>
    </div>

    <div class="filter-bar">
        <span>Фильтр по соревнованию:</span>
        <form method="get" style="display: inline;">
            <select name="contestId" onchange="this.form.submit()" style="padding: 6px 12px; border-radius: 8px; background: rgba(0,0,0,0.3); color: white; border: 1px solid rgba(139,92,246,0.3);">
                <option value="">Все соревнования</option>
                <% for (Contest c : contests) { %>
                <option value="<%= c.getId() %>" <%= (filterContestId != null && filterContestId == c.getId()) ? "selected" : "" %>><%= c.getTitle() %></option>
                <% } %>
            </select>
        </form>
    </div>

    <div class="tasks-table">
        <table>
            <thead>
            <tr>
                <th>ID</th>
                <th>Соревнование</th>
                <th>Название</th>
                <th>Очки</th>
                <th>Флаг</th>
                <th>Статус</th>
                <th>Действия</th>
            </tr>
            </thead>
            <tbody>
            <% for (Task task : tasks) { %>
            <tr>
                <td><%= task.getId() %></td>
                <td><%= task.getContestTitle() != null ? task.getContestTitle() : "—" %></td>
                <td><%= task.getTitle() %></td>
                <td><%= task.getPoints() %></td>
                <td><code style="background: rgba(0,0,0,0.3); padding: 2px 6px; border-radius: 4px;"><%= task.getFlag() %></code></td>
                <td class="<%= task.isActive() ? "status-active" : "status-inactive" %>">
                    <%= task.isActive() ? "✅ Активна" : "⛔ Неактивна" %>
                </td>
                <td>
                    <a href="${pageContext.request.contextPath}/admin/edit-task?id=<%= task.getId() %>" class="btn btn-warning btn-sm"><i class="fas fa-edit"></i> Редактировать</a>
                    <form action="${pageContext.request.contextPath}/admin/tasks" method="post" style="display: inline;" onsubmit="return confirm('Удалить задачу?');">
                        <input type="hidden" name="action" value="delete">
                        <input type="hidden" name="taskId" value="<%= task.getId() %>">
                        <button type="submit" class="btn btn-danger btn-sm"><i class="fas fa-trash"></i> Удалить</button>
                    </form>
                </td>
            </tr>
            <% } %>
            </tbody>
        </table>
    </div>
</div>
</body>
</html>