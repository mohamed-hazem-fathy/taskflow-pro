import { clsx, type ClassValue } from "clsx";
import { twMerge } from "tailwind-merge";
import type {
  ProjectStatus,
  TaskPriority,
  TaskStatus,
} from "@/types";

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

export function getInitials(name: string): string {
  return name
    .split(" ")
    .map((part) => part[0])
    .join("")
    .slice(0, 2)
    .toUpperCase();
}

export function formatDate(date?: string | null): string {
  if (!date) return "—";
  return new Intl.DateTimeFormat("en-US", {
    month: "short",
    day: "numeric",
    year: "numeric",
  }).format(new Date(date));
}

export function formatDateTime(date?: string | null): string {
  if (!date) return "—";
  return new Intl.DateTimeFormat("en-US", {
    month: "short",
    day: "numeric",
    year: "numeric",
    hour: "numeric",
    minute: "2-digit",
  }).format(new Date(date));
}

export function timeAgo(date: string): string {
  const seconds = Math.floor((Date.now() - new Date(date).getTime()) / 1000);
  const intervals: [number, string][] = [
    [31536000, "year"],
    [2592000, "month"],
    [86400, "day"],
    [3600, "hour"],
    [60, "minute"],
  ];

  for (const [secondsInUnit, label] of intervals) {
    const count = Math.floor(seconds / secondsInUnit);
    if (count >= 1) {
      return `${count} ${label}${count > 1 ? "s" : ""} ago`;
    }
  }

  return "just now";
}

export function isDueSoon(dueDate?: string, days = 7): boolean {
  if (!dueDate) return false;
  const due = new Date(dueDate).getTime();
  const now = Date.now();
  const limit = now + days * 24 * 60 * 60 * 1000;
  return due >= now && due <= limit;
}

export const taskStatusLabels: Record<TaskStatus, string> = {
  TODO: "To Do",
  IN_PROGRESS: "In Progress",
  IN_REVIEW: "In Review",
  BLOCKED: "Blocked",
  DONE: "Done",
};

export const taskPriorityLabels: Record<TaskPriority, string> = {
  LOW: "Low",
  MEDIUM: "Medium",
  HIGH: "High",
  CRITICAL: "Critical",
};

export const projectStatusLabels: Record<ProjectStatus, string> = {
  PLANNING: "Planning",
  ACTIVE: "Active",
  ON_HOLD: "On Hold",
  COMPLETED: "Completed",
  ARCHIVED: "Archived",
};

export const taskPriorityColors: Record<TaskPriority, string> = {
  CRITICAL: "bg-red-500",
  HIGH: "bg-orange-500",
  MEDIUM: "bg-blue-500",
  LOW: "bg-gray-500",
};

export const taskStatusColors: Record<TaskStatus, string> = {
  TODO: "bg-gray-500/15 text-gray-600 dark:text-gray-300 border-gray-500/30",
  IN_PROGRESS:
    "bg-blue-500/15 text-blue-600 dark:text-blue-300 border-blue-500/30",
  IN_REVIEW:
    "bg-purple-500/15 text-purple-600 dark:text-purple-300 border-purple-500/30",
  BLOCKED: "bg-red-500/15 text-red-600 dark:text-red-300 border-red-500/30",
  DONE: "bg-green-500/15 text-green-600 dark:text-green-300 border-green-500/30",
};

export const projectStatusColors: Record<ProjectStatus, string> = {
  PLANNING:
    "bg-gray-500/15 text-gray-600 dark:text-gray-300 border-gray-500/30",
  ACTIVE: "bg-blue-500/15 text-blue-600 dark:text-blue-300 border-blue-500/30",
  ON_HOLD:
    "bg-orange-500/15 text-orange-600 dark:text-orange-300 border-orange-500/30",
  COMPLETED:
    "bg-green-500/15 text-green-600 dark:text-green-300 border-green-500/30",
  ARCHIVED:
    "bg-zinc-500/15 text-zinc-600 dark:text-zinc-300 border-zinc-500/30",
};
