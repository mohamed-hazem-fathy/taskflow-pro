"use client";

import { useMemo, useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { toast } from "sonner";
import { Loader2, Search, Trash2, UserPlus } from "lucide-react";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Skeleton } from "@/components/ui/skeleton";
import {
  useAddMember,
  useProjectMembers,
  useRemoveMember,
} from "@/hooks/useProjects";
import api from "@/lib/api";
import { cn, getInitials } from "@/lib/utils";
import type { ApiResponse, PagedResponse, ProjectRole, User } from "@/types";

interface ProjectMembersPanelProps {
  projectId: string;
  canManage?: boolean;
}

const roleLabels: Record<ProjectRole, string> = {
  OWNER: "Owner",
  MANAGER: "Manager",
  MEMBER: "Member",
  VIEWER: "Viewer",
};

export function ProjectMembersPanel({
  projectId,
  canManage = false,
}: ProjectMembersPanelProps) {
  const { data: members, isLoading } = useProjectMembers(projectId);
  const addMember = useAddMember();
  const removeMember = useRemoveMember();
  const [open, setOpen] = useState(false);
  const [search, setSearch] = useState("");
  const [selectedUser, setSelectedUser] = useState<User | null>(null);
  const [role, setRole] = useState<ProjectRole>("MEMBER");

  const memberUserIds = useMemo(
    () => new Set(members?.map((member) => member.user.id) ?? []),
    [members]
  );

  const {
    data: users = [],
    isLoading: isUsersLoading,
    isError: isUsersError,
  } = useQuery({
    queryKey: ["users", "add-member", projectId],
    queryFn: async () => {
      const { data } = await api.get<ApiResponse<PagedResponse<User>>>(
        "/api/users",
        { params: { page: 0, size: 100 } }
      );
      return data.data.content;
    },
    enabled: open,
    staleTime: 30_000,
  });

  const filteredUsers = useMemo(() => {
    const query = search.trim().toLowerCase();
    if (!query) return users;

    return users.filter(
      (user) =>
        user.fullName.toLowerCase().includes(query) ||
        user.username.toLowerCase().includes(query) ||
        user.email.toLowerCase().includes(query)
    );
  }, [search, users]);

  const resetForm = () => {
    setSearch("");
    setSelectedUser(null);
    setRole("MEMBER");
  };

  const handleOpenChange = (isOpen: boolean) => {
    setOpen(isOpen);
    if (!isOpen) resetForm();
  };

  const handleAdd = async () => {
    if (!selectedUser) {
      toast.error("Please select a user");
      return;
    }

    try {
      await addMember.mutateAsync({
        projectId,
        userId: selectedUser.id,
        role,
      });
      toast.success("Member added");
      resetForm();
      setOpen(false);
    } catch {
      toast.error("Failed to add member");
    }
  };

  const handleRemove = async (userId: string) => {
    try {
      await removeMember.mutateAsync({ projectId, userId });
      toast.success("Member removed");
    } catch {
      toast.error("Failed to remove member");
    }
  };

  return (
    <div className="rounded-lg border bg-card p-4">
      <div className="mb-4 flex items-center justify-between">
        <h2 className="text-sm font-semibold">Team</h2>
        {canManage && (
          <Dialog open={open} onOpenChange={handleOpenChange}>
            <DialogTrigger asChild>
              <Button size="sm" variant="outline" className="rounded-md">
                <UserPlus className="size-4" />
                Add Member
              </Button>
            </DialogTrigger>
            <DialogContent className="sm:max-w-md">
              <DialogHeader>
                <DialogTitle>Add Team Member</DialogTitle>
              </DialogHeader>
              <div className="space-y-4">
                <div className="space-y-2">
                  <Label>Select user</Label>
                  {selectedUser ? (
                    <div className="flex items-center gap-3 rounded-md border bg-muted/30 p-3">
                      <Avatar className="size-9">
                        {selectedUser.avatarUrl && (
                          <AvatarImage
                            src={selectedUser.avatarUrl}
                            alt={selectedUser.fullName}
                          />
                        )}
                        <AvatarFallback className="text-xs">
                          {getInitials(selectedUser.fullName)}
                        </AvatarFallback>
                      </Avatar>
                      <div className="min-w-0 flex-1">
                        <p className="truncate text-sm font-medium">
                          {selectedUser.fullName}
                        </p>
                        <p className="truncate text-xs text-muted-foreground">
                          @{selectedUser.username} · {selectedUser.email}
                        </p>
                      </div>
                      <Button
                        type="button"
                        variant="ghost"
                        size="sm"
                        className="shrink-0 rounded-md"
                        onClick={() => setSelectedUser(null)}
                      >
                        Change
                      </Button>
                    </div>
                  ) : (
                    <div className="overflow-hidden rounded-md border">
                      <div className="relative border-b">
                        <Search className="absolute left-2.5 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
                        <Input
                          value={search}
                          onChange={(e) => setSearch(e.target.value)}
                          placeholder="Search by name, username, or email..."
                          className="rounded-none border-0 pl-9 shadow-none focus-visible:ring-0"
                          autoComplete="off"
                        />
                        {isUsersLoading && (
                          <Loader2 className="absolute right-2.5 top-1/2 size-4 -translate-y-1/2 animate-spin text-muted-foreground" />
                        )}
                      </div>

                      <div className="max-h-56 overflow-y-auto">
                        {isUsersLoading ? (
                          <div className="flex items-center justify-center gap-2 p-6 text-sm text-muted-foreground">
                            <Loader2 className="size-4 animate-spin" />
                            Loading users...
                          </div>
                        ) : isUsersError ? (
                          <p className="p-4 text-center text-sm text-muted-foreground">
                            Unable to load users.
                          </p>
                        ) : filteredUsers.length === 0 ? (
                          <p className="p-4 text-center text-sm text-muted-foreground">
                            No users found
                          </p>
                        ) : (
                          <ul className="divide-y">
                            {filteredUsers.map((user) => {
                              const isMember = memberUserIds.has(user.id);

                              return (
                                <li key={user.id}>
                                  <button
                                    type="button"
                                    disabled={isMember}
                                    onClick={() => setSelectedUser(user)}
                                    className={cn(
                                      "flex w-full items-center gap-3 p-3 text-left transition-colors duration-200",
                                      isMember
                                        ? "cursor-not-allowed bg-muted/40 opacity-60"
                                        : "hover:bg-muted/50"
                                    )}
                                  >
                                    <Avatar className="size-8">
                                      {user.avatarUrl && (
                                        <AvatarImage
                                          src={user.avatarUrl}
                                          alt={user.fullName}
                                        />
                                      )}
                                      <AvatarFallback className="text-xs">
                                        {getInitials(user.fullName)}
                                      </AvatarFallback>
                                    </Avatar>
                                    <div className="min-w-0 flex-1">
                                      <p className="truncate text-sm font-medium">
                                        {user.fullName}
                                      </p>
                                      <p className="truncate text-xs text-muted-foreground">
                                        @{user.username} · {user.email}
                                      </p>
                                      {isMember && (
                                        <p className="text-[10px] text-muted-foreground">
                                          Already a member
                                        </p>
                                      )}
                                    </div>
                                  </button>
                                </li>
                              );
                            })}
                          </ul>
                        )}
                      </div>
                    </div>
                  )}
                </div>

                <div className="space-y-2">
                  <Label>Role</Label>
                  <Select
                    value={role}
                    onValueChange={(v) => setRole(v as ProjectRole)}
                  >
                    <SelectTrigger className="rounded-md">
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      {(["MANAGER", "MEMBER", "VIEWER"] as ProjectRole[]).map(
                        (r) => (
                          <SelectItem key={r} value={r}>
                            {roleLabels[r]}
                          </SelectItem>
                        )
                      )}
                    </SelectContent>
                  </Select>
                </div>

                <Button
                  className="w-full rounded-md"
                  onClick={handleAdd}
                  disabled={addMember.isPending || !selectedUser}
                >
                  {addMember.isPending && (
                    <Loader2 className="size-4 animate-spin" />
                  )}
                  Add Member
                </Button>
              </div>
            </DialogContent>
          </Dialog>
        )}
      </div>

      {isLoading ? (
        <div className="space-y-3">
          {Array.from({ length: 3 }).map((_, i) => (
            <div key={i} className="flex items-center gap-3">
              <Skeleton className="size-8 rounded-full" />
              <div className="flex-1 space-y-1">
                <Skeleton className="h-4 w-24" />
                <Skeleton className="h-3 w-16" />
              </div>
            </div>
          ))}
        </div>
      ) : (
        <div className="space-y-2">
          {members?.map((member) => (
            <div
              key={member.id}
              className="flex items-center gap-3 rounded-md p-2 hover:bg-muted/50 transition-colors duration-200"
            >
              <Avatar className="size-8">
                {member.user.avatarUrl && (
                  <AvatarImage
                    src={member.user.avatarUrl}
                    alt={member.user.fullName}
                  />
                )}
                <AvatarFallback className="text-xs">
                  {getInitials(member.user.fullName)}
                </AvatarFallback>
              </Avatar>
              <div className="min-w-0 flex-1">
                <p className="truncate text-sm font-medium">
                  {member.user.fullName}
                </p>
                <p className="truncate text-xs text-muted-foreground">
                  @{member.user.username}
                </p>
                <Badge variant="outline" className="mt-0.5 rounded-md text-[10px]">
                  {roleLabels[member.role]}
                </Badge>
              </div>
              {canManage && member.role !== "OWNER" && (
                <Button
                  variant="ghost"
                  size="icon"
                  className="size-8 text-muted-foreground hover:text-destructive"
                  onClick={() => handleRemove(member.user.id)}
                  disabled={removeMember.isPending}
                >
                  <Trash2 className="size-4" />
                </Button>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
