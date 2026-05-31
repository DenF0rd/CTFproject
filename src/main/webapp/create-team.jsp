<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    if (session.getAttribute("userId") == null) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }
    int contestId = Integer.parseInt(request.getParameter("contestId"));
%>
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Создать команду - CTF Platform</title>
    <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600;700;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }

        body {
            font-family: 'Poppins', sans-serif;
            background: linear-gradient(135deg, #0f0c29 0%, #302b63 50%, #24243e 100%);
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
            position: relative;
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

        .container {
            position: relative;
            z-index: 1;
            max-width: 500px;
            width: 90%;
            margin: 2rem auto;
        }

        .card {
            background: rgba(255, 255, 255, 0.05);
            backdrop-filter: blur(20px);
            border-radius: 28px;
            padding: 2rem;
            border: 1px solid rgba(139, 92, 246, 0.3);
        }

        h2 {
            margin-bottom: 1.5rem;
            text-align: center;
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

        input, textarea {
            width: 100%;
            padding: 12px 16px;
            background: rgba(255, 255, 255, 0.08);
            border: 1px solid rgba(139, 92, 246, 0.3);
            border-radius: 16px;
            color: white;
            font-size: 1rem;
            font-family: inherit;
        }

        input:focus, textarea:focus {
            outline: none;
            border-color: #8b5cf6;
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

        .btn-secondary {
            background: rgba(255, 255, 255, 0.1);
            margin-top: 1rem;
        }

        .error {
            background: rgba(239, 68, 68, 0.2);
            border-left: 3px solid #ef4444;
            padding: 0.75rem;
            border-radius: 12px;
            margin-bottom: 1rem;
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

<div class="container">
    <div class="card">
        <h2><i class="fas fa-users"></i> Создать команду</h2>

        <% if (request.getAttribute("error") != null) { %>
        <div class="error"><%= request.getAttribute("error") %></div>
        <% } %>

        <form action="${pageContext.request.contextPath}/create-team" method="post">
            <input type="hidden" name="contestId" value="<%= contestId %>">

            <div class="form-group">
                <label>Название команды</label>
                <input type="text" name="name" placeholder="Введите название команды" required>
            </div>

            <div class="form-group">
                <label>Описание (необязательно)</label>
                <textarea name="description" rows="3" placeholder="Расскажите о команде..."></textarea>
            </div>

            <button type="submit" class="btn"><i class="fas fa-check"></i> Создать команду</button>
        </form>

        <button class="btn btn-secondary" onclick="location.href='${pageContext.request.contextPath}/contest?id=<%= contestId %>'">
            <i class="fas fa-arrow-left"></i> Назад
        </button>
    </div>
</div>

<script>
    document.querySelectorAll('.circle').forEach((circle, i) => {
        circle.style.animationDuration = `${15 + i * 5}s`;
    });
</script>

</body>
</html>