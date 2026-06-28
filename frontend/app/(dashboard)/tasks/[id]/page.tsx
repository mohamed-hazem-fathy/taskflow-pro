"use client";

import { use, useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { toast } from "sonner";
import { ArrowLeft, Loader2, Trash2 } from "lucide-react";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Skeleton } from "@/components/ui/skeleton";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Textarea } from "@/components/ui/textarea";
import { TaskComments } from "@/components/tasks/TaskComments";
import { TaskHistoryPanel } from "@/components/tasks/TaskHistory";
import { useAuth } from "@/hooks/useAuth";
import { useProjectMembers } from "@/hooks/useProjects";
import { useDeleteTask, useTask, useUpdateTask } from "@/hooks/useTasks";
import {
  formatDate,
  getInitials,
  taskPriorityLabels,
  taskStatusLabels,
} from "@/lib/utils";
import type { TaskPriority, TaskStatus } from "@/types";

interface TaskDetailPageProps {
  params: Promise<{ id: string }>;
}

export default function TaskDetailPage({ params }: TaskDetailPageProps) {
  const { id } = use(params);
  const router = useRouter();
  const { user } = useAuth();
  const { data: task, isLoading } = useTask(id);
  const updateTask = useUpdateTask();
  const deleteTask = useDeleteTask();
  const { data: members } = useProjectMembers(task?.projectId ?? "");

  const [editingTitle, setEditingTitle] = useState(false);
  const [title, setTitle] = useState("");
  const [editingDescription, setEditingDescription] = useState(false);
  const [description, setDescription] = useState("");

  const canEdit =
    user &&
    task &&
    (user.id === task.reporter.id || user.id === task.assignee?.id);

  const canDelete = user && task && user.id === task.reporter.id;

  const handleUpdate = async (
    input: Parameters<typeof updateTask.mutateAsync>[0]
  ) => {
    try {
      await updateTask.mutateAsync(input);
    } catch {
      toast.error("Failed to update task");
    }
  };

  const handleDelete = async () => {
    if (!confirm("Are you sure you want to delete this task?")) return;
    try {
      await deleteTask.mutateAsync(id);
      toast.success("Task deleted");
      router.push(task?.projectId ? `/projects/${task.projectId}` : "/projects");
    } catch {
      toast.error("Failed to delete task");
    }
  };

  if (isLoading) {
    return (
      <div className="grid gap-6 lg:grid-cols-[1fr_320px]">
        <div className="space-y-4">
          <Skeleton className="h-8 w-3/4" />
          <Skeleton className="h-32 w-full" />
          <Skeleton className="h-64 w-full" />
        </div>
        <Skeleton className="h-80 rounded-lg" />
      </div>
    );
  }

  if (!task) {
    return (
      <div className="rounded-lg border border-dashed p-12 text-center">
        <p className="text-muted-foreground">Task not found.</p>
        <Link href="/projects" className="mt-2 inline-block text-sm underline-offset-4 hover:underline">
          Back to projects
        </Link>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {task.projectId && (
        <Link
          href={`/projects/${task.projectId}`}
          className="inline-flex items-center gap-1 text-sm text-muted-foreground hover:text-foreground transition-colors duration-200"
        >
          <ArrowLeft className="size-4" />
          Back to project
        </Link>
      )}

      <div className="grid gap-6 lg:grid-cols-[1fr_320px]">
        <div className="space-y-6 min-w-0">
          {editingTitle && canEdit ? (
            <Input
              autoFocus
              value={title || task.title}
              onChange={(e) => setTitle(e.target.value)}
              onBlur={async () => {
                setEditingTitle(false);
                if (title && title !== task.title) {
                  await handleUpdate({ id, title });
                }
              }}
              onKeyDown={(e) => {
                if (e.key === "Enter") e.currentTarget.blur();
              }}
              className="text-2xl font-bold h-auto py-2 rounded-md"
            />
          ) : (
            <h2
              className="text-2xl font-bold tracking-tight cursor-text"
              onClick={() => {
                if (canEdit) {
                  setTitle(task.title);
                  setEditingTitle(true);
                }
              }}
            >
              {task.title}
            </h2>
          )}

          {editingDescription && canEdit ? (
            <Textarea
              autoFocus
              rows={6}
              value={description || task.description || ""}
              onChange={(e) => setDescription(e.target.value)}
              onBlur={async () => {
                setEditingDescription(false);
                if (description !== (task.description ?? "")) {
                  await handleUpdate({ id, description });
                }
              }}
              className="rounded-md"
            />
          ) : (
            <div
              className="min-h-[80px] rounded-lg border bg-muted/20 p-4 text-sm whitespace-pre-wrap cursor-text transition-colors duration-200 hover:bg-muted/30"
              onClick={() => {
                if (canEdit) {
                  setDescription(task.description ?? "");
                  setEditingDescription(true);
                }
              }}
            >
              {task.description || (
                <span className="text-muted-foreground">
                  {canEdit ? "Click to add a description..." : "No description"}
                </span>
              )}
            </div>
          )}

          <Tabs defaultValue="comments" className="w-full">
            <TabsList className="rounded-md">
              <TabsTrigger value="comments" className="rounded-md">
                Comments
              </TabsTrigger>
              <TabsTrigger value="history" className="rounded-md">
                History
              </TabsTrigger>
            </TabsList>
            <TabsContent value="comments" className="mt-4">
              <TaskComments taskId={id} />
            </TabsContent>
            <TabsContent value="history" className="mt-4">
              <TaskHistoryPanel taskId={id} />
            </TabsContent>
          </Tabs>
        </div>

        <aside className="space-y-4 rounded-lg border bg-card p-4 h-fit">
          <div className="space-y-2">
            <Label>Status</Label>
            <Select
              value={task.status}
              onValueChange={(value) =>
                handleUpdate({ id, status: value as TaskStatus })
              }
              disabled={!canEdit || updateTask.isPending}
            >
              <SelectTrigger className="rounded-md">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                {(Object.keys(taskStatusLabels) as TaskStatus[]).map((s) => (
                  <SelectItem key={s} value={s}>
                    {taskStatusLabels[s]}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>

          <div className="space-y-2">
            <Label>Priority</Label>
            <Select
              value={task.priority}
              onValueChange={(value) =>
                handleUpdate({ id, priority: value as TaskPriority })
              }
              disabled={!canEdit || updateTask.isPending}
            >
              <SelectTrigger className="rounded-md">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                {(Object.keys(taskPriorityLabels) as TaskPriority[]).map(
                  (p) => (
                    <SelectItem key={p} value={p}>
                      {taskPriorityLabels[p]}
                    </SelectItem>
                  )
                )}
              </SelectContent>
            </Select>
          </div>

          <div className="space-y-2">
            <Label>Assignee</Label>
            <Select
              value={task.assignee?.id ?? "unassigned"}
              onValueChange={(value) =>
                handleUpdate({
                  id,
                  assigneeId: value === "unassigned" ? null : value,
                })
              }
              disabled={!canEdit || updateTask.isPending}
            >
              <SelectTrigger className="rounded-md">
                <SelectValue placeholder="Unassigned" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="unassigned">Unassigned</SelectItem>
                {members?.map((member) => (
                  <SelectItem key={member.user.id} value={member.user.id}>
                    {member.user.fullName} @{member.user.username}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
            {task.assignee && (
              <p className="text-xs text-muted-foreground">
                {task.assignee.fullName} @{task.assignee.username}
              </p>
            )}
          </div>

          <div className="space-y-2">
            <Label>Reporter</Label>
            <div className="flex min-w-0 flex-1 items-center gap-2 rounded-md border px-3 py-2 text-sm">
              <Avatar className="size-6 shrink-0">
                {task.reporter.avatarUrl && (
                  <AvatarImage src={task.reporter.avatarUrl} alt={task.reporter.fullName} />
                )}
                <AvatarFallback className="text-[10px]">
                  {getInitials(task.reporter.fullName)}
                </AvatarFallback>
              </Avatar>
              <div className="min-w-0">
                <p className="truncate">{task.reporter.fullName}</p>
                <p className="truncate text-xs text-muted-foreground">
                  @{task.reporter.username}
                </p>
              </div>
            </div>
          </div>

          <div className="space-y-2">
            <Label htmlFor="dueDate">Due Date</Label>
            <Input
              id="dueDate"
              type="date"
              value={task.dueDate?.slice(0, 10) ?? ""}
              onChange={(e) =>
                handleUpdate({
                  id,
                  dueDate: e.target.value || null,
                })
              }
              disabled={!canEdit || updateTask.isPending}
              className="rounded-md"
            />
          </div>

          <div className="space-y-2">
            <Label htmlFor="estimatedHours">Estimated Hours</Label>
            <Input
              id="estimatedHours"
              type="number"
              min={0}
              step={0.5}
              value={task.estimatedHours ?? ""}
              onChange={(e) =>
                handleUpdate({
                  id,
                  estimatedHours: e.target.value
                    ? Number(e.target.value)
                    : null,
                })
              }
              disabled={!canEdit || updateTask.isPending}
              className="rounded-md"
            />
          </div>

          {updateTask.isPending && (
            <div className="flex items-center gap-2 text-xs text-muted-foreground">
              <Loader2 className="size-3 animate-spin" />
              Saving...
            </div>
          )}

          {canDelete && (
            <Button
              variant="destructive"
              className="w-full rounded-md mt-2"
              onClick={handleDelete}
              disabled={deleteTask.isPending}
            >
              {deleteTask.isPending ? (
                <Loader2 className="size-4 animate-spin" />
              ) : (
                <Trash2 className="size-4" />
              )}
              Delete Task
            </Button>
          )}
        </aside>
      </div>
    </div>
  );
}
