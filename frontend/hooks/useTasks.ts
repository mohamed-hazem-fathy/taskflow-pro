"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import api from "@/lib/api";
import { taskPriorityLabels, taskStatusLabels } from "@/lib/utils";
import type {
  ApiResponse,
  CreateTaskInput,
  PagedResponse,
  Task,
  TaskComment,
  TaskFilters,
  TaskHistoryEntry,
  TaskPriority,
  TaskStatus,
  UpdateTaskInput,
} from "@/types";

function projectTasksQueryKeyPrefix(projectId: string) {
  return ["projects", projectId, "tasks"] as const;
}

const TASK_STATUSES = new Set<TaskStatus>([
  "TODO",
  "IN_PROGRESS",
  "IN_REVIEW",
  "BLOCKED",
  "DONE",
]);

const TASK_PRIORITIES = new Set<TaskPriority>([
  "LOW",
  "MEDIUM",
  "HIGH",
  "CRITICAL",
]);

function toTaskStatus(value: string): TaskStatus | undefined {
  if (TASK_STATUSES.has(value as TaskStatus)) {
    return value as TaskStatus;
  }

  const match = Object.entries(taskStatusLabels).find(([, label]) => label === value);
  return match ? (match[0] as TaskStatus) : undefined;
}

function toTaskPriority(value: string): TaskPriority | undefined {
  if (TASK_PRIORITIES.has(value as TaskPriority)) {
    return value as TaskPriority;
  }

  const match = Object.entries(taskPriorityLabels).find(
    ([, label]) => label === value
  );
  return match ? (match[0] as TaskPriority) : undefined;
}

function serializeUpdateTaskInput(input: UpdateTaskInput): UpdateTaskInput {
  const body: UpdateTaskInput = {};

  if (input.title !== undefined) body.title = input.title;
  if (input.description !== undefined) body.description = input.description;

  if (input.status !== undefined) {
    const status = toTaskStatus(input.status);
    if (status) body.status = status;
  }

  if (input.priority !== undefined) {
    const priority = toTaskPriority(input.priority);
    if (priority) body.priority = priority;
  }

  if (input.assigneeId !== undefined) {
    body.assigneeId =
      input.assigneeId === null || input.assigneeId === "unassigned"
        ? null
        : input.assigneeId;
  }

  if (input.dueDate !== undefined) {
    body.dueDate =
      input.dueDate === null ? null : input.dueDate.slice(0, 10);
  }

  if (input.estimatedHours !== undefined) {
    body.estimatedHours = input.estimatedHours;
  }

  return body;
}

function projectTasksQueryKey(projectId: string, filters: TaskFilters = {}) {
  const {
    search,
    status,
    priority,
    sortBy = "createdAt",
    sortDir = "desc",
    page = 0,
    size = 50,
  } = filters;

  return [
    ...projectTasksQueryKeyPrefix(projectId),
    { search, status, priority, sortBy, sortDir, page, size },
  ] as const;
}

export function useProjectTasks(projectId: string, filters: TaskFilters = {}) {
  const {
    search,
    status,
    priority,
    sortBy = "createdAt",
    sortDir = "desc",
    page = 0,
    size = 50,
  } = filters;

  return useQuery({
    queryKey: projectTasksQueryKey(projectId, filters),
    queryFn: async () => {
      const params: Record<string, string | number> = {
        page,
        size,
        sortBy,
        sortDir,
      };
      if (search) params.search = search;
      if (status && status !== "ALL") params.status = status;
      if (priority && priority !== "ALL") params.priority = priority;

      const { data } = await api.get<ApiResponse<PagedResponse<Task>>>(
        `/api/tasks/project/${projectId}`,
        { params }
      );
      return data.data;
    },
    enabled: !!projectId,
  });
}

export function useAllTasks(filters: TaskFilters = {}) {
  const { status, page = 0, size = 100 } = filters;

  return useQuery({
    queryKey: ["tasks", "all", { status, page, size }],
    queryFn: async () => {
      const params: Record<string, string | number> = { page, size };
      if (status && status !== "ALL") params.status = status;

      const { data } = await api.get<ApiResponse<PagedResponse<Task>>>(
        "/api/tasks",
        { params }
      );
      return data.data;
    },
  });
}

export function useTask(id: string) {
  return useQuery({
    queryKey: ["tasks", id],
    queryFn: async () => {
      const { data } = await api.get<ApiResponse<Task>>(`/api/tasks/${id}`);
      return data.data;
    },
    enabled: !!id,
  });
}

export function useCreateTask(projectId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (input: CreateTaskInput) => {
      const { data } = await api.post<ApiResponse<Task>>("/api/tasks", {
        ...input,
        projectId,
      });
      return data.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: projectTasksQueryKeyPrefix(projectId),
      });
      queryClient.invalidateQueries({ queryKey: ["projects", projectId] });
      queryClient.invalidateQueries({ queryKey: ["tasks"] });
    },
  });
}

export function useUpdateTask() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async ({
      id,
      ...input
    }: UpdateTaskInput & { id: string }) => {
      const { data } = await api.put<ApiResponse<Task>>(
        `/api/tasks/${id}`,
        serializeUpdateTaskInput(input)
      );
      return data.data;
    },
    onMutate: async ({ id, assigneeId, dueDate, estimatedHours, ...rest }) => {
      await queryClient.cancelQueries({ queryKey: ["tasks", id] });
      const previous = queryClient.getQueryData<Task>(["tasks", id]);
      if (previous) {
        queryClient.setQueryData<Task>(["tasks", id], {
          ...previous,
          ...rest,
          dueDate: dueDate === null ? undefined : dueDate ?? previous.dueDate,
          estimatedHours:
            estimatedHours === null
              ? undefined
              : estimatedHours ?? previous.estimatedHours,
          assignee:
            assigneeId === null || assigneeId === "unassigned"
              ? undefined
              : previous.assignee,
        });
      }
      return { previous };
    },
    onError: (_err, variables, context) => {
      if (context?.previous) {
        queryClient.setQueryData(["tasks", variables.id], context.previous);
      }
    },
    onSettled: (_, __, variables) => {
      queryClient.invalidateQueries({ queryKey: ["tasks", variables.id] });
      queryClient.invalidateQueries({ queryKey: ["tasks"] });
      queryClient.invalidateQueries({ queryKey: ["projects"] });
    },
  });
}

export function useDeleteTask() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (id: string) => {
      await api.delete(`/api/tasks/${id}`);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["tasks"] });
      queryClient.invalidateQueries({ queryKey: ["projects"] });
    },
  });
}

export function useTaskComments(taskId: string) {
  return useQuery({
    queryKey: ["tasks", taskId, "comments"],
    queryFn: async () => {
      const { data } = await api.get<ApiResponse<TaskComment[]>>(
        `/api/tasks/${taskId}/comments`
      );
      return data.data;
    },
    enabled: !!taskId,
  });
}

export function useAddComment() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async ({
      taskId,
      content,
    }: {
      taskId: string;
      content: string;
    }) => {
      const { data } = await api.post<ApiResponse<TaskComment>>(
        `/api/tasks/${taskId}/comments`,
        { content }
      );
      return data.data;
    },
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({
        queryKey: ["tasks", variables.taskId, "comments"],
      });
    },
  });
}

export function useTaskHistory(taskId: string) {
  return useQuery({
    queryKey: ["tasks", taskId, "history"],
    queryFn: async () => {
      const { data } = await api.get<ApiResponse<TaskHistoryEntry[]>>(
        `/api/tasks/${taskId}/history`
      );
      return data.data;
    },
    enabled: !!taskId,
  });
}
