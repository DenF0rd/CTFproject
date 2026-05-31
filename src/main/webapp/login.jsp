<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Вход - CTF Platform</title>
    <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600;700;800&display=swap" rel="stylesheet">
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
            display: flex;
            align-items: center;
            justify-content: center;
            position: relative;
            overflow-x: hidden;
        }

        /* Анимированный фон с кругами */
        .bg-animation {
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            z-index: 0;
            overflow: hidden;
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

        /* Контейнер формы */
        .auth-container {
            position: relative;
            z-index: 1;
            width: 100%;
            max-width: 450px;
            margin: 1rem;
        }

        .auth-card {
            background: rgba(255, 255, 255, 0.05);
            backdrop-filter: blur(20px);
            border-radius: 32px;
            padding: 2.5rem;
            border: 1px solid rgba(255, 255, 255, 0.1);
            transition: transform 0.3s ease;
        }

        .auth-card:hover {
            transform: translateY(-5px);
        }

        .auth-header {
            text-align: center;
            margin-bottom: 2rem;
        }

        .auth-header .logo {
            font-size: 3rem;
            display: inline-block;
            animation: pulse 2s infinite;
        }

        @keyframes pulse {
            0%, 100% { transform: scale(1); }
            50% { transform: scale(1.05); }
        }

        .auth-header h2 {
            font-size: 1.75rem;
            margin: 0.5rem 0;
            color: white;
        }

        .auth-header p {
            color: rgba(255, 255, 255, 0.6);
        }

        .form-group {
            margin-bottom: 1.25rem;
        }

        .form-group label {
            display: block;
            margin-bottom: 0.5rem;
            font-weight: 500;
            color: rgba(255, 255, 255, 0.8);
        }

        .form-group input {
            width: 100%;
            padding: 14px 18px;
            background: rgba(255, 255, 255, 0.08);
            border: 1px solid rgba(255, 255, 255, 0.15);
            border-radius: 16px;
            color: white;
            font-size: 1rem;
            transition: all 0.3s;
        }

        .form-group input:focus {
            outline: none;
            border-color: #8b5cf6;
            background: rgba(255, 255, 255, 0.12);
            box-shadow: 0 0 0 4px rgba(139, 92, 246, 0.2);
        }

        .form-group input::placeholder {
            color: rgba(255, 255, 255, 0.4);
        }

        .btn {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            gap: 0.5rem;
            padding: 12px 24px;
            border-radius: 16px;
            font-weight: 600;
            text-decoration: none;
            transition: all 0.3s ease;
            cursor: pointer;
            border: none;
            font-size: 0.9rem;
        }

        .btn-primary {
            background: linear-gradient(135deg, #8b5cf6 0%, #6366f1 100%);
            color: white;
            width: 100%;
        }

        .btn-primary:hover {
            transform: translateY(-2px);
            box-shadow: 0 10px 25px -5px rgba(139, 92, 246, 0.5);
        }

        .error-message {
            background: rgba(239, 68, 68, 0.2);
            border-left: 3px solid #ef4444;
            padding: 0.75rem;
            border-radius: 12px;
            margin-bottom: 1.5rem;
            color: #fca5a5;
            font-size: 0.85rem;
        }

        .success-message {
            background: rgba(16, 185, 129, 0.2);
            border-left: 3px solid #10b981;
            padding: 0.75rem;
            border-radius: 12px;
            margin-bottom: 1.5rem;
            color: #6ee7b7;
            font-size: 0.85rem;
        }

        .auth-footer {
            text-align: center;
            margin-top: 2rem;
            padding-top: 1.5rem;
            border-top: 1px solid rgba(255, 255, 255, 0.1);
        }

        .auth-footer p {
            color: rgba(255, 255, 255, 0.6);
            margin-bottom: 0.5rem;
        }

        .auth-footer a {
            color: #a78bfa;
            text-decoration: none;
            font-weight: 600;
        }

        .auth-footer a:hover {
            text-decoration: underline;
        }

        .divider {
            margin: 1.5rem 0;
            text-align: center;
            color: rgba(255, 255, 255, 0.4);
            position: relative;
        }

        .divider::before,
        .divider::after {
            content: '';
            position: absolute;
            top: 50%;
            width: 40%;
            height: 1px;
            background: rgba(255, 255, 255, 0.2);
        }

        .divider::before { left: 0; }
        .divider::after { right: 0; }

        .quick-buttons {
            display: flex;
            gap: 10px;
            margin-top: 10px;
        }

        .quick-btn {
            flex: 1;
            padding: 10px;
            border: none;
            border-radius: 12px;
            font-size: 14px;
            font-weight: 500;
            cursor: pointer;
            transition: all 0.3s;
        }

        .quick-btn.admin {
            background: linear-gradient(135deg, #ef4444, #dc2626);
            color: white;
        }

        .quick-btn.user {
            background: linear-gradient(135deg, #10b981, #059669);
            color: white;
        }

        .quick-btn.test {
            background: linear-gradient(135deg, #f59e0b, #d97706);
            color: white;
        }

        .quick-btn:hover {
            transform: translateY(-2px);
        }

        .warning-note {
            font-size: 11px;
            text-align: center;
            margin-top: 10px;
            color: rgba(255, 255, 255, 0.4);
        }

        @keyframes fadeInUp {
            from { opacity: 0; transform: translateY(30px); }
            to { opacity: 1; transform: translateY(0); }
        }

        .animate-in {
            animation: fadeInUp 0.5s ease forwards;
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

<div class="auth-container">
    <div class="auth-card animate-in">
        <div class="auth-header">
            <div class="logo">🔐</div>
            <h2>Вход в систему</h2>
            <p>Добро пожаловать обратно!</p>
        </div>

        <% if(request.getAttribute("error") != null) { %>
        <div class="error-message">⚠️ <%= request.getAttribute("error") %></div>
        <% } %>

        <% if(request.getAttribute("message") != null) { %>
        <div class="success-message">✅ <%= request.getAttribute("message") %></div>
        <% } %>

        <form action="${pageContext.request.contextPath}/login" method="post">
            <div class="form-group">
                <label>📧 Email</label>
                <input type="email" name="email" placeholder="your@email.com" required autofocus>
            </div>

            <div class="form-group">
                <label>🔒 Пароль</label>
                <input type="password" name="password" placeholder="••••••••" required>
            </div>

            <button type="submit" class="btn btn-primary">Войти →</button>
        </form>

        <div class="divider">ИЛИ</div>

        <form action="${pageContext.request.contextPath}/quick-login" method="post">
            <div class="quick-buttons">
                <button type="submit" name="role" value="admin" class="quick-btn admin">👑 Админ</button>
                <button type="submit" name="role" value="user" class="quick-btn user">👤 Пользователь</button>
            </div>
            <div class="quick-buttons">
                <button type="submit" name="role" value="test" class="quick-btn test">🧪 Тестовый</button>
            </div>
            <div class="warning-note">
                ⚡ Тестовые аккаунты создаются автоматически
            </div>
        </form>

        <div class="auth-footer">
            <p>Нет аккаунта?</p>
            <a href="${pageContext.request.contextPath}/register">Зарегистрироваться →</a>
        </div>
    </div>
</div>

<script>
    document.querySelectorAll('.circle').forEach((circle, i) => {
        circle.style.animationDuration = `${15 + i * 5}s`;
    });
</script>

</body>
</html>