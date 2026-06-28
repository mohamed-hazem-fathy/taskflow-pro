"use client";

import { useRouter } from "next/navigation";
import { FolderKanban } from "lucide-react";
import { ProjectCard } from "@/components/projects/ProjectCard";
import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import type { Project } from "@/types";

interface ProjectListProps {
  projects: Project[];
  isLoading?: boolean;
  emptyAction?: React.ReactNode;
}

export function ProjectList({
  projects,
  isLoading,
  emptyAction,
}: ProjectListProps) {
  const router = useRouter();

  if (isLoading) {
    return (
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {Array.from({ length: 6 }).map((_, i) => (
          <Skeleton key={i} className="h-44 rounded-lg" />
        ))}
      </div>
    );
  }

  if (projects.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center rounded-lg border border-dashed py-16 text-center">
        <FolderKanban className="size-12 text-muted-foreground mb-4" />
        <h3 className="text-lg font-semibold">No projects yet</h3>
        <p className="mt-1 max-w-sm text-sm text-muted-foreground">
          Create your first project to start organizing tasks with your team.
        </p>
        {emptyAction && <div className="mt-4">{emptyAction}</div>}
      </div>
    );
  }

  return (
    <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
      {projects.map((project) => (
        <ProjectCard
          key={project.id}
          project={project}
          onClick={() => router.push(`/projects/${project.id}`)}
        />
      ))}
    </div>
  );
}

interface ProjectPaginationProps {
  page: number;
  totalPages: number;
  onPageChange: (page: number) => void;
}

export function ProjectPagination({
  page,
  totalPages,
  onPageChange,
}: ProjectPaginationProps) {
  if (totalPages <= 1) return null;

  return (
    <div className="flex items-center justify-center gap-2 pt-4">
      <Button
        variant="outline"
        size="sm"
        disabled={page <= 0}
        onClick={() => onPageChange(page - 1)}
        className="rounded-md transition-colors duration-200"
      >
        Previous
      </Button>
      <span className="text-sm text-muted-foreground">
        Page {page + 1} of {totalPages}
      </span>
      <Button
        variant="outline"
        size="sm"
        disabled={page >= totalPages - 1}
        onClick={() => onPageChange(page + 1)}
        className="rounded-md transition-colors duration-200"
      >
        Next
      </Button>
    </div>
  );
}
