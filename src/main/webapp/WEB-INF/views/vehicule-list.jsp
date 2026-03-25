<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="com.backoffice.model.Vehicule" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Véhicules · Transport</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@300;400;500;600;700&family=JetBrains+Mono:wght@400;500;700&display=swap" rel="stylesheet">
    <style>
        :root {
            --bg:        #f1f3f8;
            --surface:   #ffffff;
            --card:      #ffffff;
            --card-alt:  #f8f9fc;
            --line:      #e6e9f0;
            --line-2:    #d0d5e2;

            --text:      #0f1523;
            --sub:       #445069;
            --muted:     #8e97b0;

            --accent:    #4f46e5;
            --accent-2:  #6366f1;
            --accent-lo: #eef0ff;
            --accent-dk: #4338ca;

            --ok:        #10b981;
            --ok-lo:     #ecfdf5;
            --ok-line:   #6ee7b7;

            --warn:      #f59e0b;
            --warn-lo:   #fffbeb;
            --warn-line: #fcd34d;

            --danger:    #ef4444;
            --danger-lo: #fff1f2;

            --mono:  'JetBrains Mono', 'Courier New', monospace;
            --sans:  'Plus Jakarta Sans', 'Segoe UI', sans-serif;

            --radius-sm: 8px;
            --radius:    12px;
            --radius-lg: 16px;
            --shadow-sm: 0 1px 3px rgba(15,21,35,.06), 0 1px 2px rgba(15,21,35,.04);
            --shadow:    0 4px 16px rgba(15,21,35,.07), 0 1px 4px rgba(15,21,35,.05);
            --shadow-lg: 0 8px 32px rgba(15,21,35,.10), 0 2px 8px rgba(15,21,35,.06);
        }

        *, *::before, *::after { margin:0; padding:0; box-sizing:border-box; }

        body {
            font-family: var(--sans);
            font-size: 14px;
            color: var(--text);
            background-color: var(--bg);
            background:
                linear-gradient(rgba(241,243,248,0.70), rgba(241,243,248,0.72)),
                url('${pageContext.request.contextPath}/assets/back1.jpg') center center / cover no-repeat fixed;
            min-height: 100vh;
            line-height: 1.55;
        }

        /* ─── LAYOUT ─────────────────────────────── */
        .shell {
            max-width: 1360px;
            margin: 0 auto;
            padding: 28px 24px 64px;
        }

        /* ─── TOP BAR ────────────────────────────── */
        .topbar {
            display: flex;
            align-items: center;
            justify-content: space-between;
            margin-bottom: 28px;
            padding: 13px 18px;
            background: var(--surface);
            border: 1px solid var(--line);
            border-radius: var(--radius);
            box-shadow: var(--shadow-sm);
        }
        .brand {
            display: flex;
            align-items: center;
            gap: 12px;
        }
        .brand-icon {
            width: 36px; height: 36px;
            background: linear-gradient(135deg, var(--accent) 0%, var(--accent-2) 100%);
            border-radius: var(--radius-sm);
            display: flex; align-items: center; justify-content: center;
            box-shadow: 0 4px 12px rgba(79,70,229,.3);
        }
        .brand-icon svg { width:17px; height:17px; }
        .brand-name {
            font-size: 14px;
            font-weight: 700;
            letter-spacing: -0.01em;
            color: var(--text);
        }
        .brand-sub {
            font-family: var(--mono);
            font-size: 10px;
            color: var(--muted);
            letter-spacing: 0.06em;
        }
        .back-link {
            font-size: 13px;
            font-weight: 500;
            color: var(--sub);
            text-decoration: none;
            padding: 7px 16px;
            border: 1px solid var(--line-2);
            border-radius: var(--radius-sm);
            background: var(--card-alt);
            transition: all .18s;
        }
        .back-link:hover {
            color: var(--accent);
            border-color: var(--accent-lo);
            background: var(--accent-lo);
        }

        /* ─── PAGE HEADER ────────────────────────── */
        .page-header { margin-bottom: 24px; }
        .page-title {
            font-size: 24px;
            font-weight: 700;
            letter-spacing: -0.02em;
            color: var(--text);
        }
        .page-title span { color: var(--accent); }
        .page-desc {
            margin-top: 4px;
            font-size: 13px;
            color: var(--muted);
            font-weight: 400;
        }

        /* ─── ALERTS ─────────────────────────────── */
        .alert {
            display: flex;
            align-items: flex-start;
            gap: 10px;
            padding: 12px 16px;
            border-radius: var(--radius-sm);
            font-size: 13px;
            font-weight: 500;
            margin-bottom: 20px;
            border: 1px solid;
        }
        .alert-error   { background:#fff1f2; border-color:#fecdd3; color:#be123c; }
        .alert-success { background:#ecfdf5; border-color:#6ee7b7; color:#065f46; }
        .alert-dot { width:6px; height:6px; border-radius:50%; flex-shrink:0; margin-top:5px; }
        .alert-error .alert-dot   { background:#be123c; }
        .alert-success .alert-dot { background:#065f46; }

        /* ─── FILTER BAR ─────────────────────────── */
        .filter-bar-wrap {
            background: var(--surface);
            border: 1px solid var(--line);
            border-radius: var(--radius);
            padding: 16px 20px;
            margin-bottom: 24px;
            box-shadow: var(--shadow-sm);
        }
        .filter-bar {
            display: flex;
            align-items: flex-end;
            gap: 12px;
            flex-wrap: wrap;
        }
        .field {
            display: flex;
            flex-direction: column;
            gap: 5px;
        }
        .field label {
            font-size: 11px;
            font-weight: 600;
            letter-spacing: 0.04em;
            color: var(--sub);
            text-transform: uppercase;
        }
        .field input[type="text"],
        .field input[type="number"],
        .field select {
            padding: 9px 12px;
            background: var(--card-alt);
            border: 1.5px solid var(--line);
            border-radius: var(--radius-sm);
            color: var(--text);
            font-family: var(--sans);
            font-size: 13px;
            outline: none;
            transition: border-color .18s, box-shadow .18s;
            min-width: 150px;
        }
        .field input:focus,
        .field select:focus {
            border-color: var(--accent);
            box-shadow: 0 0 0 3px rgba(79,70,229,.12);
            background: var(--surface);
        }
        .field input[type="number"] { min-width: 100px; font-family: var(--mono); }
        .field input::placeholder { color: var(--muted); }

        .btn-filter {
            padding: 9px 22px;
            background: linear-gradient(135deg, var(--accent) 0%, var(--accent-2) 100%);
            color: white;
            border: none;
            border-radius: var(--radius-sm);
            font-size: 13px;
            font-weight: 600;
            cursor: pointer;
            transition: opacity .18s, box-shadow .18s;
            box-shadow: 0 4px 12px rgba(79,70,229,.3);
            letter-spacing: 0.01em;
        }
        .btn-filter:hover { opacity:.9; box-shadow: 0 6px 18px rgba(79,70,229,.38); }

        .btn-reset {
            padding: 9px 16px;
            background: var(--card-alt);
            color: var(--sub);
            border: 1.5px solid var(--line-2);
            border-radius: var(--radius-sm);
            font-size: 13px;
            font-weight: 500;
            cursor: pointer;
            text-decoration: none;
            display: inline-flex;
            align-items: center;
            transition: all .18s;
        }
        .btn-reset:hover { color: var(--text); border-color: var(--line); background: var(--surface); }

        /* ─── SECTION LABEL ──────────────────────── */
        .section-label {
            font-family: var(--mono);
            font-size: 10px;
            letter-spacing: 0.1em;
            color: var(--muted);
            text-transform: uppercase;
            margin-bottom: 14px;
            display: flex;
            align-items: center;
            gap: 10px;
        }
        .section-label::after {
            content:'';
            flex:1;
            height:1px;
            background: var(--line);
        }

        /* ─── TOP ACTIONS ────────────────────────── */
        .top-actions {
            display: flex;
            justify-content: flex-end;
            margin-bottom: 16px;
        }
        .btn-add {
            display: inline-flex;
            align-items: center;
            gap: 7px;
            padding: 9px 20px;
            background: var(--ok);
            color: white;
            border: none;
            border-radius: var(--radius-sm);
            font-size: 13px;
            font-weight: 600;
            text-decoration: none;
            cursor: pointer;
            transition: opacity .18s, box-shadow .18s;
            box-shadow: 0 4px 12px rgba(16,185,129,.3);
            letter-spacing: 0.01em;
        }
        .btn-add:hover { opacity:.9; box-shadow: 0 6px 18px rgba(16,185,129,.38); }

        /* ─── MAIN CARD ──────────────────────────── */
        .main-card {
            border: 1px solid var(--line);
            border-radius: var(--radius-lg);
            overflow: hidden;
            background: var(--card);
            box-shadow: var(--shadow);
        }

        /* ─── TABLE ──────────────────────────────── */
        .data-table { width:100%; border-collapse:collapse; }
        .data-table thead tr { background: #f8f9fd; }
        .data-table th {
            font-size: 11px;
            font-weight: 600;
            letter-spacing: 0.04em;
            text-transform: uppercase;
            color: var(--muted);
            padding: 10px 16px;
            text-align: left;
            border-bottom: 1px solid var(--line);
            white-space: nowrap;
        }
        .data-table td {
            padding: 13px 16px;
            font-size: 13px;
            color: var(--sub);
            border-bottom: 1px solid var(--line);
            vertical-align: middle;
        }
        .data-table tbody tr:last-child td { border-bottom: none; }
        .data-table tbody tr:hover { background: #fafbff; }

        .td-id {
            font-family: var(--mono);
            font-size: 11px;
            color: var(--muted);
        }
        .td-ref {
            font-family: var(--mono);
            font-size: 13px;
            font-weight: 700;
            color: var(--text);
            letter-spacing: 0.02em;
        }
        .td-places {
            font-family: var(--mono);
            font-size: 13px;
            font-weight: 700;
            color: var(--text);
        }

        /* ─── FUEL BADGES ────────────────────────── */
        .fuel-badge {
            display: inline-flex;
            align-items: center;
            gap: 6px;
            padding: 4px 10px;
            border-radius: 999px;
            font-size: 11px;
            font-weight: 600;
            font-family: var(--mono);
            letter-spacing: 0.03em;
            white-space: nowrap;
        }
        .fuel-dot { width: 6px; height: 6px; border-radius: 50%; }

        .fuel-D   { background: #1e2533; color: #c9d1e0; border: 1px solid #374151; }
        .fuel-D .fuel-dot { background: #6b7280; }

        .fuel-ES  { background: #e0f2fe; color: #0369a1; border: 1px solid #7dd3fc; }
        .fuel-ES .fuel-dot { background: #0ea5e9; }

        .fuel-H   { background: var(--ok-lo); color: #065f46; border: 1px solid var(--ok-line); }
        .fuel-H .fuel-dot { background: var(--ok); }

        .fuel-EL  { background: var(--accent-lo); color: var(--accent); border: 1px solid rgba(79,70,229,.3); }
        .fuel-EL .fuel-dot { background: var(--accent); }

        /* ─── ACTION BUTTONS ─────────────────────── */
        .actions { display: flex; gap: 8px; }

        .btn-edit, .btn-delete {
            padding: 6px 13px;
            border-radius: var(--radius-sm);
            font-size: 12px;
            font-weight: 600;
            text-decoration: none;
            display: inline-flex;
            align-items: center;
            gap: 5px;
            transition: all .18s;
            cursor: pointer;
            border: 1px solid;
        }
        .btn-edit {
            background: var(--warn-lo);
            color: #92400e;
            border-color: var(--warn-line);
        }
        .btn-edit:hover { background: #fef3c7; box-shadow: 0 2px 8px rgba(245,158,11,.2); }

        .btn-delete {
            background: var(--danger-lo);
            color: #be123c;
            border-color: #fecdd3;
        }
        .btn-delete:hover { background: #ffe4e6; box-shadow: 0 2px 8px rgba(239,68,68,.2); }

        /* ─── EMPTY STATE ────────────────────────── */
        .empty {
            text-align: center;
            padding: 64px 0;
            color: var(--muted);
        }
        .empty-icon { font-size: 32px; margin-bottom: 12px; opacity:.35; }
        .empty p { font-family: var(--mono); font-size: 12px; letter-spacing: 0.07em; }
        .empty-cta {
            margin-top: 20px;
            display: inline-flex;
            align-items: center;
            gap: 7px;
            padding: 9px 20px;
            background: linear-gradient(135deg, var(--accent) 0%, var(--accent-2) 100%);
            color: white;
            border-radius: var(--radius-sm);
            font-size: 13px;
            font-weight: 600;
            text-decoration: none;
            box-shadow: 0 4px 12px rgba(79,70,229,.3);
        }

        /* ─── RESPONSIVE ─────────────────────────── */
        @media (max-width: 640px) {
            body { background-attachment: scroll; }
            .shell { padding: 16px 12px 48px; }
            .data-table th, .data-table td { padding: 8px 10px; font-size: 12px; }
            .page-title { font-size: 20px; }
            .filter-bar { flex-direction: column; }
            .field input, .field select { min-width: 100%; width: 100%; }
        }
    </style>
</head>
<body>
<div class="shell">

    <!-- TOP BAR -->
    <div class="topbar">
        <div class="brand">
            <div class="brand-icon">
                <svg viewBox="0 0 24 24" fill="none"><path d="M5 17H3a2 2 0 01-2-2V5a2 2 0 012-2h11a2 2 0 012 2v3" stroke="white" stroke-width="2" stroke-linecap="round"/><rect x="9" y="11" width="14" height="10" rx="2" stroke="white" stroke-width="2"/><path d="M12 16h2M16 16h2" stroke="white" stroke-width="2" stroke-linecap="round"/></svg>
            </div>
            <div>
                <div class="brand-name">3110 . 3222 . 3152</div>
                <div class="brand-sub">BACK-OFFICE SYSTEM</div>
            </div>
        </div>
        <a href="${pageContext.request.contextPath}/" class="back-link">← MENU</a>
    </div>

    <!-- PAGE HEADER -->
    <div class="page-header">
        <h1 class="page-title">Gestion <span>/</span> Véhicules</h1>
        <p class="page-desc">Parc automobile · Capacités · Types de carburant</p>
    </div>

    <!-- ALERTS -->
    <% String error = (String) request.getAttribute("error");
       String success = (String) request.getAttribute("success");
       if (error != null) { %>
    <div class="alert alert-error"><div class="alert-dot"></div><span><%= error %></span></div>
    <% } if (success != null) { %>
    <div class="alert alert-success"><div class="alert-dot"></div><span><%= success %></span></div>
    <% } %>

    <!-- FILTER BAR -->
    <form method="GET" action="${pageContext.request.contextPath}/vehicule/list">
        <div class="filter-bar-wrap">
            <div class="filter-bar">
                <div class="field">
                    <label for="search">Référence</label>
                    <input type="text" id="search" name="search"
                           placeholder="Ex: VH-001"
                           value="<%= request.getAttribute("search") != null ? request.getAttribute("search") : "" %>">
                </div>
                <div class="field">
                    <label for="typeCarburant">Carburant</label>
                    <select id="typeCarburant" name="typeCarburant">
                        <option value="">— Tous —</option>
                        <option value="D"  <%= "D".equals(request.getAttribute("typeCarburant"))  ? "selected" : "" %>>Diesel</option>
                        <option value="ES" <%= "ES".equals(request.getAttribute("typeCarburant")) ? "selected" : "" %>>Essence</option>
                        <option value="H"  <%= "H".equals(request.getAttribute("typeCarburant"))  ? "selected" : "" %>>Hybride</option>
                        <option value="EL" <%= "EL".equals(request.getAttribute("typeCarburant")) ? "selected" : "" %>>Électrique</option>
                    </select>
                </div>
                <div class="field">
                    <label for="nombrePlaceMin">Places min</label>
                    <input type="number" id="nombrePlaceMin" name="nombrePlaceMin" min="1"
                           placeholder="Min"
                           value="<%= request.getAttribute("nombrePlaceMin") != null ? request.getAttribute("nombrePlaceMin") : "" %>">
                </div>
                <div class="field">
                    <label for="nombrePlaceMax">Places max</label>
                    <input type="number" id="nombrePlaceMax" name="nombrePlaceMax" min="1"
                           placeholder="Max"
                           value="<%= request.getAttribute("nombrePlaceMax") != null ? request.getAttribute("nombrePlaceMax") : "" %>">
                </div>
                <button type="submit" class="btn-filter">FILTRER</button>
                <a href="${pageContext.request.contextPath}/vehicule/list" class="btn-reset">Réinitialiser</a>
            </div>
        </div>
    </form>

    <!-- TOP ACTIONS -->
    <div class="top-actions">
        <a href="${pageContext.request.contextPath}/vehicule/form" class="btn-add">
            <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
            Ajouter un véhicule
        </a>
    </div>

    <%
        List<Vehicule> vehicules = (List<Vehicule>) request.getAttribute("vehicules");
        if (vehicules != null && !vehicules.isEmpty()) {
    %>
    <div class="section-label">VÉHICULES — <%= vehicules.size() %> enregistré<%= vehicules.size() > 1 ? "s" : "" %></div>

    <div class="main-card">
        <table class="data-table">
            <thead>
                <tr>
                    <th>ID</th>
                    <th>Référence</th>
                    <th>Places</th>
                    <th>Disponibilité</th>
                    <th>Carburant</th>
                    <th>Actions</th>
                </tr>
            </thead>
            <tbody>
                <% for (Vehicule v : vehicules) {
                    String fuelClass = "fuel-" + v.getTypeCarburant();
                %>
                <tr>
                    <td class="td-id">#<%= v.getId() %></td>
                    <td class="td-ref"><%= v.getReference() %></td>
                    <td class="td-places"><%= v.getNombrePlace() %><span style="font-family:var(--sans);font-size:11px;color:var(--muted);font-weight:400;margin-left:3px;">pl.</span></td>
                    <td class="td-places">
                        <%
                            String heureDispoAff = "00:00";
                            if (v.getHeureDisponibilite() != null) {
                                String rawHeure = v.getHeureDisponibilite().toString();
                                heureDispoAff = rawHeure.length() >= 5 ? rawHeure.substring(0, 5) : rawHeure;
                            }
                        %>
                        <%= heureDispoAff %>
                    </td>
                    <td>
                        <span class="fuel-badge <%= fuelClass %>">
                            <span class="fuel-dot"></span>
                            <%= v.getTypeCarburantLibelle() %>
                        </span>
                    </td>
                    <td>
                        <div class="actions">
                            <a href="${pageContext.request.contextPath}/vehicule/edit?id=<%= v.getId() %>" class="btn-edit">
                                <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M11 4H4a2 2 0 00-2 2v14a2 2 0 002 2h14a2 2 0 002-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 013 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
                                Modifier
                            </a>
                            <a href="${pageContext.request.contextPath}/vehicule/delete?id=<%= v.getId() %>"
                               class="btn-delete"
                               onclick="return confirm('Supprimer ce véhicule ?');">
                                <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14a2 2 0 01-2 2H8a2 2 0 01-2-2L5 6"/><path d="M10 11v6M14 11v6"/></svg>
                                Supprimer
                            </a>
                        </div>
                    </td>
                </tr>
                <% } %>
            </tbody>
        </table>
    </div>

    <% } else { %>
    <div class="empty">
        <div class="empty-icon">◈</div>
        <p>AUCUN VÉHICULE TROUVÉ</p>
        <a href="${pageContext.request.contextPath}/vehicule/form" class="empty-cta">+ Ajouter votre premier véhicule</a>
    </div>
    <% } %>

</div>
</body>
</html>
