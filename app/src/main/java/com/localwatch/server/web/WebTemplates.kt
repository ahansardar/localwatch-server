package com.localwatch.server.web

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object WebTemplates {
    private val styles = """
        :root {
          color-scheme: dark;
          --bg: #09090b;
          --surface: rgba(24, 24, 27, .82);
          --surface-solid: #18181b;
          --surface-2: #242428;
          --surface-3: #303036;
          --text: #f4f4f5;
          --muted: #a1a1aa;
          --outline: rgba(255,255,255,.11);
          --outline-strong: rgba(255,255,255,.19);
          --primary: #67e8f9;
          --primary-ink: #042f35;
          --secondary: #a78bfa;
          --danger: #fda4af;
          --warning: #fcd34d;
          --success: #5eead4;
          --radius-sm: 10px;
          --radius: 16px;
          --radius-lg: 24px;
          --shadow: 0 24px 70px rgba(0,0,0,.42);
          --max: 1280px;
        }
        * { box-sizing: border-box; }
        html { min-height: 100%; scroll-behavior: smooth; }
        body {
          margin: 0;
          min-height: 100vh;
          min-height: 100dvh;
          overflow-x: hidden;
          background:
            radial-gradient(circle at 92% -8%, rgba(103,232,249,.11), transparent 32rem),
            radial-gradient(circle at -8% 75%, rgba(124,58,237,.12), transparent 30rem),
            var(--bg);
          color: var(--text);
          font-family: Inter, ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
          -webkit-font-smoothing: antialiased;
        }
        body::before {
          content: "";
          position: fixed;
          inset: 0;
          pointer-events: none;
          opacity: .16;
          background-image: linear-gradient(rgba(255,255,255,.02) 1px, transparent 1px),
            linear-gradient(90deg, rgba(255,255,255,.02) 1px, transparent 1px);
          background-size: 32px 32px;
          mask-image: linear-gradient(to bottom, black, transparent 72%);
        }
        button, input, select { font: inherit; }
        button, a, select { -webkit-tap-highlight-color: transparent; }
        button:focus-visible, a:focus-visible, input:focus-visible, select:focus-visible, summary:focus-visible {
          outline: 2px solid var(--primary);
          outline-offset: 3px;
        }
        a { color: inherit; }
        .shell { width: min(var(--max), calc(100% - 40px)); margin-inline: auto; }
        .appbar {
          position: sticky;
          top: 0;
          z-index: 30;
          border-bottom: 1px solid var(--outline);
          background: rgba(9,9,11,.74);
          backdrop-filter: blur(22px) saturate(140%);
        }
        .appbar-inner {
          width: min(var(--max), calc(100% - 40px));
          min-height: 68px;
          margin-inline: auto;
          display: flex;
          align-items: center;
          justify-content: space-between;
          gap: 18px;
          padding-block: max(10px, env(safe-area-inset-top)) 10px;
        }
        .brand { display: inline-flex; align-items: center; gap: 11px; min-width: 0; text-decoration: none; }
        .brand-logo { width: 40px; height: 40px; object-fit: contain; flex: 0 0 auto; }
        .brand-copy { min-width: 0; }
        .brand-title { display: block; color: var(--primary); font-weight: 800; letter-spacing: -.03em; line-height: 1.1; }
        .brand-subtitle { display: block; margin-top: 3px; color: var(--muted); font-size: 11px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
        .status-pill {
          display: inline-flex;
          align-items: center;
          gap: 8px;
          min-height: 36px;
          padding: 7px 11px;
          border: 1px solid rgba(94,234,212,.22);
          border-radius: 999px;
          background: rgba(94,234,212,.08);
          color: var(--success);
          font-size: 11px;
          font-weight: 750;
          letter-spacing: .06em;
          text-transform: uppercase;
          white-space: nowrap;
        }
        .status-dot { width: 8px; height: 8px; border-radius: 50%; background: currentColor; box-shadow: 0 0 13px currentColor; }
        .glass {
          background: var(--surface);
          border: 1px solid var(--outline);
          box-shadow: inset 0 1px rgba(255,255,255,.045), var(--shadow);
          backdrop-filter: blur(20px);
        }
        .btn {
          min-height: 44px;
          border: 0;
          border-radius: 12px;
          padding: 10px 15px;
          display: inline-flex;
          align-items: center;
          justify-content: center;
          gap: 8px;
          cursor: pointer;
          text-decoration: none;
          background: var(--primary);
          color: var(--primary-ink);
          font-weight: 800;
          transition: transform .16s ease, filter .16s ease, border-color .16s ease, background .16s ease;
        }
        .btn:hover { filter: brightness(1.06); }
        .btn:active { transform: scale(.97); }
        .btn.secondary { color: var(--text); background: var(--surface-2); border: 1px solid var(--outline); }
        .btn.ghost { color: var(--muted); background: transparent; border: 1px solid var(--outline); }
        .btn.danger { color: #4c0519; background: var(--danger); }
        .icon-btn {
          width: 44px;
          height: 44px;
          padding: 0;
          border: 1px solid var(--outline);
          border-radius: 12px;
          background: var(--surface-2);
          color: var(--text);
          display: inline-grid;
          place-items: center;
          cursor: pointer;
        }
        .icon-btn[aria-pressed="true"] { color: var(--primary); border-color: rgba(103,232,249,.4); background: rgba(103,232,249,.1); }
        .icon { width: 20px; height: 20px; fill: none; stroke: currentColor; stroke-width: 1.8; stroke-linecap: round; stroke-linejoin: round; }
        .hero { padding: clamp(42px, 7vw, 84px) 0 clamp(26px, 5vw, 52px); }
        .eyebrow { color: var(--primary); font-size: 11px; font-weight: 800; letter-spacing: .16em; text-transform: uppercase; }
        .hero h1 { max-width: 820px; margin: 12px 0; font-size: clamp(34px, 7vw, 68px); line-height: .98; letter-spacing: -.055em; }
        .hero p { max-width: 660px; margin: 0; color: var(--muted); font-size: clamp(14px, 2vw, 17px); line-height: 1.65; }
        .toolbar {
          position: sticky;
          top: 82px;
          z-index: 20;
          display: grid;
          grid-template-columns: minmax(180px, 1fr) auto auto auto;
          gap: 10px;
          padding: 11px;
          border-radius: 18px;
        }
        .search {
          min-width: 0;
          display: flex;
          align-items: center;
          gap: 10px;
          padding: 0 13px;
          border: 1px solid var(--outline);
          border-radius: 12px;
          background: rgba(9,9,11,.72);
          color: var(--muted);
        }
        .search input { width: 100%; min-height: 44px; border: 0; outline: 0; background: transparent; color: var(--text); }
        .search input::placeholder { color: #71717a; }
        .select {
          min-height: 44px;
          max-width: 180px;
          border: 1px solid var(--outline);
          border-radius: 12px;
          padding: 0 36px 0 13px;
          background: var(--surface-2);
          color: var(--text);
          cursor: pointer;
        }
        .section-head { display: flex; align-items: end; justify-content: space-between; gap: 18px; margin: 30px 2px 16px; }
        .section-head h2 { margin: 0; font-size: clamp(20px, 4vw, 27px); letter-spacing: -.025em; }
        .count { color: var(--muted); font-size: 13px; white-space: nowrap; }
        .video-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(min(260px, 100%), 1fr)); gap: 16px; padding-bottom: 64px; }
        .video-grid.list { grid-template-columns: 1fr; }
        .video-card {
          overflow: hidden;
          border-radius: 18px;
          transition: transform .2s ease, border-color .2s ease;
        }
        .video-card:hover { transform: translateY(-3px); border-color: rgba(103,232,249,.34); }
        .video-thumb {
          position: relative;
          aspect-ratio: 16/9;
          display: grid;
          place-items: center;
          overflow: hidden;
          background:
            radial-gradient(circle at 26% 20%, rgba(103,232,249,.28), transparent 30%),
            radial-gradient(circle at 80% 85%, rgba(124,58,237,.3), transparent 36%),
            linear-gradient(145deg, #20202a, #101014);
        }
        .video-thumb::before {
          content: "";
          position: absolute;
          inset: 0;
          background-image: linear-gradient(120deg, transparent 20%, rgba(255,255,255,.055), transparent 75%);
        }
        .thumb-logo { width: 54%; height: 54%; object-fit: contain; opacity: .2; }
        .format {
          position: absolute;
          top: 12px;
          right: 12px;
          padding: 5px 8px;
          border-radius: 7px;
          background: rgba(9,9,11,.76);
          border: 1px solid var(--outline);
          color: var(--text);
          font: 700 10px ui-monospace, SFMono-Regular, Consolas, monospace;
          letter-spacing: .08em;
        }
        .play-button {
          position: absolute;
          width: 58px;
          height: 58px;
          border: 1px solid rgba(255,255,255,.28);
          border-radius: 50%;
          display: grid;
          place-items: center;
          background: rgba(244,244,245,.94);
          color: #09090b;
          box-shadow: 0 12px 32px rgba(0,0,0,.5);
        }
        .play-button .icon { width: 25px; height: 25px; fill: currentColor; stroke: none; margin-left: 3px; }
        .card-content { padding: 16px; }
        .video-title { margin: 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; font-size: 16px; }
        .video-meta { display: flex; gap: 10px; flex-wrap: wrap; margin-top: 8px; color: var(--muted); font-size: 12px; }
        .video-actions { display: grid; grid-template-columns: 1fr auto; gap: 9px; margin-top: 15px; }
        .video-grid.list .video-card { display: grid; grid-template-columns: minmax(160px, 26%) 1fr; }
        .video-grid.list .video-thumb { aspect-ratio: auto; min-height: 150px; }
        .state {
          min-height: min(570px, calc(100dvh - 190px));
          padding: clamp(32px, 7vw, 72px) 20px;
          border-radius: var(--radius-lg);
          display: grid;
          place-items: center;
          text-align: center;
        }
        .state-inner { width: min(520px, 100%); }
        .state-logo { width: 112px; height: 112px; object-fit: contain; opacity: .78; }
        .state h2 { margin: 18px 0 8px; font-size: clamp(25px, 5vw, 36px); }
        .state p { margin: 0 auto; color: var(--muted); line-height: 1.6; }
        .state-actions { display: flex; justify-content: center; gap: 10px; flex-wrap: wrap; margin-top: 24px; }
        .hidden { display: none !important; }
        .player-page { width: min(1440px, calc(100% - 40px)); margin-inline: auto; padding: 28px 0 max(42px, env(safe-area-inset-bottom)); }
        .player-nav { display: flex; align-items: center; justify-content: space-between; gap: 12px; margin-bottom: 18px; }
        .player-title-small { min-width: 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; font-weight: 750; }
        .player-layout { display: grid; grid-template-columns: minmax(0, 1fr) minmax(280px, 360px); gap: 18px; align-items: start; }
        .video-panel { overflow: hidden; border-radius: 20px; background: #000; }
        .video-panel video { display: block; width: 100%; max-height: min(76dvh, 850px); background: #000; }
        .player-side { min-width: 0; padding: 20px; border-radius: 20px; }
        .player-side h1 { margin: 0; overflow-wrap: anywhere; font-size: clamp(21px, 4vw, 30px); line-height: 1.16; letter-spacing: -.03em; }
        .detail-list { margin: 20px 0; border-block: 1px solid var(--outline); }
        .detail-row { display: flex; justify-content: space-between; gap: 18px; padding: 12px 0; color: var(--muted); font-size: 12px; }
        .detail-row + .detail-row { border-top: 1px solid rgba(255,255,255,.055); }
        .detail-row strong { color: var(--text); text-align: right; overflow-wrap: anywhere; }
        .player-controls { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 9px; }
        .player-controls .btn { min-width: 0; padding-inline: 10px; }
        .speed-control { display: grid; grid-template-columns: auto 1fr; align-items: center; gap: 9px; margin-top: 9px; }
        .speed-control label { color: var(--muted); font-size: 12px; }
        .speed-control .select { width: 100%; max-width: none; }
        .notice { margin-top: 14px; padding: 15px; border: 1px solid rgba(252,211,77,.25); border-radius: 13px; background: rgba(252,211,77,.08); color: #fde68a; line-height: 1.5; }
        .auth-page { min-height: 100dvh; display: grid; place-items: center; padding: 24px; }
        .auth-card { width: min(440px, 100%); padding: clamp(24px, 7vw, 38px); border-radius: 24px; text-align: center; }
        .auth-logo { width: 82px; height: 82px; object-fit: contain; }
        .auth-card h1 { margin: 16px 0 8px; font-size: clamp(25px, 7vw, 34px); }
        .auth-card p { margin: 0; color: var(--muted); line-height: 1.55; }
        .pin-wrap { position: relative; margin-top: 24px; }
        .pin {
          width: 100%;
          min-height: 60px;
          border: 1px solid var(--outline-strong);
          border-radius: 13px;
          background: rgba(9,9,11,.72);
          color: var(--primary);
          text-align: center;
          font: 700 25px ui-monospace, SFMono-Regular, Consolas, monospace;
          letter-spacing: .32em;
        }
        .pin-toggle { position: absolute; top: 8px; right: 8px; }
        .form-error { min-height: 21px; margin: 8px 0; color: var(--danger); font-size: 12px; }
        .auth-card .btn { width: 100%; }
        details.help { margin-top: 18px; color: var(--muted); text-align: left; font-size: 12px; }
        details.help summary { cursor: pointer; text-align: center; color: var(--primary); }
        details.help p { margin-top: 10px; padding: 12px; border-radius: 10px; background: rgba(255,255,255,.035); }
        .toast {
          position: fixed;
          left: 50%;
          bottom: max(22px, env(safe-area-inset-bottom));
          z-index: 100;
          transform: translate(-50%, 24px);
          opacity: 0;
          pointer-events: none;
          padding: 11px 16px;
          border: 1px solid var(--outline);
          border-radius: 999px;
          background: #27272a;
          box-shadow: var(--shadow);
          transition: .2s ease;
        }
        .toast.show { opacity: 1; transform: translate(-50%, 0); }
        .footer { padding: 24px 20px max(24px, env(safe-area-inset-bottom)); border-top: 1px solid rgba(255,255,255,.06); color: #71717a; text-align: center; font-size: 11px; }
        @media (max-width: 850px) {
          .player-layout { grid-template-columns: 1fr; }
          .player-side { box-shadow: none; }
          .video-panel video { max-height: 68dvh; }
        }
        @media (max-width: 680px) {
          .shell, .appbar-inner, .player-page { width: min(100% - 24px, var(--max)); }
          .brand-subtitle { display: none; }
          .status-pill { padding-inline: 9px; font-size: 9px; }
          .toolbar { top: 76px; grid-template-columns: minmax(0, 1fr) auto auto; }
          .toolbar .select { grid-column: 1 / -1; grid-row: 2; width: 100%; max-width: none; }
          .video-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 10px; }
          .video-card { border-radius: 14px; }
          .card-content { padding: 12px; }
          .video-actions { grid-template-columns: 1fr; }
          .video-actions .download { display: none; }
          .video-grid.list .video-card { grid-template-columns: 108px minmax(0, 1fr); }
          .video-grid.list .video-thumb { min-height: 124px; }
          .video-grid.list .video-actions { display: flex; }
          .player-page { padding-top: 14px; }
          .player-nav { align-items: flex-start; }
          .player-nav .btn span { display: none; }
        }
        @media (max-width: 430px) {
          .video-grid { grid-template-columns: 1fr; }
          .video-actions { grid-template-columns: 1fr auto; }
          .video-actions .download { display: inline-flex; }
          .hero { padding-top: 34px; }
          .hero h1 { font-size: 36px; }
          .player-controls { grid-template-columns: 1fr; }
        }
        @media (orientation: landscape) and (max-height: 620px) {
          .hero { padding: 28px 0 20px; }
          .hero h1 { font-size: clamp(32px, 7vw, 48px); }
          .player-page { padding-top: 12px; }
          .player-nav { margin-bottom: 10px; }
          .player-layout { grid-template-columns: minmax(0, 1fr) 300px; }
          .video-panel video { max-height: calc(100dvh - 98px); }
          .player-side { max-height: calc(100dvh - 98px); overflow: auto; }
          .player-controls { grid-template-columns: 1fr; }
          .auth-page { padding-block: 14px; }
          .auth-card { display: grid; grid-template-columns: 110px 1fr; gap: 12px 22px; align-items: center; text-align: left; max-width: 700px; }
          .auth-brand { grid-row: 1 / span 3; text-align: center; }
          .auth-card h1 { margin-top: 0; }
          .auth-card form, .auth-card details { grid-column: 2; }
        }
        @media (prefers-reduced-motion: reduce) {
          *, *::before, *::after { scroll-behavior: auto !important; transition: none !important; animation: none !important; }
        }
    """.trimIndent()

    fun library(serverName: String, downloads: Boolean): String = page(
        title = "LocalWatch · $serverName",
        body = """
            ${appBar(serverName)}
            <main class="shell">
              <section class="hero">
                <div class="eyebrow">Offline local streaming</div>
                <h1>Watch together,<br>right where you are.</h1>
                <p>Browse media shared directly by ${escape(serverName)}. Nothing is uploaded, and every stream stays on this local network.</p>
              </section>
              <section class="toolbar glass" aria-label="Library controls">
                <label class="search" for="search">${icon("search")}<input id="search" type="search" placeholder="Search videos" autocomplete="off"></label>
                <select class="select" id="sort" aria-label="Sort videos">
                  <option value="name">Name</option>
                  <option value="modified">Recently modified</option>
                  <option value="size">File size</option>
                  <option value="type">File type</option>
                </select>
                <button class="icon-btn" id="view" type="button" aria-label="Use list layout" aria-pressed="false">${icon("list")}</button>
                <button class="icon-btn" id="refresh" type="button" aria-label="Refresh library">${icon("refresh")}</button>
              </section>
              <div class="section-head"><h2>Video library</h2><span class="count" id="count" aria-live="polite">Loading…</span></div>
              <section class="video-grid" id="videos" aria-live="polite">
                ${loadingState()}
              </section>
            </main>
            <footer class="footer">LocalWatch · Streaming privately over your local network</footer>
            <div class="toast" id="toast" role="status" aria-live="polite"></div>
            <script>
              (() => {
                "use strict";
                let videos = [];
                let listLayout = localStorage.getItem("lw-layout") === "list";
                const root = document.getElementById("videos");
                const count = document.getElementById("count");
                const search = document.getElementById("search");
                const sort = document.getElementById("sort");
                const view = document.getElementById("view");
                const refresh = document.getElementById("refresh");
                const downloadsEnabled = ${downloads};
                const esc = value => String(value).replace(/[&<>"']/g, char => ({
                  "&":"&amp;","<":"&lt;",">":"&gt;",'"':"&quot;","'":"&#39;"
                })[char]);
                const formatSize = bytes => {
                  if (!bytes) return "0 B";
                  const units = ["B","KB","MB","GB","TB"];
                  const index = Math.min(Math.floor(Math.log(bytes) / Math.log(1024)), units.length - 1);
                  return (bytes / Math.pow(1024, index)).toFixed(index ? 1 : 0) + " " + units[index];
                };
                const formatDate = value => value > 0 ? new Intl.DateTimeFormat(undefined, {dateStyle:"medium"}).format(new Date(value)) : "Date unavailable";
                const extension = name => (name.split(".").pop() || "video").toUpperCase();
                const videoCard = video => {
                  const id = encodeURIComponent(video.id);
                  const watch = "/watch?id=" + id;
                  const download = downloadsEnabled
                    ? '<a class="btn secondary download" href="/download?id=' + id + '" aria-label="Download ' + esc(video.name) + '">${icon("download")}</a>'
                    : "";
                  return '<article class="video-card glass">' +
                    '<div class="video-thumb"><img class="thumb-logo" src="/assets/logo.png" alt="">' +
                    '<span class="format">' + esc(extension(video.name)) + '</span>' +
                    '<a class="play-button" href="' + watch + '" aria-label="Play ' + esc(video.name) + '">${icon("play")}</a></div>' +
                    '<div class="card-content"><h3 class="video-title" title="' + esc(video.name) + '">' + esc(video.name) + '</h3>' +
                    '<div class="video-meta"><span>' + formatSize(video.size) + '</span><span>' + formatDate(video.modifiedAt) + '</span></div>' +
                    '<div class="video-actions"><a class="btn" href="' + watch + '">${icon("play")} Watch</a>' + download + '</div></div></article>';
                };
                const empty = (title, message, action) => '<div class="state glass" style="grid-column:1/-1"><div class="state-inner">' +
                  '<img class="state-logo" src="/assets/logo.png" alt=""><h2>' + esc(title) + '</h2><p>' + esc(message) + '</p>' +
                  (action ? '<div class="state-actions"><button class="btn" id="state-action" type="button">${icon("refresh")} ' + esc(action) + '</button></div>' : "") +
                  '</div></div>';
                function render() {
                  const query = search.value.trim().toLocaleLowerCase();
                  const mode = sort.value;
                  const items = videos.filter(video => video.name.toLocaleLowerCase().includes(query));
                  items.sort((left, right) => {
                    if (mode === "size") return right.size - left.size;
                    if (mode === "modified") return right.modifiedAt - left.modifiedAt;
                    if (mode === "type") return extension(left.name).localeCompare(extension(right.name));
                    return left.name.localeCompare(right.name);
                  });
                  root.className = "video-grid" + (listLayout ? " list" : "");
                  view.setAttribute("aria-pressed", String(listLayout));
                  view.setAttribute("aria-label", listLayout ? "Use grid layout" : "Use list layout");
                  count.textContent = items.length + (items.length === 1 ? " video" : " videos");
                  if (!items.length) {
                    root.innerHTML = videos.length
                      ? empty("No matches", "Try a different title or clear the search.", "")
                      : empty("No videos available", "The host has not shared any supported videos yet.", "Refresh");
                    document.getElementById("state-action")?.addEventListener("click", load);
                    return;
                  }
                  root.innerHTML = items.map(videoCard).join("");
                }
                async function load() {
                  refresh.disabled = true;
                  refresh.setAttribute("aria-busy", "true");
                  count.textContent = "Refreshing…";
                  try {
                    const response = await fetch("/api/videos", {cache:"no-store"});
                    if (response.status === 401) { location.reload(); return; }
                    if (!response.ok) throw new Error("HTTP " + response.status);
                    const data = await response.json();
                    videos = Array.isArray(data.videos) ? data.videos : [];
                    render();
                  } catch (error) {
                    count.textContent = "Connection lost";
                    root.className = "video-grid";
                    root.innerHTML = empty("Server not reachable", "Confirm this device is still on the host's Wi-Fi or hotspot, then retry.", "Retry connection");
                    document.getElementById("state-action")?.addEventListener("click", load);
                  } finally {
                    refresh.disabled = false;
                    refresh.removeAttribute("aria-busy");
                  }
                }
                search.addEventListener("input", render);
                sort.addEventListener("change", render);
                view.addEventListener("click", () => {
                  listLayout = !listLayout;
                  localStorage.setItem("lw-layout", listLayout ? "list" : "grid");
                  render();
                });
                refresh.addEventListener("click", load);
                load();
              })();
            </script>
        """.trimIndent()
    )

    fun player(
        name: String,
        id: String,
        mime: String,
        size: Long,
        modifiedAt: Long,
        downloads: Boolean,
    ): String = page(
        title = name,
        body = """
            <main class="player-page">
              <nav class="player-nav" aria-label="Player navigation">
                <a class="btn ghost" href="/">${icon("back")} <span>Library</span></a>
                <div class="player-title-small">${escape(name)}</div>
                <button class="icon-btn" id="copy-link" type="button" aria-label="Copy video link">${icon("link")}</button>
              </nav>
              <div class="player-layout">
                <section>
                  <div class="video-panel glass" id="video-panel">
                    <video id="player" controls playsinline preload="metadata">
                      <source src="/stream?id=${url(id)}" type="${escape(mime)}">
                    </video>
                  </div>
                  <div class="notice hidden" id="unsupported" role="alert">
                    <strong>This browser could not play the video.</strong><br>
                    The file may use a container or codec your browser does not support. ${if (downloads) "Download the original and open it with a compatible player." else "Ask the host to enable downloads or choose a browser-compatible MP4/H.264 file."}
                  </div>
                </section>
                <aside class="player-side glass">
                  <div class="eyebrow">Now playing</div>
                  <h1>${escape(name)}</h1>
                  <div class="detail-list">
                    <div class="detail-row"><span>Type</span><strong>${escape(mime)}</strong></div>
                    <div class="detail-row"><span>Size</span><strong>${formatSize(size)}</strong></div>
                    <div class="detail-row"><span>Modified</span><strong>${formatDate(modifiedAt)}</strong></div>
                    <div class="detail-row"><span>Source</span><strong>Local network</strong></div>
                  </div>
                  <div class="player-controls">
                    <button class="btn secondary" id="share" type="button">${icon("share")} Share</button>
                    <button class="btn secondary" id="fullscreen" type="button">${icon("fullscreen")} Fullscreen</button>
                    ${if (downloads) """<a class="btn" href="/download?id=${url(id)}">${icon("download")} Download</a>""" else ""}
                    <a class="btn ghost" href="/">${icon("grid")} More videos</a>
                  </div>
                  <div class="speed-control">
                    <label for="speed">Speed</label>
                    <select class="select" id="speed">
                      <option value=".75">0.75×</option>
                      <option value="1" selected>1× normal</option>
                      <option value="1.25">1.25×</option>
                      <option value="1.5">1.5×</option>
                      <option value="2">2×</option>
                    </select>
                  </div>
                </aside>
              </div>
            </main>
            <div class="toast" id="toast" role="status" aria-live="polite"></div>
            <script>
              (() => {
                "use strict";
                const player = document.getElementById("player");
                const panel = document.getElementById("video-panel");
                const toast = document.getElementById("toast");
                const showToast = message => {
                  toast.textContent = message;
                  toast.classList.add("show");
                  clearTimeout(window.localWatchToast);
                  window.localWatchToast = setTimeout(() => toast.classList.remove("show"), 2200);
                };
                async function copyLink() {
                  try {
                    await navigator.clipboard.writeText(location.href);
                    showToast("Video link copied");
                  } catch (error) {
                    const input = document.createElement("textarea");
                    input.value = location.href;
                    document.body.appendChild(input);
                    input.select();
                    document.execCommand("copy");
                    input.remove();
                    showToast("Video link copied");
                  }
                }
                player.addEventListener("error", () => document.getElementById("unsupported").classList.remove("hidden"));
                document.getElementById("copy-link").addEventListener("click", copyLink);
                document.getElementById("share").addEventListener("click", async () => {
                  if (navigator.share) {
                    try { await navigator.share({title:document.title, url:location.href}); }
                    catch (error) { if (error.name !== "AbortError") await copyLink(); }
                  } else await copyLink();
                });
                document.getElementById("fullscreen").addEventListener("click", async () => {
                  try {
                    if (document.fullscreenElement) await document.exitFullscreen();
                    else await panel.requestFullscreen();
                  } catch (error) { showToast("Fullscreen is not available in this browser"); }
                });
                document.getElementById("speed").addEventListener("change", event => {
                  player.playbackRate = Number(event.target.value);
                  showToast("Playback speed: " + event.target.options[event.target.selectedIndex].text);
                });
                document.addEventListener("keydown", event => {
                  if (event.target.matches("input, select, button, a")) return;
                  if (event.code === "Space") { event.preventDefault(); player.paused ? player.play() : player.pause(); }
                  if (event.code === "ArrowRight") player.currentTime = Math.min(player.duration || Infinity, player.currentTime + 10);
                  if (event.code === "ArrowLeft") player.currentTime = Math.max(0, player.currentTime - 10);
                });
              })();
            </script>
        """.trimIndent()
    )

    fun pin(error: Boolean = false): String = page(
        title = "Enter PIN · LocalWatch",
        body = """
            <main class="auth-page">
              <section class="auth-card glass" id="auth-card">
                <div class="auth-brand">
                  <img class="auth-logo" src="/assets/logo.png" alt="LocalWatch logo">
                </div>
                <div>
                  <div class="eyebrow">Protected watch party</div>
                  <h1>Enter the room PIN</h1>
                  <p>Ask the host for the numeric PIN used by this LocalWatch server.</p>
                </div>
                <form method="post" action="/auth" id="pin-form">
                  <div class="pin-wrap">
                    <input class="pin" id="pin" name="pin" inputmode="numeric" pattern="[0-9]+" maxlength="12" autocomplete="one-time-code" autofocus required aria-describedby="pin-error">
                    <button class="icon-btn pin-toggle" id="pin-toggle" type="button" aria-label="Show PIN">${icon("eye")}</button>
                  </div>
                  <div class="form-error" id="pin-error" role="alert">${if (error) "That PIN did not match. Try again." else ""}</div>
                  <button class="btn" id="join" type="submit">${icon("arrow")} Join LocalWatch</button>
                </form>
                <details class="help">
                  <summary>Where do I find the PIN?</summary>
                  <p>The host can view or change it in LocalWatch Server → Settings → Security. All devices must also be on the same Wi-Fi network or hotspot.</p>
                </details>
              </section>
            </main>
            <script>
              (() => {
                "use strict";
                const form = document.getElementById("pin-form");
                const input = document.getElementById("pin");
                const toggle = document.getElementById("pin-toggle");
                const join = document.getElementById("join");
                input.type = "password";
                input.addEventListener("input", () => input.value = input.value.replace(/\D/g, ""));
                toggle.addEventListener("click", () => {
                  const hidden = input.type === "password";
                  input.type = hidden ? "text" : "password";
                  toggle.setAttribute("aria-label", hidden ? "Hide PIN" : "Show PIN");
                });
                form.addEventListener("submit", event => {
                  if (!input.value) { event.preventDefault(); input.focus(); return; }
                  join.disabled = true;
                  join.textContent = "Joining…";
                });
              })();
            </script>
        """.trimIndent()
    )

    fun error(title: String, message: String, code: Int): String = page(
        title = "$code · $title",
        body = """
            <main class="auth-page">
              <section class="auth-card glass">
                <img class="auth-logo" src="/assets/logo.png" alt="LocalWatch logo">
                <div class="eyebrow" style="margin-top:16px">Error $code</div>
                <h1>${escape(title)}</h1>
                <p>${escape(message)}</p>
                <div class="state-actions">
                  <a class="btn" href="/">${icon("back")} Back to library</a>
                  <button class="btn secondary" type="button" onclick="location.reload()">${icon("refresh")} Retry</button>
                </div>
              </section>
            </main>
        """.trimIndent()
    )

    private fun appBar(serverName: String): String = """
        <header class="appbar">
          <div class="appbar-inner">
            <a class="brand" href="/" aria-label="LocalWatch library">
              <img class="brand-logo" src="/assets/logo.png" alt="">
              <span class="brand-copy"><span class="brand-title">LocalWatch</span><span class="brand-subtitle">${escape(serverName)}</span></span>
            </a>
            <div class="status-pill"><span class="status-dot"></span>Local server</div>
          </div>
        </header>
    """.trimIndent()

    private fun loadingState(): String = """
        <div class="state glass" style="grid-column:1/-1">
          <div class="state-inner"><img class="state-logo" src="/assets/logo.png" alt=""><h2>Loading your library</h2><p>Fetching the latest media list from the host phone…</p></div>
        </div>
    """.trimIndent()

    private fun page(title: String, body: String) = """
        <!doctype html>
        <html lang="en" class="dark">
          <head>
            <meta charset="utf-8">
            <meta name="viewport" content="width=device-width,initial-scale=1,viewport-fit=cover">
            <meta name="theme-color" content="#09090b">
            <meta name="color-scheme" content="dark">
            <link rel="icon" type="image/png" href="/assets/logo.png">
            <title>${escape(title)}</title>
            <style>$styles</style>
          </head>
          <body>$body</body>
        </html>
    """.trimIndent()

    private fun icon(name: String): String {
        val path = when (name) {
            "search" -> """<circle cx="11" cy="11" r="7"/><path d="m20 20-4-4"/>"""
            "list" -> """<path d="M8 6h13M8 12h13M8 18h13"/><path d="M3 6h.01M3 12h.01M3 18h.01"/>"""
            "refresh" -> """<path d="M20 7v5h-5"/><path d="M4 17a8 8 0 0 0 13.6 1L20 12M4 12l2.4-6A8 8 0 0 1 20 7"/>"""
            "play" -> """<path d="m8 5 11 7-11 7z"/>"""
            "download" -> """<path d="M12 3v12m0 0 5-5m-5 5-5-5"/><path d="M4 21h16"/>"""
            "back" -> """<path d="m15 18-6-6 6-6"/>"""
            "link" -> """<path d="M10 13a5 5 0 0 0 7.5.5l2-2a5 5 0 0 0-7-7l-1.2 1.2"/><path d="M14 11a5 5 0 0 0-7.5-.5l-2 2a5 5 0 0 0 7 7l1.2-1.2"/>"""
            "share" -> """<circle cx="18" cy="5" r="3"/><circle cx="6" cy="12" r="3"/><circle cx="18" cy="19" r="3"/><path d="m8.6 10.5 6.8-4M8.6 13.5l6.8 4"/>"""
            "fullscreen" -> """<path d="M8 3H3v5M16 3h5v5M8 21H3v-5M16 21h5v-5"/>"""
            "grid" -> """<rect x="3" y="3" width="7" height="7" rx="1"/><rect x="14" y="3" width="7" height="7" rx="1"/><rect x="3" y="14" width="7" height="7" rx="1"/><rect x="14" y="14" width="7" height="7" rx="1"/>"""
            "eye" -> """<path d="M2 12s3.5-6 10-6 10 6 10 6-3.5 6-10 6S2 12 2 12"/><circle cx="12" cy="12" r="2.5"/>"""
            "arrow" -> """<path d="M5 12h14m-6-6 6 6-6 6"/>"""
            else -> ""
        }
        return """<svg class="icon" viewBox="0 0 24 24" aria-hidden="true">$path</svg>"""
    }

    private fun formatSize(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val group = (kotlin.math.ln(bytes.toDouble()) / kotlin.math.ln(1024.0))
            .toInt()
            .coerceIn(0, units.lastIndex)
        return String.format(
            Locale.US,
            if (group == 0) "%.0f %s" else "%.1f %s",
            bytes / Math.pow(1024.0, group.toDouble()),
            units[group]
        )
    }

    private fun formatDate(value: Long): String =
        if (value <= 0L) "Unavailable"
        else SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date(value))

    private fun escape(value: String): String = value
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&#39;")

    private fun url(value: String): String = java.net.URLEncoder.encode(value, "UTF-8")
}
