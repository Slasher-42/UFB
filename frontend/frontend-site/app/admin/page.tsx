"use client";

import { useCallback, useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import AuthGuard from "@/components/AuthGuard";
import {
  listUsers, disableUser, enableUser, deleteUser, createAdmin,
  currentUser, logout,
  type UserResponse, type ApiError,
} from "@/lib/api";

function AdminContent() {
  const router = useRouter();
  const me = currentUser();

  const [users, setUsers] = useState<UserResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [busyId, setBusyId] = useState<number | null>(null);

  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState({ fullName: "", email: "", password: "" });
  const [formError, setFormError] = useState<ApiError | null>(null);
  const [creating, setCreating] = useState(false);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      setUsers(await listUsers());
    } catch (err) {
      setError((err as ApiError).message ?? "Failed to load users");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  const signOut = () => {
    logout();
    router.replace("/login");
  };

  const toggleEnabled = async (u: UserResponse) => {
    setBusyId(u.id);
    setError(null);
    try {
      const updated = u.enabled ? await disableUser(u.id) : await enableUser(u.id);
      setUsers((prev) => prev.map((x) => (x.id === updated.id ? updated : x)));
    } catch (err) {
      setError((err as ApiError).message ?? "Action failed");
    } finally {
      setBusyId(null);
    }
  };

  const removeUser = async (u: UserResponse) => {
    if (!confirm(`Permanently delete ${u.email}? This cannot be undone.`)) return;
    setBusyId(u.id);
    setError(null);
    try {
      await deleteUser(u.id);
      setUsers((prev) => prev.filter((x) => x.id !== u.id));
    } catch (err) {
      setError((err as ApiError).message ?? "Delete failed");
    } finally {
      setBusyId(null);
    }
  };

  const submitCreate = async () => {
    setCreating(true);
    setFormError(null);
    try {
      const created = await createAdmin(form);
      setUsers((prev) => [...prev, created]);
      setForm({ fullName: "", email: "", password: "" });
      setShowForm(false);
    } catch (err) {
      setFormError(err as ApiError);
    } finally {
      setCreating(false);
    }
  };

  const updateForm = (k: keyof typeof form) => (e: React.ChangeEvent<HTMLInputElement>) =>
    setForm({ ...form, [k]: e.target.value });

  return (
    <main className="min-h-screen bg-ivory">
      <header className="bg-navy text-white">
        <div className="max-w-6xl mx-auto px-8 py-5 flex items-center justify-between">
          <a href="/" className="font-display text-2xl text-gold tracking-wide">UFB · Admin</a>
          <div className="flex items-center gap-6">
            <a href="/" className="text-sm text-[#c7d0de] hover:text-gold">Home</a>
            <span className="text-sm text-[#c7d0de] hidden sm:inline">{me?.fullName}</span>
            <button
              onClick={signOut}
              className="text-sm border border-gold/50 text-gold px-4 py-2 rounded-sm transition hover:bg-gold hover:text-navy"
            >
              Sign out
            </button>
          </div>
        </div>
      </header>

      <section className="max-w-6xl mx-auto px-8 py-12">
        <div className="flex items-end justify-between flex-wrap gap-4 mb-8">
          <div>
            <p className="text-gold-dark uppercase tracking-[4px] text-xs mb-3 font-semibold">User management</p>
            <h1 className="font-display text-4xl text-navy">All users</h1>
          </div>
          <button
            onClick={() => setShowForm((s) => !s)}
            className="text-sm bg-gold text-navy font-semibold px-5 py-2.5 rounded-sm transition hover:bg-navy hover:text-gold"
          >
            {showForm ? "Cancel" : "+ Create admin"}
          </button>
        </div>

        {showForm && (
          <div className="bg-white border border-line rounded-lg p-6 mb-8 max-w-xl">
            <h3 className="font-display text-lg text-navy mb-4">New admin</h3>
            <div className="space-y-4">
              <AdminField label="Full name" value={form.fullName} onChange={updateForm("fullName")}
                          error={formError?.fields?.fullName} />
              <AdminField label="Email" type="email" value={form.email} onChange={updateForm("email")}
                          error={formError?.fields?.email} />
              <AdminField label="Password" type="password" value={form.password} onChange={updateForm("password")}
                          error={formError?.fields?.password} />
              {formError && !formError.fields && (
                <p className="text-sm text-red-700">{formError.message}</p>
              )}
              <button
                onClick={submitCreate}
                disabled={creating}
                className="text-sm bg-navy text-gold font-semibold px-5 py-2.5 rounded-sm transition hover:bg-gold hover:text-navy disabled:opacity-60"
              >
                {creating ? "Creating…" : "Create admin"}
              </button>
            </div>
          </div>
        )}

        {error && <p className="text-sm text-red-700 mb-4">{error}</p>}

        <div className="bg-white border border-line rounded-lg overflow-hidden">
          {loading ? (
            <p className="text-mute text-sm p-8 text-center">Loading users…</p>
          ) : users.length === 0 ? (
            <p className="text-mute text-sm p-8 text-center">No users yet.</p>
          ) : (
            <table className="w-full text-sm">
              <thead>
                <tr className="bg-navy/5 text-left text-mute uppercase text-xs tracking-wide">
                  <th className="px-5 py-3 font-semibold">Name</th>
                  <th className="px-5 py-3 font-semibold">Email</th>
                  <th className="px-5 py-3 font-semibold">Role</th>
                  <th className="px-5 py-3 font-semibold">Status</th>
                  <th className="px-5 py-3 font-semibold text-right">Actions</th>
                </tr>
              </thead>
              <tbody>
                {users.map((u) => {
                  const isSelf = u.id === me?.id;
                  const busy = busyId === u.id;
                  return (
                    <tr key={u.id} className="border-t border-line">
                      <td className="px-5 py-3 text-char font-medium">{u.fullName}</td>
                      <td className="px-5 py-3 text-char">{u.email}</td>
                      <td className="px-5 py-3">
                        <span className={u.role === "ADMIN" ? "text-gold-dark font-semibold" : "text-mute"}>
                          {u.role}
                        </span>
                      </td>
                      <td className="px-5 py-3">
                        <span className={u.enabled ? "text-green-700" : "text-red-700"}>
                          {u.enabled ? "Active" : "Disabled"}
                        </span>
                      </td>
                      <td className="px-5 py-3">
                        <div className="flex gap-2 justify-end">
                          <button
                            onClick={() => toggleEnabled(u)}
                            disabled={busy || isSelf}
                            title={isSelf ? "You can't change your own status" : ""}
                            className="text-xs border border-navy/20 text-navy px-3 py-1.5 rounded-sm transition hover:bg-navy hover:text-white disabled:opacity-40 disabled:cursor-not-allowed"
                          >
                            {u.enabled ? "Disable" : "Enable"}
                          </button>
                          <button
                            onClick={() => removeUser(u)}
                            disabled={busy || isSelf}
                            title={isSelf ? "You can't delete yourself" : ""}
                            className="text-xs border border-red-300 text-red-700 px-3 py-1.5 rounded-sm transition hover:bg-red-700 hover:text-white disabled:opacity-40 disabled:cursor-not-allowed"
                          >
                            Delete
                          </button>
                        </div>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          )}
        </div>
      </section>
    </main>
  );
}

function AdminField({
  label, value, onChange, type = "text", error,
}: {
  label: string;
  value: string;
  onChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
  type?: string;
  error?: string;
}) {
  return (
    <div>
      <label className="block text-xs uppercase tracking-wide text-mute mb-2">{label}</label>
      <input
        type={type}
        value={value}
        onChange={onChange}
        className="w-full border border-line bg-white rounded-sm px-4 py-3 text-char outline-none focus:border-gold"
      />
      {error && <p className="text-xs text-red-700 mt-1">{error}</p>}
    </div>
  );
}

export default function AdminPage() {
  return (
    <AuthGuard requireAdmin>
      <AdminContent />
    </AuthGuard>
  );
}
