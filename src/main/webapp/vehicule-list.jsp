<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="com.backoffice.model.Vehicule" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Liste des Véhicules</title>
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
            max-width: 1200px;
            margin: 0 auto;
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
        .filter-group input, .filter-group select {
            padding: 10px;
            border: 2px solid #e0e0e0;
            border-radius: 5px;
            font-size: 14px;
        }
        .filter-group input:focus, .filter-group select:focus {
            outline: none;
            border-color: #667eea;
        }
        .filter-buttons {
            display: flex;
            gap: 10px;
        }
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
        .btn-secondary {
            background: #6c757d;
            color: white;
        }
        .btn-success {
            background: #28a745;
            color: white;
        }
        .btn-warning {
            background: #ffc107;
            color: #333;
        }
        .btn-danger {
            background: #dc3545;
            color: white;
        }
        .btn-sm {
            padding: 6px 12px;
            font-size: 12px;
        }
        
        /* Actions */
        .top-actions {
            margin-bottom: 20px;
            display: flex;
            justify-content: flex-end;
        }
        
        /* Table */
        table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 10px;
        }
        th, td {
            padding: 12px 15px;
            text-align: left;
            border-bottom: 1px solid #e0e0e0;
        }
        th {
            background: #667eea;
            color: white;
            font-weight: 600;
        }
        tr:hover {
            background: #f8f9fa;
        }
        .actions {
            display: flex;
            gap: 8px;
        }
        .badge {
            padding: 4px 10px;
            border-radius: 15px;
            font-size: 12px;
            font-weight: 600;
        }
        .badge-diesel { background: #343a40; color: white; }
        .badge-essence { background: #17a2b8; color: white; }
        .badge-hybride { background: #28a745; color: white; }
        .badge-electrique { background: #6610f2; color: white; }
        
        .empty-state {
            text-align: center;
            padding: 40px;
            color: #666;
        }
        
        /* Responsive */
        @media (max-width: 768px) {
            .filters form {
                flex-direction: column;
            }
            .filter-group {
                width: 100%;
            }
            table {
                font-size: 12px;
            }
            th, td {
                padding: 8px;
            }
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>Gestion des Véhicules</h1>

        <% if (request.getAttribute("error") != null) { %>
            <div class="error"><%= request.getAttribute("error") %></div>
        <% } %>

        <% if (request.getAttribute("success") != null) { %>
            <div class="success"><%= request.getAttribute("success") %></div>
        <% } %>

        <!-- Filtres et recherche -->
        <div class="filters">
            <form action="${pageContext.request.contextPath}/vehicule/list" method="GET">
                <div class="filter-group">
                    <label for="search">Recherche (référence)</label>
                    <input type="text" id="search" name="search" 
                           placeholder="Ex: VH-001"
                           value="<%= request.getAttribute("search") != null ? request.getAttribute("search") : "" %>">
                </div>
                
                <div class="filter-group">
                    <label for="typeCarburant">Type de carburant</label>
                    <select id="typeCarburant" name="typeCarburant">
                        <option value="">-- Tous --</option>
                        <option value="D" <%= "D".equals(request.getAttribute("typeCarburant")) ? "selected" : "" %>>Diesel</option>
                        <option value="ES" <%= "ES".equals(request.getAttribute("typeCarburant")) ? "selected" : "" %>>Essence</option>
                        <option value="H" <%= "H".equals(request.getAttribute("typeCarburant")) ? "selected" : "" %>>Hybride</option>
                        <option value="EL" <%= "EL".equals(request.getAttribute("typeCarburant")) ? "selected" : "" %>>Électrique</option>
                    </select>
                </div>
                
                <div class="filter-group">
                    <label for="nombrePlaceMin">Places (min)</label>
                    <input type="number" id="nombrePlaceMin" name="nombrePlaceMin" min="1" 
                           placeholder="Min"
                           value="<%= request.getAttribute("nombrePlaceMin") != null ? request.getAttribute("nombrePlaceMin") : "" %>">
                </div>
                
                <div class="filter-group">
                    <label for="nombrePlaceMax">Places (max)</label>
                    <input type="number" id="nombrePlaceMax" name="nombrePlaceMax" min="1"
                           placeholder="Max"
                           value="<%= request.getAttribute("nombrePlaceMax") != null ? request.getAttribute("nombrePlaceMax") : "" %>">
                </div>
                
                <div class="filter-buttons">
                    <button type="submit" class="btn btn-primary">Filtrer</button>
                    <a href="${pageContext.request.contextPath}/vehicule/list" class="btn btn-secondary">Réinitialiser</a>
                </div>
            </form>
        </div>

        <!-- Actions -->
        <div class="top-actions">
            <a href="${pageContext.request.contextPath}/vehicule/form" class="btn btn-success">+ Ajouter un véhicule</a>
        </div>

        <!-- Liste des véhicules -->
        <%
            List<Vehicule> vehicules = (List<Vehicule>) request.getAttribute("vehicules");
            if (vehicules != null && !vehicules.isEmpty()) {
        %>
        <table>
            <thead>
                <tr>
                    <th>ID</th>
                    <th>Référence</th>
                    <th>Nombre de places</th>
                    <th>Type de carburant</th>
                    <th>Actions</th>
                </tr>
            </thead>
            <tbody>
                <% for (Vehicule v : vehicules) { 
                    String badgeClass = "";
                    switch(v.getTypeCarburant()) {
                        case "D": badgeClass = "badge-diesel"; break;
                        case "ES": badgeClass = "badge-essence"; break;
                        case "H": badgeClass = "badge-hybride"; break;
                        case "EL": badgeClass = "badge-electrique"; break;
                    }
                %>
                <tr>
                    <td><%= v.getId() %></td>
                    <td><strong><%= v.getReference() %></strong></td>
                    <td><%= v.getNombrePlace() %></td>
                    <td><span class="badge <%= badgeClass %>"><%= v.getTypeCarburantLibelle() %></span></td>
                    <td class="actions">
                        <a href="${pageContext.request.contextPath}/vehicule/edit?id=<%= v.getId() %>" 
                           class="btn btn-warning btn-sm">Modifier</a>
                        <a href="${pageContext.request.contextPath}/vehicule/delete?id=<%= v.getId() %>" 
                           class="btn btn-danger btn-sm"
                           onclick="return confirm('Êtes-vous sûr de vouloir supprimer ce véhicule ?');">Supprimer</a>
                    </td>
                </tr>
                <% } %>
            </tbody>
        </table>
        <% } else { %>
        <div class="empty-state">
            <p>Aucun véhicule trouvé.</p>
            <p style="margin-top: 10px;">
                <a href="${pageContext.request.contextPath}/vehicule/form" class="btn btn-primary">Ajouter votre premier véhicule</a>
            </p>
        </div>
        <% } %>
    </div>
</body>
</html>
