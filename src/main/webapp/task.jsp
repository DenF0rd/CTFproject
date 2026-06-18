<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*, com.example.model.Contest" %>
<%
    if (session.getAttribute("userId") == null) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }

    Map<String, Object> task = (Map<String, Object>) request.getAttribute("task");
    Contest contest = (Contest) request.getAttribute("contest");
    List<Map<String, Object>> submissionHistory = (List<Map<String, Object>>) request.getAttribute("submissionHistory");
    int contestId = (int) request.getAttribute("contestId");
    String username = (String) session.getAttribute("username");
    String message = (String) request.getAttribute("message");
    String error = (String) request.getAttribute("error");

    if (submissionHistory == null) submissionHistory = new ArrayList<>();

    boolean isSolved = task != null && (boolean) task.get("is_solved");

    // Очки, которые пользователь реально получил за эту задачу
    int userEarnedPoints = 0;
    if (request.getAttribute("userEarnedPoints") != null) {
        userEarnedPoints = (int) request.getAttribute("userEarnedPoints");
    }

    // БЕЗОПАСНОЕ ПОЛУЧЕНИЕ ДАННЫХ О ДИНАМИЧЕСКОЙ СТОИМОСТИ
    int currentPoints = 0;
    int basePoints = 0;
    int minPoints = 10;
    int solvesCount = 0;

    if (task != null) {
        currentPoints = task.containsKey("points") ? (int) task.get("points") : 0;
        basePoints = task.containsKey("base_points") && task.get("base_points") != null ?
                (int) task.get("base_points") : currentPoints;
        minPoints = task.containsKey("min_points") && task.get("min_points") != null ?
                (int) task.get("min_points") : 10;
        solvesCount = task.containsKey("solves_count") ? (int) task.get("solves_count") : 0;
    }

    int pointsReduced = basePoints - currentPoints;
    boolean priceChanged = solvesCount > 0 && basePoints != currentPoints;
