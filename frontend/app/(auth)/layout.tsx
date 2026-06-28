import { Kanban } from "lucide-react";

export default function AuthLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <div className="relative flex min-h-screen items-center justify-center bg-background p-4">
      <div
        className="pointer-events-none absolute inset-0 opacity-[0.03] dark:opacity-[0.06]"
        style={{
          backgroundImage:
            "linear-gradient(to right, currentColor 1px, transparent 1px), linear-gradient(to bottom, currentColor 1px, transparent 1px)",
          backgroundSize: "32px 32px",
        }}
      />
      <div className="relative w-full max-w-md">
        <div className="mb-8 flex flex-col items-center gap-2 text-center">
          <div className="flex size-10 items-center justify-center rounded-md bg-primary text-primary-foreground">
            <Kanban className="size-5" />
          </div>
          <h1 className="text-2xl font-bold tracking-tight">TaskFlow Pro</h1>
          <p className="text-sm text-muted-foreground">
            Task & project management for modern teams
          </p>
        </div>
        <div className="rounded-lg border bg-card p-6 shadow-sm">{children}</div>
      </div>
    </div>
  );
}
