"use client";

import { History } from "lucide-react";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { Skeleton } from "@/components/ui/skeleton";
import { useTaskHistory } from "@/hooks/useTasks";
import { formatDateTime, getInitials } from "@/lib/utils";

interface TaskHistoryProps {
  taskId: string;
}

export function TaskHistoryPanel({ taskId }: TaskHistoryProps) {
  const { data: history, isLoading } = useTaskHistory(taskId);

  if (isLoading) {
    return (
      <div className="space-y-4">
        {Array.from({ length: 4 }).map((_, i) => (
          <div key={i} className="flex gap-3">
            <Skeleton className="size-8 rounded-full" />
            <div className="flex-1 space-y-2">
              <Skeleton className="h-4 w-full" />
              <Skeleton className="h-3 w-24" />
            </div>
          </div>
        ))}
      </div>
    );
  }

  if (!history || history.length === 0) {
    return (
      <div className="flex flex-col items-center py-8 text-center">
        <History className="size-8 text-muted-foreground mb-2" />
        <p className="text-sm text-muted-foreground">No history yet</p>
      </div>
    );
  }

  return (
    <div className="relative space-y-4 before:absolute before:left-4 before:top-2 before:h-[calc(100%-1rem)] before:w-px before:bg-border">
      {history.map((entry, index) => (
        <div key={`${entry.fieldName}-${entry.changedAt}-${index}`} className="relative flex gap-3">
          <Avatar className="relative z-10 size-8">
            {entry.changedBy.avatarUrl && (
              <AvatarImage
                src={entry.changedBy.avatarUrl}
                alt={entry.changedBy.fullName}
              />
            )}
            <AvatarFallback className="text-xs">
              {getInitials(entry.changedBy.fullName)}
            </AvatarFallback>
          </Avatar>
          <div className="min-w-0 flex-1 pt-0.5">
            <p className="text-sm">
              <span className="font-medium">{entry.changedBy.fullName}</span>
              <span className="text-muted-foreground">
                {" "}
                @{entry.changedBy.username}
              </span>{" "}
              changed{" "}
              <span className="font-medium">{entry.fieldName}</span>
              {entry.oldValue !== undefined && (
                <>
                  {" "}
                  from{" "}
                  <span className="text-muted-foreground">
                    {entry.oldValue || "empty"}
                  </span>
                </>
              )}
              {entry.newValue !== undefined && (
                <>
                  {" "}
                  to{" "}
                  <span className="text-muted-foreground">
                    {entry.newValue || "empty"}
                  </span>
                </>
              )}
            </p>
            <p className="mt-1 text-xs text-muted-foreground">
              {formatDateTime(entry.changedAt)}
            </p>
          </div>
        </div>
      ))}
    </div>
  );
}
