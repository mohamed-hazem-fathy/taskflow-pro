"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import {
  Bell,
  FolderKanban,
  LayoutDashboard,
  LogOut,
  Menu,
  Settings,
  X,
} from "lucide-react";
import { useState } from "react";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { Button } from "@/components/ui/button";
import { ThemeToggle } from "@/components/layout/ThemeToggle";
import { useAuth } from "@/hooks/useAuth";
import { useUnreadCount } from "@/hooks/useNotifications";
import { cn, getInitials } from "@/lib/utils";

const navItems = [
  { href: "/dashboard", label: "Dashboard", icon: LayoutDashboard },
  { href: "/projects", label: "Projects", icon: FolderKanban },
  { href: "/notifications", label: "Notifications", icon: Bell },
  { href: "/settings", label: "Settings", icon: Settings },
];

export function Sidebar() {
  const pathname = usePathname();
  const { user, storedUser, logout } = useAuth();
  const { data: unreadCount = 0 } = useUnreadCount();
  const [mobileOpen, setMobileOpen] = useState(false);

  const displayName = user?.fullName ?? storedUser?.fullName ?? "User";
  const displayUsername = user?.username ?? storedUser?.username;
  const displayEmail = user?.email ?? storedUser?.email ?? "";

  const sidebarContent = (
    <div className="flex h-full flex-col bg-zinc-950 text-zinc-100">
      <div className="flex h-14 items-center gap-2 border-b border-zinc-800 px-4">
        <div className="flex size-8 items-center justify-center rounded-md bg-white text-xs font-bold text-zinc-950">
          TF
        </div>
        <span className="font-semibold tracking-tight">TaskFlow</span>
      </div>

      <nav className="flex-1 space-y-1 p-3">
        {navItems.map(({ href, label, icon: Icon }) => {
          const active =
            pathname === href || (href !== "/dashboard" && pathname.startsWith(href));
          const showBadge = href === "/notifications" && unreadCount > 0;

          return (
            <Link
              key={href}
              href={href}
              onClick={() => setMobileOpen(false)}
              className={cn(
                "flex items-center gap-3 rounded-md px-3 py-2 text-sm font-medium transition-colors duration-200",
                active
                  ? "bg-zinc-800 text-white"
                  : "text-zinc-400 hover:bg-zinc-900 hover:text-white"
              )}
            >
              <Icon className="size-4 shrink-0" />
              <span className="flex-1">{label}</span>
              {showBadge && (
                <span className="flex size-5 items-center justify-center rounded-md bg-blue-600 text-[10px] font-semibold text-white">
                  {unreadCount > 99 ? "99+" : unreadCount}
                </span>
              )}
            </Link>
          );
        })}
      </nav>

      <div className="border-t border-zinc-800 p-3 space-y-2">
        <div className="flex items-center gap-3 rounded-md px-2 py-2">
          <Avatar className="size-8">
            {user?.avatarUrl && <AvatarImage src={user.avatarUrl} alt={displayName} />}
            <AvatarFallback className="bg-zinc-800 text-xs text-zinc-100">
              {getInitials(displayName)}
            </AvatarFallback>
          </Avatar>
          <div className="min-w-0 flex-1">
            <p className="truncate text-sm font-medium">{displayName}</p>
            {displayUsername && (
              <p className="truncate text-xs text-zinc-500">@{displayUsername}</p>
            )}
            <p className="truncate text-xs text-zinc-500">{displayEmail}</p>
          </div>
          <ThemeToggle />
        </div>
        <Button
          variant="ghost"
          className="w-full justify-start gap-3 text-zinc-400 hover:text-white hover:bg-zinc-900 transition-colors duration-200"
          onClick={logout}
        >
          <LogOut className="size-4" />
          Logout
        </Button>
      </div>
    </div>
  );

  return (
    <>
      <Button
        variant="outline"
        size="icon"
        className="fixed left-4 top-4 z-50 lg:hidden"
        onClick={() => setMobileOpen(true)}
      >
        <Menu className="size-4" />
      </Button>

      {mobileOpen && (
        <div
          className="fixed inset-0 z-40 bg-black/50 lg:hidden"
          onClick={() => setMobileOpen(false)}
        />
      )}

      <aside
        className={cn(
          "fixed inset-y-0 left-0 z-50 w-60 -translate-x-full transition-transform duration-200 lg:translate-x-0",
          mobileOpen && "translate-x-0"
        )}
      >
        <Button
          variant="ghost"
          size="icon"
          className="absolute right-2 top-2 z-10 text-zinc-400 lg:hidden"
          onClick={() => setMobileOpen(false)}
        >
          <X className="size-4" />
        </Button>
        {sidebarContent}
      </aside>
    </>
  );
}
