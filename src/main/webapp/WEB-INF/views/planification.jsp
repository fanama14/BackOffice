<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="com.backoffice.model.Reservation" %>
<%@ page import="com.backoffice.model.Parametre" %>
<%@ page import="java.text.SimpleDateFormat" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Planification des Transports</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            padding: 20px;
        }
        .container {
            background: white;
            padding: 30px;
            border-radius: 10px;
            box-shadow: 0 10px 25px rgba(0,0,0,0.2);
            max-width: 1400px;
            margin: 0 auto;
        }
        h1 {
            color: #667eea;
            margin-bottom: 25px;
            text-align: center;
        }
        .back-link {
            display: inline-block;
            margin-bottom: 20px;
            color: #667eea;
            text-decoration: none;
            font-weight: 600;
        }
        .back-link:hover {
            text-decoration: underline;
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
        
        /* Filtres */
        .filters {
            background: #f8f9fa;
            padding: 20px;
            border-radius: 8px;
            margin-bottom: 25px;
        }
        .filters form {
            display: flex;
            flex-wrap: wrap;
            gap: 15px;
            align-items: flex-end;
        }
        .filter-group {
            display: flex;
            flex-direction: column;
            min-width: 150px;
        }
        .filter-group label {
            font-size: 12px;
            color: #666;
            margin-bottom: 5px;
            font-weight: 600;
        }
        .filter-group input {
            padding: 10px;
            border: 2px solid #e0e0e0;
            border-radius: 5px;
            font-size: 14px;
        }
        .filter-group input:focus {
            outline: none;
            border-color: #667eea;
        }
        
        /* Paramètres */
        .params-info {
            background: #e3f2fd;
            padding: 15px;
            border-radius: 8px;
            margin-bottom: 20px;
            display: flex;
            gap: 30px;
            flex-wrap: wrap;
        }
        .param-item {
            display: flex;
            align-items: center;
            gap: 8px;
        }
        .param-item strong {
            color: #1565c0;
        }
        
        /* Boutons */
        .btn {
            padding: 10px 20px;
            border: none;
            border-radius: 5px;
            cursor: pointer;
            font-size: 14px;
            font-weight: 600;
            transition: transform 0.2s, box-shadow 0.2s;
            text-decoration: none;
            display: inline-block;
            text-align: center;
        }
        .btn:hover {
            transform: translateY(-2px);
        }
        .btn-primary {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
        }
        .btn-primary:hover {
            box-shadow: 0 5px 15px rgba(102, 126, 234, 0.4);
        }
        .btn-success {
            background: linear-gradient(135deg, #2e7d32 0%, #4caf50 100%);
            color: white;
        }
        .btn-success:hover {
            box-shadow: 0 5px 15px rgba(76, 175, 80, 0.4);
        }
        .btn-danger {
            background: linear-gradient(135deg, #c62828 0%, #e53935 100%);
            color: white;
        }
        .btn-danger:hover {
            box-shadow: 0 5px 15px rgba(229, 57, 53, 0.4);
        }
        .btn-sm {
            padding: 6px 12px;
            font-size: 12px;
        }
        
        /* Table */
        table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 10px;
        }
        th, td {
            padding: 12px 10px;
            text-align: left;
            border-bottom: 1px solid #e0e0e0;
        }
        th {
            background: #f5f5f5;
            font-weight: 600;
            color: #333;
            font-size: 13px;
            text-transform: uppercase;
        }
        tr:hover {
            background: #f9f9f9;
        }
        
        /* États */
        .status-assigned {
            background: #e8f5e9;
        }
        .status-pending {
            background: #fff3e0;
        }
        .vehicule-badge {
            display: inline-block;
            padding: 4px 10px;
            border-radius: 20px;
            font-size: 12px;
            font-weight: 600;
        }
        .vehicule-assigned {
            background: #c8e6c9;
            color: #2e7d32;
        }
        .vehicule-pending {
            background: #ffe0b2;
            color: #e65100;
        }
        .time-display {
            font-family: 'Courier New', monospace;
            font-size: 13px;
        }
        .empty-cell {
            color: #999;
            font-style: italic;
        }
        
        /* Responsive */
        @media (max-width: 1200px) {
            .container {
                padding: 15px;
            }
            table {
                font-size: 13px;
            }
            th, td {
                padding: 8px 5px;
            }
        }
    </style>
</head>
<body>
    <div class="container">
        <a href="${pageContext.request.contextPath}/" class="back-link">← Retour au menu</a>
        <h1>Planification des Transports</h1>
        
        <%
            String error = (String) request.getAttribute("error");
            String success = (String) request.getAttribute("success");
            if (error != null) {
        %>
            <div class="error"><%= error %></div>
        <% } %>
        
        <% if (success != null) { %>
            <div class="success"><%= success %></div>
        <% } %>
        
        <%
            Parametre parametre = (Parametre) request.getAttribute("parametre");
            if (parametre != null) {
        %>
        <div class="params-info">
            <div class="param-item">
                <span>Temps d'attente aéroport:</span>
                <strong><%= parametre.getTempsAttente() %> min</strong>
            </div>
            <div class="param-item">
                <span>Vitesse moyenne:</span>
                <strong><%= parametre.getVitesseMoyenne() %> km/h</strong>
            </div>
        </div>
        <% } %>
        
        <!-- Filtres de date -->
        <div class="filters">
            <form method="get" action="${pageContext.request.contextPath}/planification">
                <div class="filter-group">
                    <label for="dateDebut">Date début</label>
                    <input type="date" id="dateDebut" name="dateDebut" 
                           value="<%= request.getAttribute("dateDebut") != null ? request.getAttribute("dateDebut") : "" %>">
                </div>
                <div class="filter-group">
                    <label for="dateFin">Date fin</label>
                    <input type="date" id="dateFin" name="dateFin" 
                           value="<%= request.getAttribute("dateFin") != null ? request.getAttribute("dateFin") : "" %>">
                </div>
                <button type="submit" class="btn btn-primary">Filtrer</button>
            </form>
        </div>
        
        <!-- Liste des réservations -->
        <%
            List<Reservation> reservations = (List<Reservation>) request.getAttribute("reservations");
            SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy");
            SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm");
            SimpleDateFormat sdfDateTime = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            String dateDebutParam = (String) request.getAttribute("dateDebut");
            String dateFinParam = (String) request.getAttribute("dateFin");
        %>
        
        <% if (reservations != null && !reservations.isEmpty()) { %>
        <table>
            <thead>
                <tr>
                    <th>ID</th>
                    <th>Client</th>
                    <th>Passagers</th>
                    <th>Date Arrivée</th>
                    <th>Hôtel</th>
                    <th>Véhicule</th>
                    <th>Départ Aéroport</th>
                    <th>Retour Aéroport</th>
                    <th>Actions</th>
                </tr>
            </thead>
            <tbody>
                <% for (Reservation r : reservations) { 
                    boolean hasVehicule = r.getIdVehicule() != null;
                %>
                <tr class="<%= hasVehicule ? "status-assigned" : "status-pending" %>">
                    <td><%= r.getId() %></td>
                    <td><%= r.getClientId() %></td>
                    <td><%= r.getNombrePassager() %></td>
                    <td class="time-display"><%= sdfDateTime.format(r.getDateArrivee()) %></td>
                    <td><%= r.getHotelNom() != null ? r.getHotelNom() : "-" %></td>
                    <td>
                        <% if (hasVehicule) { %>
                            <span class="vehicule-badge vehicule-assigned">
                                <%= r.getVehiculeReference() %>
                            </span>
                        <% } else { %>
                            <span class="vehicule-badge vehicule-pending">Non assigné</span>
                        <% } %>
                    </td>
                    <td class="time-display">
                        <% if (r.getHeureDepartAeroport() != null) { %>
                            <%= sdfTime.format(r.getHeureDepartAeroport()) %>
                        <% } else { %>
                            <span class="empty-cell">-</span>
                        <% } %>
                    </td>
                    <td class="time-display">
                        <% if (r.getHeureArriveeAeroport() != null) { %>
                            <%= sdfTime.format(r.getHeureArriveeAeroport()) %>
                        <% } else { %>
                            <span class="empty-cell">-</span>
                        <% } %>
                    </td>
                    <td>
                        <% if (!hasVehicule) { %>
                        <form method="post" action="${pageContext.request.contextPath}/planification/assigner" style="display:inline;">
                            <input type="hidden" name="reservationId" value="<%= r.getId() %>">
                            <input type="hidden" name="dateDebut" value="<%= dateDebutParam != null ? dateDebutParam : "" %>">
                            <input type="hidden" name="dateFin" value="<%= dateFinParam != null ? dateFinParam : "" %>">
                            <button type="submit" class="btn btn-success btn-sm">Assigner véhicule</button>
                        </form>
                        <% } else { %>
                        <form method="post" action="${pageContext.request.contextPath}/planification/retirer" style="display:inline;">
                            <input type="hidden" name="reservationId" value="<%= r.getId() %>">
                            <input type="hidden" name="dateDebut" value="<%= dateDebutParam != null ? dateDebutParam : "" %>">
                            <input type="hidden" name="dateFin" value="<%= dateFinParam != null ? dateFinParam : "" %>">
                            <button type="submit" class="btn btn-danger btn-sm">Retirer</button>
                        </form>
                        <% } %>
                    </td>
                </tr>
                <% } %>
            </tbody>
        </table>
        <% } else { %>
            <p style="text-align: center; color: #666; padding: 40px;">
                Aucune réservation trouvée pour cette période.
            </p>
        <% } %>
    </div>
</body>
</html>
