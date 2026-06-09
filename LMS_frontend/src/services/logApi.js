import api from "./axiosConfig";

export const fetchLogStats = () =>
  api.get("/logs/stats").then(r => r.data);

export const fetchLogCount = () =>
  api.get("/logs/count").then(r => r.data);

export const fetchAllLogs = (page = 0, size = 20) =>
  api.get("/logs/all", { params: { page, size } }).then(r => r.data);

export const searchLogs = (params) =>
  api.get("/logs/search", { params }).then(r => r.data);