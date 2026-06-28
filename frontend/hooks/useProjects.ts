"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import api from "@/lib/api";
import type {
  ApiResponse,
  CreateProjectInput,
  PagedResponse,
  Project,
  ProjectFilters,
  ProjectMember,
  ProjectRole,
  ProjectStatus,
} from "@/types";

export function useProjects(filters: ProjectFilters = {}) {
  const { search, status, page = 0, size = 12 } = filters;

  return useQuery({
    queryKey: ["projects", { search, status, page, size }],
    queryFn: async () => {
      const params: Record<string, string | number> = { page, size };
      if (search) params.search = search;
      if (status && status !== "ALL") params.status = status;

      const { data } = await api.get<ApiResponse<PagedResponse<Project>>>(
        "/api/projects",
        { params }
      );
      return data.data;
    },
  });
}

export function useProject(id: string) {
  return useQuery({
    queryKey: ["projects", id],
    queryFn: async () => {
      const { data } = await api.get<ApiResponse<Project>>(
        `/api/projects/${id}`
      );
      return data.data;
    },
    enabled: !!id,
  });
}

export function useCreateProject() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (input: CreateProjectInput) => {
      const { data } = await api.post<ApiResponse<Project>>(
        "/api/projects",
        input
      );
      return data.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["projects"] });
    },
  });
}

export function useUpdateProject() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async ({
      id,
      ...input
    }: Partial<CreateProjectInput> & { id: string; status?: ProjectStatus }) => {
      const { data } = await api.patch<ApiResponse<Project>>(
        `/api/projects/${id}`,
        input
      );
      return data.data;
    },
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ["projects"] });
      queryClient.invalidateQueries({ queryKey: ["projects", variables.id] });
    },
  });
}

export function useDeleteProject() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (id: string) => {
      await api.delete(`/api/projects/${id}`);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["projects"] });
    },
  });
}

export function useProjectMembers(projectId: string) {
  return useQuery({
    queryKey: ["projects", projectId, "members"],
    queryFn: async () => {
      const { data } = await api.get<ApiResponse<ProjectMember[]>>(
        `/api/projects/${projectId}/members`
      );
      return data.data;
    },
    enabled: !!projectId,
  });
}

export function useAddMember() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async ({
      projectId,
      userId,
      role,
    }: {
      projectId: string;
      userId: string;
      role: ProjectRole;
    }) => {
      const { data } = await api.post<ApiResponse<ProjectMember>>(
        `/api/projects/${projectId}/members`,
        { userId, role }
      );
      return data.data;
    },
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({
        queryKey: ["projects", variables.projectId, "members"],
      });
      queryClient.invalidateQueries({
        queryKey: ["projects", variables.projectId],
      });
    },
  });
}

export function useRemoveMember() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async ({
      projectId,
      userId,
    }: {
      projectId: string;
      userId: string;
    }) => {
      await api.delete(`/api/projects/${projectId}/members/${userId}`);
    },
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({
        queryKey: ["projects", variables.projectId, "members"],
      });
      queryClient.invalidateQueries({
        queryKey: ["projects", variables.projectId],
      });
    },
  });
}
