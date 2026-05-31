<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Регистрация успешна - CTF Platform</title>
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
            background: rgba(99, 102, 241, 0.15);
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
            max-width: 500px;
            margin: 1rem;
        }

        .auth-card {
            background: rgba(255, 255, 255, 0.05);
            backdrop-filter: blur(20px);
            border-radius: 32px;
            padding: 2.5rem;
            border: 1px solid rgba(255, 255, 255, 0.1);
            transition: transform 0.3s ease;
            text-align: center;
        }

        .auth-card:hover {
            transform: translateY(-5px);
        }

        .auth-card h2 {
            font-size: 1.75rem;
            margin: 0.5rem 0;
            color: #10b981;
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

        .info-box {
            background: rgba(255, 255, 255, 0.05);
            padding: 1rem;
            border-radius: 16px;
            margin: 1rem 0;
        }

        .info-box .email {
            font-weight: 600;
            color: #a78bfa;
            word-break: break-all;
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
            margin-top: 1.5rem;
            padding-top: 1.5rem;
            border-top: 1px solid rgba(255, 255, 255, 0.1);
        }

        .auth-footer a {
            color: #a78bfa;
            text-decoration: none;
            font-weight: 600;
        }

        .auth-footer a:hover {
            text-decoration: underline;
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
        <div style="font-size: 4rem; margin-bottom: 1rem;">🎉</div>
        <h2>Регистрация успешна!</h2>

        <% if(request.getAttribute("message") != null) { %>
        <div class="success-message">📧 <%= request.getAttribute("message") %></div>
        <% } %>

        <div class="info-box">
            <p style="opacity: 0.7;">Письмо отправлено на адрес:</p>
            <p class="email"><%= request.getAttribute("email") %></p>
            <p style="font-size: 0.75rem; opacity: 0.5; margin-top: 0.5rem;">Ссылка действительна в течение 24 часов</p>
        </div>

        <div style="margin-top: 1rem;">
            <form action="${pageContext.request.contextPath}/resend-verification" method="post">
                <input type="hidden" name="email" value="<%= request.getAttribute("email") %>">
                <button type="submit" class="btn btn-primary">📧 Отправить письмо повторно</button>
            </form>
        </div>

        <div class="auth-footer">
            <a href="${pageContext.request.contextPath}/login">Перейти на страницу входа →</a>
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