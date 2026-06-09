import { levelConfig } from "../services/dashboasdData";
import LevelBadge from "./LevelBadge";

export default function LogsPage({
  search,
  setSearch,
  filters,
  activeFilter,
  setActiveFilter,
  filteredLogs,
  newLogIds, totalElements, loading, page, setPage
}) {
  return (
    <>
          <div style={{ display: "flex", flexDirection: "column", gap: 16 }}>
            {/* Controls */}
            <div style={{ display: "flex", gap: 12, flexWrap: "wrap", alignItems: "center" }}>
              {/* Search */}
              <div style={{ position: "relative", flex: 1, minWidth: 200 }}>
                <span style={{ position: "absolute", left: 12, top: "50%", transform: "translateY(-50%)", color: "#475569", fontSize: 14 }}>⌕</span>
                <input value={search} onChange={e => setSearch(e.target.value)} placeholder="Search logs..." style={{
                  width: "100%", padding: "9px 12px 9px 34px", background: "rgba(255,255,255,0.03)",
                  border: "1px solid rgba(255,255,255,0.08)", borderRadius: 8, color: "#e2e8f0",
                  fontSize: 13, fontFamily: "'JetBrains Mono'", outline: "none",
                }} />
              </div>
              {/* Filter buttons */}
              <div style={{ display: "flex", gap: 6, flexWrap: "wrap" }}>
                {filters.map(f => {
                  const cfg = f === "ALL" ? { color: "#94a3b8" } : levelConfig[f];
                  return (
                    <button key={f} className="filter-btn" onClick={() => setActiveFilter(f)} style={{
                      padding: "7px 14px", borderRadius: 6, fontSize: 11, fontFamily: "'JetBrains Mono'",
                      fontWeight: 600, cursor: "pointer", letterSpacing: "0.05em",
                      background: activeFilter === f ? (f === "ALL" ? "rgba(148,163,184,0.15)" : levelConfig[f].bg) : "transparent",
                      border: `1px solid ${activeFilter === f ? (f === "ALL" ? "#94a3b8" : cfg.color) : "rgba(255,255,255,0.07)"}`,
                      color: activeFilter === f ? (f === "ALL" ? "#94a3b8" : cfg.color) : "#475569",
                      opacity: activeFilter === f ? 1 : 0.7,
                    }}>
                      {f}
                    </button>
                  );
                })}
              </div>
              <span style={{ fontSize: 11, color: "#475569", fontFamily: "'JetBrains Mono'" }}>{filteredLogs.length} entries</span>
            </div>

            {/* Log table */}
            <div style={{ background: "rgba(255,255,255,0.02)", border: "1px solid rgba(255,255,255,0.07)", borderRadius: 12, overflow: "hidden" }}>
              {/* Table header */}
              <div style={{ padding: "10px 20px", display: "grid", gridTemplateColumns: "140px 90px 130px 1fr", gap: 16, background: "rgba(255,255,255,0.03)", borderBottom: "1px solid rgba(255,255,255,0.06)" }}>
                {["TIMESTAMP", "LEVEL", "SERVICE", "MESSAGE"].map(h => (
                  <span key={h} style={{ fontSize: 10, color: "#475569", fontFamily: "'JetBrains Mono'", fontWeight: 600, letterSpacing: "0.08em" }}>{h}</span>
                ))}
              </div>
              {/* Scrollable log rows */}
              <div style={{ maxHeight: "calc(100vh - 280px)", overflowY: "auto" }}>
                {filteredLogs.length === 0 ? (
                  <div style={{ padding: 48, textAlign: "center", color: "#334155", fontFamily: "'JetBrains Mono'", fontSize: 13 }}>No logs match the filter</div>
                ) : filteredLogs.map((log, i) => (
                  <div key={log.id} className={`log-row ${newLogIds.has(log.id) ? "new-log" : ""}`} style={{
                    padding: "10px 20px", display: "grid", gridTemplateColumns: "140px 90px 130px 1fr",
                    gap: 16, alignItems: "center", borderBottom: "1px solid rgba(255,255,255,0.03)",
                    background: newLogIds.has(log.id) ? "rgba(59,130,246,0.05)" : log.level === "CRITICAL" ? "rgba(251,146,60,0.03)" : log.level === "ERROR" ? "rgba(248,113,113,0.02)" : "transparent",
                  }}>
                    <span style={{ fontSize: 11, color: "#475569", fontFamily: "'JetBrains Mono'" }}>
                      {log.createdAt
                        ? new Date(log.createdAt).toLocaleString("en-IN", { dateStyle: "short", timeStyle: "medium" })
                        : "—"}
                    </span>                    
                    <LevelBadge level={log.level} />
                    <span style={{ fontSize: 11, color: "#64748b", fontFamily: "'JetBrains Mono'" }}>
                      {log.serviceName ?? "—"}
                    </span>
                    <span style={{ fontSize: 12, color: "#94a3b8" }}>{log.message}</span>
                  </div>
                ))}
              </div>
            </div>

          <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", padding: "12px 20px", borderTop: "1px solid rgba(255,255,255,0.06)", fontSize: 12, color: "#475569", fontFamily: "'JetBrains Mono'" }}>
            <span>{totalElements} total entries</span>
            <div style={{ display: "flex", gap: 8 }}>
              <button onClick={() => setPage(p => Math.max(0, p - 1))} disabled={page === 0}
                style={{ padding: "4px 12px", borderRadius: 6, background: "rgba(255,255,255,0.04)", border: "1px solid rgba(255,255,255,0.08)", color: "#94a3b8", cursor: page === 0 ? "not-allowed" : "pointer" }}>
                ← Prev
              </button>
              <span style={{ padding: "4px 8px" }}>Page {page + 1}</span>
              <button onClick={() => setPage(p => p + 1)} disabled={(page + 1) * 50 >= totalElements}
                style={{ padding: "4px 12px", borderRadius: 6, background: "rgba(255,255,255,0.04)", border: "1px solid rgba(255,255,255,0.08)", color: "#94a3b8", cursor: (page + 1) * 50 >= totalElements ? "not-allowed" : "pointer" }}>
                Next →
              </button>
            </div>
          </div>
          </div>
    </>
  );
}