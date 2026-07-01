export type RegisterRequest = { email: string; fullName: string; password: string };
export type LoginRequest = { email: string; password: string };

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

export type ApiError = { status: number; message: string; fields?: Record<string, string> };

async function post<T>(path: string, body: unknown): Promise<T> {
  const res = await fetch(path, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  });
  const data = await res.json().catch(() => ({}));
  if (!res.ok) {
    throw { status: res.status, message: data.message ?? "Request failed", fields: data.fields } as ApiError;
  }
  return data as T;
}

export function registerUser(body: RegisterRequest): Promise<UserResponse> {
  return post<UserResponse>("/api/auth/register", body);
}

export async function loginUser(body: LoginRequest): Promise<AuthResponse> {
  const auth = await post<AuthResponse>("/api/auth/login", body);
  localStorage.setItem("ufb_access", auth.accessToken);
  localStorage.setItem("ufb_refresh", auth.refreshToken);
  localStorage.setItem("ufb_user", JSON.stringify(auth.user));
  return auth;
}

export function logout() {
  localStorage.removeItem("ufb_access");
  localStorage.removeItem("ufb_refresh");
  localStorage.removeItem("ufb_user");
}

export function currentUser(): UserResponse | null {
  if (typeof window === "undefined") return null;
  const raw = localStorage.getItem("ufb_user");
  return raw ? (JSON.parse(raw) as UserResponse) : null;
}
