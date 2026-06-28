"use client";

import { useState } from "react";
import { Search } from "lucide-react";
import {
  CreateProjectDialog,
} from "@/components/projects/CreateProjectDialog";
import {
  ProjectList,
  ProjectPagination,
} from "@/components/projects/ProjectList";
import { Input } from "@/components/ui/input";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { useAuth } from "@/hooks/useAuth";
import { useProjects } from "@/hooks/useProjects";
import { projectStatusLabels } from "@/lib/utils";
import type { ProjectStatus } from "@/types";

export default function ProjectsPage() {
  const { canManageProjects } = useAuth();
  const [search, setSearch] = useState("");
  const [status, setStatus] = useState<ProjectStatus | "ALL">("ALL");
  const [page, setPage] = useState(0);

  const { data, isLoading } = useProjects({
    search: search || undefined,
    status,
    page,
    size: 12,
  });

  return (
    <div className="space-y-6">
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h2 className="text-2xl font-bold tracking-tight">Projects</h2>
          <p className="text-sm text-muted-foreground">
            Manage and organize your team&apos;s work
          </p>
        </div>
        {canManageProjects() && <CreateProjectDialog />}
      </div>

      <div className="flex flex-col gap-3 sm:flex-row">
        <div className="relative flex-1">
          <Search className="absolute left-2.5 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
          <Input
            placeholder="Search projects..."
            className="pl-9 rounded-md"
            value={search}
            onChange={(e) => {
              setSearch(e.target.value);
              setPage(0);
            }}
          />
        </div>
        <Select
          value={status}
          onValueChange={(value) => {
            setStatus(value as ProjectStatus | "ALL");
            setPage(0);
          }}
        >
          <SelectTrigger className="w-full sm:w-44 rounded-md">
            <SelectValue placeholder="Status" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="ALL">All statuses</SelectItem>
            {(Object.keys(projectStatusLabels) as ProjectStatus[]).map((s) => (
              <SelectItem key={s} value={s}>
                {projectStatusLabels[s]}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>

      <ProjectList
        projects={data?.content ?? []}
        isLoading={isLoading}
        emptyAction={
          canManageProjects() ? <CreateProjectDialog /> : undefined
        }
      />

      {data && (
        <ProjectPagination
          page={page}
          totalPages={data.totalPages}
          onPageChange={setPage}
        />
      )}
    </div>
  );
}
