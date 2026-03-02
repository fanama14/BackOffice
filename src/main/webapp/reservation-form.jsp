<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="com.backoffice.model.Hotel" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Nouvelle Réservation</title>
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
            max-width: 550px;
            width: 90%;
        }
        h1 {
            color: #667eea;
            margin-bottom: 25px;
            text-align: center;
        }
        .error {
            background: #ffe0e0;
            color: #c00;
            padding: 12px;
            border-radius: 5px;
            margin-bottom: 20px;
            border-left: 4px solid #c00;
        }
        .success {
            background: #e0ffe0;
            color: #2e7d32;
            padding: 12px;
            border-radius: 5px;
            margin-bottom: 20px;
            border-left: 4px solid #2e7d32;
        }
        .form-group {
            margin-bottom: 18px;
            display: flex;
            flex-direction: column;
        }
        label {
            margin-bottom: 6px;
            color: #555;
            font-weight: 600;
        }
        input, select {
            padding: 12px;
            border: 2px solid #e0e0e0;
            border-radius: 5px;
            font-size: 14px;
            transition: border-color 0.3s;
        }
        input:focus, select:focus {
            outline: none;
            border-color: #667eea;
        }
        button {
            width: 100%;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 14px;
            border: none;
            border-radius: 5px;
            font-size: 16px;
            font-weight: 600;
            cursor: pointer;
            margin-top: 10px;
            transition: transform 0.2s;
        }
        button:hover {
            transform: translateY(-2px);
            box-shadow: 0 5px 15px rgba(102, 126, 234, 0.4);
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>Nouvelle Reservation</h1>

        <% if (request.getAttribute("error") != null) { %>
            <div class="error"><%= request.getAttribute("error") %></div>
        <% } %>

        <% if (request.getAttribute("success") != null) { %>
            <div class="success"><%= request.getAttribute("success") %></div>
        <% } %>

        <form action="${pageContext.request.contextPath}/reservation/save" method="POST">
            <div class="form-group">
                <label for="clientId">Nom du client</label>
                <input type="text" id="clientId" name="clientId" 
                       placeholder="Ex: Jean Dupont" required>
            </div>

            <div class="form-group">
                <label for="nombrePassager">Nombre de passagers</label>
                <input type="number" id="nombrePassager" name="nombrePassager" 
                       min="1" max="50" value="1" required>
            </div>

            <div class="form-group">
                <label for="dateArrivee">Date d'arrivee</label>
                <input type="datetime-local" id="dateArrivee" name="dateArrivee" required>
            </div>

            <div class="form-group">
                <label for="hotelId">Hotel</label>
                <select id="hotelId" name="hotelId" required>
                    <option value="">-- Selectionnez un hotel --</option>
                    <%
                        List<Hotel> hotels = (List<Hotel>) request.getAttribute("hotels");
                        if (hotels != null) {
                            for (Hotel hotel : hotels) {
                    %>
                        <option value="<%= hotel.getId() %>">
                            <%= hotel.getNom() %> - <%= hotel.getVille() %>
                        </option>
                    <%
                            }
                        }
                    %>
                </select>
            </div>

            <button type="submit">Reserver</button>
        </form>
    </div>
</body>
</html>
