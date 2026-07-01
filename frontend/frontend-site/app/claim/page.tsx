"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { claimAccount, currentUser, isLoggedIn, type ApiError } from "@/lib/api";

export default function ClaimPage() {
  const router = useRouter();
  const [form, setForm] = useState({ email: "", claimToken: "", newPassword: "" });
  const [confirm, setConfirm] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<ApiError | null>(null);
  const [localError, setLocalError] = useState<string | null>(null);
  const [showNew, setShowNew] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);
  const [checking, setChecking] = useState(true);

  useEffect(() => {
    if (isLoggedIn()) {
      const user = currentUser();
      router.replace(user?.role === "ADMIN" ? "/admin" : "/portal");
      return;
    }
    setChecking(false);
  }, [router]);

  const update = (k: keyof typeof form) => (e: React.ChangeEvent<HTMLInputElement>) =>
    setForm({ ...form, [k]: e.target.value });

  const submit = async () => {
    setError(null);
    setLocalError(null);

    if (form.newPassword.length < 8) {
      setLocalError("Password must be at least 8 characters.");
      return;
    }
    if (form.newPassword !== confirm) {
      setLocalError("Passwords do not match.");
      return;
    }

    setLoading(true);
    try {
      const auth = await claimAccount(form);
      if (auth.user.role === "ADMIN") {
        router.push("/admin");
      } else {
        router.push("/portal");
      }
    } catch (err) {
      setError(err as ApiError);
    } finally {
      setLoading(false);
    }
  };

  if (checking) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-ivory">
        <div className="text-center">
          <span className="font-display text-2xl text-gold tracking-wide">UFB</span>
          <p className="text-mute text-sm mt-3">Checking access…</p>
        </div>
      </div>
    );
  }

  return (
    <main className="min-h-screen grid lg:grid-cols-2">
      <section className="hidden lg:flex flex-col justify-between bg-navy text-white p-14">
        <span className="font-display text-2xl text-gold tracking-wide">UFB</span>
        <div>
          <p className="text-gold uppercase tracking-[5px] text-xs mb-6">Unified Finance Bridge</p>
          <h1 className="font-display text-4xl leading-tight mb-5">
            Claim your <span className="text-gold">account</span>.
          </h1>
          <p className="text-[#c7d0de] font-light max-w-sm">
            Set your password using the one-time token issued to your account. This link is single-use and expires.
          </p>
        </div>
        <p className="font-display italic text-[#c7d0de] text-sm">Where Capital Meets Expertise</p>
      </section>

      <section className="flex items-center justify-center bg-ivory p-8">
        <div className="w-full max-w-md">
          <p className="text-gold-dark uppercase tracking-[4px] text-xs mb-3 font-semibold">Activate access</p>
          <h2 className="font-display text-3xl text-navy mb-8">Set your password</h2>

          <div className="space-y-5">
            <div>
              <label className="block text-xs uppercase tracking-wide text-mute mb-2">Email</label>
              <input
                type="email"
                value={form.email}
                onChange={update("email")}
                className="w-full border border-line bg-white rounded-sm px-4 py-3 text-char outline-none focus:border-gold"
              />
              {error?.fields?.email && <p className="text-xs text-red-700 mt-1">{error.fields.email}</p>}
            </div>

            <div>
              <label className="block text-xs uppercase tracking-wide text-mute mb-2">Claim token</label>
              <input
                type="text"
                value={form.claimToken}
                onChange={update("claimToken")}
                placeholder="Paste the one-time token"
                className="w-full border border-line bg-white rounded-sm px-4 py-3 text-char outline-none focus:border-gold font-mono text-sm"
              />
              {error?.fields?.claimToken && <p className="text-xs text-red-700 mt-1">{error.fields.claimToken}</p>}
            </div>

            <div>
              <label className="block text-xs uppercase tracking-wide text-mute mb-2">New password</label>
              <div className="relative">
                <input
                  type={showNew ? "text" : "password"}
                  value={form.newPassword}
                  onChange={update("newPassword")}
                  className="w-full border border-line bg-white rounded-sm px-4 py-3 text-char outline-none focus:border-gold pr-11"
                />
                <button
                  type="button"
                  tabIndex={-1}
                  onClick={() => setShowNew((s) => !s)}
                  className="absolute inset-y-0 right-3 flex items-center text-mute hover:text-char"
                >
                  {showNew ? <EyeOff /> : <Eye />}
                </button>
              </div>
              {error?.fields?.newPassword && <p className="text-xs text-red-700 mt-1">{error.fields.newPassword}</p>}
            </div>

            <div>
              <label className="block text-xs uppercase tracking-wide text-mute mb-2">Confirm password</label>
              <div className="relative">
                <input
                  type={showConfirm ? "text" : "password"}
                  value={confirm}
                  onChange={(e) => setConfirm(e.target.value)}
                  className="w-full border border-line bg-white rounded-sm px-4 py-3 text-char outline-none focus:border-gold pr-11"
                />
                <button
                  type="button"
                  tabIndex={-1}
                  onClick={() => setShowConfirm((s) => !s)}
                  className="absolute inset-y-0 right-3 flex items-center text-mute hover:text-char"
                >
                  {showConfirm ? <EyeOff /> : <Eye />}
                </button>
              </div>
            </div>

            {localError && <p className="text-sm text-red-700">{localError}</p>}
            {error && !error.fields && <p className="text-sm text-red-700">{error.message}</p>}

            <button
              onClick={submit}
              disabled={loading}
              className="w-full bg-gold text-navy font-semibold tracking-wide py-3.5 rounded-sm transition hover:bg-navy hover:text-gold disabled:opacity-60"
            >
              {loading ? "Activating…" : "Claim account"}
            </button>

            <p className="text-sm text-mute text-center">
              Already activated?{" "}
              <a href="/login" className="text-gold-dark font-semibold">Sign in</a>
            </p>
          </div>
        </div>
      </section>
    </main>
  );
}

function Eye() {
  return (
    <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24"
         fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M2 12s3-7 10-7 10 7 10 7-3 7-10 7-10-7-10-7Z"/>
      <circle cx="12" cy="12" r="3"/>
    </svg>
  );
}

function EyeOff() {
  return (
    <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24"
         fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94"/>
      <path d="M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19"/>
      <line x1="1" y1="1" x2="23" y2="23"/>
    </svg>
  );
}
