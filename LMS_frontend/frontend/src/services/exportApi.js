import api from "./axiosConfig";

export const exportExcel = () =>
  api.get("/export/excel", {
    responseType: "blob",
  });

export const exportCsv = () =>
  api.get("/export/csv", {
    responseType: "blob",
  });