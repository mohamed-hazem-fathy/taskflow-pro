"use client";

import Link from "next/link";
import {
  Bell,
  MessageSquare,
  RefreshCw,
  UserPlus,
} from "lucide-react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { useNotifications } from "@/hooks/useNotifications";
import { cn, timeAgo } from "@/lib/utils";
import type { NotificationType } from "@/types";

const typeIcons: Record<NotificationType, typeof Bell> = {
  TASK_ASSIGNED: UserPlus,
  TASK_STATUS_CHANGED: RefreshCw,
  TASK_COMMENT_ADDED: MessageSquare,
  PROJECT_MEMBER_ADDED: UserPlus,
};

export function RecentActivity() {
  const { data, isLoading, isError } = useNotifications(0, 5);
  const notifications = data?.content ?? [];

  if (isLoading) {
    return (
      <Card className="rounded-lg border">
        <CardHeader>
          <CardTitle>Recent Activity</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          {Array.from({ length: 5 }).map((_, i) => (
            <div key={i} className="flex gap-3">
              <Skeleton className="size-8 rounded-full" />
              <div className="flex-1 space-y-2">
                <Skeleton className="h-4 w-3/4" />
                <Skeleton className="h-3 w-1/2" />
              </div>
            </div>
          ))}
        </CardContent>
      </Card>
    );
  }

  if (isError) {
    return (
      <Card className="rounded-lg border">
        <CardHeader>
          <CardTitle>Recent Activity</CardTitle>
        </CardHeader>
        <CardContent>
          <p className="text-sm text-muted-foreground">
            Unable to load recent activity.
          </p>
        </CardContent>
      </Card>
    );
  }

  if (notifications.length === 0) {
    return (
      <Card className="rounded-lg border">
        <CardHeader>
          <CardTitle>Recent Activity</CardTitle>
        </CardHeader>
        <CardContent className="flex flex-col items-center py-8 text-center">
          <Bell className="size-8 text-muted-foreground mb-2" />
          <p className="text-sm text-muted-foreground">No recent activity</p>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card className="rounded-lg border">
      <CardHeader className="flex flex-row items-center justify-between">
        <CardTitle>Recent Activity</CardTitle>
        <Link
          href="/notifications"
          className="text-sm text-muted-foreground hover:text-foreground transition-colors duration-200"
        >
          View all
        </Link>
      </CardHeader>
      <CardContent>
        <div className="relative space-y-4 before:absolute before:left-4 before:top-2 before:h-[calc(100%-1rem)] before:w-px before:bg-border">
          {notifications.map((notification) => {
            const Icon = typeIcons[notification.type] ?? Bell;
            return (
              <div key={notification.id} className="relative flex gap-3 pl-1">
                <div
                  className={cn(
                    "relative z-10 flex size-8 shrink-0 items-center justify-center rounded-full border bg-background",
                    !notification.isRead && "border-blue-500/50 bg-blue-500/10"
                  )}
                >
                  <Icon className="size-3.5 text-muted-foreground" />
                </div>
                <div className="min-w-0 flex-1 pt-0.5">
                  <p className="text-sm font-medium leading-none">
                    {notification.title}
                  </p>
                  <p className="mt-1 text-sm text-muted-foreground line-clamp-2">
                    {notification.message}
                  </p>
                  <p className="mt-1 text-xs text-muted-foreground">
                    {timeAgo(notification.createdAt)}
                  </p>
                </div>
              </div>
            );
          })}
        </div>
      </CardContent>
    </Card>
  );
}
