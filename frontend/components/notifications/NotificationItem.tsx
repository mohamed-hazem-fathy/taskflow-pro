"use client";

import { useRouter } from "next/navigation";
import {
  Bell,
  MessageSquare,
  RefreshCw,
  UserPlus,
} from "lucide-react";
import { cn, timeAgo } from "@/lib/utils";
import { useMarkAsRead } from "@/hooks/useNotifications";
import type { Notification, NotificationType } from "@/types";

const typeIcons: Record<NotificationType, typeof Bell> = {
  TASK_ASSIGNED: UserPlus,
  TASK_STATUS_CHANGED: RefreshCw,
  TASK_COMMENT_ADDED: MessageSquare,
  PROJECT_MEMBER_ADDED: UserPlus,
};

interface NotificationItemProps {
  notification: Notification;
}

function getReferencePath(notification: Notification): string | null {
  if (!notification.referenceId) return null;

  switch (notification.referenceType?.toUpperCase()) {
    case "TASK":
      return `/tasks/${notification.referenceId}`;
    case "PROJECT":
      return `/projects/${notification.referenceId}`;
    default:
      return null;
  }
}

export function NotificationItem({ notification }: NotificationItemProps) {
  const router = useRouter();
  const markAsRead = useMarkAsRead();
  const Icon = typeIcons[notification.type] ?? Bell;

  const handleClick = async () => {
    if (!notification.isRead) {
      try {
        await markAsRead.mutateAsync(notification.id);
      } catch {
        // navigation still proceeds
      }
    }

    const path = getReferencePath(notification);
    if (path) router.push(path);
  };

  return (
    <button
      type="button"
      onClick={handleClick}
      className={cn(
        "flex w-full items-start gap-3 rounded-lg border p-4 text-left transition-colors duration-200 hover:bg-muted/50",
        !notification.isRead && "border-blue-500/20 bg-blue-500/5"
      )}
    >
      <div
        className={cn(
          "flex size-9 shrink-0 items-center justify-center rounded-md border",
          !notification.isRead
            ? "border-blue-500/30 bg-blue-500/10"
            : "bg-muted"
        )}
      >
        <Icon className="size-4 text-muted-foreground" />
      </div>
      <div className="min-w-0 flex-1">
        <div className="flex items-start justify-between gap-2">
          <p className="font-medium leading-snug">{notification.title}</p>
          {!notification.isRead && (
            <span className="size-2 shrink-0 rounded-full bg-blue-500 mt-1.5" />
          )}
        </div>
        <p className="mt-1 text-sm text-muted-foreground">{notification.message}</p>
        <p className="mt-2 text-xs text-muted-foreground">
          {timeAgo(notification.createdAt)}
        </p>
      </div>
    </button>
  );
}
