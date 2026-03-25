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
    <title>Planification · Transport</title>
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

            --sidebar-w: 240px;

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

        /* ─── PAGE WRAPPER ───────────────────────── */
        .page-wrapper {
            display: flex;
            align-items: flex-start;
            min-height: 100vh;
            padding: 28px 24px 64px;
            gap: 20px;
            max-width: 1560px;
            margin: 0 auto;
        }

        /* ─── SIDEBAR ────────────────────────────── */
        .sidebar {
            width: var(--sidebar-w);
            flex-shrink: 0;
            position: sticky;
            top: 28px;
            transition: width .28s cubic-bezier(.4,0,.2,1);
            overflow: hidden;
        }
        .sidebar.collapsed {
            width: 44px;
        }

        .sidebar-inner {
            background: var(--surface);
            border: 1px solid var(--line);
            border-radius: var(--radius-lg);
            box-shadow: var(--shadow);
            overflow: hidden;
            min-height: 120px;
        }

        /* sidebar header */
        .sidebar-header {
            display: flex;
            align-items: center;
            justify-content: space-between;
            padding: 12px 14px;
            border-bottom: 1px solid var(--line);
            background: var(--card-alt);
            gap: 8px;
        }
        .sidebar-title {
            font-family: var(--mono);
            font-size: 10px;
            letter-spacing: 0.1em;
            color: var(--muted);
            text-transform: uppercase;
            white-space: nowrap;
            overflow: hidden;
            opacity: 1;
            transition: opacity .2s .1s;
        }
        .sidebar.collapsed .sidebar-title {
            opacity: 0;
            transition: opacity .1s;
        }

        .toggle-btn {
            width: 22px; height: 22px;
            border-radius: 5px;
            border: 1px solid var(--line-2);
            background: var(--surface);
            display: flex; align-items: center; justify-content: center;
            cursor: pointer;
            flex-shrink: 0;
            transition: background .18s, border-color .18s;
            color: var(--muted);
        }
        .toggle-btn:hover {
            background: var(--accent-lo);
            border-color: rgba(79,70,229,.3);
            color: var(--accent);
        }
        .toggle-btn svg {
            width: 11px; height: 11px;
            transition: transform .28s cubic-bezier(.4,0,.2,1);
        }
        .sidebar.collapsed .toggle-btn svg {
            transform: rotate(180deg);
        }

        /* sidebar body */
        .sidebar-body {
            padding: 10px 8px;
            display: flex;
            flex-direction: column;
            gap: 4px;
            opacity: 1;
            transition: opacity .2s .08s;
        }
        .sidebar.collapsed .sidebar-body {
            opacity: 0;
            pointer-events: none;
            transition: opacity .1s;
        }

        .nav-item {
            display: flex;
            align-items: center;
            gap: 10px;
            padding: 10px 10px;
            border-radius: var(--radius-sm);
            text-decoration: none;
            transition: background .18s;
            position: relative;
            overflow: hidden;
            white-space: nowrap;
        }
        .nav-item::before {
            content: '';
            position: absolute;
            left: 0; top: 20%; bottom: 20%;
            width: 0;
            background: linear-gradient(180deg, var(--accent) 0%, var(--accent-2) 100%);
            border-radius: 0 3px 3px 0;
            transition: width .18s;
        }
        .nav-item:hover { background: var(--accent-lo); }
        .nav-item:hover::before { width: 3px; }
        .nav-item.active { background: var(--accent-lo); }
        .nav-item.active::before { width: 3px; }

        .nav-icon {
            width: 30px; height: 30px;
            border-radius: 7px;
            display: flex; align-items: center; justify-content: center;
            flex-shrink: 0;
        }
        .nav-icon svg { width: 18px; height: 18px; }

        .icon-accent { background: var(--accent-lo); color: var(--accent); border: 1px solid rgba(79,70,229,.2); }
        .icon-ok     { background: var(--ok-lo);     color: var(--ok);     border: 1px solid var(--ok-line); }
        .icon-warn   { background: var(--warn-lo);   color: var(--warn);   border: 1px solid var(--warn-line); }

        .nav-text { overflow: hidden; }
        .nav-label {
            font-size: 12px;
            font-weight: 600;
            color: var(--text);
            white-space: nowrap;
        }
        .nav-sub {
            font-family: var(--mono);
            font-size: 9px;
            color: var(--muted);
            letter-spacing: 0.04em;
            margin-top: 1px;
            white-space: nowrap;
        }

        .sidebar-divider {
            height: 1px;
            background: var(--line);
            margin: 4px 8px;
        }

        /* collapsed icon strip */
        .sidebar-collapsed-icons {
            padding: 10px 7px;
            display: flex;
            flex-direction: column;
            gap: 6px;
            align-items: center;
            position: absolute;
            top: 52px;
            left: 0; right: 0;
            opacity: 0;
            pointer-events: none;
            transition: opacity .15s;
        }
        .sidebar.collapsed .sidebar-collapsed-icons {
            opacity: 1;
            pointer-events: auto;
            transition: opacity .2s .15s;
        }
        .collapsed-icon-btn {
            width: 30px; height: 30px;
            border-radius: 7px;
            display: flex; align-items: center; justify-content: center;
            text-decoration: none;
            transition: background .18s;
        }
        .collapsed-icon-btn:hover { background: var(--accent-lo); }
        .collapsed-icon-btn svg { width: 14px; height: 14px; }

        /* ─── MAIN CONTENT ───────────────────────── */
        .main {
            flex: 1;
            min-width: 0;
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

        /* ─── PARAMS STRIP ───────────────────────── */
        .params-strip {
            display: flex;
            margin-bottom: 20px;
            background: var(--surface);
            border: 1px solid var(--line);
            border-radius: var(--radius);
            overflow: hidden;
            box-shadow: var(--shadow-sm);
        }
        .param-cell {
            flex: 1;
            padding: 14px 20px;
            border-right: 1px solid var(--line);
            position: relative;
        }
        .param-cell::before {
            content:'';
            position: absolute;
            left: 0; top: 20%; bottom: 20%;
            width: 3px;
            background: linear-gradient(180deg, var(--accent) 0%, var(--accent-2) 100%);
            border-radius: 0 3px 3px 0;
        }
        .param-cell:last-child { border-right: none; }
        .param-label {
            font-family: var(--mono);
            font-size: 10px;
            letter-spacing: 0.07em;
            color: var(--muted);
            text-transform: uppercase;
            margin-bottom: 4px;
        }
        .param-value {
            font-size: 20px;
            font-weight: 700;
            color: var(--text);
            letter-spacing: -0.02em;
        }
        .param-unit {
            font-size: 12px;
            color: var(--muted);
            font-weight: 400;
            margin-left: 3px;
        }

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
        .field input[type="date"] {
            padding: 9px 12px;
            background: var(--card-alt);
            border: 1.5px solid var(--line);
            border-radius: var(--radius-sm);
            color: var(--text);
            font-family: var(--mono);
            font-size: 12px;
            outline: none;
            transition: border-color .18s, box-shadow .18s;
            color-scheme: light;
        }
        .field input[type="date"]:focus {
            border-color: var(--accent);
            box-shadow: 0 0 0 3px rgba(79,70,229,.12);
            background: var(--surface);
        }
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
        .btn-filter:hover { opacity: .9; box-shadow: 0 6px 18px rgba(79,70,229,.38); }

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

        /* ─── GROUPE CARD ────────────────────────── */
        .groupe-card {
            border: 1px solid var(--line);
            border-radius: var(--radius-lg);
            margin-bottom: 14px;
            overflow: hidden;
            background: var(--card);
            box-shadow: var(--shadow);
            transition: box-shadow .22s, transform .22s;
        }
        .groupe-card:hover {
            box-shadow: var(--shadow-lg);
            transform: translateY(-1px);
        }

        .card-head {
            padding: 16px 20px;
            display: grid;
            grid-template-columns: auto 1fr auto;
            gap: 0 24px;
            align-items: center;
            border-bottom: 1px solid var(--line);
        }
        .card-head-ok   { background: var(--ok-lo);   border-bottom-color: var(--ok-line); }
        .card-head-warn { background: var(--warn-lo);  border-bottom-color: var(--warn-line); }

        .card-id {
            font-family: var(--mono);
            font-size: 10px;
            letter-spacing: 0.07em;
            color: var(--muted);
            white-space: nowrap;
        }
        .card-id strong {
            font-family: var(--sans);
            font-size: 15px;
            font-weight: 700;
            color: var(--text);
            display: block;
            letter-spacing: -0.01em;
        }

        .card-meta {
            display: flex;
            flex-wrap: wrap;
            gap: 6px 18px;
            align-items: center;
        }
        .meta-item {
            display: flex;
            align-items: center;
            gap: 5px;
            font-size: 13px;
            color: var(--sub);
        }
        .meta-item strong { color: var(--text); font-weight: 600; }
        .meta-dot {
            width: 3px; height: 3px;
            border-radius: 50%;
            background: var(--line-2);
        }

        .veh-badge {
            display: inline-flex;
            align-items: center;
            gap: 7px;
            padding: 5px 12px;
            border-radius: 999px;
            font-size: 12px;
            font-weight: 600;
            white-space: nowrap;
        }
        .veh-ok   { background: #d1fae5; color: #065f46; border: 1px solid #6ee7b7; }
        .veh-warn { background: #fef3c7; color: #92400e; border: 1px solid #fcd34d; }
        .veh-dot  { width: 6px; height: 6px; border-radius: 50%; }
        .veh-ok .veh-dot   { background: #10b981; }
        .veh-warn .veh-dot { background: #f59e0b; }

        .time-pill {
            font-family: var(--mono);
            font-size: 11px;
            padding: 3px 9px;
            background: var(--card-alt);
            border: 1px solid var(--line);
            border-radius: 6px;
            color: var(--sub);
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
            padding: 12px 16px;
            font-size: 13px;
            color: var(--sub);
            border-bottom: 1px solid var(--line);
            vertical-align: middle;
        }
        .data-table tbody tr:last-child td { border-bottom: none; }
        .data-table tbody tr:hover { background: #fafbff; }

        .td-mono { font-family: var(--mono); font-size: 12px; color: var(--sub); }
        .td-id   { font-family: var(--mono); font-size: 11px; color: var(--muted); }
        .td-highlight { font-weight: 600; color: var(--text); }
        .td-pax  { font-family: var(--mono); font-size: 13px; font-weight: 700; color: var(--text); }
        .td-dist { font-family: var(--mono); font-size: 12px; font-weight: 600; color: var(--accent); }

        .order-badge {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            width: 26px; height: 26px;
            border-radius: 6px;
            font-family: var(--mono);
            font-size: 11px;
            font-weight: 700;
        }
        .order-first  { background: var(--accent-lo); color: var(--accent); border: 1px solid rgba(79,70,229,.25); }
        .order-normal { background: #f3f4f8; color: var(--muted); border: 1px solid var(--line); }

        /* ─── EMPTY STATE ────────────────────────── */
        .empty {
            text-align: center;
            padding: 64px 0;
            color: var(--muted);
        }
        .empty-icon { font-size: 32px; margin-bottom: 12px; opacity:.35; }
        .empty p { font-family: var(--mono); font-size: 12px; letter-spacing: 0.07em; }

        /* ─── RESPONSIVE ─────────────────────────── */
        @media (max-width: 900px) {
            .card-head { grid-template-columns: 1fr; gap: 10px; }
            .params-strip { flex-direction: column; }
            .param-cell { border-right: none; border-bottom: 1px solid var(--line); }
            .param-cell:last-child { border-bottom: none; }
            .sidebar { display: none; }
        }
        @media (max-width: 640px) {
            body { background-attachment: scroll; }
            .page-wrapper { padding: 16px 12px 48px; }
            .data-table th, .data-table td { padding: 8px 10px; font-size: 12px; }
            .page-title { font-size: 20px; }
        }
    </style>
</head>
<body>
<div class="page-wrapper">

    <!-- ═══ SIDEBAR ═══════════════════════════════ -->
    <aside class="sidebar" id="sidebar">
        <div class="sidebar-inner">
            <!-- header -->
            <div class="sidebar-header">
                <span class="sidebar-title">NAVIGATION</span>
                <button class="toggle-btn" id="toggleBtn" title="Réduire">
                    <!-- chevrons left -->
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                        <polyline points="15 18 9 12 15 6"/>
                    </svg>
                </button>
            </div>

            <!-- expanded nav -->
            <div class="sidebar-body">

                <a href="${pageContext.request.contextPath}/reservation/form" class="nav-item">
                    <div class="nav-icon icon-accent">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z"/>
                            <polyline points="14 2 14 8 20 8"/>
                            <line x1="12" y1="18" x2="12" y2="12"/>
                            <line x1="9" y1="15" x2="15" y2="15"/>
                        </svg>
                    </div>
                    <div class="nav-text">
                        <div class="nav-label">Formulaire de Réservation</div>
                        <div class="nav-sub">SAISIE · CLIENTS · ARRIVÉES</div>
                    </div>
                </a>

                <div class="sidebar-divider"></div>

                <a href="${pageContext.request.contextPath}/vehicule/list" class="nav-item">
                    <div class="nav-icon icon-ok">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <rect x="1" y="3" width="15" height="13" rx="2"/>
                            <path d="M16 8h4l3 5v3h-7V8z"/>
                            <circle cx="5.5" cy="18.5" r="2.5"/>
                            <circle cx="18.5" cy="18.5" r="2.5"/>
                        </svg>
                    </div>
                    <div class="nav-text">
                        <div class="nav-label">Gestion des Véhicules</div>
                        <div class="nav-sub">PARC · CAPACITÉS · CARBURANT</div>
                    </div>
                </a>

                <div class="sidebar-divider"></div>

                <a href="${pageContext.request.contextPath}/planification" class="nav-item active">
                    <div class="nav-icon icon-warn">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
                            <line x1="16" y1="2" x2="16" y2="6"/>
                            <line x1="8" y1="2" x2="8" y2="6"/>
                            <line x1="3" y1="10" x2="21" y2="10"/>
                        </svg>
                    </div>
                    <div class="nav-text">
                        <div class="nav-label">Planification des Transports</div>
                        <div class="nav-sub">GROUPES · TRAJETS · HORAIRES</div>
                    </div>
                </a>

            </div>

            <!-- collapsed icon strip -->
            <div class="sidebar-collapsed-icons">
                <a href="${pageContext.request.contextPath}/reservation/form" class="collapsed-icon-btn icon-accent" title="Formulaire de Réservation">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z"/>
                        <polyline points="14 2 14 8 20 8"/>
                        <line x1="12" y1="18" x2="12" y2="12"/>
                        <line x1="9" y1="15" x2="15" y2="15"/>
                    </svg>
                </a>
                <a href="${pageContext.request.contextPath}/vehicule/list" class="collapsed-icon-btn icon-ok" title="Gestion des Véhicules">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <rect x="1" y="3" width="15" height="13" rx="2"/>
                        <path d="M16 8h4l3 5v3h-7V8z"/>
                        <circle cx="5.5" cy="18.5" r="2.5"/>
                        <circle cx="18.5" cy="18.5" r="2.5"/>
                    </svg>
                </a>
                <a href="${pageContext.request.contextPath}/planification" class="collapsed-icon-btn icon-warn" title="Planification des Transports">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
                        <line x1="16" y1="2" x2="16" y2="6"/>
                        <line x1="8" y1="2" x2="8" y2="6"/>
                        <line x1="3" y1="10" x2="21" y2="10"/>
                    </svg>
                </a>
            </div>
        </div>
    </aside>

    <!-- ═══ MAIN ═══════════════════════════════════ -->
    <div class="main">

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
            <h1 class="page-title">Planification <span>/</span> Transports</h1>
            <p class="page-desc">Orchestration des véhicules · Suivi des groupes · Progression des trajets</p>
        </div>

        <!-- ALERTS -->
        <%
            String error = (String) request.getAttribute("error");
            String success = (String) request.getAttribute("success");
            if (error != null) { %>
        <div class="alert alert-error"><div class="alert-dot"></div><span><%= error %></span></div>
        <% } if (success != null) { %>
        <div class="alert alert-success"><div class="alert-dot"></div><span><%= success %></span></div>
        <% } %>

        <!-- PARAMS STRIP -->
        <%
            Parametre parametre = (Parametre) request.getAttribute("parametre");
            if (parametre != null) { %>
        <div class="params-strip">
            <div class="param-cell">
                <div class="param-label">Temps d'attente aéroport</div>
                <div class="param-value"><%= parametre.getTempsAttente() %><span class="param-unit">min</span></div>
            </div>
            <div class="param-cell">
                <div class="param-label">Vitesse moyenne</div>
                <div class="param-value"><%= parametre.getVitesseMoyenne() %><span class="param-unit">km/h</span></div>
            </div>
        </div>
        <% } %>

        <!-- FILTER BAR -->
        <form method="get" action="${pageContext.request.contextPath}/planification">
            <div class="filter-bar-wrap">
                <div class="filter-bar">
                    <div class="field">
                        <label for="dateDebut">Date début</label>
                        <input type="date" id="dateDebut" name="dateDebut"
                               value="<%= request.getAttribute("dateDebut") != null ? request.getAttribute("dateDebut") : "" %>">
                    </div>
                    <div class="field">
                        <label for="dateFin">Date fin</label>
                        <input type="date" id="dateFin" name="dateFin"
                               value="<%= request.getAttribute("dateFin") != null ? request.getAttribute("dateFin") : "" %>">
                    </div>
                    <button type="submit" class="btn-filter">FILTRER</button>
                </div>
            </div>
        </form>

        <!-- LIST -->
        <%
            List<Reservation> reservations = (List<Reservation>) request.getAttribute("reservations");
            SimpleDateFormat sdfDate     = new SimpleDateFormat("dd/MM/yyyy");
            SimpleDateFormat sdfTime     = new SimpleDateFormat("HH:mm");
            SimpleDateFormat sdfDateTime = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        %>

        <% if (reservations != null && !reservations.isEmpty()) { %>
        <%
            LinkedHashMap<Integer, List<Reservation>> groupesMap = new LinkedHashMap<>();
            for (Reservation r : reservations) {
                int gid = r.getGroupeId();
                if (!groupesMap.containsKey(gid)) groupesMap.put(gid, new ArrayList<>());
                groupesMap.get(gid).add(r);
            }
            Map<String, Integer> displayedTripsByVehicule = new LinkedHashMap<>();
            int cardIndex = 0;
        %>

        <div class="section-label">GROUPES — <%= groupesMap.size() %> enregistré<%= groupesMap.size() > 1 ? "s" : "" %></div>

        <% for (Map.Entry<Integer, List<Reservation>> entry : groupesMap.entrySet()) {
            int groupeId       = entry.getKey();
            List<Reservation> groupe = entry.getValue();
            Reservation first  = groupe.get(0);
            boolean hasVehicule = first.getVehiculeReference() != null;

            int nombreTrajets = 1;
            if (hasVehicule && displayedTripsByVehicule.containsKey(first.getVehiculeReference()))
                nombreTrajets = displayedTripsByVehicule.get(first.getVehiculeReference()) + 1;

            int totalPassagers = 0;
            for (Reservation rr : groupe) totalPassagers += rr.getNombrePassager();
            cardIndex++;
        %>
        <div class="groupe-card">
            <div class="card-head <%= hasVehicule ? "card-head-ok" : "card-head-warn" %>">
                <div class="card-id">
                    <span>GROUPE</span>
                    <strong>#<%= String.format("%03d", groupeId) %></strong>
                </div>
                <div class="card-meta">
                    <% if (hasVehicule) { %>
                    <span class="veh-badge veh-ok">
                        <span class="veh-dot"></span>
                        <%= first.getVehiculeReference() %>
                    </span>
                    <span class="meta-item">
                        <strong><%= first.getVehiculeNombrePlace() %> places</strong>
                        <span style="color:var(--muted); font-size:12px;"><%= first.getVehiculeTypeCarburant() %></span>
                    </span>
                    <% } else { %>
                    <span class="veh-badge veh-warn">
                        <span class="veh-dot"></span>
                        Aucun véhicule
                    </span>
                    <% } %>
                    <div class="meta-dot"></div>
                    <span class="meta-item">
                        <svg width="12" height="12" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/></svg>
                        <strong><%= totalPassagers %></strong> passager<%= totalPassagers > 1 ? "s" : "" %>
                        <% if (hasVehicule) { %>
                            <span style="color:var(--muted);">/ <%= first.getVehiculeNombrePlace() %></span>
                        <% } %>
                    </span>
                    <% if (hasVehicule) { %>
                    <div class="meta-dot"></div>
                    <span class="meta-item">Trajet <strong><%= nombreTrajets %></strong></span>
                    <% } %>
                    <% if (first.getHeureDepartAeroport() != null) { %>
                    <div class="meta-dot"></div>
                    <span class="meta-item">
                        <svg width="12" height="12" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
                        Dép. <span class="time-pill"><%= sdfDateTime.format(first.getHeureDepartAeroport()) %></span>
                    </span>
                    <% } %>
                    <% if (first.getHeureRetourAeroport() != null) { %>
                    <span class="meta-item">
                        Ret. <span class="time-pill"><%= sdfDateTime.format(first.getHeureRetourAeroport()) %></span>
                    </span>
                    <% } %>
                </div>
                <div style="font-family:var(--mono); font-size:11px; color:var(--muted); text-align:right; white-space:nowrap; background:var(--card-alt); padding:4px 10px; border-radius:5px; border:1px solid var(--line);">
                    <%= groupe.size() %> résa<%= groupe.size() > 1 ? "s" : "" %>
                </div>
            </div>

            <table class="data-table">
                <thead>
                    <tr>
                        <th>#</th>
                        <th>ID Résa</th>
                        <th>Client</th>
                        <th>Pax</th>
                        <th>Arrivée</th>
                        <th>Hôtel</th>
                        <th>Distance A/R</th>
                    </tr>
                </thead>
                <tbody>
                    <% for (Reservation r : groupe) { %>
                    <tr>
                        <td>
                            <span class="order-badge <%= r.getOrdreLivraison() == 1 ? "order-first" : "order-normal" %>">
                                <%= r.getOrdreLivraison() %>
                            </span>
                        </td>
                        <td class="td-id">#<%= r.getId() %></td>
                        <td class="td-highlight"><%= r.getClientId() %></td>
                        <td class="td-pax"><%= r.getNombrePassager() %></td>
                        <td class="td-mono"><%= sdfDateTime.format(r.getDateArrivee()) %></td>
                        <td style="color:var(--text);"><%= r.getHotelNom() != null ? r.getHotelNom() : "—" %></td>
                        <td class="td-dist"><%= r.getDistanceKm() > 0 ? (r.getDistanceKm() * 2) + " km" : "—" %></td>
                    </tr>
                    <% } %>
                </tbody>
            </table>
        </div>

        <% if (hasVehicule) displayedTripsByVehicule.put(first.getVehiculeReference(), nombreTrajets); %>
        <% } %>

        <% } else { %>
        <div class="empty">
            <div class="empty-icon">◈</div>
            <p>AUCUNE RÉSERVATION TROUVÉE POUR CETTE PÉRIODE</p>
        </div>
        <% } %>

    </div><!-- /main -->
</div><!-- /page-wrapper -->

<script>
    const sidebar   = document.getElementById('sidebar');
    const toggleBtn = document.getElementById('toggleBtn');
    const STORAGE_KEY = 'sidebar_collapsed';

    // restore state
    if (localStorage.getItem(STORAGE_KEY) === 'true') {
        sidebar.classList.add('collapsed');
    }

    toggleBtn.addEventListener('click', () => {
        const isCollapsed = sidebar.classList.toggle('collapsed');
        localStorage.setItem(STORAGE_KEY, isCollapsed);
    });
</script>
</body>
</html>
