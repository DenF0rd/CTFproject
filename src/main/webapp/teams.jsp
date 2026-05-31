<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*" %>
<%
    if (session.getAttribute("userId") == null) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }

    List<Map<String, Object>> allTeams = (List<Map<String, Object>>) request.getAttribute("allTeams");
    Map<String, Object> userTeam = (Map<String, Object>) request.getAttribute("userTeam");
    int userId = (int) session.getAttribute("userId");
    boolean isAdmin = session.getAttribute("isAdmin") != null && (Boolean) session.getAttribute("isAdmin");
    String searchQuery = (String) request.getAttribute("searchQuery");

    if (allTeams == null) allTeams = new ArrayList<>();
%>
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Команды - CTF Platform</title>
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
            color: #c084fc;
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

        .search-section {
            margin-bottom: 2rem;
        }

        .search-box {
            display: flex;
            gap: 0.5rem;
            max-width: 500px;
        }

        .search-input {
            flex: 1;
            padding: 10px 16px;
            background: rgba(255, 255, 255, 0.1);
            border: 1px solid rgba(139, 92, 246, 0.3);
            border-radius: 12px;
            color: white;
            font-size: 0.9rem;
        }

        .search-input:focus {
            outline: none;
            border-color: #8b5cf6;
        }

        .search-input::placeholder {
            color: rgba(255, 255, 255, 0.5);
        }

        .success-message {
            background: rgba(16, 185, 129, 0.2);
            border-left: 3px solid #10b981;
            padding: 0.75rem;
            border-radius: 12px;
            margin-bottom: 1.5rem;
            color: #6ee7b7;
        }

        .error-message {
            background: rgba(239, 68, 68, 0.2);
            border-left: 3px solid #ef4444;
            padding: 0.75rem;
            border-radius: 12px;
            margin-bottom: 1.5rem;
            color: #fca5a5;
        }

        .teams-grid {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
            gap: 1.5rem;
            margin-bottom: 2rem;
        }

        .team-card {
            background: rgba(255, 255, 255, 0.05);
            backdrop-filter: blur(20px);
            border-radius: 20px;
            padding: 1.5rem;
            transition: all 0.3s;
            border: 1px solid rgba(139, 92, 246, 0.3);
        }

        .team-card:hover {
            transform: translateY(-5px);
            background: rgba(255, 255, 255, 0.08);
            border-color: rgba(139, 92, 246, 0.6);
        }

        .team-card.my-team {
            border-color: #8b5cf6;
            background: rgba(139, 92, 246, 0.15);
        }

        .team-name {
            font-size: 1.2rem;
            font-weight: 700;
            margin-bottom: 0.5rem;
            display: flex;
            justify-content: space-between;
            align-items: center;
            flex-wrap: wrap;
            gap: 0.5rem;
        }

        .team-description {
            font-size: 0.8rem;
            color: rgba(255, 255, 255, 0.7);
            margin-bottom: 1rem;
            line-height: 1.4;
        }

        .team-meta {
            display: flex;
            justify-content: space-between;
            align-items: center;
            font-size: 0.7rem;
            color: #a78bfa;
            margin-bottom: 1rem;
            flex-wrap: wrap;
            gap: 0.5rem;
        }

        .team-actions {
            display: flex;
            gap: 0.5rem;
            margin-top: 1rem;
            flex-wrap: wrap;
            align-items: center;
        }

        .btn {
            padding: 8px 16px;
            border-radius: 10px;
            font-size: 0.75rem;
            font-weight: 600;
            cursor: pointer;
            border: none;
            transition: all 0.3s;
            text-decoration: none;
            display: inline-flex;
            align-items: center;
            gap: 0.3rem;
        }

        .btn-primary {
            background: linear-gradient(135deg, #8b5cf6, #6366f1);
            color: white;
        }

        .btn-primary:hover {
            transform: translateY(-2px);
            box-shadow: 0 0 15px rgba(139, 92, 246, 0.5);
        }

        .btn-secondary {
            background: rgba(255, 255, 255, 0.1);
            color: white;
            border: 1px solid rgba(139, 92, 246, 0.3);
        }

        .btn-secondary:hover {
            background: rgba(139, 92, 246, 0.3);
        }

        .btn-danger {
            background: rgba(239, 68, 68, 0.3);
            color: white;
            border: 1px solid #ef4444;
        }

        .btn-danger:hover {
            background: #ef4444;
        }

        .member-badge {
            background: #8b5cf6;
            padding: 2px 8px;
            border-radius: 12px;
            font-size: 0.6rem;
        }

        .empty-state {
            text-align: center;
            padding: 3rem;
            background: rgba(255, 255, 255, 0.05);
            border-radius: 20px;
            color: rgba(255, 255, 255, 0.5);
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
            max-width: 500px;
            width: 90%;
            border: 1px solid rgba(139, 92, 246, 0.3);
        }

        .modal-content h3 { margin-bottom: 1rem; }
        .modal-content input, .modal-content textarea {
            width: 100%;
            padding: 10px;
            background: rgba(255, 255, 255, 0.1);
            border: 1px solid rgba(139, 92, 246, 0.3);
            border-radius: 10px;
            color: white;
            margin-bottom: 1rem;
        }
        .modal-buttons { display: flex; gap: 1rem; justify-content: flex-end; }

        @media (max-width: 768px) {
            .navbar { flex-direction: column; height: auto; padding: 1rem; }
            .nav-links { flex-wrap: wrap; justify-content: center; }
            .teams-grid { grid-template-columns: 1fr; }
            .team-actions { flex-direction: column; }
            .team-actions form { width: 100%; }
            .team-actions .btn { width: 100%; justify-content: center; }
            .search-box { max-width: 100%; }
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
        <a href="${pageContext.request.contextPath}/teams" class="active">Команды</a>
        <a href="${pageContext.request.contextPath}/scoreboard">Рейтинг</a>
        <a href="${pageContext.request.contextPath}/profile?id=<%= session.getAttribute("userId") %>">Профиль</a>
        <% if (isAdmin) { %>
        <a href="${pageContext.request.contextPath}/admin">👑 Админ-панель</a>
        <% } %>
    </div>
    <a href="${pageContext.request.contextPath}/logout" class="logout-btn"><i class="fas fa-sign-out-alt"></i> Выйти</a>
</nav>

<div class="container">
    <div class="page-header">
        <h1><i class="fas fa-users"></i> Команды</h1>
        <p>Объединяйтесь с другими участниками для совместного решения задач</p>
    </div>

    <div class="search-section">
        <div class="search-box">
            <input type="text" id="searchInput" class="search-input" placeholder="🔍 Поиск команд по названию..." value="<%= searchQuery != null ? searchQuery : "" %>">
            <button onclick="searchTeams()" class="btn btn-primary">
                <i class="fas fa-search"></i> Найти
            </button>
            <button onclick="resetSearch()" class="btn btn-secondary">
                <i class="fas fa-times"></i> Сброс
            </button>
        </div>
    </div>

    <% if (session.getAttribute("success") != null) { %>
    <div class="success-message">✅ <%= session.getAttribute("success") %></div>
    <% session.removeAttribute("success"); %>
    <% } %>

    <% if (session.getAttribute("error") != null) { %>
    <div class="error-message">⚠️ <%= session.getAttribute("error") %></div>
    <% session.removeAttribute("error"); %>
    <% } %>

    <div class="teams-grid">
        <% for (Map<String, Object> team : allTeams) {
            boolean isUserTeam = team.containsKey("isUserTeam") && (Boolean) team.get("isUserTeam");
            boolean isCaptain = team.containsKey("isCaptain") && (Boolean) team.get("isCaptain");
        %>
        <div class="team-card <%= isUserTeam ? "my-team" : "" %>">
            <div class="team-name">
                <%= team.get("name") %>
                <% if (isUserTeam) { %>
                <span class="member-badge"><i class="fas fa-check"></i> Моя команда</span>
                <% } %>
            </div>
            <div class="team-description"><%= team.get("description") != null ? team.get("description") : "Нет описания" %></div>
            <div class="team-meta">
                <span><i class="fas fa-crown"></i> Капитан: <%= team.get("captain_name") %></span>
                <span><i class="fas fa-users"></i> Участников: <%= team.get("members_count") %> / 5</span>
                <span><i class="fas fa-trophy"></i> Очков: <%= team.get("total_points") %></span>
            </div>
            <div class="team-actions">
                <% if (isUserTeam) { %>
                <button class="btn btn-secondary" onclick="location.href='${pageContext.request.contextPath}/team-members?teamId=<%= team.get("id") %>'">
                    <i class="fas fa-users"></i> Участники
                </button>
                <% if (isCaptain) { %>
                <button class="btn btn-secondary" onclick="openEditModal(<%= team.get("id") %>, '<%= team.get("name") %>', '<%= team.get("description") != null ? team.get("description") : "" %>')">
                    <i class="fas fa-edit"></i> Редактировать
                </button>
                <form action="${pageContext.request.contextPath}/delete-team" method="post" style="display: inline;"
                      onsubmit="return confirm('Вы уверены, что хотите удалить команду? Это действие необратимо!');">
                    <input type="hidden" name="teamId" value="<%= team.get("id") %>">
                    <button type="submit" class="btn btn-danger">
                        <i class="fas fa-trash"></i> Удалить команду
                    </button>
                </form>
                <% } else { %>
                <form action="${pageContext.request.contextPath}/leave-team" method="get" style="display: inline;"
                      onsubmit="return confirm('Вы уверены, что хотите покинуть команду?');">
                    <input type="hidden" name="teamId" value="<%= team.get("id") %>">
                    <button type="submit" class="btn btn-danger">
                        <i class="fas fa-sign-out-alt"></i> Покинуть
                    </button>
                </form>
                <% } %>
                <% } else { %>
                <button class="btn btn-primary" onclick="openJoinModal(<%= team.get("id") %>, '<%= team.get("name") %>')">
                    <i class="fas fa-user-plus"></i> Вступить
                </button>
                <% } %>
            </div>
        </div>
        <% } %>

        <% if (allTeams.isEmpty()) { %>
        <div class="empty-state">
            <i class="fas fa-users" style="font-size: 3rem;"></i>
            <p style="margin-top: 1rem;">
                <% if (searchQuery != null && !searchQuery.isEmpty()) { %>
                По запросу "<%= searchQuery %>" ничего не найдено
                <% } else { %>
                Пока нет созданных команд
                <% } %>
            </p>
            <button class="btn btn-primary" style="margin-top: 1rem;" onclick="location.href='${pageContext.request.contextPath}/create-team-global'">
                <i class="fas fa-plus"></i> Создать первую команду
            </button>
        </div>
        <% } %>
    </div>

    <div style="text-align: center; margin-top: 2rem;">
        <button class="btn btn-primary" onclick="location.href='${pageContext.request.contextPath}/create-team-global'">
            <i class="fas fa-plus"></i> Создать новую команду
        </button>
    </div>
</div>

<!-- Модальное окно вступления в команду -->
<div id="joinModal" class="modal">
    <div class="modal-content">
        <h3><i class="fas fa-user-plus"></i> Вступить в команду</h3>
        <input type="hidden" id="joinTeamId">
        <p>Вы хотите вступить в команду <strong id="joinTeamName"></strong>?</p>
        <div class="modal-buttons" style="margin-top: 1.5rem;">
            <button type="button" class="btn btn-primary" onclick="confirmJoin()">Да, вступить</button>
            <button type="button" class="btn btn-secondary" onclick="closeJoinModal()">Отмена</button>
        </div>
    </div>
</div>

<!-- Модальное окно редактирования команды -->
<div id="editModal" class="modal">
    <div class="modal-content">
        <h3><i class="fas fa-edit"></i> Редактировать команду</h3>
        <form action="${pageContext.request.contextPath}/edit-team" method="post">
            <input type="hidden" name="teamId" id="editTeamId">
            <input type="text" name="name" id="editTeamName" placeholder="Название команды" required>
            <textarea name="description" id="editTeamDescription" rows="3" placeholder="Описание команды"></textarea>
            <div class="modal-buttons">
                <button type="submit" class="btn btn-primary">Сохранить</button>
                <button type="button" class="btn btn-secondary" onclick="closeEditModal()">Отмена</button>
            </div>
        </form>
    </div>
</div>

<script>
    document.querySelectorAll('.circle').forEach((circle, i) => {
        circle.style.animationDuration = `${15 + i * 5}s`;
    });

    function searchTeams() {
        var query = document.getElementById('searchInput').value;
        if (query.trim() === '') {
            resetSearch();
            return;
        }
        window.location.href = '${pageContext.request.contextPath}/teams?search=' + encodeURIComponent(query);
    }

    function resetSearch() {
        window.location.href = '${pageContext.request.contextPath}/teams';
    }

    document.getElementById('searchInput').addEventListener('keypress', function(e) {
        if (e.key === 'Enter') {
            searchTeams();
        }
    });

    function openJoinModal(teamId, teamName) {
        var teamIdInput = document.getElementById('joinTeamId');
        var teamNameSpan = document.getElementById('joinTeamName');

        teamIdInput.value = teamId;
        teamNameSpan.innerText = teamName;

        document.getElementById('joinModal').style.display = 'flex';
    }

    function closeJoinModal() {
        document.getElementById('joinModal').style.display = 'none';
    }

    function confirmJoin() {
        var teamIdInput = document.getElementById('joinTeamId');
        if (teamIdInput && teamIdInput.value) {
            window.location.href = '${pageContext.request.contextPath}/send-join-request?teamId=' + teamIdInput.value;
        } else {
            alert("Ошибка: не удалось определить команду");
        }
    }

    function openEditModal(teamId, name, description) {
        document.getElementById('editTeamId').value = teamId;
        document.getElementById('editTeamName').value = name;
        document.getElementById('editTeamDescription').value = description;
        document.getElementById('editModal').style.display = 'flex';
    }

    function closeEditModal() {
        document.getElementById('editModal').style.display = 'none';
    }

    window.onclick = function(event) {
        if (event.target.classList.contains('modal')) {
            event.target.style.display = 'none';
        }
    }
</script>

</body>
</html>