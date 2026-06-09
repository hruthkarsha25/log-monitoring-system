import { useContext } from "react";
import { AuthContext } from "../context/AuthContext";

export default function SettingsPage() {
  const { logout } = useContext(AuthContext);

  const settings = [
    {
      section: "System",
      items: [
        { label: "Log Retention",     desc: "How long logs are kept in database",  value: "Persistent" },
        { label: "Refresh Rate",      desc: "Live dashboard polling interval",      value: "5 seconds" },
        { label: "Timezone",          desc: "Display timezone",                     value: "IST (UTC+5:30)" },
        { label: "Backend",           desc: "API base URL",                         value: "localhost:8080" },
      ]
    },
    {
      section: "Log Levels",
      items: [
        { label: "Tracked Levels",    desc: "Log levels captured by the system",   value: "INFO · WARN · ERROR" },
        { label: "Alert Triggers",    desc: "Levels that generate alerts",          value: "ERROR_SPIKE · LOGIN_FAILURE · TRAFFIC_SPIKE" },
      ]
    },
    {
      section: "Security",
      items: [
        { label: "Access Token",      desc: "JWT expiration",                       value: "24 hours" },
        { label: "Refresh Token",     desc: "Refresh token expiration",             value: "7 days" },
        { label: "Auth",              desc: "Authentication strategy",              value: "JWT Bearer" },
        { label: "Admin Endpoints",   desc: "Restricted to role",                   value: "ADMIN only" },
      ]
    },
  ];

  return (
    <div style={{ display: "flex", flexDirection: "column", gap: 28, maxWidth: 600 }}>

      {settings.map(group => (
        <div key={group.section}>
          {/* Section header */}
          <div style={{
            fontSize: 11, fontWeight: 600, color: "#475569",
            fontFamily: "'JetBrains Mono'", letterSpacing: "0.08em",
            textTransform: "uppercase", marginBottom: 10,
          }}>
            {group.section}
          </div>

          <div style={{ display: "flex", flexDirection: "column", gap: 8 }}>
            {group.items.map((s, i) => (
              <div key={i} style={{
                background: "rgba(255,255,255,0.02)",
                border: "1px solid rgba(255,255,255,0.07)",
                borderRadius: 10, padding: "14px 18px",
                display: "flex", justifyContent: "space-between", alignItems: "center",
              }}>
                <div>
                  <div style={{ fontWeight: 600, fontSize: 13, color: "#e2e8f0" }}>{s.label}</div>
                  <div style={{ color: "#475569", fontSize: 11, fontFamily: "'JetBrains Mono'", marginTop: 3 }}>{s.desc}</div>
                </div>
                <div style={{
                  color: "#60a5fa", fontFamily: "'JetBrains Mono'", fontSize: 11,
                  background: "rgba(59,130,246,0.1)", padding: "4px 12px",
                  borderRadius: 6, border: "1px solid rgba(59,130,246,0.2)",
                  whiteSpace: "nowrap", marginLeft: 16,
                }}>
                  {s.value}
                </div>
              </div>
            ))}
          </div>
        </div>
      ))}

      {/* Logout */}
      <div style={{ paddingTop: 8 }}>
        <div style={{ fontSize: 11, fontWeight: 600, color: "#475569", fontFamily: "'JetBrains Mono'", letterSpacing: "0.08em", textTransform: "uppercase", marginBottom: 10 }}>
          Account
        </div>
        <button
          onClick={logout}
          style={{
            padding: "10px 24px", borderRadius: 8, cursor: "pointer",
            background: "rgba(239,68,68,0.1)", border: "1px solid rgba(239,68,68,0.25)",
            color: "#f87171", fontSize: 13, fontFamily: "'JetBrains Mono'", fontWeight: 600,
          }}
        >
          Sign Out
        </button>
      </div>
    </div>
  );
}