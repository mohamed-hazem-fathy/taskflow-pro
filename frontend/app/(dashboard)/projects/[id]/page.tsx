"use client";

import { use, useMemo, useState } from "react";
import Link from "next/link";
import { ArrowLeft, Calendar, Users } from "lucide-react";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { Badge } from "@/components/ui/badge";
import { Skeleton } from "@/components/ui/skeleton";
import { CreateTaskDialog } from "@/components/tasks/CreateTaskDialog";
import { TaskFiltersBar } from "@/components/tasks/TaskFilters";
import { TaskList } from "@/components/tasks/TaskList";
import { ProjectMembersPanel } from "@/components/projects/ProjectMembersPanel";
import { useAuth } from "@/hooks/useAuth";
import { useProject } from "@/hooks/useProjects";
import { useProjectTasks } from "@/hooks/useTasks";
import {
  cn,
  formatDate,
  getInitials,
  projectStatusColors,
  projectStatusLabels,
} from "@/lib/utils";
import type { TaskFilters } from "@/types";

interface ProjectDetailPageProps {
  params: Promise<{ id: string }>;
}

export default function ProjectDetailPage({ params }: ProjectDetailPageProps) {
  const { id } = use(params);
  const { canManageProjects } = useAuth();
  const { data: project, isLoading: projectLoading } = useProject(id);
  const [filters, setFilters] = useState<TaskFilters>({
    status: "ALL",
    priority: "ALL",
    sortBy: "createdAt",
    sortDir: "desc",
  });

  const { data: tasksData, isLoading: tasksLoading } = useProjectTasks(
    id,
    filters
  );

  const tasks = useMemo(() => tasksData?.content ?? [], [tasksData]);

  if (projectLoading) {
    return (
      <div className="space-y-6">
        <Skeleton className="h-8 w-64" />
        <Skeleton className="h-4 w-full max-w-2xl" />
        <div className="grid gap-6 lg:grid-cols-[1fr_320px]">
          <Skeleton className="h-96 rounded-lg" />
          <Skeleton className="h-64 rounded-lg" />
        </div>
      </div>
    );
  }

  if (!project) {
    return (
      <div className="rounded-lg border border-dashed p-12 text-center">
        <p className="text-muted-foreground">Project not found.</p>
        <Link href="/projects" className="mt-2 inline-block text-sm underline-offset-4 hover:underline">
          Back to projects
        </Link>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <Link
        href="/projects"
        className="inline-flex items-center gap-1 text-sm text-muted-foreground hover:text-foreground transition-colors duration-200"
      >
        <ArrowLeft className="size-4" />
        Back to projects
      </Link>

      <div className="space-y-3">
        <div className="flex flex-wrap items-start gap-3">
          <h2 className="text-2xl font-bold tracking-tight">{project.name}</h2>
          <Badge
            variant="outline"
            className={cn(
              "rounded-md border",
              projectStatusColors[project.status]
            )}
          >
            {projectStatusLabels[project.status]}
          </Badge>
        </div>
        {project.description && (
          <p className="max-w-3xl text-muted-foreground">{project.description}</p>
        )}
        <div className="flex flex-wrap items-center gap-4 text-sm text-muted-foreground">
          <span className="flex items-center gap-2">
            <Avatar className="size-6">
              {project.owner.avatarUrl && (
                <AvatarImage src={project.owner.avatarUrl} alt={project.owner.fullName} />
              )}
              <AvatarFallback className="text-[10px]">
                {getInitials(project.owner.fullName)}
              </AvatarFallback>
            </Avatar>
            <span>
              {project.owner.fullName}
              <span className="text-muted-foreground/80">
                {" "}
                @{project.owner.username}
              </span>
            </span>
          </span>
          <span className="flex items-center gap-1">
            <Calendar className="size-4" />
            {formatDate(project.startDate)} – {formatDate(project.endDate)}
          </span>
          <span className="flex items-center gap-1">
            <Users className="size-4" />
            {project.memberCount} members
          </span>
        </div>
      </div>

      <div className="grid gap-6 lg:grid-cols-[1fr_320px]">
        <div className="space-y-4 min-w-0">
          <div className="flex items-center justify-between">
            <h3 className="text-lg font-semibold">Tasks</h3>
            <CreateTaskDialog projectId={id} />
          </div>
          <TaskFiltersBar filters={filters} onChange={setFilters} />
          <TaskList
            tasks={tasks}
            isLoading={tasksLoading}
            emptyAction={<CreateTaskDialog projectId={id} />}
          />
        </div>

        <ProjectMembersPanel
          projectId={id}
          canManage={canManageProjects()}
        />
      </div>
    </div>
  );
}
