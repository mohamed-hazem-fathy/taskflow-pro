"use client";

import Link from "next/link";
import { StatsCards } from "@/components/dashboard/StatsCards";
import { RecentActivity } from "@/components/dashboard/RecentActivity";
import { ProjectCard } from "@/components/projects/ProjectCard";
import { Skeleton } from "@/components/ui/skeleton";
import { useProjects } from "@/hooks/useProjects";

export default function DashboardPage() {
  const { data: projects, isLoading } = useProjects({ size: 6 });

  return (
    <div className="space-y-8">
      <StatsCards />

      <section>
        <div className="mb-4 flex items-center justify-between">
          <h2 className="text-lg font-semibold">My Projects</h2>
          <Link
            href="/projects"
            className="text-sm text-muted-foreground hover:text-foreground transition-colors duration-200"
          >
            View all
          </Link>
        </div>

        {isLoading ? (
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {Array.from({ length: 6 }).map((_, i) => (
              <Skeleton key={i} className="h-44 rounded-lg" />
            ))}
          </div>
        ) : projects && projects.content.length > 0 ? (
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {projects.content.slice(0, 6).map((project) => (
              <Link key={project.id} href={`/projects/${project.id}`}>
                <ProjectCard project={project} />
              </Link>
            ))}
          </div>
        ) : (
          <div className="rounded-lg border border-dashed p-8 text-center text-sm text-muted-foreground">
            No projects yet.{" "}
            <Link href="/projects" className="text-foreground underline-offset-4 hover:underline">
              Create your first project
            </Link>
          </div>
        )}
      </section>

      <RecentActivity />
    </div>
  );
}
