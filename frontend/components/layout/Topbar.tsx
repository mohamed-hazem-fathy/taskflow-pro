"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { Bell, LogOut, User } from "lucide-react";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { Button } from "@/components/ui/button";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { useAuth } from "@/hooks/useAuth";
import { useUnreadCount } from "@/hooks/useNotifications";
import { getInitials } from "@/lib/utils";

interface TopbarProps {
  title: string;
}

export function Topbar({ title }: TopbarProps) {
  const router = useRouter();
  const { user, storedUser, logout } = useAuth();
  const { data: unreadCount = 0 } = useUnreadCount();

  const displayName = user?.fullName ?? storedUser?.fullName ?? "User";

  return (
    <header className="sticky top-0 z-30 flex h-14 items-center justify-between border-b border-border bg-background/95 px-4 pl-16 backdrop-blur supports-[backdrop-filter]:bg-background/60 lg:pl-6">
      <h1 className="text-lg font-semibold tracking-tight">{title}</h1>

      <div className="flex items-center gap-2">
        <Button
          variant="ghost"
          size="icon"
          className="relative size-8 transition-colors duration-200"
          asChild
        >
          <Link href="/notifications">
            <Bell className="size-4" />
            {unreadCount > 0 && (
              <span className="absolute -right-0.5 -top-0.5 flex size-4 items-center justify-center rounded-md bg-blue-600 text-[10px] font-semibold text-white">
                {unreadCount > 9 ? "9+" : unreadCount}
              </span>
            )}
          </Link>
        </Button>

        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button variant="ghost" size="icon" className="size-8 rounded-full">
              <Avatar className="size-8">
                {user?.avatarUrl && (
                  <AvatarImage src={user.avatarUrl} alt={displayName} />
                )}
                <AvatarFallback className="text-xs">
                  {getInitials(displayName)}
                </AvatarFallback>
              </Avatar>
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent align="end" className="w-48">
            <DropdownMenuItem onClick={() => router.push("/settings")}>
              <User className="size-4" />
              Profile
            </DropdownMenuItem>
            <DropdownMenuItem onClick={() => router.push("/notifications")}>
              <Bell className="size-4" />
              Notifications
              {unreadCount > 0 && (
                <span className="ml-auto text-xs text-muted-foreground">
                  {unreadCount}
                </span>
              )}
            </DropdownMenuItem>
            <DropdownMenuSeparator />
            <DropdownMenuItem onClick={logout}>
              <LogOut className="size-4" />
              Logout
            </DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>
      </div>
    </header>
  );
}
