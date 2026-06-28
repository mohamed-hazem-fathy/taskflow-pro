"use client";

import { useState } from "react";
import { toast } from "sonner";
import { Loader2, Send } from "lucide-react";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import { Textarea } from "@/components/ui/textarea";
import { useAddComment, useTaskComments } from "@/hooks/useTasks";
import { getInitials, timeAgo } from "@/lib/utils";

interface TaskCommentsProps {
  taskId: string;
}

export function TaskComments({ taskId }: TaskCommentsProps) {
  const { data: comments, isLoading } = useTaskComments(taskId);
  const addComment = useAddComment();
  const [content, setContent] = useState("");

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!content.trim()) return;

    try {
      await addComment.mutateAsync({ taskId, content: content.trim() });
      setContent("");
      toast.success("Comment added");
    } catch {
      toast.error("Failed to add comment");
    }
  };

  return (
    <div className="space-y-4">
      <h3 className="text-sm font-semibold">Comments</h3>

      {isLoading ? (
        <div className="space-y-4">
          {Array.from({ length: 3 }).map((_, i) => (
            <div key={i} className="flex gap-3">
              <Skeleton className="size-8 rounded-full" />
              <div className="flex-1 space-y-2">
                <Skeleton className="h-4 w-32" />
                <Skeleton className="h-12 w-full" />
              </div>
            </div>
          ))}
        </div>
      ) : comments && comments.length > 0 ? (
        <div className="space-y-4">
          {comments.map((comment) => (
            <div key={comment.id} className="flex gap-3">
              <Avatar className="size-8">
                {comment.author.avatarUrl && (
                  <AvatarImage
                    src={comment.author.avatarUrl}
                    alt={comment.author.fullName}
                  />
                )}
                <AvatarFallback className="text-xs">
                  {getInitials(comment.author.fullName)}
                </AvatarFallback>
              </Avatar>
              <div className="min-w-0 flex-1">
                <div className="flex flex-wrap items-baseline gap-x-2 gap-y-0.5">
                  <span className="text-sm font-medium">
                    {comment.author.fullName}
                  </span>
                  <span className="text-xs text-muted-foreground">
                    @{comment.author.username}
                  </span>
                  <span className="text-xs text-muted-foreground">
                    {timeAgo(comment.createdAt)}
                  </span>
                </div>
                <p className="mt-1 text-sm whitespace-pre-wrap">{comment.content}</p>
              </div>
            </div>
          ))}
        </div>
      ) : (
        <p className="text-sm text-muted-foreground">No comments yet.</p>
      )}

      <form onSubmit={handleSubmit} className="space-y-3">
        <Textarea
          placeholder="Write a comment..."
          rows={3}
          value={content}
          onChange={(e) => setContent(e.target.value)}
          className="rounded-md"
        />
        <Button
          type="submit"
          size="sm"
          className="rounded-md"
          disabled={addComment.isPending || !content.trim()}
        >
          {addComment.isPending ? (
            <Loader2 className="size-4 animate-spin" />
          ) : (
            <Send className="size-4" />
          )}
          Add Comment
        </Button>
      </form>
    </div>
  );
}
