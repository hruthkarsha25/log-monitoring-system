import api from "./axiosConfig";

export const fetchAllUsers = () =>
  api.get("/auth/users").then(r => r.data);

export const refreshToken = (refreshToken) =>
  api.post("/auth/refresh", { refreshToken }).then(r => r.data);