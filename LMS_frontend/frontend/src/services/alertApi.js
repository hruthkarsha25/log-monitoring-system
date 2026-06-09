import api from "./axiosConfig";

export const getAlerts = () => api.get("/alerts").then(r => r.data);
export const getOpenAlerts = () => api.get("/alerts/open").then(r => r.data);
export const getLatestAlerts = () => api.get("/alerts/latest").then(r => r.data);
export const acknowledgeAlert = (id) => api.put(`/alerts/${id}/acknowledge`).then(r => r.data);
export const resolveAlert = (id) => api.put(`/alerts/${id}/resolve`).then(r => r.data);