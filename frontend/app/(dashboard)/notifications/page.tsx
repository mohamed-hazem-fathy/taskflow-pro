"use client";

import { toast } from "sonner";
import { Bell, Loader2 } from "lucide-react";
import { NotificationItem } from "@/components/notifications/NotificationItem";
import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import { useMarkAllAsRead, useNotifications } from "@/hooks/useNotifications";

export default function NotificationsPage() {
  const { data, isLoading, isError } = useNotifications();
  const markAllAsRead = useMarkAllAsRead();
  const notifications = data?.content ?? [];
  const hasUnread = notifications.some((n) => !n.isRead);

  const handleMarkAll = async () => {
    try {
      await markAllAsRead.mutateAsync();
      toast.success("All notifications marked as read");
    } catch {
      toast.error("Failed to mark notifications as read");
    }
  };

  return (
    <div className="mx-auto max-w-3xl space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold tracking-tight">Notifications</h2>
          <p className="text-sm text-muted-foreground">
            Stay updated on your tasks and projects
          </p>
        </div>
        {hasUnread && (
          <Button
            variant="outline"
            size="sm"
            className="rounded-md transition-colors duration-200"
            onClick={handleMarkAll}
            disabled={markAllAsRead.isPending}
          >
            {markAllAsRead.isPending && (
              <Loader2 className="size-4 animate-spin" />
            )}
            Mark all as read
          </Button>
        )}
      </div>

      {isLoading ? (
        <div className="space-y-3">
          {Array.from({ length: 5 }).map((_, i) => (
            <Skeleton key={i} className="h-24 rounded-lg" />
          ))}
        </div>
      ) : isError ? (
        <div className="rounded-lg border border-dashed p-12 text-center text-sm text-muted-foreground">
          Unable to load notifications.
        </div>
      ) : notifications.length === 0 ? (
        <div className="flex flex-col items-center rounded-lg border border-dashed py-16 text-center">
          <Bell className="size-12 text-muted-foreground mb-4" />
          <h3 className="font-semibold">No notifications</h3>
          <p className="mt-1 text-sm text-muted-foreground">
            You&apos;re all caught up!
          </p>
        </div>
      ) : (
        <div className="space-y-3">
          {notifications.map((notification) => (
            <NotificationItem
              key={notification.id}
              notification={notification}
            />
          ))}
        </div>
      )}
    </div>
  );
}
