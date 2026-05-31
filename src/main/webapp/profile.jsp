<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*, com.example.model.User, com.example.model.Team" %>
<%
    if (session.getAttribute("userId") == null) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }

    User profileUser = (User) request.getAttribute("profileUser");
    boolean isOwnProfile = (boolean) request.getAttribute("isOwnProfile");
    int totalScore = (int) request.getAttribute("totalScore");
    int solvedCount = (int) request.getAttribute("solvedCount");
    int rank = (int) request.getAttribute("rank");
    Team userTeam = (Team) request.getAttribute("userTeam");
    boolean isCaptain = (boolean) request.getAttribute("isCaptain");
    int currentUserId = (int) session.getAttribute("userId");

    if (profileUser == null) profileUser = new User();
%>
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Профиль - CTF Platform</title>
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
            max-width: 1200px;
            margin: 2rem auto;
            padding: 0 2rem;
        }

        .profile-card {
            background: rgba(255, 255, 255, 0.05);
            backdrop-filter: blur(20px);
            border-radius: 28px;
            padding: 2rem;
            border: 1px solid rgba(139, 92, 246, 0.3);
            transition: all 0.3s;
        }

        .profile-card:hover {
            transform: translateY(-5px);
            border-color: rgba(139, 92, 246, 0.6);
        }

        .profile-header {
            display: flex;
            gap: 2rem;
            align-items: center;
            flex-wrap: wrap;
            margin-bottom: 2rem;
        }

        .avatar-container {
            position: relative;
        }

        .avatar {
            width: 120px;
            height: 120px;
            background: linear-gradient(135deg, #8b5cf6, #6366f1);
            border-radius: 60px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 3rem;
            font-weight: 700;
            border: 3px solid rgba(255,255,255,0.2);
            transition: all 0.3s;
            object-fit: cover;
        }

        .avatar-img {
            width: 120px;
            height: 120px;
            border-radius: 60px;
            object-fit: cover;
        }

        .avatar:hover {
            transform: scale(1.05);
            box-shadow: 0 0 20px rgba(139, 92, 246, 0.5);
        }

        .change-avatar-btn {
            position: absolute;
            bottom: 5px;
            right: 5px;
            background: #8b5cf6;
            border: none;
            border-radius: 50%;
            width: 32px;
            height: 32px;
            cursor: pointer;
            transition: all 0.3s;
            color: white;
        }

        .change-avatar-btn:hover {
            background: #6366f1;
            transform: scale(1.1);
        }

        .profile-info {
            flex: 1;
        }

        .profile-name {
            font-size: 1.8rem;
            font-weight: 700;
            margin-bottom: 0.25rem;
            display: flex;
            align-items: center;
            gap: 0.5rem;
            flex-wrap: wrap;
        }

        .edit-name-btn {
            background: rgba(255,255,255,0.1);
            border: none;
            padding: 4px 12px;
            border-radius: 20px;
            font-size: 0.7rem;
            cursor: pointer;
            transition: all 0.3s;
            color: #a78bfa;
        }

        .edit-name-btn:hover {
            background: #8b5cf6;
            color: white;
        }

        .profile-role {
            color: #c084fc;
            font-size: 0.85rem;
            margin-bottom: 0.5rem;
        }

        .profile-stats {
            display: flex;
            gap: 1rem;
            margin-top: 0.5rem;
        }

        .stat-badge {
            background: rgba(0, 0, 0, 0.3);
            padding: 4px 12px;
            border-radius: 20px;
            font-size: 0.75rem;
        }

        .rank-badge {
            display: inline-block;
            background: linear-gradient(135deg, #f59e0b, #d97706);
            padding: 4px 12px;
            border-radius: 20px;
            font-size: 0.7rem;
            font-weight: 600;
        }

        .stats-grid {
            display: grid;
            grid-template-columns: repeat(3, 1fr);
            gap: 1rem;
            margin: 1.5rem 0;
        }

        .stat-item {
            background: rgba(0, 0, 0, 0.2);
            border-radius: 16px;
            padding: 1rem;
            text-align: center;
        }

        .stat-number {
            font-size: 1.8rem;
            font-weight: 700;
            color: #c084fc;
        }

        .stat-label {
            font-size: 0.7rem;
            color: rgba(255, 255, 255, 0.6);
        }

        .info-row {
            display: flex;
            justify-content: space-between;
            padding: 0.75rem 0;
            border-bottom: 1px solid rgba(255, 255, 255, 0.1);
        }

        .info-label {
            color: rgba(255, 255, 255, 0.6);
            font-size: 0.85rem;
        }

        .info-value {
            font-weight: 500;
        }

        .edit-btn {
            width: 100%;
            margin-top: 1rem;
            background: linear-gradient(135deg, #8b5cf6, #6366f1);
            border: none;
            padding: 10px;
            border-radius: 12px;
            color: white;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.3s;
        }

        .edit-btn:hover {
            transform: translateY(-2px);
            box-shadow: 0 0 15px rgba(139, 92, 246, 0.5);
        }

        .btn-primary {
            background: linear-gradient(135deg, #8b5cf6, #6366f1);
            color: white;
            border: none;
            padding: 8px 16px;
            border-radius: 10px;
            cursor: pointer;
            transition: all 0.3s;
        }

        .btn-primary:hover {
            transform: translateY(-2px);
            box-shadow: 0 0 15px rgba(139, 92, 246, 0.5);
        }

        .btn-secondary {
            background: rgba(255, 255, 255, 0.1);
            color: white;
            border: 1px solid rgba(139, 92, 246, 0.3);
            padding: 8px 16px;
            border-radius: 10px;
            cursor: pointer;
            transition: all 0.3s;
        }

        .btn-secondary:hover {
            background: rgba(139, 92, 246, 0.3);
        }

        .btn-danger {
            background: rgba(239, 68, 68, 0.3);
            color: white;
            border: 1px solid #ef4444;
            padding: 8px 16px;
            border-radius: 10px;
            cursor: pointer;
            transition: all 0.3s;
        }

        .btn-danger:hover {
            background: #ef4444;
        }

        .team-card {
            background: rgba(255, 255, 255, 0.05);
            border-radius: 16px;
            padding: 1rem;
            margin-bottom: 1rem;
        }

        /* Модальные окна */
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
            border-radius: 28px;
            padding: 2rem;
            max-width: 500px;
            width: 90%;
            border: 1px solid rgba(139, 92, 246, 0.3);
        }

        .modal-content h3 {
            margin-bottom: 1rem;
        }

        .modal-content input, .modal-content textarea {
            width: 100%;
            padding: 12px;
            background: rgba(255, 255, 255, 0.1);
            border: 1px solid rgba(139, 92, 246, 0.3);
            border-radius: 12px;
            color: white;
            margin-bottom: 1rem;
        }

        .modal-content input:focus, .modal-content textarea:focus {
            outline: none;
            border-color: #8b5cf6;
        }

        .modal-buttons {
            display: flex;
            gap: 1rem;
        }

        .btn {
            padding: 10px 20px;
            border-radius: 12px;
            font-weight: 600;
            border: none;
            cursor: pointer;
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
            .profile-header {
                flex-direction: column;
                text-align: center;
            }
            .stats-grid {
                grid-template-columns: 1fr;
                gap: 0.5rem;
            }
            .info-row {
                flex-direction: column;
                gap: 0.3rem;
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
        <a href="${pageContext.request.contextPath}/profile?id=<%= session.getAttribute("userId") %>" class="active">Профиль</a>
        <% if (session.getAttribute("isAdmin") != null && (Boolean) session.getAttribute("isAdmin")) { %>
        <a href="${pageContext.request.contextPath}/admin">👑 Админ-панель</a>
        <% } %>
    </div>
    <a href="${pageContext.request.contextPath}/logout" class="logout-btn"><i class="fas fa-sign-out-alt"></i> Выйти</a>
</nav>

<div class="container">
    <div class="profile-card">
        <div class="profile-header">
            <div class="avatar-container">
                <% if (profileUser.getAvatarPath() != null && !profileUser.getAvatarPath().isEmpty()) { %>
                <img src="${pageContext.request.contextPath}/<%= profileUser.getAvatarPath() %>" alt="Avatar" class="avatar-img">
                <% } else { %>
                <div class="avatar">
                    <%= profileUser.getUsername() != null && !profileUser.getUsername().isEmpty() ? profileUser.getUsername().charAt(0) : "?" %>
                </div>
                <% } %>
                <% if (isOwnProfile) { %>
                <button class="change-avatar-btn" onclick="openAvatarModal()">
                    <i class="fas fa-camera"></i>
                </button>
                <% } %>
            </div>
            <div class="profile-info">
                <div class="profile-name">
                    <%= profileUser.getUsername() != null ? profileUser.getUsername() : "Пользователь" %>
                    <% if (rank <= 3 && rank > 0) { %>
                    <span class="rank-badge">
                            <% if (rank == 1) { %>🥇 TOP 1<% }
                    else if (rank == 2) { %>🥈 TOP 2<% }
                    else if (rank == 3) { %>🥉 TOP 3<% } %>
                        </span>
                    <% } %>
                    <% if (isOwnProfile) { %>
                    <button class="edit-name-btn" onclick="openNameModal()">
                        <i class="fas fa-pencil-alt"></i> Изменить ник
                    </button>
                    <% } %>
                </div>
                <div class="profile-role">
                    <% if (profileUser.isAdmin()) { %>
                    <i class="fas fa-crown"></i> Администратор
                    <% } else { %>
                    <i class="fas fa-user"></i> Участник
                    <% } %>
                    <% if (!isOwnProfile) { %>
                    <span style="margin-left: 0.5rem; font-size: 0.7rem;">
                            <i class="fas fa-eye"></i> Просмотр профиля
                        </span>
                    <% } %>
                </div>
                <div class="profile-stats">
                    <span class="stat-badge"><i class="fas fa-calendar"></i> Регистрация: <%= profileUser.getRegistrationDate() != null ? profileUser.getRegistrationDate() : "—" %></span>
                </div>
            </div>
        </div>

        <!-- Статистика -->
        <div class="stats-grid">
            <div class="stat-item">
                <div class="stat-number"><%= totalScore %></div>
                <div class="stat-label">Очков</div>
            </div>
            <div class="stat-item">
                <div class="stat-number"><%= solvedCount %></div>
                <div class="stat-label">Решено задач</div>
            </div>
            <div class="stat-item">
                <div class="stat-number">#<%= rank %></div>
                <div class="stat-label">Рейтинг</div>
            </div>
        </div>

        <!-- Информация -->
        <div class="info-row">
            <span class="info-label"><i class="fas fa-envelope"></i> Email</span>
            <span class="info-value"><%= profileUser.getEmail() != null ? profileUser.getEmail() : "—" %></span>
        </div>

        <% if (profileUser.getBio() != null && !profileUser.getBio().isEmpty()) { %>
        <div class="info-row" style="flex-direction: column; gap: 0.3rem;">
            <span class="info-label"><i class="fas fa-info-circle"></i> О себе</span>
            <span class="info-value"><%= profileUser.getBio() %></span>
        </div>
        <% } %>

        <% if (profileUser.getCity() != null && !profileUser.getCity().isEmpty()) { %>
        <div class="info-row">
            <span class="info-label"><i class="fas fa-map-marker-alt"></i> Город</span>
            <span class="info-value"><%= profileUser.getCity() %></span>
        </div>
        <% } %>

        <% if (profileUser.getAge() > 0) { %>
        <div class="info-row">
            <span class="info-label"><i class="fas fa-birthday-cake"></i> Возраст</span>
            <span class="info-value"><%= profileUser.getAge() %> лет</span>
        </div>
        <% } %>

        <% if (isOwnProfile) { %>
        <button class="edit-btn" onclick="openEditModal()">
            <i class="fas fa-edit"></i> Редактировать профиль
        </button>
        <% } %>

        <!-- БЛОК МОЯ КОМАНДА -->
        <div style="margin-top: 2rem; padding-top: 1rem; border-top: 1px solid rgba(255,255,255,0.1);">
            <h3 style="margin-bottom: 1rem;"><i class="fas fa-users"></i> Моя команда</h3>

            <% if (userTeam == null) { %>
            <div style="text-align: center; padding: 1.5rem; background: rgba(255,255,255,0.05); border-radius: 16px;">
                <i class="fas fa-users" style="font-size: 2rem; opacity: 0.5;"></i>
                <p style="margin: 0.5rem 0;">Вы пока не состоите в команде</p>
                <button class="btn-primary" onclick="location.href='${pageContext.request.contextPath}/create-team-global'">
                    <i class="fas fa-plus"></i> Создать команду
                </button>
            </div>
            <% } else { %>
            <div class="team-card" style="background: rgba(255,255,255,0.05); border-radius: 16px; padding: 1rem;">
                <div style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 0.5rem;">
                    <div>
                        <strong><i class="fas fa-tag"></i> <%= userTeam.getName() %></strong>
                        <span style="font-size: 0.7rem; color: #a78bfa; margin-left: 0.5rem;">
                        <i class="fas fa-trophy"></i> Очков: <%= userTeam.getTotalPoints() %>
                    </span>
                    </div>
                    <div>
                    <span style="font-size: 0.7rem; background: rgba(139,92,246,0.3); padding: 2px 8px; border-radius: 12px;">
                        <i class="fas fa-users"></i> Участников: <%= userTeam.getMembersCount() %> / 5
                    </span>
                    </div>
                </div>
                <div style="font-size: 0.8rem; color: rgba(255,255,255,0.6); margin-top: 0.5rem;">
                    <i class="fas fa-info-circle"></i> <%= userTeam.getDescription() != null ? userTeam.getDescription() : "Нет описания" %>
                </div>
                <div style="margin-top: 0.75rem; display: flex; gap: 0.5rem; flex-wrap: wrap;">
                    <button class="btn-secondary" onclick="location.href='${pageContext.request.contextPath}/team-members?teamId=<%= userTeam.getId() %>'">
                        <i class="fas fa-users"></i> Участники
                    </button>

                    <% if (isCaptain) { %>
                    <button class="btn-secondary" onclick="openProfileEditTeamModal(<%= userTeam.getId() %>, '<%= userTeam.getName() %>', '<%= userTeam.getDescription() != null ? userTeam.getDescription() : "" %>')">
                        <i class="fas fa-edit"></i> Редактировать
                    </button>
                    <!-- ИСПРАВЛЕННАЯ ФОРМА ДЛЯ УДАЛЕНИЯ -->
                    <form action="${pageContext.request.contextPath}/delete-team" method="post" style="display: inline;"
                          onsubmit="return confirm('Вы уверены, что хотите удалить команду? Это действие необратимо!');">
                        <input type="hidden" name="teamId" value="<%= userTeam.getId() %>">
                        <button type="submit" class="btn-danger">
                            <i class="fas fa-trash"></i> Удалить команду
                        </button>
                    </form>
                    <% } else { %>
                    <form action="${pageContext.request.contextPath}/leave-team" method="post" style="display: inline;"
                          onsubmit="return confirm('Вы уверены, что хотите покинуть команду?');">
                        <input type="hidden" name="teamId" value="<%= userTeam.getId() %>">
                        <button type="submit" class="btn-danger">
                            <i class="fas fa-sign-out-alt"></i> Покинуть команду
                        </button>
                    </form>
                    <% } %>
                </div>
            </div>
            <% } %>
        </div>

<!-- Модальное окно редактирования профиля -->
<div id="editModal" class="modal">
    <div class="modal-content">
        <h3><i class="fas fa-edit"></i> Редактировать профиль</h3>
        <form action="${pageContext.request.contextPath}/profile" method="post" id="editForm">
            <input type="hidden" name="action" value="update">
            <textarea name="bio" rows="3" placeholder="О себе..."><%= profileUser.getBio() != null ? profileUser.getBio() : "" %></textarea>
            <input type="text" name="city" placeholder="Город" value="<%= profileUser.getCity() != null ? profileUser.getCity() : "" %>">
            <input type="number" name="age" placeholder="Возраст" value="<%= profileUser.getAge() > 0 ? profileUser.getAge() : "" %>">
            <div class="modal-buttons">
                <button type="submit" class="btn-primary">Сохранить</button>
                <button type="button" class="btn-secondary" onclick="closeEditModal()">Отмена</button>
            </div>
        </form>
    </div>
</div>

<!-- Модальное окно изменения ника -->
<div id="nameModal" class="modal">
    <div class="modal-content">
        <h3><i class="fas fa-user-tag"></i> Изменить никнейм</h3>
        <form action="${pageContext.request.contextPath}/profile" method="post">
            <input type="hidden" name="action" value="updateName">
            <input type="text" name="username" placeholder="Новый никнейм" value="<%= profileUser.getUsername() %>" required minlength="3" maxlength="50">
            <div class="modal-buttons">
                <button type="submit" class="btn-primary">Сохранить</button>
                <button type="button" class="btn-secondary" onclick="closeNameModal()">Отмена</button>
            </div>
        </form>
    </div>
</div>

<!-- Модальное окно загрузки аватара -->
<div id="avatarModal" class="modal">
    <div class="modal-content">
        <h3><i class="fas fa-camera"></i> Загрузить аватар</h3>
        <form action="${pageContext.request.contextPath}/profile" method="post" enctype="multipart/form-data">
            <input type="hidden" name="action" value="updateAvatar">
            <input type="file" name="avatar" accept="image/jpeg,image/png,image/gif" required>
            <div style="margin-top: 1rem; display: flex; gap: 1rem;">
                <button type="submit" class="btn-primary">Загрузить</button>
                <button type="button" class="btn-secondary" onclick="closeAvatarModal()">Отмена</button>
            </div>
        </form>
        <% if (profileUser.getAvatarPath() != null && !profileUser.getAvatarPath().isEmpty()) { %>
        <hr style="margin: 1rem 0; border-color: rgba(255,255,255,0.1);">
        <form action="${pageContext.request.contextPath}/profile" method="post">
            <input type="hidden" name="action" value="deleteAvatar">
            <button type="submit" class="btn-secondary" style="background: rgba(239,68,68,0.3); border: 1px solid #ef4444; width: 100%;">Удалить аватар</button>
        </form>
        <% } %>
    </div>
</div>

<!-- Модальное окно редактирования команды в профиле -->
<div id="editTeamModal" class="modal">
    <div class="modal-content">
        <h3><i class="fas fa-edit"></i> Редактировать команду</h3>
        <form action="${pageContext.request.contextPath}/edit-team" method="post">
            <input type="hidden" name="teamId" id="editTeamId">
            <input type="text" name="name" id="editTeamName" placeholder="Название команды" required>
            <textarea name="description" id="editTeamDescription" rows="3" placeholder="Описание команды"></textarea>
            <div class="modal-buttons">
                <button type="submit" class="btn-primary">Сохранить</button>
                <button type="button" class="btn-secondary" onclick="closeProfileEditTeamModal()">Отмена</button>
            </div>
        </form>
    </div>
</div>

<script>
    document.querySelectorAll('.circle').forEach((circle, i) => {
        circle.style.animationDuration = `${15 + i * 5}s`;
    });

    function openEditModal() {
        document.getElementById('editModal').style.display = 'flex';
    }

    function closeEditModal() {
        document.getElementById('editModal').style.display = 'none';
    }

    function openNameModal() {
        document.getElementById('nameModal').style.display = 'flex';
    }

    function closeNameModal() {
        document.getElementById('nameModal').style.display = 'none';
    }

    function openAvatarModal() {
        document.getElementById('avatarModal').style.display = 'flex';
    }

    function closeAvatarModal() {
        document.getElementById('avatarModal').style.display = 'none';
    }

    function openProfileEditTeamModal(teamId, name, description) {
        document.getElementById('editTeamId').value = teamId;
        document.getElementById('editTeamName').value = name;
        document.getElementById('editTeamDescription').value = description;
        document.getElementById('editTeamModal').style.display = 'flex';
    }

    function closeProfileEditTeamModal() {
        document.getElementById('editTeamModal').style.display = 'none';
    }

    // Закрытие модальных окон по клику вне области
    window.onclick = function(event) {
        if (event.target.classList.contains('modal')) {
            event.target.style.display = 'none';
        }
    }
</script>

</body>
</html>