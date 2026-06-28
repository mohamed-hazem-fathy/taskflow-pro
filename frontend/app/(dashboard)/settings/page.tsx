"use client";

import { Settings } from "lucide-react";
import { useAuth } from "@/hooks/useAuth";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { getInitials } from "@/lib/utils";

export default function SettingsPage() {
  const { user, storedUser } = useAuth();
  const displayName = user?.fullName ?? storedUser?.fullName ?? "User";
  const displayUsername = user?.username ?? storedUser?.username;
  const displayEmail = user?.email ?? storedUser?.email ?? "";

  return (
    <div className="mx-auto max-w-2xl space-y-6">
      <div>
        <h2 className="text-2xl font-bold tracking-tight">Settings</h2>
        <p className="text-sm text-muted-foreground">
          Manage your account preferences
        </p>
      </div>

      <div className="rounded-lg border bg-card p-6">
        <div className="flex items-center gap-4">
          <Avatar className="size-16">
            {user?.avatarUrl && (
              <AvatarImage src={user.avatarUrl} alt={displayName} />
            )}
            <AvatarFallback>{getInitials(displayName)}</AvatarFallback>
          </Avatar>
          <div>
            <p className="font-semibold">{displayName}</p>
            {displayUsername && (
              <p className="text-sm text-muted-foreground">@{displayUsername}</p>
            )}
            <p className="text-sm text-muted-foreground">{displayEmail}</p>
            {user?.roles && (
              <p className="mt-1 text-xs text-muted-foreground">
                Roles: {user.roles.join(", ")}
              </p>
            )}
          </div>
        </div>
      </div>

      <div className="flex flex-col items-center rounded-lg border border-dashed py-16 text-center">
        <Settings className="size-10 text-muted-foreground mb-3" />
        <h3 className="font-semibold">More settings coming soon</h3>
        <p className="mt-1 text-sm text-muted-foreground">
          Profile editing and notification preferences will be available here.
        </p>
      </div>
    </div>
  );
}
