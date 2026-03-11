<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.LinkedHashMap" %>
<%@ page import="java.util.Map" %>
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
        
        /* Groupe card */
        .groupe-card {
            border: 2px solid #e0e0e0;
            border-radius: 10px;
            margin-bottom: 20px;
            overflow: hidden;
        }
        .groupe-header {
            padding: 15px 20px;
            display: flex;
            flex-wrap: wrap;
            align-items: center;
            gap: 25px;
        }
        .groupe-header-assigned {
            background: linear-gradient(135deg, #e8f5e9 0%, #c8e6c9 100%);
            border-bottom: 2px solid #a5d6a7;
        }
        .groupe-header-pending {
            background: linear-gradient(135deg, #fff3e0 0%, #ffe0b2 100%);
            border-bottom: 2px solid #ffcc80;
        }
        .groupe-title {
            font-size: 18px;
            font-weight: 700;
            color: #333;
        }
        .groupe-info {
            display: flex;
            align-items: center;
            gap: 6px;
            font-size: 14px;
            color: #555;
        }
        .groupe-info strong {
            color: #333;
        }
        .vehicule-tag {
            display: inline-flex;
            align-items: center;
            gap: 6px;
            padding: 6px 14px;
            border-radius: 20px;
            font-size: 14px;
            font-weight: 700;
        }
        .vehicule-tag-assigned {
            background: #2e7d32;
            color: white;
        }
        .vehicule-tag-pending {
            background: #e65100;
            color: white;
        }
        .horaire-tag {
            display: inline-flex;
            align-items: center;
            gap: 4px;
            padding: 4px 10px;
            background: white;
            border-radius: 5px;
            font-family: 'Courier New', monospace;
            font-size: 13px;
            color: #333;
            border: 1px solid #ccc;
        }
        
        /* Table dans la carte */
        .groupe-card table {
            width: 100%;
            border-collapse: collapse;
        }
        .groupe-card th, .groupe-card td {
            padding: 10px 15px;
            text-align: left;
            border-bottom: 1px solid #eee;
        }
        .groupe-card th {
            background: #f5f5f5;
            font-weight: 600;
            color: #333;
            font-size: 12px;
            text-transform: uppercase;
        }
        .groupe-card tr:last-child td {
            border-bottom: none;
        }
        .groupe-card tr:hover {
            background: #fafafa;
        }
        .time-display {
            font-family: 'Courier New', monospace;
            font-size: 13px;
        }
        
        /* Responsive */
        @media (max-width: 1200px) {
            .container {
                padding: 15px;
            }
            .groupe-card th, .groupe-card td {
                padding: 8px 10px;
                font-size: 13px;
            }
            .groupe-header {
                gap: 10px;
            }
        }
    </style>
</head>
<body>
    <div class="container">
        <a href="${pageContext.request.contextPath}/" class="back-link">&larr; Retour au menu</a>
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
                <span>Temps d'attente a&eacute;roport:</span>
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
                    <label for="dateDebut">Date d&eacute;but</label>
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
        
        <!-- Liste des réservations par groupe/véhicule -->
        <%
            List<Reservation> reservations = (List<Reservation>) request.getAttribute("reservations");
            SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy");
            SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm");
            SimpleDateFormat sdfDateTime = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        %>
        
        <% if (reservations != null && !reservations.isEmpty()) { %>
        <%
            // Regrouper les réservations par groupeId (ordre conservé)
            LinkedHashMap<Integer, List<Reservation>> groupesMap = new LinkedHashMap<>();
            for (Reservation r : reservations) {
                int gid = r.getGroupeId();
                if (!groupesMap.containsKey(gid)) {
                    groupesMap.put(gid, new ArrayList<>());
                }
                groupesMap.get(gid).add(r);
            }
        %>
        
        <% for (Map.Entry<Integer, List<Reservation>> entry : groupesMap.entrySet()) {
            int groupeId = entry.getKey();
            List<Reservation> groupe = entry.getValue();
            Reservation first = groupe.get(0);
            boolean hasVehicule = first.getVehiculeReference() != null;
            
            // Total passagers du groupe
            int totalPassagers = 0;
            for (Reservation rr : groupe) {
                totalPassagers += rr.getNombrePassager();
            }
        %>
        <div class="groupe-card">
            <!-- En-tête du groupe : véhicule + horaires -->
            <div class="groupe-header <%= hasVehicule ? "groupe-header-assigned" : "groupe-header-pending" %>">
                <span class="groupe-title">Groupe <%= groupeId %></span>
                
                <% if (hasVehicule) { %>
                    <span class="vehicule-tag vehicule-tag-assigned">
                        <%= first.getVehiculeReference() %> &mdash; <%= first.getVehiculeNombrePlace() %> places (<%= first.getVehiculeTypeCarburant() %>)
                    </span>
                <% } else { %>
                    <span class="vehicule-tag vehicule-tag-pending">Aucun v&eacute;hicule disponible</span>
                <% } %>
                
                <div class="groupe-info">
                    <strong>Passagers:</strong> <%= totalPassagers %>
                    <% if (hasVehicule) { %>
                        / <%= first.getVehiculeNombrePlace() %> places
                    <% } %>
                </div>
                
                <% if (first.getHeureDepartAeroport() != null) { %>
                <div class="groupe-info">
                    <strong>D&eacute;part:</strong>
                    <span class="horaire-tag"><%= sdfDateTime.format(first.getHeureDepartAeroport()) %></span>
                </div>
                <% } %>
                
                <% if (first.getHeureRetourAeroport() != null) { %>
                <div class="groupe-info">
                    <strong>Retour:</strong>
                    <span class="horaire-tag"><%= sdfDateTime.format(first.getHeureRetourAeroport()) %></span>
                </div>
                <% } %>
            </div>
            
            <!-- Tableau des réservations du groupe -->
            <table>
                <thead>
                    <tr>
                        <th>Ordre</th>
                        <th>R&eacute;servation</th>
                        <th>Client</th>
                        <th>Nb Personnes</th>
                        <th>Date/Heure Arriv&eacute;e</th>
                        <th>H&ocirc;tel</th>
                        <th>Distance (aller-retour)</th>
                    </tr>
                </thead>
                <tbody>
                    <% for (Reservation r : groupe) { %>
                    <tr>
                        <td>
                            <strong><%= r.getOrdreLivraison() %></strong>
                            <% if (r.getOrdreLivraison() == 1) { %>
                                <span style="color:#2e7d32; font-size:11px;"> (1er)</span>
                            <% } %>
                        </td>
                        <td><%= r.getId() %></td>
                        <td><%= r.getClientId() %></td>
                        <td><strong><%= r.getNombrePassager() %></strong></td>
                        <td class="time-display"><%= sdfDateTime.format(r.getDateArrivee()) %></td>
                        <td><%= r.getHotelNom() != null ? r.getHotelNom() : "-" %></td>
                        <td><%= r.getDistanceKm() > 0 ? (r.getDistanceKm() * 2) + " km" : "-" %></td>
                    </tr>
                    <% } %>
                </tbody>
            </table>
        </div>
        <% } %>
        
        <% } else { %>
            <p style="text-align: center; color: #666; padding: 40px;">
                Aucune r&eacute;servation trouv&eacute;e pour cette p&eacute;riode.
            </p>
        <% } %>
    </div>
</body>
</html>
