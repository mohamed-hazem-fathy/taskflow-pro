"use client";

import { ListTodo } from "lucide-react";
import { TaskCard } from "@/components/tasks/TaskCard";
import { Skeleton } from "@/components/ui/skeleton";
import type { Task } from "@/types";

interface TaskListProps {
  tasks: Task[];
  isLoading?: boolean;
  emptyAction?: React.ReactNode;
}

export function TaskList({ tasks, isLoading, emptyAction }: TaskListProps) {
  if (isLoading) {
    return (
      <div className="space-y-3">
        {Array.from({ length: 5 }).map((_, i) => (
          <Skeleton key={i} className="h-24 rounded-lg" />
        ))}
      </div>
    );
  }

  if (tasks.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center rounded-lg border border-dashed py-12 text-center">
        <ListTodo className="size-10 text-muted-foreground mb-3" />
        <h3 className="font-semibold">No tasks found</h3>
        <p className="mt-1 text-sm text-muted-foreground">
          Try adjusting filters or create a new task.
        </p>
        {emptyAction && <div className="mt-4">{emptyAction}</div>}
      </div>
    );
  }

  return (
    <div className="space-y-3">
      {tasks.map((task) => (
        <TaskCard key={task.id} task={task} />
      ))}
    </div>
  );
}
