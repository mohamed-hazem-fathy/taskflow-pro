"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useRouter } from "next/navigation";
import { useCallback, useEffect, useState } from "react";
import api from "@/lib/api";
import {
  canManageProjects,
  clearToken,
  getStoredUser,
  hasRole,
  isAuthenticated,
  setStoredUser,
  setToken,
  type StoredUser,
} from "@/lib/auth";
import type { ApiResponse, AuthResponse, User } from "@/types";

export function useAuth() {
  const router = useRouter();
  const queryClient = useQueryClient();
  const [storedUser, setStoredUserState] = useState<StoredUser | null>(null);
  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    setStoredUserState(getStoredUser());
    setMounted(true);
  }, []);

  const { data: user, isLoading: isUserLoading } = useQuery({
    queryKey: ["auth", "me"],
    queryFn: async () => {
      const { data } = await api.get<ApiResponse<User>>("/api/users/me");
      return data.data;
    },
    enabled: mounted && isAuthenticated(),
    retry: false,
  });

  const loginMutation = useMutation({
    mutationFn: async ({
      email,
      password,
    }: {
      email: string;
      password: string;
    }) => {
      const { data } = await api.post<ApiResponse<AuthResponse>>(
        "/api/auth/login",
        { email, password }
      );
      return data.data;
    },
    onSuccess: (data) => {
      setToken(data.token);
      const userData: StoredUser = {
        username: data.username,
        email: data.email,
        fullName: data.fullName,
        roles: data.roles,
      };
      setStoredUser(userData);
      setStoredUserState(userData);
      queryClient.invalidateQueries({ queryKey: ["auth"] });
      router.push("/dashboard");
    },
  });

  const registerMutation = useMutation({
    mutationFn: async (payload: {
      fullName: string;
      username: string;
      email: string;
      password: string;
    }) => {
      const { data } = await api.post<ApiResponse<AuthResponse>>(
        "/api/auth/register",
        payload
      );
      return data.data;
    },
    onSuccess: (data) => {
      setToken(data.token);
      const userData: StoredUser = {
        username: data.username,
        email: data.email,
        fullName: data.fullName,
        roles: data.roles,
      };
      setStoredUser(userData);
      setStoredUserState(userData);
      queryClient.invalidateQueries({ queryKey: ["auth"] });
      router.push("/dashboard");
    },
  });

  const logout = useCallback(() => {
    clearToken();
    setStoredUserState(null);
    queryClient.clear();
    router.push("/login");
  }, [queryClient, router]);

  const checkRole = useCallback(
    (role: string) => hasRole(role, user?.roles ?? storedUser?.roles),
    [storedUser?.roles, user?.roles]
  );

  const canManage = useCallback(
    () => canManageProjects(user?.roles ?? storedUser?.roles),
    [storedUser?.roles, user?.roles]
  );

  return {
    user: user ?? null,
    storedUser,
    isAuthenticated: mounted && isAuthenticated(),
    isLoading: !mounted || isUserLoading,
    login: loginMutation.mutateAsync,
    isLoggingIn: loginMutation.isPending,
    register: registerMutation.mutateAsync,
    isRegistering: registerMutation.isPending,
    logout,
    hasRole: checkRole,
    canManageProjects: canManage,
  };
}
