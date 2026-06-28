import { cn, taskStatusColors, taskStatusLabels } from "@/lib/utils";
import type { TaskStatus } from "@/types";
import { Badge } from "@/components/ui/badge";

interface TaskStatusBadgeProps {
  status: TaskStatus;
  className?: string;
}

export function TaskStatusBadge({ status, className }: TaskStatusBadgeProps) {
  return (
    <Badge
      variant="outline"
      className={cn("rounded-md border text-[10px]", taskStatusColors[status], className)}
    >
      {taskStatusLabels[status]}
    </Badge>
  );
}
