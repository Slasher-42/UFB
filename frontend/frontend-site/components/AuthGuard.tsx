"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { currentUser, isLoggedIn, type UserResponse } from "@/lib/api";

type Props = {
  children: React.ReactNode;
  requireAdmin?: boolean;
};

export default function AuthGuard({ children, requireAdmin = false }: Props) {
  const router = useRouter();
  const [status, setStatus] = useState<"checking" | "ok">("checking");

  useEffect(() => {
    if (!isLoggedIn()) {
      router.replace("/login");
      return;
    }

    const user: UserResponse | null = currentUser();

    if (!user) {
      router.replace("/login");
      return;
    }

    if (requireAdmin && user.role !== "ADMIN") {
      router.replace("/portal");
      return;
    }

    setStatus("ok");
  }, [router, requireAdmin]);

  if (status === "checking") {
    return (
      <div className="min-h-screen flex items-center justify-center bg-ivory">
        <div className="text-center">
          <span className="font-display text-2xl text-gold tracking-wide">UFB</span>
          <p className="text-mute text-sm mt-3">Verifying access…</p>
        </div>
      </div>
    );
  }

  return <>{children}</>;
}