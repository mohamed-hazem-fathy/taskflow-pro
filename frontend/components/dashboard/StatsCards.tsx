"use client";

import { FolderKanban, Bell, Clock, ListTodo } from "lucide-react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { useProjects } from "@/hooks/useProjects";
import { useAllTasks } from "@/hooks/useTasks";
import { useUnreadCount } from "@/hooks/useNotifications";
import { isDueSoon } from "@/lib/utils";

export function StatsCards() {
  const { data: projects, isLoading: projectsLoading } = useProjects({ size: 100 });
  const { data: tasks, isLoading: tasksLoading } = useAllTasks({ size: 200 });
  const { data: unreadCount = 0, isLoading: unreadLoading } = useUnreadCount();

  const isLoading = projectsLoading || tasksLoading || unreadLoading;
  const taskList = tasks?.content ?? [];
  const activeTasks = taskList.filter((t) => t.status !== "DONE").length;
  const dueSoon = taskList.filter(
    (t) => t.status !== "DONE" && isDueSoon(t.dueDate)
  ).length;

  const stats = [
    {
      label: "Total Projects",
      value: projects?.totalElements ?? 0,
      icon: FolderKanban,
    },
    {
      label: "Active Tasks",
      value: activeTasks,
      icon: ListTodo,
    },
    {
      label: "Tasks Due Soon",
      value: dueSoon,
      icon: Clock,
    },
    {
      label: "Unread Notifications",
      value: unreadCount,
      icon: Bell,
    },
  ];

  if (isLoading) {
    return (
      <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        {Array.from({ length: 4 }).map((_, i) => (
          <Card key={i} className="rounded-lg border">
            <CardHeader className="pb-2">
              <Skeleton className="h-4 w-24" />
            </CardHeader>
            <CardContent>
              <Skeleton className="h-8 w-16" />
            </CardContent>
          </Card>
        ))}
      </div>
    );
  }

  return (
    <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
      {stats.map(({ label, value, icon: Icon }) => (
        <Card key={label} className="rounded-lg border bg-card transition-colors duration-200">
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground">
              {label}
            </CardTitle>
            <Icon className="size-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{value}</div>
          </CardContent>
        </Card>
      ))}
    </div>
  );
}
