"use client";

import { useState } from "react";
import { loginUser, type ApiError, type AuthResponse } from "@/lib/api";

export default function LoginPage() {
  const [form, setForm] = useState({ email: "", password: "" });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<ApiError | null>(null);
  const [auth, setAuth] = useState<AuthResponse | null>(null);

  const update = (k: keyof typeof form) => (e: React.ChangeEvent<HTMLInputElement>) =>
    setForm({ ...form, [k]: e.target.value });

  const submit = async () => {
    setLoading(true);
    setError(null);
    try {
      setAuth(await loginUser(form));
    } catch (err) {
      setError(err as ApiError);
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className="min-h-screen grid lg:grid-cols-2">
      <section className="hidden lg:flex flex-col justify-between bg-navy text-white p-14">
        <span className="font-display text-2xl text-gold tracking-wide">UFB</span>
        <div>
          <p className="text-gold uppercase tracking-[5px] text-xs mb-6">Unified Finance Bridge</p>
          <h1 className="font-display text-4xl leading-tight mb-5">
            Welcome <span className="text-gold">back</span>.
          </h1>
          <p className="text-[#c7d0de] font-light max-w-sm">
            Sign in to your portal to manage your business profile and consultations.
          </p>
        </div>
        <p className="font-display italic text-[#c7d0de] text-sm">Where Capital Meets Expertise</p>
      </section>

      <section className="flex items-center justify-center bg-ivory p-8">
        <div className="w-full max-w-md">
          <p className="text-gold-dark uppercase tracking-[4px] text-xs mb-3 font-semibold">Sign in</p>
          <h2 className="font-display text-3xl text-navy mb-8">Access your portal</h2>

          {auth ? (
            <div className="border border-line bg-white rounded-md p-6">
              <h3 className="font-display text-xl text-navy mb-2">Signed in as {auth.user.fullName}.</h3>
              <p className="text-mute text-sm">You&rsquo;re authenticated. Portal routing comes next.</p>
            </div>
          ) : (
            <div className="space-y-5">
              <div>
                <label className="block text-xs uppercase tracking-wide text-mute mb-2">Email</label>
                <input
                  type="email"
                  value={form.email}
                  onChange={update("email")}
                  className="w-full border border-line bg-white rounded-sm px-4 py-3 text-char outline-none focus:border-gold"
                />
              </div>
              <div>
                <label className="block text-xs uppercase tracking-wide text-mute mb-2">Password</label>
                <input
                  type="password"
                  value={form.password}
                  onChange={update("password")}
                  className="w-full border border-line bg-white rounded-sm px-4 py-3 text-char outline-none focus:border-gold"
                />
              </div>

              {error && <p className="text-sm text-red-700">{error.message}</p>}

              <button
                onClick={submit}
                disabled={loading}
                className="w-full bg-gold text-navy font-semibold tracking-wide py-3.5 rounded-sm transition hover:bg-navy hover:text-gold disabled:opacity-60"
              >
                {loading ? "Signing in…" : "Sign in"}
              </button>

              <p className="text-sm text-mute text-center">
                No account?{" "}
                <a href="/register" className="text-gold-dark font-semibold">Create one</a>
              </p>
            </div>
          )}
        </div>
      </section>
    </main>
  );
}
