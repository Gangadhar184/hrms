import { Role } from "./common.types";

export interface LoginRequest {
  username: string;
  password: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  employee: EmployeeAuthInfo;
}

export interface EmployeeAuthInfo {
  id: number;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  role: Role;
  isFirstLogin: boolean;
}


export interface RefreshTokenRequest {
  refreshToken: string;
}

export interface RefreshTokenResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
}


export interface LogoutRequest {
  refreshToken: string;
}


export interface ResetPasswordRequest {
  currentPassword: string;
  newPassword: string;
  confirmPassword: string;
}