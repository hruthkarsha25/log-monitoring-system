import { useState, useEffect } from "react";
import { fetchLogStats } from "../services/logApi";

export default function ServicesPage() {
  const [stats, setStats]   = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchLogStats()
      .then(setStats)
      .catch(e => console.error("Failed to load service stats", e))
      .finally(() => setLoading(false));
  }, []);

  // Build service rows from logsByService + derive errors from logsByLevel per service
  // Since backend gives total counts per service (all levels combined),
  // we use logsByService for total logs and estimate health by name heuristics
  // For real error-per-service breakdown you'd need a dedicated endpoint
  const serviceRows = stats?.logsByService
    ? Object.entries(stats.logsByService).map(([name, totalLogs]) => ({
        name,
        totalLogs,
      }))
    : [];

  const totalLogs = stats?.totalLogs ?? 0;
  const totalErrors = stats?.errorLogs ?? 0;

  return (
    <div style={{ display: "flex", flexDirection: "column", gap: 16 }}>

      {/* Summary cards */}
      <div style={{ display: "grid", gridTemplateColumns: "repeat(3, 1fr)", gap: 16, marginBottom: 4 }}>
        {[
          { label: "Total Services", value: serviceRows.length, color: "#3b82f6" },
          { label: "Total Log Events", value: totalLogs.toLocaleString(), color: "#38bdf8" },
          { label: "Total Errors", value: totalErrors.toLocaleString(), color: "#f87171" },
        ].map(c => (
          <div key={c.label} style={{
            background: "rgba(255,255,255,0.02)",
            border: "1px solid rgba(255,255,255,0.07)",
            borderRadius: 12, padding: "18px 22px",
            borderTop: `2px solid ${c.color}`,
          }}>
            <div style={{ fontSize: 11, color: "#64748b", fontFamily: "'JetBrains Mono'", letterSpacing: "0.06em", textTransform: "uppercase", marginBottom: 8 }}>{c.label}</div>
            <div style={{ fontSize: 28, fontWeight: 700, color: "#f1f5f9" }}>{c.value}</div>
          </div>
        ))}
      </div>

      {/* Service list */}
      <div style={{ background: "rgba(255,255,255,0.02)", border: "1px solid rgba(255,255,255,0.07)", borderRadius: 12, overflow: "hidden" }}>

        {/* Table header */}
        <div style={{ padding: "10px 22px", display: "grid", gridTemplateColumns: "1fr 120px 120px 120px 160px", gap: 16, background: "rgba(255,255,255,0.03)", borderBottom: "1px solid rgba(255,255,255,0.06)" }}>
          {["SERVICE", "TOTAL LOGS", "LOG SHARE", "STATUS", "ACTIVITY"].map(h => (
            <span key={h} style={{ fontSize: 10, color: "#475569", fontFamily: "'JetBrains Mono'", fontWeight: 600, letterSpacing: "0.08em" }}>{h}</span>
          ))}
        </div>

        {loading ? (
          <div style={{ padding: 48, textAlign: "center", color: "#334155", fontFamily: "'JetBrains Mono'", fontSize: 12 }}>Loading services...</div>
        ) : serviceRows.length === 0 ? (
          <div style={{ padding: 48, textAlign: "center", color: "#334155", fontFamily: "'JetBrains Mono'", fontSize: 12 }}>No service data</div>
        ) : serviceRows
            .sort((a, b) => b.totalLogs - a.totalLogs)
            .map((svc, i) => {
              const sharePercent = totalLogs > 0 ? ((svc.totalLogs / totalLogs) * 100) : 0;
              // Heuristic: services with very high log share might be noisy/degraded
              const isActive = svc.totalLogs > 0;

              return (
                <div key={svc.name} style={{
                  padding: "14px 22px",
                  display: "grid",
                  gridTemplateColumns: "1fr 120px 120px 120px 160px",
                  gap: 16,
                  alignItems: "center",
                  borderBottom: i < serviceRows.length - 1 ? "1px solid rgba(255,255,255,0.03)" : "none",
                }}>
                  {/* Service name */}
                  <div style={{ display: "flex", alignItems: "center", gap: 12 }}>
                    <div style={{ width: 8, height: 8, borderRadius: "50%", background: isActive ? "#22c55e" : "#475569", flexShrink: 0 }} />
                    <span style={{ fontWeight: 600, fontFamily: "'JetBrains Mono'", fontSize: 13, color: "#e2e8f0" }}>{svc.name}</span>
                  </div>

                  {/* Total logs */}
                  <span style={{ fontSize: 15, fontWeight: 700, color: "#f1f5f9" }}>
                    {svc.totalLogs.toLocaleString()}
                  </span>

                  {/* Log share % */}
                  <div>
                    <div style={{ fontSize: 13, fontWeight: 600, color: "#60a5fa", marginBottom: 4 }}>
                      {sharePercent.toFixed(1)}%
                    </div>
                    {/* Mini progress bar */}
                    <div style={{ height: 3, background: "rgba(255,255,255,0.06)", borderRadius: 2, width: 80 }}>
                      <div style={{ height: 3, background: "#3b82f6", borderRadius: 2, width: `${Math.min(sharePercent, 100)}%` }} />
                    </div>
                  </div>

                  {/* Status */}
                  <span style={{
                    fontSize: 11, fontFamily: "'JetBrains Mono'", fontWeight: 600,
                    color: isActive ? "#22c55e" : "#475569",
                    background: isActive ? "rgba(34,197,94,0.1)" : "rgba(71,85,105,0.1)",
                    border: `1px solid ${isActive ? "rgba(34,197,94,0.25)" : "rgba(71,85,105,0.25)"}`,
                    padding: "3px 10px", borderRadius: 4, display: "inline-block",
                  }}>
                    {isActive ? "● Active" : "○ Idle"}
                  </span>

                  {/* Mini sparkline bar (relative activity) */}
                  <div style={{ display: "flex", alignItems: "center", gap: 6 }}>
                    <div style={{ flex: 1, height: 6, background: "rgba(255,255,255,0.04)", borderRadius: 3 }}>
                      <div style={{
                        height: 6, borderRadius: 3,
                        width: `${Math.min((svc.totalLogs / Math.max(...serviceRows.map(s => s.totalLogs))) * 100, 100)}%`,
                        background: "linear-gradient(90deg, #3b82f6, #8b5cf6)",
                      }} />
                    </div>
                    <span style={{ fontSize: 10, color: "#475569", fontFamily: "'JetBrains Mono'", width: 30, textAlign: "right" }}>
                      {svc.totalLogs > 999 ? `${(svc.totalLogs / 1000).toFixed(1)}k` : svc.totalLogs}
                    </span>
                  </div>
                </div>
              );
            })}
      </div>

      {/* Level breakdown */}
      {stats?.logsByLevel && Object.keys(stats.logsByLevel).length > 0 && (
        <div style={{ background: "rgba(255,255,255,0.02)", border: "1px solid rgba(255,255,255,0.07)", borderRadius: 12, padding: "18px 22px" }}>
          <div style={{ fontSize: 13, fontWeight: 600, color: "#94a3b8", marginBottom: 16 }}>Overall Log Level Breakdown</div>
          <div style={{ display: "flex", gap: 12, flexWrap: "wrap" }}>
            {Object.entries(stats.logsByLevel).map(([level, count]) => {
              const colors = { INFO: "#38bdf8", WARN: "#fbbf24", ERROR: "#f87171", DEBUG: "#a78bfa", CRITICAL: "#fb923c" };
              const color = colors[level] ?? "#94a3b8";
              return (
                <div key={level} style={{
                  background: `${color}11`, border: `1px solid ${color}33`,
                  borderRadius: 10, padding: "12px 20px", minWidth: 100, textAlign: "center",
                }}>
                  <div style={{ fontSize: 22, fontWeight: 700, color }}>{count.toLocaleString()}</div>
                  <div style={{ fontSize: 11, color: "#64748b", fontFamily: "'JetBrains Mono'", marginTop: 4 }}>{level}</div>
                </div>
              );
            })}
          </div>
        </div>
      )}
    </div>
  );
}