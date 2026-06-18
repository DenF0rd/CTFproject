<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.example.model.Task, com.example.model.Contest, java.util.*" %>
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

    Task task = (Task) request.getAttribute("task");
    List<Contest> contests = (List<Contest>) request.getAttribute("contests");
    Integer preselectedContestId = (Integer) request.getAttribute("preselectedContestId");

    boolean isEdit = task != null;
    if (contests == null) contests = new ArrayList<>();
%>
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%= isEdit ? "Редактировать задачу" : "Создать задачу" %> - Админ-панель</title>
    <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600;700;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <style>
        /* Стили для выпадающего списка в тёмной теме */
        select {
            width: 100%;
            padding: 12px 16px;
            background: rgba(255, 255, 255, 0.08);
            border: 1px solid rgba(139, 92, 246, 0.3);
            border-radius: 16px;
            color: white;
            font-size: 1rem;
            font-family: inherit;
            appearance: none;  /* Убираем стандартную стрелку */
            -webkit-appearance: none;
            background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 12 12'%3E%3Cpath fill='%238b5cf6' d='M6 8L1 3h10z'/%3E%3C/svg%3E");
            background-repeat: no-repeat;
            background-position: right 16px center;
            cursor: pointer;
        }

        select:focus {
            outline: none;
            border-color: #8b5cf6;
            box-shadow: 0 0 0 4px rgba(139, 92, 246, 0.2);
        }

        /* Стили для опций */
        select option {
            background: #1a1a2e;
            color: white;
            padding: 8px;
        }

        select option:hover,
        select option:checked {
            background: rgba(139, 92, 246, 0.3);
        }

        /* Для браузеров на WebKit */
        select::-webkit-scrollbar {
            width: 8px;
            background: #1a1a2e;
            border-radius: 4px;
        }

        select::-webkit-scrollbar-thumb {
            background: #8b5cf6;
            border-radius: 4px;
        }

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
        input, textarea, select {
            width: 100%;
            padding: 12px 16px;
            background: rgba(255,255,255,0.08);
            border: 1px solid rgba(139,92,246,0.3);
            border-radius: 16px;
            color: white;
            font-size: 1rem;
            font-family: inherit;
        }
        input:focus, textarea:focus, select:focus { outline: none; border-color: #8b5cf6; }
        .btn { padding: 12px 24px; border-radius: 16px; font-weight: 600; border: none; cursor: pointer; transition: all 0.3s; margin-right: 1rem; }
        .btn-primary { background: linear-gradient(135deg, #8b5cf6, #6366f1); color: white; }
        .btn-primary:hover { transform: translateY(-2px); box-shadow: 0 0 15px rgba(139,92,246,0.5); }
        .btn-secondary { background: rgba(255,255,255,0.1); color: white; }
        .btn-secondary:hover { background: rgba(139,92,246,0.3); }
        .warning { color: #f59e0b; font-size: 0.75rem; margin-top: 0.25rem; }
        .hint-text { color: rgba(255,255,255,0.5); font-size: 0.8rem; margin-top: 0.25rem; }
        .row { display: flex; gap: 1rem; }
        .row .form-group { flex: 1; }
        @media (max-width: 768px) {
            .navbar { flex-direction: column; height: auto; padding: 1rem; gap: 0.8rem; }
            .nav-links { flex-wrap: wrap; justify-content: center; }
            .row { flex-direction: column; gap: 0; }
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
        <h1><%= isEdit ? "✏️ Редактирование задачи" : "➕ Создание задачи" %></h1>

        <form action="${pageContext.request.contextPath}/admin/edit-task" method="post">
            <input type="hidden" name="action" value="<%= isEdit ? "update" : "create" %>">
            <% if (isEdit) { %>
            <input type="hidden" name="taskId" value="<%= task.getId() %>">
            <% } %>

            <div class="form-group">
                <label>Соревнование</label>
                <select name="contestId" required>
                    <option value="">Выберите соревнование</option>
                    <% for (Contest c : contests) {
                        int selectedId = 0;
                        if (isEdit && task.getContestId() == c.getId()) {
                            selectedId = c.getId();
                        } else if (!isEdit && preselectedContestId != null && preselectedContestId == c.getId()) {
                            selectedId = c.getId();
                        }
                    %>
                    <option value="<%= c.getId() %>" <%= selectedId == c.getId() ? "selected" : "" %>><%= c.getTitle() %></option>
                    <% } %>
                </select>
            </div>

            <div class="form-group">
                <label>Название задачи</label>
                <input type="text" name="title" required value="<%= isEdit ? task.getTitle() : "" %>">
            </div>

            <div class="form-group">
                <label>Описание задачи</label>
                <textarea name="description" rows="6" required><%= isEdit ? task.getDescription() : "" %></textarea>
            </div>

            <div class="row">
                <div class="form-group">
                    <label>Базовая стоимость (макс. очки)</label>
                    <input type="number" name="basePoints" required
                           value="<%= isEdit ? task.getBasePoints() : 100 %>" min="10">
                    <div class="hint-text">Максимальное количество очков за задачу</div>
                </div>
                <div class="form-group">
                    <label>Минимальная стоимость</label>
                    <input type="number" name="minPoints" required
                           value="<%= isEdit ? task.getMinPoints() : 10 %>" min="1">
                    <div class="hint-text">Минимальное количество очков после снижения</div>
                </div>
            </div>

            <div class="form-group">
                <label>Текущая стоимость (начальная = базовая)</label>
                <input type="number" name="points" required
                       value="<%= isEdit ? task.getPoints() : "" %>" min="1">
                <div class="hint-text">Обычно равна базовой стоимости при создании</div>
            </div>

            <div class="form-group">
                <label>Флаг (правильный ответ)</label>
                <input type="text" name="flag" required value="<%= isEdit ? task.getFlag() : "" %>" placeholder="CTF{...}">
                <div class="warning">⚠️ Флаг будет проверяться точно по этой строке. Рекомендуемый формат: CTF{...}</div>
            </div>

            <div class="form-group">
                <label>Подсказка (необязательно)</label>
                <textarea name="hint" rows="2"><%= isEdit && task.getHint() != null ? task.getHint() : "" %></textarea>
            </div>

            <% if (isEdit) { %>
            <div class="form-group">
                <label>Активна</label>
                <select name="isActive">
                    <option value="true" <%= task.isActive() ? "selected" : "" %>>Да</option>
                    <option value="false" <%= !task.isActive() ? "selected" : "" %>>Нет</option>
                </select>
            </div>
            <% } %>

            <div style="margin-top: 2rem;">
                <button type="submit" class="btn btn-primary"><%= isEdit ? "Сохранить изменения" : "Создать задачу" %></button>
                <a href="${pageContext.request.contextPath}/admin/tasks" class="btn btn-secondary">Отмена</a>
            </div>
        </form>
    </div>
</div>
</body>
</html>