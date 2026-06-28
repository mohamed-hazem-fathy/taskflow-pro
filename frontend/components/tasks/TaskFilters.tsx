"use client";

import { Search } from "lucide-react";
import { Input } from "@/components/ui/input";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Tabs, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { taskPriorityLabels, taskStatusLabels } from "@/lib/utils";
import type { TaskFilters, TaskPriority, TaskStatus } from "@/types";

interface TaskFiltersBarProps {
  filters: TaskFilters;
  onChange: (filters: TaskFilters) => void;
}

const statusTabs: Array<{ value: TaskStatus | "ALL"; label: string }> = [
  { value: "ALL", label: "All" },
  { value: "TODO", label: taskStatusLabels.TODO },
  { value: "IN_PROGRESS", label: taskStatusLabels.IN_PROGRESS },
  { value: "IN_REVIEW", label: taskStatusLabels.IN_REVIEW },
  { value: "BLOCKED", label: taskStatusLabels.BLOCKED },
  { value: "DONE", label: taskStatusLabels.DONE },
];

export function TaskFiltersBar({ filters, onChange }: TaskFiltersBarProps) {
  return (
    <div className="space-y-4">
      <Tabs
        value={filters.status ?? "ALL"}
        onValueChange={(value) =>
          onChange({ ...filters, status: value as TaskStatus | "ALL" })
        }
      >
        <TabsList className="h-auto flex-wrap rounded-md">
          {statusTabs.map((tab) => (
            <TabsTrigger
              key={tab.value}
              value={tab.value}
              className="rounded-md text-xs transition-colors duration-200"
            >
              {tab.label}
            </TabsTrigger>
          ))}
        </TabsList>
      </Tabs>

      <div className="flex flex-col gap-3 sm:flex-row">
        <div className="relative flex-1">
          <Search className="absolute left-2.5 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
          <Input
            placeholder="Search tasks..."
            className="pl-9 rounded-md"
            value={filters.search ?? ""}
            onChange={(e) => onChange({ ...filters, search: e.target.value })}
          />
        </div>

        <Select
          value={filters.priority ?? "ALL"}
          onValueChange={(value) =>
            onChange({ ...filters, priority: value as TaskPriority | "ALL" })
          }
        >
          <SelectTrigger className="w-full sm:w-40 rounded-md">
            <SelectValue placeholder="Priority" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="ALL">All priorities</SelectItem>
            {(Object.keys(taskPriorityLabels) as TaskPriority[]).map((p) => (
              <SelectItem key={p} value={p}>
                {taskPriorityLabels[p]}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>

        <Select
          value={filters.sortBy ?? "createdAt"}
          onValueChange={(value) =>
            onChange({
              ...filters,
              sortBy: value as TaskFilters["sortBy"],
            })
          }
        >
          <SelectTrigger className="w-full sm:w-40 rounded-md">
            <SelectValue placeholder="Sort by" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="dueDate">Due Date</SelectItem>
            <SelectItem value="priority">Priority</SelectItem>
            <SelectItem value="createdAt">Created At</SelectItem>
          </SelectContent>
        </Select>
      </div>
    </div>
  );
}
