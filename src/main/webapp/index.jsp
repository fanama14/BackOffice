<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>BackOffice - Gestion Hotel</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            display: flex;
            justify-content: center;
            align-items: center;
        }
        .container {
            background: white;
            padding: 40px;
            border-radius: 10px;
            box-shadow: 0 10px 25px rgba(0,0,0,0.2);
            max-width: 500px;
            width: 90%;
            text-align: center;
        }
        h1 { color: #667eea; margin-bottom: 20px; }
        .menu a {
            display: block;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 15px 20px;
            border-radius: 5px;
            text-decoration: none;
            margin: 10px 0;
            font-weight: 600;
            transition: transform 0.2s;
        }
        .menu a:hover {
            transform: translateY(-2px);
            box-shadow: 0 5px 15px rgba(102, 126, 234, 0.4);
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>Gestion Hotel</h1>
        <div class="menu">
            <a href="${pageContext.request.contextPath}/reservation/form">Formulaire de Reservation</a>
            <a href="${pageContext.request.contextPath}/vehicule/list">Gestion des Vehicules</a>
        </div>
    </div>
</body>
</html>
