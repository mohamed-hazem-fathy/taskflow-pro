import { cn, formatDate, projectStatusColors, projectStatusLabels } from "@/lib/utils";
import type { Project } from "@/types";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Progress } from "@/components/ui/progress";
import { Users, ListTodo } from "lucide-react";

interface ProjectCardProps {
  project: Project;
  doneTasks?: number;
  onClick?: () => void;
  className?: string;
}

export function ProjectCard({
  project,
  doneTasks = 0,
  onClick,
  className,
}: ProjectCardProps) {
  const progress =
    project.taskCount > 0
      ? Math.round((doneTasks / project.taskCount) * 100)
      : 0;

  return (
    <Card
      className={cn(
        "cursor-pointer rounded-lg border bg-card transition-all duration-200 hover:shadow-md hover:border-border/80",
        className
      )}
      onClick={onClick}
    >
      <CardHeader className="pb-2">
        <div className="flex items-start justify-between gap-2">
          <CardTitle className="line-clamp-1 text-base font-semibold">
            {project.name}
          </CardTitle>
          <Badge
            variant="outline"
            className={cn("shrink-0 rounded-md border", projectStatusColors[project.status])}
          >
            {projectStatusLabels[project.status]}
          </Badge>
        </div>
      </CardHeader>
      <CardContent className="space-y-4">
        {project.description && (
          <p className="line-clamp-2 text-sm text-muted-foreground">
            {project.description}
          </p>
        )}

        <div className="space-y-2">
          <div className="flex items-center justify-between text-xs text-muted-foreground">
            <span>Progress</span>
            <span>{progress}%</span>
          </div>
          <Progress value={progress} className="h-1.5" />
        </div>

        <div className="flex items-center justify-between text-xs text-muted-foreground">
          <div className="flex items-center gap-3">
            <span className="flex items-center gap-1">
              <Users className="size-3.5" />
              {project.memberCount}
            </span>
            <span className="flex items-center gap-1">
              <ListTodo className="size-3.5" />
              {project.taskCount}
            </span>
          </div>
          {project.endDate && (
            <span>Due {formatDate(project.endDate)}</span>
          )}
        </div>
      </CardContent>
    </Card>
  );
}
