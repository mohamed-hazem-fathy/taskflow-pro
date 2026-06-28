"use client";

import { useEffect } from "react";
import { usePathname, useRouter } from "next/navigation";
import { Sidebar } from "@/components/layout/Sidebar";
import { Topbar } from "@/components/layout/Topbar";
import { Skeleton } from "@/components/ui/skeleton";
import { isAuthenticated } from "@/lib/auth";

function getPageTitle(pathname: string): string {
  if (pathname === "/dashboard") return "Dashboard";
  if (pathname === "/projects") return "Projects";
  if (pathname.startsWith("/projects/")) return "Project Details";
  if (pathname.startsWith("/tasks/")) return "Task Details";
  if (pathname === "/notifications") return "Notifications";
  if (pathname === "/settings") return "Settings";
  return "TaskFlow Pro";
}

export function DashboardShell({ children }: { children: React.ReactNode }) {
  const router = useRouter();
  const pathname = usePathname();
  const title = getPageTitle(pathname);

  useEffect(() => {
    if (!isAuthenticated()) {
      router.replace("/login");
    }
  }, [router]);

  if (typeof window !== "undefined" && !isAuthenticated()) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <Skeleton className="h-8 w-48" />
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-background">
      <Sidebar />
      <div className="lg:pl-60">
        <Topbar title={title} />
        <main className="p-4 lg:p-6">{children}</main>
      </div>
    </div>
  );
}
