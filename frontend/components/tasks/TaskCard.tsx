"use client";

import { useRouter } from "next/navigation";
import { MessageSquare } from "lucide-react";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { TaskPriorityBadge } from "@/components/tasks/TaskPriorityBadge";
import { TaskStatusBadge } from "@/components/tasks/TaskStatusBadge";
import { cn, formatDate, getInitials, taskPriorityColors } from "@/lib/utils";
import type { Task } from "@/types";

interface TaskCardProps {
  task: Task;
  className?: string;
}

export function TaskCard({ task, className }: TaskCardProps) {
  const router = useRouter();

  return (
    <div
      className={cn(
        "group flex cursor-pointer overflow-hidden rounded-lg border bg-card transition-all duration-200 hover:bg-muted/50 hover:shadow-sm",
        className
      )}
      onClick={() => router.push(`/tasks/${task.id}`)}
    >
      <div
        className={cn("w-1 shrink-0", taskPriorityColors[task.priority])}
      />
      <div className="min-w-0 flex-1 p-4">
        <div className="flex items-start justify-between gap-2">
          <h3 className="font-medium leading-snug group-hover:text-foreground">
            {task.title}
          </h3>
          <TaskPriorityBadge priority={task.priority} />
        </div>

        {task.description && (
          <p className="mt-1 line-clamp-2 text-sm text-muted-foreground">
            {task.description}
          </p>
        )}

        <div className="mt-3 flex flex-wrap items-center gap-2">
          {task.assignee ? (
            <div className="flex items-center gap-1.5">
              <Avatar className="size-6">
                {task.assignee.avatarUrl && (
                  <AvatarImage
                    src={task.assignee.avatarUrl}
                    alt={task.assignee.fullName}
                  />
                )}
                <AvatarFallback className="text-[10px]">
                  {getInitials(task.assignee.fullName)}
                </AvatarFallback>
              </Avatar>
              <span className="text-xs">
                {task.assignee.fullName}
                <span className="text-muted-foreground">
                  {" "}
                  @{task.assignee.username}
                </span>
              </span>
            </div>
          ) : (
            <span className="text-xs text-muted-foreground">Unassigned</span>
          )}
          <TaskStatusBadge status={task.status} />
          {task.dueDate && (
            <span className="text-xs text-muted-foreground">
              Due {formatDate(task.dueDate)}
            </span>
          )}
          {(task.commentCount ?? 0) > 0 && (
            <span className="flex items-center gap-1 text-xs text-muted-foreground">
              <MessageSquare className="size-3" />
              {task.commentCount}
            </span>
          )}
        </div>
      </div>
    </div>
  );
}
