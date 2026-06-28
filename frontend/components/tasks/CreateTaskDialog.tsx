"use client";

import { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { toast } from "sonner";
import { Loader2, Plus } from "lucide-react";
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
import { Textarea } from "@/components/ui/textarea";
import { useCreateTask } from "@/hooks/useTasks";
import { taskPriorityLabels, taskStatusLabels } from "@/lib/utils";
import type { TaskPriority, TaskStatus } from "@/types";

const schema = z.object({
  title: z.string().min(1, "Title is required"),
  description: z.string().optional(),
  status: z.enum(["TODO", "IN_PROGRESS", "IN_REVIEW", "BLOCKED", "DONE"]),
  priority: z.enum(["LOW", "MEDIUM", "HIGH", "CRITICAL"]),
  dueDate: z.string().optional(),
  estimatedHours: z.number().min(0).optional(),
});

type FormValues = z.infer<typeof schema>;

interface CreateTaskDialogProps {
  projectId: string;
  trigger?: React.ReactNode;
}

export function CreateTaskDialog({ projectId, trigger }: CreateTaskDialogProps) {
  const [open, setOpen] = useState(false);
  const createTask = useCreateTask(projectId);
  const {
    register,
    handleSubmit,
    reset,
    setValue,
    watch,
    formState: { errors },
  } = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: {
      title: "",
      description: "",
      status: "TODO",
      priority: "MEDIUM",
      dueDate: "",
      estimatedHours: undefined,
    },
  });

  const status = watch("status");
  const priority = watch("priority");

  const onSubmit = async (values: FormValues) => {
    try {
      await createTask.mutateAsync({
        title: values.title,
        description: values.description || undefined,
        status: values.status,
        priority: values.priority,
        dueDate: values.dueDate || undefined,
        estimatedHours: values.estimatedHours,
      });
      toast.success("Task created");
      reset();
      setOpen(false);
    } catch {
      toast.error("Failed to create task");
    }
  };

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>
        {trigger ?? (
          <Button size="sm" className="rounded-md">
            <Plus className="size-4" />
            New Task
          </Button>
        )}
      </DialogTrigger>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle>Create Task</DialogTitle>
        </DialogHeader>
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="title">Title *</Label>
            <Input id="title" {...register("title")} aria-invalid={!!errors.title} />
            {errors.title && (
              <p className="text-xs text-destructive">{errors.title.message}</p>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="description">Description</Label>
            <Textarea id="description" rows={3} {...register("description")} />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label>Status</Label>
              <Select
                value={status}
                onValueChange={(v) => setValue("status", v as TaskStatus)}
              >
                <SelectTrigger className="rounded-md">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  {(Object.keys(taskStatusLabels) as TaskStatus[]).map((s) => (
                    <SelectItem key={s} value={s}>
                      {taskStatusLabels[s]}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div className="space-y-2">
              <Label>Priority</Label>
              <Select
                value={priority}
                onValueChange={(v) => setValue("priority", v as TaskPriority)}
              >
                <SelectTrigger className="rounded-md">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  {(Object.keys(taskPriorityLabels) as TaskPriority[]).map(
                    (p) => (
                      <SelectItem key={p} value={p}>
                        {taskPriorityLabels[p]}
                      </SelectItem>
                    )
                  )}
                </SelectContent>
              </Select>
            </div>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label htmlFor="dueDate">Due Date</Label>
              <Input id="dueDate" type="date" {...register("dueDate")} />
            </div>
            <div className="space-y-2">
              <Label htmlFor="estimatedHours">Est. Hours</Label>
              <Input
                id="estimatedHours"
                type="number"
                min={0}
                step={0.5}
                {...register("estimatedHours", {
                  setValueAs: (v) =>
                    v === "" || v === undefined ? undefined : Number(v),
                })}
              />
            </div>
          </div>

          <Button
            type="submit"
            className="w-full rounded-md"
            disabled={createTask.isPending}
          >
            {createTask.isPending && <Loader2 className="size-4 animate-spin" />}
            Create Task
          </Button>
        </form>
      </DialogContent>
    </Dialog>
  );
}
