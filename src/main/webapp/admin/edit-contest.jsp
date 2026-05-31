<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.example.model.Contest" %>
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

    Contest contest = (Contest) request.getAttribute("contest");
    boolean isEdit = contest != null;

    // Форматирование даты для input datetime-local
    String startTimeValue = "";
    String endTimeValue = "";
    if (isEdit && contest.getStartTime() != null) {
        startTimeValue = contest.getStartTime().toString().replace(" ", "T");
        if (startTimeValue.length() > 16) startTimeValue = startTimeValue.substring(0, 16);
    }
    if (isEdit && contest.getEndTime() != null) {
        endTimeValue = contest.getEndTime().toString().replace(" ", "T");
        if (endTimeValue.length() > 16) endTimeValue = endTimeValue.substring(0, 16);
    }
%>
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%= isEdit ? "Редактировать соревнование" : "Создать соревнование" %> - Админ-панель</title>
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
        .container { max-width: 800px; margin: 2rem auto; padding: 0 2rem; }
        .card { background: rgba(255,255,255,0.05); backdrop-filter: blur(20px); border-radius: 28px; padding: 2rem; border: 1px solid rgba(139,92,246,0.3); }
        h1 { margin-bottom: 1.5rem; display: flex; align-items: center; gap: 0.5rem; }
        .form-group { margin-bottom: 1.25rem; }
        label { display: block; margin-bottom: 0.5rem; color: rgba(255,255,255,0.8); font-weight: 500; }
        input, textarea {
            width: 100%;
            padding: 12px 16px;
            background: rgba(255,255,255,0.08);
            border: 1px solid rgba(139,92,246,0.3);
            border-radius: 16px;
            color: white;
            font-size: 1rem;
            font-family: inherit;
        }
        input:focus, textarea:focus { outline: none; border-color: #8b5cf6; }
        .btn { padding: 12px 24px; border-radius: 16px; font-weight: 600; border: none; cursor: pointer; transition: all 0.3s; margin-right: 1rem; }
        .btn-primary { background: linear-gradient(135deg, #8b5cf6, #6366f1); color: white; }
        .btn-primary:hover { transform: translateY(-2px); box-shadow: 0 0 15px rgba(139,92,246,0.5); }
        .btn-secondary { background: rgba(255,255,255,0.1); color: white; }
        .btn-secondary:hover { background: rgba(139,92,246,0.3); }
        .error { background: rgba(239,68,68,0.2); border-left: 3px solid #ef4444; padding: 0.75rem; border-radius: 12px; margin-bottom: 1rem; color: #fca5a5; }
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
        <a href="${pageContext.request.contextPath}/scoreboard">Рейтинг</a>
        <a href="${pageContext.request.contextPath}/profile?id=<%= session.getAttribute("userId") %>">Профиль</a>
        <a href="${pageContext.request.contextPath}/admin" style="color: #c084fc;">Админ-панель</a>
    </div>
    <a href="${pageContext.request.contextPath}/logout" class="logout-btn"><i class="fas fa-sign-out-alt"></i> Выйти</a>
</nav>

<div class="container">
    <div class="card">
        <h1><%= isEdit ? "✏️ Редактирование соревнования" : "➕ Создание соревнования" %></h1>

        <% if (request.getAttribute("error") != null) { %>
        <div class="error"><%= request.getAttribute("error") %></div>
        <% } %>

        <form action="${pageContext.request.contextPath}/admin/edit-contest" method="post">
            <input type="hidden" name="action" value="<%= isEdit ? "update" : "create" %>">
            <% if (isEdit) { %>
            <input type="hidden" name="contestId" value="<%= contest.getId() %>">
            <% } %>

            <div class="form-group">
                <label>Название соревнования</label>
                <input type="text" name="title" required value="<%= isEdit ? contest.getTitle() : "" %>">
            </div>

            <div class="form-group">
                <label>Описание</label>
                <textarea name="description" rows="4"><%= isEdit ? contest.getDescription() : "" %></textarea>
            </div>

            <div class="form-group">
                <label>🏆 Награда за победу</label>
                <input type="text" name="reward" placeholder="Например: 5000 рублей, подарочный сертификат, мерч..." value="<%= isEdit && contest.getReward() != null ? contest.getReward() : "" %>">
                <small style="color: rgba(255,255,255,0.5);">Укажите приз для победителей соревнования</small>
            </div>

            <div class="form-group">
                <label>Дата начала</label>
                <input type="datetime-local" name="startTime" required value="<%= startTimeValue %>">
            </div>

            <div class="form-group">
                <label>Дата окончания</label>
                <input type="datetime-local" name="endTime" required value="<%= endTimeValue %>">
            </div>

            <div style="margin-top: 2rem;">
                <button type="submit" class="btn btn-primary"><%= isEdit ? "Сохранить изменения" : "Создать соревнование" %></button>
                <a href="${pageContext.request.contextPath}/admin/contests" class="btn btn-secondary">Отмена</a>
            </div>
        </form>
    </div>
</div>
</body>
</html>