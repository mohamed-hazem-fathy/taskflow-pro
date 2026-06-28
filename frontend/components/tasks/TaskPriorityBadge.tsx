import { cn, taskPriorityLabels } from "@/lib/utils";
import type { TaskPriority } from "@/types";
import { Badge } from "@/components/ui/badge";

const priorityStyles: Record<TaskPriority, string> = {
  CRITICAL: "border-red-500/30 bg-red-500/10 text-red-600 dark:text-red-300",
  HIGH: "border-orange-500/30 bg-orange-500/10 text-orange-600 dark:text-orange-300",
  MEDIUM: "border-blue-500/30 bg-blue-500/10 text-blue-600 dark:text-blue-300",
  LOW: "border-gray-500/30 bg-gray-500/10 text-gray-600 dark:text-gray-300",
};

interface TaskPriorityBadgeProps {
  priority: TaskPriority;
  className?: string;
}

export function TaskPriorityBadge({ priority, className }: TaskPriorityBadgeProps) {
  return (
    <Badge
      variant="outline"
      className={cn("rounded-md border text-[10px]", priorityStyles[priority], className)}
    >
      {taskPriorityLabels[priority]}
    </Badge>
  );
}
