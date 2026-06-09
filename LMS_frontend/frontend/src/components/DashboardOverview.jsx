import StatCard from "./StatCard";
import LevelBadge from "./LevelBadge";
import {
  AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip,
  ResponsiveContainer, PieChart, Pie, Cell, BarChart, Bar
} from "recharts";

export default function DashboardOverview({
  stats,
  pieData,
  recentLogs,
  newLogIds,
  setActivePage,
  logsByService
}) {
  // Build time series from recentLogs since there's no dedicated API for it
  const timeData = (() => {
    const buckets = {};
    (recentLogs || []).forEach(log => {
      const d = new Date(log.createdAt);
      const bucket = `${String(d.getHours()).padStart(2,"0")}:${String(Math.floor(d.getMinutes()/5)*5).padStart(2,"0")}`;
      if (!buckets[bucket]) buckets[bucket] = { time: bucket, info: 0, warnings: 0, errors: 0 };
      if (log.level === "INFO")  buckets[bucket].info++;
      if (log.level === "WARN")  buckets[bucket].warnings++;
      if (log.level === "ERROR" || log.level === "CRITICAL") buckets[bucket].errors++;
    });
    return Object.values(buckets).sort((a, b) => a.time.localeCompare(b.time));
  })();

  const serviceChartData = Object.entries(logsByService).map(([name, logs]) => ({
    name,
    logs,
  }));

  return (
    <div style={{ display: "flex", flexDirection: "column", gap: 20 }}>

    <div className="grid-4">
      <StatCard
        label="Total Logs"
        value={stats?.totalLogs?.toLocaleString() ?? "—"}
        sub="All time"
        color="#3b82f6"
        icon="📋"
      />
      <StatCard
        label="Errors"
        value={stats?.errorLogs ?? "—"}
        sub={stats?.totalLogs
          ? `${((stats.errorLogs / stats.totalLogs) * 100).toFixed(1)}% error rate`
          : "..."}
        color="#ef4444"
        icon="🔴"
      />
      <StatCard
        label="Warnings"
        value={stats?.warnLogs ?? "—"}
        sub="Needs attention"
        color="#f59e0b"
        icon="⚠️"
      />
      <StatCard
        label="Today"
        value={stats?.logsToday ?? "—"}
        sub={`${stats?.logsLast24hours ?? "—"} in last 24h`}  // ← lowercase 'h'
        color="#22c55e"
        icon="📅"
      />
    </div>

      {/* Charts row */}
      <div className="grid-2">

        {/* Area chart — built from recentLogs */}
        <div style={{ background: "rgba(255,255,255,0.02)", border: "1px solid rgba(255,255,255,0.07)", borderRadius: 12, padding: "20px" }}>
          <div style={{ fontSize: 13, fontWeight: 600, color: "#94a3b8", marginBottom: 16, display: "flex", justifyContent: "space-between" }}>
            <span>Log Volume (recent)</span>
            <span style={{ color: "#475569", fontFamily: "'JetBrains Mono'", fontSize: 11 }}>per 5 min</span>
          </div>
          {timeData.length === 0 ? (
            <div style={{ height: 180, display: "flex", alignItems: "center", justifyContent: "center", color: "#334155", fontFamily: "'JetBrains Mono'", fontSize: 12 }}>
              No data yet
            </div>
          ) : (
            <ResponsiveContainer width="100%" height={180}>
              <AreaChart data={timeData}>
                <defs>
                  <linearGradient id="gErr" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#f87171" stopOpacity={0.3} />
                    <stop offset="95%" stopColor="#f87171" stopOpacity={0} />
                  </linearGradient>
                  <linearGradient id="gWarn" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#fbbf24" stopOpacity={0.2} />
                    <stop offset="95%" stopColor="#fbbf24" stopOpacity={0} />
                  </linearGradient>
                  <linearGradient id="gInfo" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#38bdf8" stopOpacity={0.15} />
                    <stop offset="95%" stopColor="#38bdf8" stopOpacity={0} />
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.04)" />
                <XAxis dataKey="time" tick={{ fill: "#475569", fontSize: 10 }} axisLine={false} tickLine={false} />
                <YAxis tick={{ fill: "#475569", fontSize: 10 }} axisLine={false} tickLine={false} />
                <Tooltip contentStyle={{ background: "#0f172a", border: "1px solid #1e293b", borderRadius: 8, fontSize: 12 }} />
                <Area type="monotone" dataKey="info"     stroke="#38bdf8" strokeWidth={1.5} fill="url(#gInfo)" />
                <Area type="monotone" dataKey="warnings" stroke="#fbbf24" strokeWidth={1.5} fill="url(#gWarn)" />
                <Area type="monotone" dataKey="errors"   stroke="#f87171" strokeWidth={1.5} fill="url(#gErr)" />
              </AreaChart>
            </ResponsiveContainer>
          )}
        </div>

        {/* Pie chart — from /logs/count */}
        <div style={{ background: "rgba(255,255,255,0.02)", border: "1px solid rgba(255,255,255,0.07)", borderRadius: 12, padding: "20px" }}>
          <div style={{ fontSize: 13, fontWeight: 600, color: "#94a3b8", marginBottom: 16 }}>Log Level Distribution</div>
          {(!pieData || pieData.length === 0) ? (
            <div style={{ height: 160, display: "flex", alignItems: "center", justifyContent: "center", color: "#334155", fontFamily: "'JetBrains Mono'", fontSize: 12 }}>
              No data yet
            </div>
          ) : (
            <div style={{ display: "flex", gap: 16, alignItems: "center" }}>
              <ResponsiveContainer width={160} height={160}>
                <PieChart>
                  <Pie data={pieData} cx="50%" cy="50%" innerRadius={45} outerRadius={70} paddingAngle={3} dataKey="value">
                    {pieData.map((entry, i) => <Cell key={i} fill={entry.color} opacity={0.85} />)}
                  </Pie>
                  <Tooltip contentStyle={{ background: "#0f172a", border: "1px solid #1e293b", borderRadius: 8, fontSize: 12 }} />
                </PieChart>
              </ResponsiveContainer>
              <div style={{ display: "flex", flexDirection: "column", gap: 8, flex: 1 }}>
                {pieData.map(d => (
                  <div key={d.name} style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                    <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
                      <span style={{ width: 8, height: 8, borderRadius: 2, background: d.color, display: "inline-block" }} />
                      <span style={{ fontSize: 12, color: "#94a3b8", fontFamily: "'JetBrains Mono'" }}>{d.name}</span>
                    </div>
                    <span style={{ fontSize: 12, color: "#cbd5e1", fontFamily: "'JetBrains Mono'" }}>{d.value}</span>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>

      </div>

      <div style={{ background: "rgba(255,255,255,0.02)", border: "1px solid rgba(255,255,255,0.07)", borderRadius: 12, padding: "20px" }}>
        <div style={{ fontSize: 13, fontWeight: 600, color: "#94a3b8", marginBottom: 16 }}>Logs by Service</div>
        {serviceChartData.length === 0 ? (
          <div style={{ height: 160, display: "flex", alignItems: "center", justifyContent: "center", color: "#334155", fontFamily: "'JetBrains Mono'", fontSize: 12 }}>
            No service data yet
          </div>
        ) : (
          <ResponsiveContainer width="100%" height={160}>
            <BarChart data={serviceChartData} barSize={28}>
              <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.04)" vertical={false} />
              <XAxis dataKey="name" tick={{ fill: "#475569", fontSize: 11, fontFamily: "'JetBrains Mono'" }} axisLine={false} tickLine={false} />
              <YAxis tick={{ fill: "#475569", fontSize: 10 }} axisLine={false} tickLine={false} />
              <Tooltip contentStyle={{ background: "#0f172a", border: "1px solid #1e293b", borderRadius: 8, fontSize: 12 }} />
              <Bar dataKey="logs" fill="#3b82f6" opacity={0.7} radius={[3, 3, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        )}
      </div>

      {/* Recent logs — using backend field names: createdAt, level, serviceName, message */}
      <div style={{ background: "rgba(255,255,255,0.02)", border: "1px solid rgba(255,255,255,0.07)", borderRadius: 12, overflow: "hidden" }}>
        <div style={{ padding: "16px 20px", borderBottom: "1px solid rgba(255,255,255,0.06)", display: "flex", justifyContent: "space-between", alignItems: "center" }}>
          <span style={{ fontSize: 13, fontWeight: 600, color: "#94a3b8" }}>Recent Activity</span>
          <button onClick={() => setActivePage("logs")} style={{ fontSize: 11, color: "#3b82f6", background: "none", border: "none", cursor: "pointer", fontFamily: "'JetBrains Mono'" }}>
            View all →
          </button>
        </div>

        {(!recentLogs || recentLogs.length === 0) ? (
          <div style={{ padding: 32, textAlign: "center", color: "#334155", fontFamily: "'JetBrains Mono'", fontSize: 12 }}>
            No recent logs
          </div>
        ) : recentLogs.slice(0, 8).map((log, i) => (
          <div
            key={log.id}
            style={{
              padding: "11px 20px",
              display: "grid",
              gridTemplateColumns: "180px 90px 130px 1fr",
              gap: 16,
              alignItems: "center",
              borderBottom: i < 7 ? "1px solid rgba(255,255,255,0.04)" : "none",
              background: newLogIds.has(log.id) ? "rgba(59,130,246,0.05)" : "transparent",
              transition: "background 0.3s",
            }}
          >
            {/* createdAt from backend */}
            <span style={{ fontSize: 11, color: "#475569", fontFamily: "'JetBrains Mono'" }}>
              {log.createdAt
                ? new Date(log.createdAt).toLocaleString("en-IN", { dateStyle: "short", timeStyle: "medium" })
                : "—"}
            </span>
            <LevelBadge level={log.level} />
            {/* serviceName from backend */}
            <span style={{ fontSize: 11, color: "#64748b", fontFamily: "'JetBrains Mono'" }}>
              {log.serviceName ?? log.service ?? "—"}
            </span>
            <span style={{ fontSize: 12, color: "#94a3b8", overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>
              {log.message}
            </span>
          </div>
        ))}
      </div>

    </div>
  );
}