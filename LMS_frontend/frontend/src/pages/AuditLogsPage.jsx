import { useState, useEffect, useMemo } from "react";
import { fetchAuditLogs } from "../services/auditApi";
import { PieChart, Pie, Cell, ResponsiveContainer, Tooltip } from "recharts";

const PAGE_SIZE = 25;
const COLORS = ["#60a5fa", "#34d399", "#fbbf24", "#f87171", "#a78bfa"];

export default function AuditLogsPage() {
  const [logs, setLogs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [page, setPage] = useState(1);

  const [search, setSearch] = useState("");
  const [methodFilter, setMethodFilter] = useState("ALL");
  const [endpointFilter, setEndpointFilter] = useState("ALL");

  useEffect(() => {
    const loadLogs = () => {
      fetchAuditLogs()
        .then((data) => {
          const sorted = [...data].sort(
            (a, b) => new Date(b.timestamp) - new Date(a.timestamp)
          );
          setLogs(sorted);
        })
        .catch((e) => {
          if (e.response?.status === 403) setError("Admin access required");
          else setError("Failed to load audit logs");
        })
        .finally(() => setLoading(false));
    };

    loadLogs();
    const interval = setInterval(loadLogs, 5000);
    return () => clearInterval(interval);
  }, []);

  const methods = useMemo(
    () => [...new Set(logs.map((l) => l.method).filter(Boolean))],
    [logs]
  );

  const endpoints = useMemo(
    () => [...new Set(logs.map((l) => l.endpoint).filter(Boolean))],
    [logs]
  );

  const filteredLogs = useMemo(() => {
    return logs.filter((log) => {
      const searchMatch =
        !search ||
        log.username?.toLowerCase().includes(search.toLowerCase()) ||
        log.email?.toLowerCase().includes(search.toLowerCase()) ||
        log.endpoint?.toLowerCase().includes(search.toLowerCase());

      const methodMatch =
        methodFilter === "ALL" || log.method === methodFilter;

      const endpointMatch =
        endpointFilter === "ALL" || log.endpoint === endpointFilter;

      return searchMatch && methodMatch && endpointMatch;
    });
  }, [logs, search, methodFilter, endpointFilter]);

  const totalLogs = filteredLogs.length;
  const uniqueUsers = new Set(filteredLogs.map((l) => l.username)).size;

  const methodCounts = filteredLogs.reduce((acc, log) => {
    acc[log.method] = (acc[log.method] || 0) + 1;
    return acc;
  }, {});

  const pieData = Object.entries(methodCounts).map(([name, value]) => ({
    name,
    value,
  }));

  const topUser = Object.entries(
    filteredLogs.reduce((acc, log) => {
      acc[log.username] = (acc[log.username] || 0) + 1;
      return acc;
    }, {})
  ).sort((a, b) => b[1] - a[1])[0];

  const topEndpoint = Object.entries(
    filteredLogs.reduce((acc, log) => {
      acc[log.endpoint] = (acc[log.endpoint] || 0) + 1;
      return acc;
    }, {})
  ).sort((a, b) => b[1] - a[1])[0];

  const successRate =
    totalLogs > 0
      ? ((((methodCounts.GET || 0) +
          (methodCounts.POST || 0) +
          (methodCounts.PUT || 0)) /
          totalLogs) *
          100).toFixed(1)
      : 0;

  const totalPages = Math.max(
    1,
    Math.ceil(filteredLogs.length / PAGE_SIZE)
  );

  const pageLogs = filteredLogs.slice(
    (page - 1) * PAGE_SIZE,
    page * PAGE_SIZE
  );

  const goTo = (p) => setPage(Math.min(Math.max(1, p), totalPages));

  if (error) {
    return (
      <div style={{ padding: 40, color: "#f87171" }}>
        ⚠ {error}
      </div>
    );
  }

  return (
    <div style={{ display: "flex", flexDirection: "column", gap: 16 }}>

      <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fit,minmax(220px,1fr))", gap: 16 }}>
        <StatCard title="Total Logs" value={totalLogs} />
        <StatCard title="Unique Users" value={uniqueUsers} />
        <StatCard title="Success Rate" value={`${successRate}%`} />
        <StatCard title="Top User" value={topUser?.[0] || "-"} />
      </div>

      <div style={{ display: "flex", gap: 12, flexWrap: "wrap" }}>
        <input
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          placeholder="Search username, email, endpoint..."
          style={inputStyle}
        />

        <select
          value={methodFilter}
          onChange={(e) => setMethodFilter(e.target.value)}
          style={inputStyle}
        >
          <option value="ALL" style={{background: "#0f172a", color: "#e2e8f0",}}>All Methods</option>
          {methods.map((m) => (
            <option key={m} value={m} style={{background: "#0f172a", color: "#e2e8f0",}}>{m}</option>
          ))}
        </select>

        <select
          value={endpointFilter}
          onChange={(e) => setEndpointFilter(e.target.value)}
          style={inputStyle}
        >
          <option value="ALL" style={{background: "#0f172a", color: "#e2e8f0",}}>All Endpoints</option>
          {endpoints.map((e) => (
            <option key={e} value={e} style={{background: "#0f172a", color: "#e2e8f0",}}>{e}</option>
          ))}
        </select>
      </div>

      <div style={{ background: "rgba(255,255,255,0.03)", borderRadius: 12, padding: 16, height: 280 }}>
        <ResponsiveContainer width="100%" height="100%">
          <PieChart>
            <Pie data={pieData} dataKey="value" nameKey="name" outerRadius={90}>
              {pieData.map((_, i) => (
                <Cell key={i} fill={COLORS[i % COLORS.length]} />
              ))}
            </Pie>
            <Tooltip />
          </PieChart>
        </ResponsiveContainer>
      </div>

      <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 16 }}>
        <StatCard title="Top Endpoint" value={topEndpoint?.[0] || "-"} />
        <StatCard title="Most Active User" value={topUser?.[0] || "-"} />
      </div>

      <div style={{ background: "rgba(255,255,255,0.02)", border: "1px solid rgba(255,255,255,0.07)", borderRadius: 12 }}>
        <div style={{ padding: "10px 20px", display: "grid", gridTemplateColumns: "180px 120px 1fr 140px", gap: 16 }}>
          <span>TIMESTAMP</span>
          <span>USER</span>
          <span>METHOD · ENDPOINT</span>
          <span>EMAIL</span>
        </div>

        {loading ? (
          <div style={{ padding: 24 }}>Loading...</div>
        ) : (
          pageLogs.map((log) => (
            <div
              key={log.id}
              style={{
                padding: "10px 20px",
                display: "grid",
                gridTemplateColumns: "180px 120px 1fr 140px",
                gap: 16,
                borderTop: "1px solid rgba(255,255,255,0.05)",
              }}
            >
              <span>{new Date(log.timestamp).toLocaleString()}</span>
              <span>{log.username}</span>
              <span>{log.method} {log.endpoint}</span>
              <span>{log.email}</span>
            </div>
          ))
        )}

        <div style={{ padding: 12, display: "flex", justifyContent: "space-between" }}>
          <span>{filteredLogs.length} records</span>

          <div style={{ display: "flex", gap: 6 }}>
            <button onClick={() => goTo(page - 1)} disabled={page === 1}>Prev</button>
            <span>{page} / {totalPages}</span>
            <button onClick={() => goTo(page + 1)} disabled={page === totalPages}>Next</button>
          </div>
        </div>
      </div>
    </div>
  );
}

function StatCard({ title, value }) {
  return (
    <div style={{ background: "rgba(255,255,255,0.03)", border: "1px solid rgba(255,255,255,0.08)", borderRadius: 12, padding: 16 }}>
      <div style={{ color: "#64748b", fontSize: 12 }}>{title}</div>
      <div style={{ color: "#e2e8f0", fontSize: 24, fontWeight: 700 }}>{value}</div>
    </div>
  );
}

const inputStyle = {
  background: "rgba(255,255,255,0.04)",
  border: "1px solid rgba(255,255,255,0.08)",
  borderRadius: 8,
  padding: "10px 12px",
  color: "#e2e8f0",
  fontSize: 12,
  outline: "none"
};
