<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Регистрация - CTF Platform</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: 'Poppins', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
            background: linear-gradient(135deg, #0f0c29 0%, #302b63 50%, #24243e 100%);
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
            position: relative;
            overflow-x: hidden;
        }

        @import url('https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600;700;800&display=swap');

        .bg-animation {
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            z-index: 0;
        }

        .circle {
            position: absolute;
            background: rgba(99, 102, 241, 0.1);
            border-radius: 50%;
            animation: float 20s infinite ease-in-out;
        }

        @keyframes float {
            0%, 100% { transform: translateY(0) translateX(0); }
            50% { transform: translateY(-50px) translateX(50px); }
        }

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

        .error-message {
            background: rgba(239, 68, 68, 0.2);
            border-left: 3px solid #ef4444;
            padding: 0.75rem;
            border-radius: 12px;
            margin-bottom: 1.5rem;
            color: #fca5a5;
            font-size: 0.85rem;
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
    <div class="auth-card">
        <div class="auth-header">
            <div class="logo">🚀</div>
            <h2>Создать аккаунт</h2>
            <p>Присоединяйся к CTF сообществу</p>
        </div>

        <% if(request.getAttribute("error") != null) { %>
        <div class="error-message">⚠️ <%= request.getAttribute("error") %></div>
        <% } %>

        <form action="${pageContext.request.contextPath}/register" method="post">
            <div class="form-group">
                <label>📧 Email</label>
                <input type="email" name="email" placeholder="your@email.com" required autofocus>
            </div>

            <div class="form-group">
                <label>🔒 Пароль (мин. 6 символов)</label>
                <input type="password" id="password" name="password" placeholder="••••••••" required minlength="6">
            </div>

            <div class="form-group">
                <label>✓ Подтверждение пароля</label>
                <input type="password" id="confirmPassword" name="confirmPassword" placeholder="••••••••" required>
            </div>

            <div style="margin: 1rem 0;">
                <label style="display: flex; align-items: center; gap: 0.5rem; cursor: pointer; color: rgba(255,255,255,0.7);">
                    <input type="checkbox" required> Я согласен с условиями использования
                </label>
            </div>

            <button type="submit" class="btn btn-primary">Зарегистрироваться</button>
        </form>

        <div class="auth-footer">
            <p>Уже есть аккаунт?</p>
            <a href="${pageContext.request.contextPath}/login">Войти в систему →</a>
        </div>
    </div>
</div>

<script>
    document.querySelectorAll('.circle').forEach((circle, i) => {
        circle.style.animationDuration = `${15 + i * 5}s`;
    });

    document.querySelector('form').addEventListener('submit', function(e) {
        var password = document.getElementById('password').value;
        var confirm = document.getElementById('confirmPassword').value;
        if(password !== confirm) {
            e.preventDefault();
            alert('❌ Пароли не совпадают!');
        }
    });
</script>
</body>
</html>