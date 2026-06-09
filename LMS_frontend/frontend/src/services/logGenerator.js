export const generateLogStream = () => {
  const levels = ["INFO", "WARN", "ERROR", "DEBUG", "CRITICAL"];
  const services = ["auth-service", "api-gateway", "db-connector", "cache-layer", "scheduler"];
  const messages = {
    INFO: ["User login successful", "Request processed", "Cache hit", "Health check OK", "Session started"],
    WARN: ["High memory usage: 78%", "Slow query detected (340ms)", "Rate limit approaching", "Disk usage 85%", "Retry attempt 2/3"],
    ERROR: ["Connection refused: DB", "Timeout after 5000ms", "JWT verification failed", "Null pointer exception", "Failed to parse response"],
    DEBUG: ["Query executed in 12ms", "Token refreshed", "Event dispatched", "Cache invalidated", "Batch processed: 120 items"],
    CRITICAL: ["Database unreachable!", "Service crash detected", "Memory overflow", "Disk full", "Auth service down"],
  };
  const lvl = levels[Math.floor(Math.random() * levels.length)];
  return {
    id: Date.now() + Math.random(),
    timestamp: new Date().toISOString().replace("T", " ").split(".")[0],
    level: lvl,
    service: services[Math.floor(Math.random() * services.length)],
    message: messages[lvl][Math.floor(Math.random() * messages[lvl].length)],
  };
};

export const INITIAL_LOGS = Array.from({ length: 18 }, (_, i) => ({
  ...generateLogStream(),
  id: i,
  timestamp: new Date(Date.now() - (18 - i) * 4000).toISOString().replace("T", " ").split(".")[0],
}));

export const generateTimeSeriesData = () =>
  Array.from({ length: 12 }, (_, i) => ({
    time: `${String(new Date().getHours()).padStart(2, "0")}:${String(i * 5).padStart(2, "0")}`,
    errors: Math.floor(Math.random() * 30 + 2),
    warnings: Math.floor(Math.random() * 60 + 10),
    info: Math.floor(Math.random() * 120 + 40),
  }));