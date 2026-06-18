<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Подтверждение email - CTF Platform</title>
    <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600;700;800&display=swap" rel="stylesheet">
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }

        body {
            font-family: 'Poppins', sans-serif;
            background: linear-gradient(135deg, #0f0c29 0%, #302b63 50%, #24243e 100%);
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
        }

        .container {
            max-width: 450px;
            width: 90%;
            margin: 2rem auto;
        }

        .card {
            background: rgba(255, 255, 255, 0.05);
            backdrop-filter: blur(20px);
            border-radius: 28px;
            padding: 2.5rem;
            border: 1px solid rgba(139, 92, 246, 0.3);
        }

        h2 {
            text-align: center;
            margin-bottom: 0.5rem;
        }

        .subtitle {
            text-align: center;
            color: rgba(255, 255, 255, 0.6);
            font-size: 0.9rem;
            margin-bottom: 1.5rem;
        }

        .email-display {
            background: rgba(139, 92, 246, 0.2);
            padding: 0.75rem;
            border-radius: 12px;
            text-align: center;
            margin-bottom: 1.5rem;
            font-weight: 500;
            color: #c084fc;
        }

        .form-group {
            margin-bottom: 1.25rem;
        }

        label {
            display: block;
            margin-bottom: 0.5rem;
            color: rgba(255, 255, 255, 0.8);
            font-weight: 500;
        }

        input {
            width: 100%;
            padding: 14px 18px;
            background: rgba(255, 255, 255, 0.08);
            border: 1px solid rgba(139, 92, 246, 0.3);
            border-radius: 16px;
            color: white;
            font-size: 1.2rem;
            text-align: center;
            letter-spacing: 10px;
            font-weight: 600;
        }

        input:focus {
            outline: none;
            border-color: #8b5cf6;
            box-shadow: 0 0 0 4px rgba(139, 92, 246, 0.2);
        }

        .btn {
            width: 100%;
            padding: 12px;
            background: linear-gradient(135deg, #8b5cf6, #6366f1);
            border: none;
            border-radius: 16px;
            color: white;
            font-weight: 600;
            font-size: 1rem;
            cursor: pointer;
            transition: all 0.3s;
        }

        .btn:hover {
            transform: translateY(-2px);
            box-shadow: 0 0 15px rgba(139, 92, 246, 0.5);
        }

        .error {
            background: rgba(239, 68, 68, 0.2);
            border-left: 3px solid #ef4444;
            padding: 0.75rem;
            border-radius: 12px;
            margin-bottom: 1.5rem;
            color: #fca5a5;
        }

        .success {
            background: rgba(16, 185, 129, 0.2);
            border-left: 3px solid #10b981;
            padding: 0.75rem;
            border-radius: 12px;
            margin-bottom: 1.5rem;
            color: #6ee7b7;
        }

        .resend-link {
            text-align: center;
            margin-top: 1.5rem;
        }

        .resend-link a {
            color: #a78bfa;
            text-decoration: none;
            font-size: 0.85rem;
        }

        .resend-link a:hover {
            text-decoration: underline;
        }

        .login-link {
            text-align: center;
            margin-top: 1rem;
            padding-top: 1rem;
            border-top: 1px solid rgba(255,255,255,0.1);
        }

        .login-link a {
            color: #a78bfa;
            text-decoration: none;
        }
    </style>
</head>
<body>

<div class="container">
    <div class="card">
        <h2>🔐 Подтверждение email</h2>
        <p class="subtitle">Введите 6-значный код, отправленный на вашу почту</p>

        <div class="email-display">
            📧 <%= request.getAttribute("email") != null ? request.getAttribute("email") : "" %>
        </div>

        <% if (request.getAttribute("error") != null) { %>
        <div class="error">❌ <%= request.getAttribute("error") %></div>
        <% } %>

        <% if (request.getAttribute("message") != null) { %>
        <div class="success">✅ <%= request.getAttribute("message") %></div>
        <% } %>

        <form action="${pageContext.request.contextPath}/verify-code" method="post">
            <input type="hidden" name="email" value="<%= request.getAttribute("email") != null ? request.getAttribute("email") : "" %>">

            <div class="form-group">
                <label>Код подтверждения</label>
                <input type="text" name="code" placeholder="______" maxlength="6" pattern="[0-9]{6}" required autofocus>
            </div>

            <button type="submit" class="btn">Подтвердить</button>
        </form>

        <div class="resend-link">
            <a href="${pageContext.request.contextPath}/resend-code?email=<%= request.getAttribute("email") != null ? request.getAttribute("email") : "" %>">
                Отправить код повторно
            </a>
        </div>

        <div class="login-link">
            <a href="${pageContext.request.contextPath}/login">Перейти на страницу входа</a>
        </div>
    </div>
</div>

<script>
    // Автоматический переход на следующее поле
    document.querySelector('input[name="code"]').addEventListener('input', function() {
        if (this.value.length === 6) {
            // Можно автоматически отправить форму
            // this.form.submit();
        }
    });
</script>

</body>
</html>