%>
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%= task != null ? task.get("title") : "Задача" %> - CTF Platform</title>
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
            max-width: 1000px;
            margin: 2rem auto;
            padding: 0 2rem;
        }

        .back-link {
            display: inline-flex;
            align-items: center;
            gap: 0.5rem;
            color: #a78bfa;
            text-decoration: none;
            margin-bottom: 1.5rem;
            transition: color 0.3s;
        }

        .back-link:hover {
            color: #c084fc;
        }

        .task-card {
            background: rgba(255, 255, 255, 0.05);
            backdrop-filter: blur(20px);
            border-radius: 28px;
            border: 1px solid rgba(139, 92, 246, 0.3);
            overflow: hidden;
            margin-bottom: 1.5rem;
        }

        .task-header {
            background: linear-gradient(135deg, rgba(139, 92, 246, 0.3), rgba(99, 102, 241, 0.2));
            padding: 1.5rem;
            border-bottom: 1px solid rgba(139, 92, 246, 0.3);
        }

        .task-title {
            font-size: 1.5rem;
            font-weight: 700;
            margin-bottom: 0.5rem;
            display: flex;
            align-items: center;
            gap: 0.5rem;
            flex-wrap: wrap;
        }

        .task-meta {
            display: flex;
            gap: 1.5rem;
            flex-wrap: wrap;
            font-size: 0.85rem;
        }

        .task-meta span {
            color: #a78bfa;
        }

        .price-info {
            display: inline-flex;
            align-items: center;
            gap: 0.5rem;
            background: rgba(245, 158, 11, 0.15);
            padding: 4px 12px;
            border-radius: 20px;
            font-size: 0.75rem;
        }

        .price-info .reduced {
            color: #f59e0b;
        }

        .price-info .original {
            text-decoration: line-through;
            opacity: 0.5;
        }

        .task-content {
            padding: 1.5rem;
        }

        .task-description {
            background: rgba(0, 0, 0, 0.2);
            border-radius: 16px;
            padding: 1.5rem;
            margin-bottom: 1.5rem;
            font-family: 'Courier New', monospace;
            white-space: pre-wrap;
            line-height: 1.6;
        }

        .flag-form {
            background: rgba(0, 0, 0, 0.2);
            border-radius: 16px;
            padding: 1.5rem;
            margin-bottom: 1.5rem;
        }

        .flag-input-group {
            display: flex;
            gap: 0.5rem;
            margin-top: 0.5rem;
            flex-wrap: wrap;
        }

        .flag-input {
            flex: 1;
            padding: 12px 16px;
            background: rgba(255, 255, 255, 0.1);
            border: 1px solid rgba(139, 92, 246, 0.3);
            border-radius: 12px;
            color: white;
            font-family: monospace;
            font-size: 1rem;
            transition: all 0.3s;
        }

        .flag-input:focus {
            outline: none;
            border-color: #8b5cf6;
            box-shadow: 0 0 0 4px rgba(139, 92, 246, 0.2);
        }

        .flag-input::placeholder {
            color: rgba(255, 255, 255, 0.4);
        }

        .btn {
            padding: 12px 24px;
            border-radius: 12px;
            font-weight: 600;
            border: none;
            cursor: pointer;
            transition: all 0.3s;
        }

        .btn-primary {
            background: linear-gradient(135deg, #8b5cf6, #6366f1);
            color: white;
        }

        .btn-primary:hover {
            transform: translateY(-2px);
            box-shadow: 0 0 15px rgba(139, 92, 246, 0.5);
        }

        .btn-primary:disabled {
            opacity: 0.5;
            cursor: not-allowed;
            transform: none !important;
        }

        .message-success {
            background: rgba(16, 185, 129, 0.2);
            border-left: 3px solid #10b981;
            padding: 1rem;
            border-radius: 12px;
            margin-bottom: 1rem;
            color: #6ee7b7;
        }

        .message-error {
            background: rgba(239, 68, 68, 0.2);
            border-left: 3px solid #ef4444;
            padding: 1rem;
            border-radius: 12px;
            margin-bottom: 1rem;
            color: #fca5a5;
        }

        .hint-box {
            background: rgba(245, 158, 11, 0.1);
            border-left: 3px solid #f59e0b;
            padding: 1rem;
            border-radius: 12px;
            margin-top: 1rem;
            font-size: 0.85rem;
        }

        .solved-badge {
            background: #10b981;
            padding: 4px 12px;
            border-radius: 20px;
            font-size: 0.7rem;
            display: inline-flex;
            align-items: center;
            gap: 0.3rem;
        }

        .history-table {
            background: rgba(255, 255, 255, 0.05);
            border-radius: 20px;
            overflow: hidden;
        }

        .history-row {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 0.8rem 1.5rem;
            border-bottom: 1px solid rgba(255, 255, 255, 0.05);
        }

        .history-row:last-child {
            border-bottom: none;
        }

        .history-flag {
            font-family: monospace;
            font-size: 0.85rem;
        }

        .history-correct {
            color: #10b981;
        }

        .history-wrong {
            color: #ef4444;
        }

        .files-list {
            display: flex;
            gap: 1rem;
            flex-wrap: wrap;
            margin-bottom: 1.5rem;
        }

        .file-link {
            background: rgba(139, 92, 246, 0.2);
            padding: 8px 16px;
            border-radius: 12px;
            text-decoration: none;
            color: #a78bfa;
            font-size: 0.85rem;
            transition: all 0.3s;
        }

        .file-link:hover {
            background: rgba(139, 92, 246, 0.4);
            color: white;
        }

        .empty-history {
            text-align: center;
            padding: 2rem;
            color: rgba(255, 255, 255, 0.4);
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
            .flag-input-group {
                flex-direction: column;
            }
            .btn-primary {
                width: 100%;
            }
            .history-row {
                flex-direction: column;
                text-align: center;
                gap: 0.3rem;
                padding: 0.8rem 1rem;
            }
            .task-meta {
                flex-direction: column;
                gap: 0.5rem;
            }
            .task-title {
                font-size: 1.2rem;
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
        <a href="${pageContext.request.contextPath}/scoreboard">Рейтинг</a>
        <a href="${pageContext.request.contextPath}/profile?id=<%= session.getAttribute("userId") %>">Профиль</a>
        <% if (session.getAttribute("isAdmin") != null && (Boolean) session.getAttribute("isAdmin")) { %>
        <a href="${pageContext.request.contextPath}/admin">Админ-панель</a>
        <% } %>
    </div>
    <a href="${pageContext.request.contextPath}/logout" class="logout-btn"><i class="fas fa-sign-out-alt"></i> Выйти</a>
</nav>

<div class="container">
    <a href="${pageContext.request.contextPath}/contest?id=<%= contestId %>" class="back-link">
        <i class="fas fa-arrow-left"></i> Назад к соревнованию
    </a>

    <% if (task == null) { %>
    <div class="task-card" style="text-align: center; padding: 3rem;">
        <i class="fas fa-exclamation-triangle" style="font-size: 3rem; opacity: 0.5;"></i>
        <p style="margin-top: 1rem;">Задача не найдена</p>
    </div>
    <% } else { %>

    <div class="task-card">
        <div class="task-header">
            <div class="task-title">
                <%= task.get("title") %>
                <% if (isSolved) { %>
                <span class="solved-badge"><i class="fas fa-check"></i> Решено</span>
                <% } %>
            </div>
            <div class="task-meta">
                <span><i class="fas fa-tag"></i> <%= task.get("category") != null ? task.get("category") : "Без категории" %></span>

                <%-- ДИНАМИЧЕСКАЯ СТОИМОСТЬ --%>
                <span class="price-info">
                    <i class="fas fa-star" style="color: #f59e0b;"></i>
                    <strong><%= currentPoints %></strong> очков
                    <% if (priceChanged) { %>
                        <span class="original"><%= basePoints %></span>
                        <span class="reduced">(-<%= pointsReduced %>)</span>
                    <% } %>
                </span>

                <span><i class="fas fa-users"></i> Решили: <%= solvesCount %> человек</span>

                <% if (priceChanged && !isSolved) { %>
                <span style="color: #f59e0b; font-size: 0.7rem;">
                        <i class="fas fa-arrow-down"></i>
                        Стоимость снижена на <%= pointsReduced %> очков
                    </span>
                <% } %>
            </div>
        </div>

        <div class="task-content">
            <!-- Условие задачи -->
            <div class="task-description">
                <%= task.get("description") %>
            </div>

            <!-- Файлы -->
            <% if (task.get("file_url") != null && !((String)task.get("file_url")).isEmpty()) { %>
            <div class="files-list">
                <a href="${pageContext.request.contextPath}/<%= task.get("file_url") %>" class="file-link" download>
                    <i class="fas fa-download"></i> Скачать файл
                </a>
            </div>
            <% } %>

            <!-- Форма отправки флага -->
            <div class="flag-form">
                <% if (message != null) { %>
                <div class="message-success"><i class="fas fa-check-circle"></i> <%= message %></div>
                <% } %>
                <% if (error != null) { %>
                <div class="message-error"><i class="fas fa-exclamation-circle"></i> <%= error %></div>
                <% } %>

                <% if (isSolved) { %>
                <div class="message-success">
                    <i class="fas fa-trophy" style="color: #f59e0b;"></i>
                    Вы уже решили эту задачу!
                    <div style="margin-top: 0.5rem;">
                        Получено: <strong><%= userEarnedPoints %></strong> очков
                        <span style="color: rgba(255,255,255,0.5); font-size: 0.8rem; display: block; margin-top: 0.2rem;">
                            Текущая стоимость задачи: <%= currentPoints %> очков
                        </span>
                    </div>
                </div>
                <% } else { %>
                <form action="${pageContext.request.contextPath}/task" method="post" id="flagForm">
                    <input type="hidden" name="taskId" value="<%= task.get("id") %>">
                    <input type="hidden" name="contestId" value="<%= contestId %>">
                    <div style="font-weight: 500; margin-bottom: 0.5rem;">
                        <i class="fas fa-flag-checkered"></i> Введите флаг:
                    </div>
                    <div class="flag-input-group">
                        <input type="text" name="flag" class="flag-input" placeholder="CTF{...}" required autocomplete="off">
                        <button type="button" class="btn btn-primary" onclick="submitFlagForm()">
                            <i class="fas fa-paper-plane"></i> Проверить
                        </button>
                    </div>
                </form>
                <% } %>

                <!-- Подсказка -->
                <% if (task.get("hint") != null && !((String)task.get("hint")).isEmpty()) { %>
                <div class="hint-box">
                    <i class="fas fa-lightbulb"></i> <strong>Подсказка:</strong> <%= task.get("hint") %>
                </div>
                <% } %>
            </div>

            <script>
                let formSubmitting = false;

                function submitFlagForm() {
                    if (formSubmitting) return;

                    const form = document.getElementById('flagForm');
                    const flagInput = form.querySelector('input[name="flag"]');
                    const flag = flagInput.value.trim();

                    if (!flag) {
                        alert('⚠️ Введите флаг!');
                        flagInput.focus();
                        return;
                    }

                    formSubmitting = true;

                    const submitBtn = document.querySelector('.flag-input-group .btn-primary');
                    if (submitBtn) {
                        submitBtn.disabled = true;
                        submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Проверка...';
                    }

                    form.submit();
                }

                // Обработка Enter в поле ввода
                document.addEventListener('DOMContentLoaded', function() {
                    const flagInput = document.querySelector('.flag-input');
                    if (flagInput) {
                        flagInput.addEventListener('keypress', function(e) {
                            if (e.key === 'Enter') {
                                e.preventDefault();
                                submitFlagForm();
                            }
                        });
                    }
                });
            </script>

            <!-- История попыток -->
            <div style="margin-top: 1.5rem;">
                <h3 style="margin-bottom: 1rem; font-size: 1.1rem; display: flex; align-items: center; gap: 0.5rem;">
                    <i class="fas fa-history"></i> История попыток
                    <span style="font-size: 0.7rem; color: rgba(255,255,255,0.4); font-weight: 400;">
                        (последние 10)
                    </span>
                </h3>
                <% if (submissionHistory.isEmpty()) { %>
                <div class="empty-history">
                    <i class="fas fa-inbox" style="font-size: 2rem; opacity: 0.3;"></i>
                    <p style="margin-top: 0.5rem;">Пока нет попыток</p>
                </div>
                <% } else { %>
                <div class="history-table">
                    <% for (Map<String, Object> attempt : submissionHistory) { %>
                    <div class="history-row">
                        <div class="history-flag"><code><%= attempt.get("flag") %></code></div>
                        <div class="<%= (boolean)attempt.get("is_correct") ? "history-correct" : "history-wrong" %>">
                            <% if ((boolean)attempt.get("is_correct")) { %>
                            <i class="fas fa-check-circle"></i> Правильно
                            <% } else { %>
                            <i class="fas fa-times-circle"></i> Неправильно
                            <% } %>
                        </div>
                        <div class="history-date" style="font-size: 0.7rem; color: rgba(255,255,255,0.4);">
                            <%= attempt.get("submitted_at") %>
                        </div>
                    </div>
                    <% } %>
                </div>
                <% } %>
            </div>
        </div>
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