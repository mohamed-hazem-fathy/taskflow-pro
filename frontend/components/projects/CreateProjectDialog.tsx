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
import { useCreateProject } from "@/hooks/useProjects";
import { projectStatusLabels } from "@/lib/utils";
import type { ProjectStatus } from "@/types";

const schema = z.object({
  name: z.string().min(1, "Project name is required"),
  description: z.string().optional(),
  startDate: z.string().optional(),
  endDate: z.string().optional(),
  status: z.enum(["PLANNING", "ACTIVE", "ON_HOLD", "COMPLETED", "ARCHIVED"]),
});

type FormValues = z.infer<typeof schema>;

interface CreateProjectDialogProps {
  trigger?: React.ReactNode;
}

export function CreateProjectDialog({ trigger }: CreateProjectDialogProps) {
  const [open, setOpen] = useState(false);
  const createProject = useCreateProject();
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
      name: "",
      description: "",
      startDate: "",
      endDate: "",
      status: "PLANNING",
    },
  });

  const status = watch("status");

  const onSubmit = async (values: FormValues) => {
    try {
      await createProject.mutateAsync({
        name: values.name,
        description: values.description || undefined,
        startDate: values.startDate || undefined,
        endDate: values.endDate || undefined,
        status: values.status,
      });
      toast.success("Project created successfully");
      reset();
      setOpen(false);
    } catch {
      toast.error("Failed to create project");
    }
  };

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>
        {trigger ?? (
          <Button className="rounded-md transition-colors duration-200">
            <Plus className="size-4" />
            New Project
          </Button>
        )}
      </DialogTrigger>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle>Create Project</DialogTitle>
        </DialogHeader>
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="name">Name *</Label>
            <Input id="name" {...register("name")} aria-invalid={!!errors.name} />
            {errors.name && (
              <p className="text-xs text-destructive">{errors.name.message}</p>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="description">Description</Label>
            <Textarea id="description" rows={3} {...register("description")} />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label htmlFor="startDate">Start Date</Label>
              <Input id="startDate" type="date" {...register("startDate")} />
            </div>
            <div className="space-y-2">
              <Label htmlFor="endDate">End Date</Label>
              <Input id="endDate" type="date" {...register("endDate")} />
            </div>
          </div>

          <div className="space-y-2">
            <Label>Status</Label>
            <Select
              value={status}
              onValueChange={(value) =>
                setValue("status", value as ProjectStatus)
              }
            >
              <SelectTrigger className="rounded-md">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                {(Object.keys(projectStatusLabels) as ProjectStatus[]).map(
                  (s) => (
                    <SelectItem key={s} value={s}>
                      {projectStatusLabels[s]}
                    </SelectItem>
                  )
                )}
              </SelectContent>
            </Select>
          </div>

          <Button
            type="submit"
            className="w-full rounded-md"
            disabled={createProject.isPending}
          >
            {createProject.isPending && (
              <Loader2 className="size-4 animate-spin" />
            )}
            Create Project
          </Button>
        </form>
      </DialogContent>
    </Dialog>
  );
}
