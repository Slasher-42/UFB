export type RegisterRequest = { email: string; fullName: string; password: string };
export type LoginRequest = { email: string; password: string };
export type ClaimRequest = { email: string; claimToken: string; newPassword: string };
export type CreateAdminRequest = { email: string; fullName: string; password: string };
export type ResetPasswordRequest = { email: string; resetToken: string; newPassword: string };
export type VerifyEmailRequest = { email: string; verificationToken: string };

export type UserResponse = {
  id: number;
  email: string;
  fullName: string;
  role: "USER" | "ADMIN";
  enabled: boolean;
  createdAt: string;
};

export type AuthResponse = {
  accessToken: string;
  refreshToken: string;
  user: UserResponse;
};

export type LoginResponse = {
  twoFactorRequired: boolean;
  auth: AuthResponse | null;
};

export type ApiError = { status: number; message: string; fields?: Record<string, string> };
export type ClaimStatusResponse = { needsClaim: boolean };

const ACCESS_KEY = "ufb_access";
const REFRESH_KEY = "ufb_refresh";
const USER_KEY = "ufb_user";

function getAccess(): string | null {
  return typeof window === "undefined" ? null : localStorage.getItem(ACCESS_KEY);
}
function getRefresh(): string | null {
  return typeof window === "undefined" ? null : localStorage.getItem(REFRESH_KEY);
}
function storeAuth(auth: AuthResponse) {
  localStorage.setItem(ACCESS_KEY, auth.accessToken);
  localStorage.setItem(REFRESH_KEY, auth.refreshToken);
  localStorage.setItem(USER_KEY, JSON.stringify(auth.user));
}

export function logout() {
  localStorage.removeItem(ACCESS_KEY);
  localStorage.removeItem(REFRESH_KEY);
  localStorage.removeItem(USER_KEY);
}

export function currentUser(): UserResponse | null {
  if (typeof window === "undefined") return null;
  const raw = localStorage.getItem(USER_KEY);
  return raw ? (JSON.parse(raw) as UserResponse) : null;
}

export function isLoggedIn(): boolean {
  return !!getAccess();
}

async function parse<T>(res: Response): Promise<T> {
  const data = await res.json().catch(() => ({}));
  if (!res.ok) {
    throw {
      status: res.status,
      message: (data as ApiError).message ?? "Request failed",
      fields: (data as ApiError).fields,
    } as ApiError;
  }
  return data as T;
}

async function postPublic<T>(path: string, body: unknown): Promise<T> {
  const res = await fetch(path, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  });
  return parse<T>(res);
}

export function registerUser(body: RegisterRequest): Promise<UserResponse> {
  return postPublic<UserResponse>("/api/auth/register", body);
}

export async function loginUser(body: LoginRequest): Promise<LoginResponse> {
  const result = await postPublic<LoginResponse>("/api/auth/login", body);
  if (!result.twoFactorRequired && result.auth) {
    storeAuth(result.auth);
  }
  return result;
}

export async function verifyTwoFactor(email: string, code: string): Promise<AuthResponse> {
  const auth = await postPublic<AuthResponse>("/api/auth/verify-2fa", { email, code });
  storeAuth(auth);
  return auth;
}

export function resendTwoFactor(email: string): Promise<{ message: string }> {
  return postPublic<{ message: string }>("/api/auth/resend-2fa", { email });
}

export async function claimAccount(body: ClaimRequest): Promise<AuthResponse> {
  const auth = await postPublic<AuthResponse>("/api/auth/claim", body);
  storeAuth(auth);
  return auth;
}

export async function claimStatus(): Promise<ClaimStatusResponse> {
  return parse<ClaimStatusResponse>(await fetch("/api/auth/claim-status"));
}

export function forgotPassword(email: string): Promise<{ message: string }> {
  return postPublic<{ message: string }>("/api/auth/forgot-password", { email });
}

export async function resetPassword(body: ResetPasswordRequest): Promise<AuthResponse> {
  const auth = await postPublic<AuthResponse>("/api/auth/reset-password", body);
  storeAuth(auth);
  return auth;
}

export async function checkEmailDeliverable(email: string): Promise<boolean> {
  const res = await fetch(`/api/auth/email-check?email=${encodeURIComponent(email)}`);
  const data = await parse<{ deliverable: boolean }>(res);
  return data.deliverable;
}

export async function verifyEmail(body: VerifyEmailRequest): Promise<AuthResponse> {
  const auth = await postPublic<AuthResponse>("/api/auth/verify-email", body);
  storeAuth(auth);
  return auth;
}

export function resendVerification(email: string): Promise<{ message: string }> {
  return postPublic<{ message: string }>("/api/auth/resend-verification", { email });
}

async function tryRefresh(): Promise<boolean> {
  const refreshToken = getRefresh();
  if (!refreshToken) return false;
  try {
    const auth = await postPublic<AuthResponse>("/api/auth/refresh", { refreshToken });
    storeAuth(auth);
    return true;
  } catch {
    logout();
    return false;
  }
}

async function authFetch(path: string, init: RequestInit, retry = true): Promise<Response> {
  const token = getAccess();
  const headers = new Headers(init.headers);
  if (token) headers.set("Authorization", `Bearer ${token}`);
  if (init.body) headers.set("Content-Type", "application/json");

  const res = await fetch(path, { ...init, headers });

  if (res.status === 401 && retry) {
    const refreshed = await tryRefresh();
    if (refreshed) return authFetch(path, init, false);
  }
  return res;
}

async function authGet<T>(path: string): Promise<T> {
  return parse<T>(await authFetch(path, { method: "GET" }));
}
async function authSend<T>(path: string, method: string, body?: unknown): Promise<T> {
  return parse<T>(
    await authFetch(path, { method, body: body ? JSON.stringify(body) : undefined })
  );
}

export function listUsers(): Promise<UserResponse[]> {
  return authGet<UserResponse[]>("/api/admin/users");
}
export function disableUser(id: number): Promise<UserResponse> {
  return authSend<UserResponse>(`/api/admin/users/${id}/disable`, "PATCH");
}
export function enableUser(id: number): Promise<UserResponse> {
  return authSend<UserResponse>(`/api/admin/users/${id}/enable`, "PATCH");
}
export function deleteUser(id: number): Promise<void> {
  return authFetch(`/api/admin/users/${id}`, { method: "DELETE" }).then((res) => {
    if (!res.ok && res.status !== 204) throw { status: res.status, message: "Delete failed" } as ApiError;
  });
}
export function createAdmin(body: CreateAdminRequest): Promise<UserResponse> {
  return authSend<UserResponse>("/api/admin/admins", "POST", body);
}
