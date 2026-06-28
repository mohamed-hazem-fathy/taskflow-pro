"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import api from "@/lib/api";
import type { ApiResponse, Notification, PagedResponse } from "@/types";

export function useNotifications(page = 0, size = 20) {
  return useQuery({
    queryKey: ["notifications", { page, size }],
    queryFn: async () => {
      const { data } = await api.get<ApiResponse<PagedResponse<Notification>>>(
        "/api/notifications",
        { params: { page, size } }
      );
      return data.data;
    },
  });
}

export function useUnreadCount() {
  return useQuery({
    queryKey: ["notifications", "unread-count"],
    queryFn: async () => {
      const { data } = await api.get<ApiResponse<number>>(
        "/api/notifications/unread-count"
      );
      return data.data;
    },
    refetchInterval: 30_000,
  });
}

export function useMarkAsRead() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (id: string) => {
      await api.patch(`/api/notifications/${id}/read`);
    },
    onMutate: async (id) => {
      await queryClient.cancelQueries({ queryKey: ["notifications"] });
      const previousLists = queryClient.getQueriesData<PagedResponse<Notification>>({
        queryKey: ["notifications"],
      });

      previousLists.forEach(([key, data]) => {
        if (!data || !Array.isArray(data.content)) return;
        queryClient.setQueryData<PagedResponse<Notification>>(key, {
          ...data,
          content: data.content.map((n) =>
            n.id === id ? { ...n, isRead: true } : n
          ),
        });
      });

      const previousCount = queryClient.getQueryData<number>([
        "notifications",
        "unread-count",
      ]);
      if (typeof previousCount === "number" && previousCount > 0) {
        queryClient.setQueryData(["notifications", "unread-count"], previousCount - 1);
      }

      return { previousLists, previousCount };
    },
    onError: (_err, _id, context) => {
      context?.previousLists.forEach(([key, data]) => {
        if (data) queryClient.setQueryData(key, data);
      });
      if (context?.previousCount !== undefined) {
        queryClient.setQueryData(
          ["notifications", "unread-count"],
          context.previousCount
        );
      }
    },
    onSettled: () => {
      queryClient.invalidateQueries({ queryKey: ["notifications"] });
    },
  });
}

export function useMarkAllAsRead() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async () => {
      await api.patch("/api/notifications/read-all");
    },
    onMutate: async () => {
      await queryClient.cancelQueries({ queryKey: ["notifications"] });
      const previousLists = queryClient.getQueriesData<PagedResponse<Notification>>({
        queryKey: ["notifications"],
      });

      previousLists.forEach(([key, data]) => {
        if (!data || !Array.isArray(data.content)) return;
        queryClient.setQueryData<PagedResponse<Notification>>(key, {
          ...data,
          content: data.content.map((n) => ({ ...n, isRead: true })),
        });
      });

      const previousCount = queryClient.getQueryData<number>([
        "notifications",
        "unread-count",
      ]);
      queryClient.setQueryData(["notifications", "unread-count"], 0);

      return { previousLists, previousCount };
    },
    onError: (_err, _vars, context) => {
      context?.previousLists.forEach(([key, data]) => {
        if (data) queryClient.setQueryData(key, data);
      });
      if (context?.previousCount !== undefined) {
        queryClient.setQueryData(
          ["notifications", "unread-count"],
          context.previousCount
        );
      }
    },
    onSettled: () => {
      queryClient.invalidateQueries({ queryKey: ["notifications"] });
    },
  });
}
