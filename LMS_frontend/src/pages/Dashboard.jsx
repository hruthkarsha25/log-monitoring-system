import { useState, useEffect, useCallback } from "react";
import LevelBadge from "../components/LevelBadge";
import StatCard from "../components/StatCard";
import Sidebar from "../components/Sidebar";
import Header from "../components/Header";
import DashboardOverview from "../components/DashboardOverview";
import AlertsPage from "../components/AlertsPage";
import ServicesPage from "../components/ServicesPage";
import SettingsPage from "../components/SettingsPage";
import LogsPage from "../components/LogsPage";
import { fetchLogStats, fetchLogCount, fetchAllLogs, searchLogs } from "../services/logApi";
import AuditLogsPage from "./AuditLogsPage";
import UsersPage from "./Users";
import { exportExcel, exportCsv } from "../services/exportApi";

const FILTERS = ["ALL", "INFO", "WARN", "ERROR"];

export default function Dashboard() {
  const [activePage, setActivePage]   = useState("dashboard");
  const [liveMode, setLiveMode]       = useState(true);
  const [alertCount, setAlertCount] = useState(0);

  // --- Stats (for stat cards) ---
  const [stats, setStats] = useState(null);

  // --- Count (for pie chart) ---
  const [logCounts, setLogCounts] = useState({});

  // --- Recent logs (for dashboard feed) ---
  const [recentLogs, setRecentLogs]   = useState([]);
  const [newLogIds, setNewLogIds]     = useState(new Set());

  // --- Log explorer state ---
  const [search, setSearch]           = useState("");
  const [activeFilter, setActiveFilter] = useState("ALL");
  const [explorerLogs, setExplorerLogs] = useState([]);
  const [totalElements, setTotalElements] = useState(0);
  const [page, setPage]               = useState(0);
  const [loading, setLoading]         = useState(false);

  // ── Fetch stat cards ──────────────────────────────────────────────
  const loadStats = useCallback(async () => {
    try {
      const data = await fetchLogStats();
      setStats(data);
    } catch (e) {
      console.error("Failed to load stats", e);
    }
  }, []);

  // ── Fetch pie chart counts ────────────────────────────────────────
  const loadCounts = useCallback(async () => {
    try {
      const data = await fetchLogCount(); // { INFO: 120, WARN: 40, ERROR: 10, ... }
      setLogCounts(data);
    } catch (e) {
      console.error("Failed to load counts", e);
    }
  }, []);

  // ── Fetch recent logs (dashboard feed) ───────────────────────────
  const loadRecentLogs = useCallback(async () => {
    try {
      const data = await fetchAllLogs(0, 8);
      const incoming = data.content ?? [];
      // highlight newly arrived logs
      const prevIds = new Set(recentLogs.map(l => l.id));
      const freshIds = new Set(incoming.filter(l => !prevIds.has(l.id)).map(l => l.id));
      if (freshIds.size > 0) {
        setNewLogIds(freshIds);
        setTimeout(() => setNewLogIds(new Set()), 2000);
      }
      setRecentLogs(incoming);
    } catch (e) {
      console.error("Failed to load recent logs", e);
    }
  }, [recentLogs]);

  // ── Fetch log explorer (search + filter) ─────────────────────────
  const loadExplorerLogs = useCallback(async () => {
    setLoading(true);
    try {
      const params = {
        page,
        size: 50,
        ...(activeFilter !== "ALL" && { level: activeFilter }),
        ...(search.trim() && { serviceName: search.trim() }),
      };
      const data = await searchLogs(params);
      setExplorerLogs(data.content ?? []);
      setTotalElements(data.totalElements ?? 0);
    } catch (e) {
      console.error("Failed to load explorer logs", e);
    } finally {
      setLoading(false);
    }
  }, [page, activeFilter, search]);

  // ── Initial load ──────────────────────────────────────────────────
  useEffect(() => {
    loadStats();
    loadCounts();
    loadRecentLogs();
  }, []);

  // ── Live mode polling ─────────────────────────────────────────────
  useEffect(() => {
    if (!liveMode) return;
    const interval = setInterval(() => {
      loadStats();
      loadCounts();
      loadRecentLogs();
    }, 5000);
    return () => clearInterval(interval);
  }, [liveMode, loadStats, loadCounts, loadRecentLogs]);

  // ── Log explorer: refetch on filter/search/page change ───────────
  useEffect(() => {
    if (activePage === "logs") loadExplorerLogs();
  }, [activePage, activeFilter, search, page]);

  // ── Derive pie data from /logs/count response ─────────────────────
  const pieData = Object.entries(logCounts).map(([name, value]) => ({
    name,
    value,
    color: {
      INFO: "#38bdf8", WARN: "#fbbf24", ERROR: "#f87171",
      DEBUG: "#a78bfa", CRITICAL: "#fb923c",
    }[name] ?? "#94a3b8",
  }));

  const handleExportExcel = async () => {
  try {
    const response = await exportExcel();

    const url = window.URL.createObjectURL(
      new Blob([response.data])
    );

    const link = document.createElement("a");

    link.href = url;
    link.download = "logs.xlsx";

    document.body.appendChild(link);
    link.click();
    link.remove();
  } catch (error) {
    console.error(error);
    alert("Excel export failed");
  }
};

const handleExportCsv = async () => {
  try {
    const response = await exportCsv();

    const url = window.URL.createObjectURL(
      new Blob([response.data])
    );

    const link = document.createElement("a");

    link.href = url;
    link.download = "logs.csv";

    document.body.appendChild(link);
    link.click();
    link.remove();
  } catch (error) {
    console.error(error);
    alert("CSV export failed");
  }
};

  return (
    <div style={{ minHeight: "100vh", display: "flex", background: "#0a0f1e", color: "#e2e8f0" }}>
      <Sidebar activePage={activePage} setActivePage={setActivePage} alertCount={alertCount} />

      <main style={{ flex: 1, marginLeft: "220px", padding: "28px 32px", overflow: "auto", height: "100vh" }}>
        <Header
          activePage={activePage}
          liveMode={liveMode}
          setLiveMode={setLiveMode}
          onExportExcel={handleExportExcel}
          onExportCsv={handleExportCsv}
        />

        {activePage === "dashboard" && (
          <DashboardOverview
            stats={stats}
            pieData={pieData}
            recentLogs={recentLogs}
            newLogIds={newLogIds}
            setActivePage={setActivePage}
            logsByService={stats?.logsByService ?? {}}
          />
        )}

        {activePage === "logs" && (
          <LogsPage
            search={search}
            setSearch={(v) => { setSearch(v); setPage(0); }}
            filters={FILTERS}
            activeFilter={activeFilter}
            setActiveFilter={(f) => { setActiveFilter(f); setPage(0); }}
            filteredLogs={explorerLogs}
            newLogIds={newLogIds}
            totalElements={totalElements}
            loading={loading}
            page={page}
            setPage={setPage}
          />
        )}

        {activePage === "alerts"   && <AlertsPage setAlertCount={setAlertCount} />}
        {activePage === "services" && <ServicesPage />}
        {activePage === "settings" && <SettingsPage />}
        {activePage === "users" && <UsersPage />}
        {activePage === "audit" && <AuditLogsPage />}
      </main>
    </div>
  );
}