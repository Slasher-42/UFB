"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import AuthGuard from "@/components/AuthGuard";
import { currentUser, logout, type UserResponse } from "@/lib/api";

function PortalContent() {
  const router = useRouter();
  const [user, setUser] = useState<UserResponse | null>(null);

  useEffect(() => {
    setUser(currentUser());
  }, []);

  const signOut = () => {
    logout();
    router.replace("/login");
  };

  return (
    <main className="min-h-screen bg-ivory">
      <header className="bg-navy text-white">
        <div className="max-w-6xl mx-auto px-8 py-5 flex items-center justify-between">
          <Link href="/" className="font-display text-2xl text-gold tracking-wide">UFB</Link>
          <div className="flex items-center gap-6">
            <Link href="/" className="text-sm text-[#c7d0de] hover:text-gold">Home</Link>
            <span className="text-sm text-[#c7d0de] hidden sm:inline">
              {user?.fullName}
            </span>
            <button
              onClick={signOut}
              className="text-sm border border-gold/50 text-gold px-4 py-2 rounded-sm transition hover:bg-gold hover:text-navy"
            >
              Sign out
            </button>
          </div>
        </div>
      </header>

      <section className="max-w-6xl mx-auto px-8 py-14">
        <p className="text-gold-dark uppercase tracking-[4px] text-xs mb-3 font-semibold">Your portal</p>
        <h1 className="font-display text-4xl text-navy mb-3">
          Welcome, <span className="text-gold-dark">{user?.fullName?.split(" ")[0] ?? "there"}</span>.
        </h1>
        <p className="text-mute max-w-xl mb-12">
          This is your space to describe your business and follow your consultations.
        </p>

        <div className="grid md:grid-cols-2 gap-6">
          <div className="bg-white border border-line rounded-lg p-8">
            <h3 className="font-display text-xl text-navy mb-2">Your business profile</h3>
            <p className="text-mute text-sm mb-5">
              Tell us about your business — sector, stage, and what you need — so we can consult effectively.
            </p>
            <button
              disabled
              className="text-sm bg-navy/10 text-navy/50 px-4 py-2.5 rounded-sm cursor-not-allowed"
            >
              Describe your business (coming soon)
            </button>
          </div>

          <div className="bg-white border border-line rounded-lg p-8">
            <h3 className="font-display text-xl text-navy mb-2">Your consultations</h3>
            <p className="text-mute text-sm mb-5">
              Once you&rsquo;ve described your business, your consultation thread and advice will appear here.
            </p>
            <button
              disabled
              className="text-sm bg-navy/10 text-navy/50 px-4 py-2.5 rounded-sm cursor-not-allowed"
            >
              View consultations (coming soon)
            </button>
          </div>
        </div>

        <div className="mt-10 bg-white border border-line rounded-lg p-6">
          <h4 className="text-xs uppercase tracking-wide text-mute mb-3">Account</h4>
          <dl className="grid sm:grid-cols-3 gap-4 text-sm">
            <div>
              <dt className="text-mute">Name</dt>
              <dd className="text-char font-medium">{user?.fullName}</dd>
            </div>
            <div>
              <dt className="text-mute">Email</dt>
              <dd className="text-char font-medium">{user?.email}</dd>
            </div>
            <div>
              <dt className="text-mute">Role</dt>
              <dd className="text-char font-medium">{user?.role}</dd>
            </div>
          </dl>
        </div>
      </section>
    </main>
  );
}

export default function PortalPage() {
  return (
    <AuthGuard>
      <PortalContent />
    </AuthGuard>
  );
}
