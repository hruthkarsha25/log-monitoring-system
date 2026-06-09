import { useState, useEffect } from "react";
import { getAlerts, acknowledgeAlert, resolveAlert } from "../services/alertApi";
import { levelConfig } from "../services/dashboasdData";
import LevelBadge from "./LevelBadge";
import StatCard from "./StatCard";

// Map backend Severity enum → levelConfig keys
const severityToLevel = {
  HIGH:   "ERROR",
  MEDIUM: "WARN",
  LOW:    "INFO",
  CRITICAL: "ERROR",
};

// Map AlertStatus → display badge color
const statusStyles = {
  OPEN:         { color: "#f87171", bg: "rgba(248,113,113,0.1)",  border: "rgba(248,113,113,0.25)" },
  ACKNOWLEDGED: { color: "#fbbf24", bg: "rgba(251,191,36,0.1)",   border: "rgba(251,191,36,0.25)"  },
  RESOLVED:     { color: "#22c55e", bg: "rgba(34,197,94,0.1)",    border: "rgba(34,197,94,0.25)"   },
};

export default function AlertsPage({ setAlertCount }) {
  const [alerts, setAlerts]   = useState([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter]   = useState("ALL"); // ALL | OPEN | ACKNOWLEDGED | RESOLVED

  const load = async () => {
    try {
      const data = await getAlerts();
      setAlerts(data);
      // sync badge count in sidebar
      const openCount = data.filter(a => a.status === "OPEN").length;
      setAlertCount(openCount);
    } catch (e) {
      console.error("Failed to load alerts", e);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, []);

  const handleAcknowledge = async (id) => {
    try {
      await acknowledgeAlert(id);
      await load(); // refresh list
    } catch (e) {
      console.error("Acknowledge failed", e);
    }
  };

  const handleResolve = async (id) => {
    try {
      await resolveAlert(id);
      await load();
    } catch (e) {
      console.error("Resolve failed", e);
    }
  };

  const openCount     = alerts.filter(a => a.status === "OPEN").length;
  const resolvedCount = alerts.filter(a => a.status === "RESOLVED").length;
  const ackCount      = alerts.filter(a => a.status === "ACKNOWLEDGED").length;

  const filtered = filter === "ALL"
    ? alerts
    : alerts.filter(a => a.status === filter);

  const FILTER_TABS = ["ALL", "OPEN", "ACKNOWLEDGED", "RESOLVED"];

  return (
    <div style={{ display: "flex", flexDirection: "column", gap: 16 }}>

      {/* Stat cards */}
      <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr 1fr", gap: 16 }}>
        <StatCard label="Open Alerts"        value={openCount}     sub="Requires action"   color="#ef4444" icon="🚨" />
        <StatCard label="Acknowledged"        value={ackCount}      sub="Being investigated" color="#f59e0b" icon="👁" />
        <StatCard label="Resolved"            value={resolvedCount} sub="Closed alerts"     color="#22c55e" icon="✅" />
      </div>

      {/* Filter tabs */}
      <div style={{ display: "flex", gap: 6 }}>
        {FILTER_TABS.map(tab => (
          <button key={tab} onClick={() => setFilter(tab)} style={{
            padding: "7px 16px", borderRadius: 6, fontSize: 11,
            fontFamily: "'JetBrains Mono'", fontWeight: 600, cursor: "pointer",
            letterSpacing: "0.05em",
            background: filter === tab ? "rgba(59,130,246,0.15)" : "transparent",
            border: `1px solid ${filter === tab ? "#3b82f6" : "rgba(255,255,255,0.07)"}`,
            color: filter === tab ? "#60a5fa" : "#475569",
          }}>
            {tab}
            {tab !== "ALL" && (
              <span style={{
                marginLeft: 6, fontSize: 10,
                background: "rgba(255,255,255,0.08)",
                padding: "1px 5px", borderRadius: 8,
              }}>
                {alerts.filter(a => a.status === tab).length}
              </span>
            )}
          </button>
        ))}
      </div>

      {/* Alert list */}
      {loading ? (
        <div style={{ padding: 48, textAlign: "center", color: "#334155", fontFamily: "'JetBrains Mono'", fontSize: 12 }}>
          Loading alerts...
        </div>
      ) : filtered.length === 0 ? (
        <div style={{ padding: 48, textAlign: "center", color: "#334155", fontFamily: "'JetBrains Mono'", fontSize: 12 }}>
          No {filter !== "ALL" ? filter.toLowerCase() : ""} alerts
        </div>
      ) : filtered.map((a) => {
        const level     = severityToLevel[a.severity] ?? "WARN";
        const lvlCfg    = levelConfig[level] ?? levelConfig.WARN;
        const statusSty = statusStyles[a.status] ?? statusStyles.OPEN;

        return (
          <div key={a.id} style={{
            background: "rgba(255,255,255,0.02)",
            border: `1px solid ${lvlCfg.color}22`,
            borderRadius: 12, padding: "18px 20px",
            display: "flex", justifyContent: "space-between",
            alignItems: "flex-start", gap: 16,
          }}>
            {/* Left accent bar + content */}
            <div style={{ display: "flex", gap: 14, flex: 1 }}>
              <div style={{ width: 2, background: lvlCfg.color, borderRadius: 2, flexShrink: 0 }} />
              <div style={{ flex: 1 }}>

                {/* Title row */}
                <div style={{ display: "flex", alignItems: "center", gap: 10, marginBottom: 6, flexWrap: "wrap" }}>
                  <span style={{ fontWeight: 600, fontSize: 14, color: "#e2e8f0" }}>
                    {a.type?.replace(/_/g, " ") ?? "Alert"}
                  </span>
                  <LevelBadge level={level} />
                  {/* Status badge */}
                  <span style={{
                    fontSize: 10, fontFamily: "'JetBrains Mono'", fontWeight: 600,
                    padding: "2px 8px", borderRadius: 4,
                    background: statusSty.bg,
                    border: `1px solid ${statusSty.border}`,
                    color: statusSty.color,
                  }}>
                    {a.status}
                  </span>
                </div>

                {/* Meta row */}
                <div style={{ fontSize: 12, color: "#64748b", fontFamily: "'JetBrains Mono'", marginBottom: 6 }}>
                  {a.endpoint ?? "—"} · {a.severity} · {
                    a.timestamp
                      ? new Date(a.timestamp).toLocaleString("en-IN", { dateStyle: "short", timeStyle: "medium" })
                      : "—"
                  }
                </div>

                {/* Message */}
                <div style={{ fontSize: 12, color: "#94a3b8" }}>{a.message ?? "—"}</div>

                {/* Resolved at */}
                {a.resolvedAt && (
                  <div style={{ fontSize: 11, color: "#22c55e", fontFamily: "'JetBrains Mono'", marginTop: 6 }}>
                    ✓ Resolved at {new Date(a.resolvedAt).toLocaleString("en-IN", { dateStyle: "short", timeStyle: "medium" })}
                  </div>
                )}
              </div>
            </div>

            {/* Action buttons */}
            <div style={{ display: "flex", flexDirection: "column", gap: 6, flexShrink: 0 }}>
              {a.status === "OPEN" && (
                <button onClick={() => handleAcknowledge(a.id)} style={{
                  padding: "6px 14px", borderRadius: 6,
                  background: "rgba(251,191,36,0.1)",
                  border: "1px solid rgba(251,191,36,0.25)",
                  color: "#fbbf24", cursor: "pointer",
                  fontSize: 11, fontFamily: "'JetBrains Mono'",
                  whiteSpace: "nowrap",
                }}>
                  Acknowledge
                </button>
              )}
              {(a.status === "OPEN" || a.status === "ACKNOWLEDGED") && (
                <button onClick={() => handleResolve(a.id)} style={{
                  padding: "6px 14px", borderRadius: 6,
                  background: "rgba(34,197,94,0.1)",
                  border: "1px solid rgba(34,197,94,0.25)",
                  color: "#22c55e", cursor: "pointer",
                  fontSize: 11, fontFamily: "'JetBrains Mono'",
                  whiteSpace: "nowrap",
                }}>
                  Resolve
                </button>
              )}
              {a.status === "RESOLVED" && (
                <span style={{ fontSize: 11, color: "#334155", fontFamily: "'JetBrains Mono'" }}>
                  Closed
                </span>
              )}
            </div>
          </div>
        );
      })}
    </div>
  );
}