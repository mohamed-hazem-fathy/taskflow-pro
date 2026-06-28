const TOKEN_KEY = "taskflow_token";
const USER_KEY = "taskflow_user";

export interface StoredUser {
  username: string;
  email: string;
  fullName: string;
  roles: string[];
}

export function getToken(): string | null {
  if (typeof window === "undefined") return null;
  return localStorage.getItem(TOKEN_KEY);
}

export function setToken(token: string): void {
  if (typeof window === "undefined") return;
  localStorage.setItem(TOKEN_KEY, token);
  document.cookie = `${TOKEN_KEY}=${token}; path=/; max-age=${60 * 60 * 24 * 7}; SameSite=Lax`;
}

export function clearToken(): void {
  if (typeof window === "undefined") return;
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(USER_KEY);
  document.cookie = `${TOKEN_KEY}=; path=/; max-age=0; SameSite=Lax`;
}

export function getStoredUser(): StoredUser | null {
  if (typeof window === "undefined") return null;
  const raw = localStorage.getItem(USER_KEY);
  if (!raw) return null;
  try {
    return JSON.parse(raw) as StoredUser;
  } catch {
    return null;
  }
}

export function setStoredUser(user: StoredUser): void {
  if (typeof window === "undefined") return;
  localStorage.setItem(USER_KEY, JSON.stringify(user));
}

export function isAuthenticated(): boolean {
  return !!getToken();
}

export function hasRole(role: string, roles?: string[]): boolean {
  const userRoles = roles ?? getStoredUser()?.roles ?? [];
  return userRoles.some(
    (r) => r === role || r === `ROLE_${role}` || r.replace("ROLE_", "") === role
  );
}

export function canManageProjects(roles?: string[]): boolean {
  const userRoles = roles ?? getStoredUser()?.roles ?? [];
  return userRoles.some((r) => {
    const normalized = r.replace("ROLE_", "");
    return (
      normalized === "ADMIN" ||
      normalized === "MANAGER" ||
      normalized === "OWNER"
    );
  });
}
