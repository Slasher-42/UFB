"use client";

import { Suspense, useState } from "react";
import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import { resendTwoFactor, verifyTwoFactor, type ApiError } from "@/lib/api";

function VerifyTwoFactorForm() {
  const searchParams = useSearchParams();
  const email = searchParams.get("email") ?? "";
  const [code, setCode] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<ApiError | null>(null);
  const [resent, setResent] = useState(false);
  const router = useRouter();

  const submit = async () => {
    setLoading(true);
    setError(null);
    try {
      const auth = await verifyTwoFactor(email, code);
      router.replace(auth.user.role === "ADMIN" ? "/admin" : "/portal");
    } catch (err) {
      setError(err as ApiError);
    } finally {
      setLoading(false);
    }
  };

  const resend = async () => {
    try {
      await resendTwoFactor(email);
    } finally {
      setResent(true);
    }
  };

  const codeError = error?.fields?.code ?? (error && !error.fields ? error.message : undefined);

  return (
    <main className="min-h-screen grid lg:grid-cols-2">
      <section className="hidden lg:flex flex-col justify-between bg-navy text-white p-14">
        <Link href="/" className="font-display text-2xl text-gold tracking-wide">UFB</Link>
        <div>
          <p className="text-gold uppercase tracking-[5px] text-xs mb-6">Unified Finance Bridge</p>
          <h1 className="font-display text-4xl leading-tight mb-5">
            Confirm it&rsquo;s <span className="text-gold">you</span>.
          </h1>
          <p className="text-[#c7d0de] font-light max-w-sm">
            We&rsquo;ve sent a one-time code to your email to confirm this is your first sign-in.
          </p>
        </div>
        <p className="font-display italic text-[#c7d0de] text-sm">Where Capital Meets Expertise</p>
      </section>

      <section className="flex items-center justify-center bg-ivory p-8">
        <div className="w-full max-w-md">
          <p className="text-gold-dark uppercase tracking-[4px] text-xs mb-3 font-semibold">Verify</p>
          <h2 className="font-display text-3xl text-navy mb-2">Enter your code</h2>
          <p className="text-mute text-sm mb-8">
            We sent a 6-digit code to <span className="text-char font-semibold">{email}</span>.
          </p>

          <div className="space-y-5">
            <div>
              <label className="block text-xs uppercase tracking-wide text-mute mb-2">Verification code</label>
              <input
                type="text"
                inputMode="numeric"
                maxLength={6}
                value={code}
                onChange={(e) => setCode(e.target.value.replace(/\D/g, ""))}
                className="w-full border border-line bg-white rounded-sm px-4 py-3 text-char text-center text-2xl tracking-[8px] outline-none focus:border-gold"
              />
              {codeError && <p className="text-xs text-red-700 mt-1">{codeError}</p>}
            </div>

            <p className="text-sm text-mute">
              {resent ? (
                "A new code is on its way."
              ) : (
                <>
                  Didn&rsquo;t get it?{" "}
                  <button type="button" onClick={resend} className="text-gold-dark font-semibold">
                    Resend code
                  </button>
                </>
              )}
            </p>

            <button
              onClick={submit}
              disabled={loading || code.length !== 6}
              className="w-full bg-gold text-navy font-semibold tracking-wide py-3.5 rounded-sm transition hover:bg-navy hover:text-gold disabled:opacity-60"
            >
              {loading ? "Verifying…" : "Verify and sign in"}
            </button>

            <p className="text-sm text-mute text-center">
              <Link href="/login" className="text-gold-dark font-semibold">Back to sign in</Link>
            </p>
          </div>
        </div>
      </section>
    </main>
  );
}

export default function VerifyTwoFactorPage() {
  return (
    <Suspense fallback={null}>
      <VerifyTwoFactorForm />
    </Suspense>
  );
}
