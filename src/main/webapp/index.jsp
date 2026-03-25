<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>3110 . 3222 . 3152 · Menu</title>
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
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 24px;
            line-height: 1.55;
        }

        /* ─── SHELL ──────────────────────────────── */
        .shell {
            width: 100%;
            max-width: 480px;
        }

        /* ─── TOP BAR ────────────────────────────── */
        .topbar {
            display: flex;
            align-items: center;
            justify-content: space-between;
            margin-bottom: 24px;
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
        .version-pill {
            font-family: var(--mono);
            font-size: 10px;
            color: var(--muted);
            background: var(--card-alt);
            border: 1px solid var(--line);
            padding: 3px 9px;
            border-radius: 999px;
            letter-spacing: 0.05em;
        }

        /* ─── PAGE HEADER ────────────────────────── */
        .page-header {
            margin-bottom: 20px;
            padding: 0 2px;
        }
        .page-title {
            font-size: 22px;
            font-weight: 700;
            letter-spacing: -0.02em;
            color: var(--text);
        }
        .page-title span { color: var(--accent); }
        .page-desc {
            margin-top: 4px;
            font-size: 12px;
            color: var(--muted);
            font-weight: 400;
            font-family: var(--mono);
            letter-spacing: 0.03em;
        }

        /* ─── SECTION LABEL ──────────────────────── */
        .section-label {
            font-family: var(--mono);
            font-size: 10px;
            letter-spacing: 0.1em;
            color: var(--muted);
            text-transform: uppercase;
            margin-bottom: 12px;
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

        /* ─── MENU CARD ──────────────────────────── */
        .menu-card {
            background: var(--card);
            border: 1px solid var(--line);
            border-radius: var(--radius-lg);
            overflow: hidden;
            box-shadow: var(--shadow);
        }

        .menu-item {
            display: flex;
            align-items: center;
            gap: 16px;
            padding: 18px 20px;
            text-decoration: none;
            border-bottom: 1px solid var(--line);
            transition: background .18s, transform .18s;
            position: relative;
            overflow: hidden;
        }
        .menu-item:last-child { border-bottom: none; }
        .menu-item::before {
            content: '';
            position: absolute;
            left: 0; top: 0; bottom: 0;
            width: 3px;
            background: transparent;
            transition: background .18s;
        }
        .menu-item:hover {
            background: var(--accent-lo);
        }
        .menu-item:hover::before {
            background: linear-gradient(180deg, var(--accent) 0%, var(--accent-2) 100%);
        }
        .menu-item:hover .item-arrow {
            transform: translateX(3px);
            color: var(--accent);
        }

        .item-icon {
            width: 38px; height: 38px;
            border-radius: var(--radius-sm);
            display: flex; align-items: center; justify-content: center;
            flex-shrink: 0;
        }
        .item-icon svg { width: 17px; height: 17px; }

        .icon-accent { background: var(--accent-lo); color: var(--accent); border: 1px solid rgba(79,70,229,.2); }
        .icon-ok     { background: var(--ok-lo);     color: var(--ok);     border: 1px solid var(--ok-line); }
        .icon-warn   { background: var(--warn-lo);   color: var(--warn);   border: 1px solid var(--warn-line); }

        .item-body { flex: 1; }
        .item-title {
            font-size: 13px;
            font-weight: 600;
            color: var(--text);
            letter-spacing: -0.01em;
        }
        .item-desc {
            font-family: var(--mono);
            font-size: 10px;
            color: var(--muted);
            letter-spacing: 0.04em;
            margin-top: 2px;
        }

        .item-arrow {
            color: var(--line-2);
            transition: transform .18s, color .18s;
            flex-shrink: 0;
        }

        /* ─── RESPONSIVE ─────────────────────────── */
        @media (max-width: 480px) {
            body { background-attachment: scroll; }
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
        <span class="version-pill">v1.0</span>
    </div>

    <!-- PAGE HEADER -->
    <div class="page-header">
        <h1 class="page-title">Gestion <span>/</span> Hôtel</h1>
        <p class="page-desc">SÉLECTIONNER UN MODULE</p>
    </div>

    <!-- SECTION LABEL -->
    <div class="section-label">NAVIGATION — 3 modules</div>

    <!-- MENU -->
    <div class="menu-card">

        <a href="${pageContext.request.contextPath}/reservation/form" class="menu-item">
            <div class="item-icon icon-accent">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="12" y1="18" x2="12" y2="12"/><line x1="9" y1="15" x2="15" y2="15"/>
                </svg>
            </div>
            <div class="item-body">
                <div class="item-title">Formulaire de Réservation</div>
                <div class="item-desc">SAISIE · CLIENTS · ARRIVÉES</div>
            </div>
            <svg class="item-arrow" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="9 18 15 12 9 6"/></svg>
        </a>

        <a href="${pageContext.request.contextPath}/vehicule/list" class="menu-item">
            <div class="item-icon icon-ok">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <rect x="1" y="3" width="15" height="13" rx="2"/><path d="M16 8h4l3 5v3h-7V8z"/><circle cx="5.5" cy="18.5" r="2.5"/><circle cx="18.5" cy="18.5" r="2.5"/>
                </svg>
            </div>
            <div class="item-body">
                <div class="item-title">Gestion des Véhicules</div>
                <div class="item-desc">PARC · CAPACITÉS · CARBURANT</div>
            </div>
            <svg class="item-arrow" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="9 18 15 12 9 6"/></svg>
        </a>

        <a href="${pageContext.request.contextPath}/planification" class="menu-item">
            <div class="item-icon icon-warn">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/>
                </svg>
            </div>
            <div class="item-body">
                <div class="item-title">Planification des Transports</div>
                <div class="item-desc">GROUPES · TRAJETS · HORAIRES</div>
            </div>
            <svg class="item-arrow" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="9 18 15 12 9 6"/></svg>
        </a>

    </div>

</div>
</body>
</html>
