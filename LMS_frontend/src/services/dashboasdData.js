export const PIE_DATA = [
  { name: "INFO", value: 52, color: "#38bdf8" },
  { name: "WARN", value: 24, color: "#fbbf24" },
  { name: "ERROR", value: 15, color: "#f87171" },
  { name: "DEBUG", value: 6, color: "#a78bfa" },
  { name: "CRITICAL", value: 3, color: "#fb923c" },
];

export const SERVICE_DATA = [
  { name: "auth-service", logs: 312, errors: 18 },
  { name: "api-gateway", logs: 540, errors: 6 },
  { name: "db-connector", logs: 198, errors: 41 },
  { name: "cache-layer", logs: 87, errors: 3 },
  { name: "scheduler", logs: 124, errors: 11 },
];

// ── Level badge ───────────────────────────────────────────────────────────────
export const levelConfig = {
  INFO:     { bg: "rgba(56,189,248,0.12)", color: "#38bdf8", dot: "#38bdf8" },
  WARN:     { bg: "rgba(251,191,36,0.12)", color: "#fbbf24", dot: "#fbbf24" },
  ERROR:    { bg: "rgba(248,113,113,0.12)", color: "#f87171", dot: "#f87171" },
  DEBUG:    { bg: "rgba(167,139,250,0.12)", color: "#a78bfa", dot: "#a78bfa" },
  CRITICAL: { bg: "rgba(251,146,60,0.15)", color: "#fb923c", dot: "#fb923c" },
};