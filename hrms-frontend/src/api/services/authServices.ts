import apiClient from "../client";

import type {
    LoginRequest,
    AuthResponse,
    RefreshTokenRequest,
    RefreshTokenResponse,
    LogoutRequest,
    ResetPasswordRequest,
    EmployeeAuthInfo,
    MessageResponse,
} from '@/types';

/**
 * Authentication Service
 * Handles all authentication-related API calls
 */
class AuthService {
  /**
   * Login user
   */
  async login(credentials: LoginRequest): Promise<AuthResponse> {
    const response = await apiClient.post<AuthResponse>('/auth/login', credentials);
    
    // Store tokens
    if (response.data.accessToken) {
      localStorage.setItem('accessToken', response.data.accessToken);
      localStorage.setItem('refreshToken', response.data.refreshToken);
      localStorage.setItem('user', JSON.stringify(response.data.employee));
    }
    
    return response.data;
  }

  /**
   * Refresh access token
   */
  async refreshToken(refreshToken: string): Promise<RefreshTokenResponse> {
    const response = await apiClient.post<RefreshTokenResponse>(
      '/auth/refresh',
      { refreshToken } as RefreshTokenRequest
    );
    
    if (response.data.accessToken) {
      localStorage.setItem('accessToken', response.data.accessToken);
      if (response.data.refreshToken) {
        localStorage.setItem('refreshToken', response.data.refreshToken);
      }
    }
    
    return response.data;
  }

  /**
   * Logout user
   */
  async logout(refreshToken: string): Promise<void> {
    try {
      await apiClient.post('/auth/logout', { refreshToken } as LogoutRequest);
    } finally {
      // Clear local storage regardless of API response
      localStorage.removeItem('accessToken');
      localStorage.removeItem('refreshToken');
      localStorage.removeItem('user');
    }
  }

  /**
   * Logout from all devices
   */
  async logoutAll(): Promise<MessageResponse> {
    try {
      const response = await apiClient.post<MessageResponse>('/auth/logout-all');
      return response.data;
    } finally {
      localStorage.removeItem('accessToken');
      localStorage.removeItem('refreshToken');
      localStorage.removeItem('user');
    }
  }

  /**
   * Reset password
   */
  async resetPassword(data: ResetPasswordRequest): Promise<MessageResponse> {
    const response = await apiClient.post<MessageResponse>('/auth/reset-password', data);
    return response.data;
  }

  /**
   * Check if password reset is required
   */
  async requiresPasswordReset(): Promise<boolean> {
    const response = await apiClient.get<boolean>('/auth/requires-password-reset');
    return response.data;
  }

  /**
   * Get current user from localStorage
   */
  getCurrentUser(): EmployeeAuthInfo | null {
    const user = localStorage.getItem('user');
    return user ? JSON.parse(user) : null;
  }

  /**
   * Check if user is authenticated
   */
  isAuthenticated(): boolean {
    return !!localStorage.getItem('accessToken');
  }

  /**
   * Get access token
   */
  getAccessToken(): string | null {
    return localStorage.getItem('accessToken');
  }

  /**
   * Get refresh token
   */
  getRefreshToken(): string | null {
    return localStorage.getItem('refreshToken');
  }
}

export default new AuthService();