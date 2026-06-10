
export default function Header({
  activePage,
  liveMode,
  setLiveMode,
  onExportExcel,
  onExportCSV,
}) {
  return (
    <div
      style={{
        display: "flex",
        justifyContent: "space-between",
        alignItems: "center",
        marginBottom: 28,
      }}
    >
      <div>
        <h1
          style={{
            fontSize: 22,
            fontWeight: 700,
            letterSpacing: "-0.03em",
            color: "#f1f5f9",
          }}
        >
          {activePage === "dashboard" && "System Overview"}
          {activePage === "logs" && "Log Explorer"}
          {activePage === "alerts" && "Alerts Center"}
          {activePage === "services" && "Services"}
          {activePage === "settings" && "Settings"}
          {activePage === "users"  && "Users"}
          {activePage === "audit"  && "Audit Logs"}
        </h1>

        <p
          style={{
            color: "#475569",
            fontSize: 12,
            fontFamily: "'JetBrains Mono'",
            marginTop: 4,
          }}
        >
          {new Date().toLocaleDateString("en-IN", {
            weekday: "long",
            year: "numeric",
            month: "short",
            day: "numeric",
          })}
          {" · "}
          {new Date().toLocaleTimeString("en-IN")}
        </p>
      </div>

      <div
        style={{
          display: "flex",
          alignItems: "center",
          gap: 12,
        }}
      >
        <button
          onClick={() => setLiveMode((v) => !v)}
          style={{
            display: "flex",
            alignItems: "center",
            gap: 8,
            padding: "8px 16px",
            borderRadius: 8,
            border: `1px solid ${
              liveMode ? "#22c55e33" : "#334155"
            }`,
            background: liveMode
              ? "rgba(34,197,94,0.08)"
              : "rgba(255,255,255,0.03)",
            color: liveMode ? "#22c55e" : "#64748b",
            cursor: "pointer",
            fontSize: 12,
            fontFamily: "'JetBrains Mono'",
            fontWeight: 600,
          }}
        >
          <span
            className={liveMode ? "live-dot" : ""}
            style={{
              width: 7,
              height: 7,
              borderRadius: "50%",
              background: liveMode ? "#22c55e" : "#475569",
              display: "inline-block",
            }}
          />

          {liveMode ? "LIVE" : "PAUSED"}
        </button>

        <select
          onChange={(e) => {
            if (e.target.value === "excel") {
              onExportExcel();
            }

            if (e.target.value === "csv") {
              onExportCSV();
            }

            e.target.value = "";
          }}
          style={{
            padding: "8px 12px",
            borderRadius: 8,
            border: "1px solid rgba(255,255,255,0.08)",
            background: "rgba(255,255,255,0.03)",
            color: "#94a3b8",
            cursor: "pointer",
            fontSize: 12,
          }}
        >
          <option value="">⇣ Export</option>
          <option value="excel">Export Excel</option>
          <option value="csv">Export CSV</option>
        </select>
      </div>
    </div>
  );
}