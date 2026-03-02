<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.backoffice.model.Vehicule" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%= request.getAttribute("isEdit") != null && (Boolean)request.getAttribute("isEdit") ? "Modifier" : "Ajouter" %> un Véhicule</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            display: flex;
            justify-content: center;
            align-items: center;
            padding: 20px;
        }
        .container {
            background: white;
            padding: 40px;
            border-radius: 10px;
            box-shadow: 0 10px 25px rgba(0,0,0,0.2);
            max-width: 550px;
            width: 100%;
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
        .btn {
            width: 100%;
            padding: 14px;
            border: none;
            border-radius: 5px;
            font-size: 16px;
            font-weight: 600;
            cursor: pointer;
            margin-top: 10px;
            transition: transform 0.2s;
            text-decoration: none;
            display: block;
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
            margin-top: 15px;
        }
        
        .carburant-options {
            display: grid;
            grid-template-columns: repeat(2, 1fr);
            gap: 10px;
            margin-top: 5px;
        }
        .carburant-option {
            display: flex;
            align-items: center;
            padding: 12px;
            border: 2px solid #e0e0e0;
            border-radius: 5px;
            cursor: pointer;
            transition: all 0.3s;
        }
        .carburant-option:hover {
            border-color: #667eea;
        }
        .carburant-option input[type="radio"] {
            width: auto;
            margin-right: 10px;
        }
        .carburant-option.selected {
            border-color: #667eea;
            background: #f0f4ff;
        }
        .carburant-icon {
            margin-left: auto;
            font-size: 18px;
        }
    </style>
</head>
<body>
    <div class="container">
        <% 
            Boolean isEdit = (Boolean) request.getAttribute("isEdit");
            Vehicule vehicule = (Vehicule) request.getAttribute("vehicule");
            boolean editing = isEdit != null && isEdit;
        %>
        
        <h1><%= editing ? "Modifier le Véhicule" : "Nouveau Véhicule" %></h1>

        <% if (request.getAttribute("error") != null) { %>
            <div class="error"><%= request.getAttribute("error") %></div>
        <% } %>

        <% if (request.getAttribute("success") != null) { %>
            <div class="success"><%= request.getAttribute("success") %></div>
        <% } %>

        <form action="${pageContext.request.contextPath}/vehicule/save" method="POST">
            <% if (editing && vehicule != null) { %>
                <input type="hidden" name="id" value="<%= vehicule.getId() %>">
            <% } else { %>
                <input type="hidden" name="id" value="">
            <% } %>
            
            <div class="form-group">
                <label for="reference">Référence du véhicule</label>
                <input type="text" id="reference" name="reference" 
                       placeholder="Ex: VH-001" required
                       value="<%= vehicule != null ? vehicule.getReference() : "" %>">
            </div>

            <div class="form-group">
                <label for="nombrePlace">Nombre de places</label>
                <input type="number" id="nombrePlace" name="nombrePlace" 
                       min="1" max="100" required
                       value="<%= vehicule != null ? vehicule.getNombrePlace() : "5" %>">
            </div>

            <div class="form-group">
                <label>Type de carburant</label>
                <div class="carburant-options">
                    <label class="carburant-option">
                        <input type="radio" name="typeCarburant" value="ES" 
                               <%= vehicule == null || "ES".equals(vehicule.getTypeCarburant()) ? "checked" : "" %>>
                        Essence
                        <span class="carburant-icon">⛽</span>
                    </label>
                    <label class="carburant-option">
                        <input type="radio" name="typeCarburant" value="D"
                               <%= vehicule != null && "D".equals(vehicule.getTypeCarburant()) ? "checked" : "" %>>
                        Diesel
                        <span class="carburant-icon">🛢️</span>
                    </label>
                    <label class="carburant-option">
                        <input type="radio" name="typeCarburant" value="H"
                               <%= vehicule != null && "H".equals(vehicule.getTypeCarburant()) ? "checked" : "" %>>
                        Hybride
                        <span class="carburant-icon">🔋</span>
                    </label>
                    <label class="carburant-option">
                        <input type="radio" name="typeCarburant" value="EL"
                               <%= vehicule != null && "EL".equals(vehicule.getTypeCarburant()) ? "checked" : "" %>>
                        Électrique
                        <span class="carburant-icon">⚡</span>
                    </label>
                </div>
            </div>

            <button type="submit" class="btn btn-primary">
                <%= editing ? "Enregistrer les modifications" : "Ajouter le véhicule" %>
            </button>
        </form>
        
        <a href="${pageContext.request.contextPath}/vehicule/list" class="btn btn-secondary">
            ← Retour à la liste
        </a>
    </div>

    <script>
        // Mise en surbrillance de l'option sélectionnée
        document.querySelectorAll('.carburant-option input').forEach(function(radio) {
            radio.addEventListener('change', function() {
                document.querySelectorAll('.carburant-option').forEach(function(opt) {
                    opt.classList.remove('selected');
                });
                if (this.checked) {
                    this.closest('.carburant-option').classList.add('selected');
                }
            });
            // Initialisation
            if (radio.checked) {
                radio.closest('.carburant-option').classList.add('selected');
            }
        });
    </script>
</body>
</html>
