import axios from "axios";
import { clearToken, getToken } from "./auth";

const api = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080",
  headers: { "Content-Type": "application/json" },
});

api.interceptors.request.use((config) => {
  const token = getToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401 && typeof window !== "undefined") {
      const requestUrl = error.config?.url ?? "";
      const isAuthRequest =
        requestUrl.includes("/api/auth/login") ||
        requestUrl.includes("/api/auth/register");

      // Don't redirect on failed login/register — let the form show the error.
      if (!isAuthRequest) {
        clearToken();
        if (!window.location.pathname.startsWith("/login")) {
          window.location.href = "/login";
        }
      }
    }
    return Promise.reject(error);
  }
);

export default api;
