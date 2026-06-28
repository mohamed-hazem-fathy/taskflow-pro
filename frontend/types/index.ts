export interface User {
  id: string;
  username: string;
  email: string;
  fullName: string;
  avatarUrl?: string;
  isActive: boolean;
  roles: string[];
  createdAt: string;
}

export interface AuthResponse {
  token: string;
  tokenType: string;
  username: string;
  email: string;
  fullName: string;
  roles: string[];
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

export interface PagedResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

export type ProjectStatus =
  | "PLANNING"
  | "ACTIVE"
  | "ON_HOLD"
  | "COMPLETED"
  | "ARCHIVED";

export type TaskStatus =
  | "TODO"
  | "IN_PROGRESS"
  | "IN_REVIEW"
  | "BLOCKED"
  | "DONE";

export type TaskPriority = "LOW" | "MEDIUM" | "HIGH" | "CRITICAL";

export type ProjectRole = "OWNER" | "MANAGER" | "MEMBER" | "VIEWER";

export type NotificationType =
  | "TASK_ASSIGNED"
  | "TASK_STATUS_CHANGED"
  | "TASK_COMMENT_ADDED"
  | "PROJECT_MEMBER_ADDED";

export interface UserSummary {
  id: string;
  fullName: string;
  username: string;
  email: string;
  avatarUrl?: string;
}

export interface Project {
  id: string;
  name: string;
  description?: string;
  status: ProjectStatus;
  owner: UserSummary;
  startDate?: string;
  endDate?: string;
  memberCount: number;
  taskCount: number;
  createdAt: string;
}

export interface ProjectMember {
  id: string;
  user: UserSummary;
  role: ProjectRole;
  joinedAt: string;
}

export interface Task {
  id: string;
  title: string;
  description?: string;
  status: TaskStatus;
  priority: TaskPriority;
  projectId?: string;
  assignee?: UserSummary;
  reporter: UserSummary;
  dueDate?: string;
  estimatedHours?: number;
  commentCount?: number;
  createdAt: string;
  updatedAt: string;
}

export interface TaskComment {
  id: string;
  author: UserSummary;
  content: string;
  createdAt: string;
}

export interface TaskHistoryEntry {
  fieldName: string;
  oldValue?: string;
  newValue?: string;
  changedBy: UserSummary;
  changedAt: string;
}

export interface Notification {
  id: string;
  type: NotificationType;
  title: string;
  message: string;
  isRead: boolean;
  referenceId?: string;
  referenceType?: string;
  createdAt: string;
}

export interface ProjectFilters {
  search?: string;
  status?: ProjectStatus | "ALL";
  page?: number;
  size?: number;
}

export interface TaskFilters {
  search?: string;
  status?: TaskStatus | "ALL";
  priority?: TaskPriority | "ALL";
  sortBy?: "dueDate" | "priority" | "createdAt";
  sortDir?: "asc" | "desc";
  page?: number;
  size?: number;
}

export interface CreateProjectInput {
  name: string;
  description?: string;
  startDate?: string;
  endDate?: string;
  status?: ProjectStatus;
}

export interface CreateTaskInput {
  title: string;
  description?: string;
  status?: TaskStatus;
  priority?: TaskPriority;
  assigneeId?: string;
  dueDate?: string;
  estimatedHours?: number;
}

export interface UpdateTaskInput {
  title?: string;
  description?: string;
  status?: TaskStatus;
  priority?: TaskPriority;
  assigneeId?: string | null;
  dueDate?: string | null;
  estimatedHours?: number | null;
}
