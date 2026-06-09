import api from "./axiosConfig";

export const fetchAuditLogs = () =>
  api.get("/audit/logs").then(r => r.data);