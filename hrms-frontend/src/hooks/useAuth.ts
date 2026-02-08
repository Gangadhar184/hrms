import { useMutation, useQuery, useQueryClient, type UseMutationResult, type UseQueryResult } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-hot-toast';
import type {
  LoginRequest,
  AuthResponse,
  ResetPasswordRequest,
  MessageResponse,
  EmployeeAuthInfo,
  ErrorResponse,
} from '@/types';
import { AxiosError } from 'axios';

import authServices from '@/api/services/authServices';
import { useAuth } from '@/contexts/AuthContext';

/**
 * Hook for login mutation
 */
export const useLogin = (): UseMutationResult<AuthResponse, AxiosError<ErrorResponse>, LoginRequest> => {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const { login } = useAuth();

  return useMutation({
    mutationFn: (credentials: LoginRequest) => authServices.login(credentials),
    onSuccess: (data: AuthResponse) => {
      // Update AuthContext with the logged in user
      login(data.employee);

      // Check if password reset required
      if (data.employee.isFirstLogin) {
        toast.success('Login successful! Please reset your password.');
        navigate('/reset-password');
      } else {
        toast.success('Login successful!');
        navigate('/dashboard');
      }
      // Invalidate queries to refetch with new auth
      queryClient.invalidateQueries();
    },
    onError: (error) => {
      toast.error(error.response?.data?.message || 'Login failed');
    },
  });
};

/**
 * Hook for logout mutation
 */
export const useLogout = (): UseMutationResult<void, AxiosError<ErrorResponse>, void> => {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const { logout } = useAuth();

  return useMutation({
    mutationFn: () => {
      const refreshToken = authServices.getRefreshToken();
      if (!refreshToken) {
        throw new Error('No refresh token found');
      }
      return authServices.logout(refreshToken);
    },
    onSuccess: () => {
      // Clear auth context
      logout();
      // Clear all queries
      queryClient.clear();
      toast.success('Logged out successfully');
      navigate('/login');
    },
    onError: (error) => {
      // Still logout locally even if API fails
      logout();
      queryClient.clear();
      navigate('/login');
      console.error('Logout error:', error);
    },
  });
};

/**
 * Hook for logout from all devices
 */
export const useLogoutAll = (): UseMutationResult<MessageResponse, AxiosError<ErrorResponse>, void> => {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const { logout } = useAuth();

  return useMutation({
    mutationFn: () => authServices.logoutAll(),
    onSuccess: () => {
      logout();
      queryClient.clear();
      toast.success('Logged out from all devices');
      navigate('/login');
    },
    onError: () => {
      logout();
      queryClient.clear();
      navigate('/login');
      toast.error('Logout error, but logged out locally');
    },
  });
};

/**
 * Hook for password reset mutation
 */
export const useResetPassword = (): UseMutationResult<MessageResponse, AxiosError<ErrorResponse>, ResetPasswordRequest> => {
  const navigate = useNavigate();
  const { updateUser } = useAuth();

  return useMutation({
    mutationFn: (data: ResetPasswordRequest) => authServices.resetPassword(data),
    onSuccess: () => {
      // Update isFirstLogin to false in context
      updateUser({ isFirstLogin: false });
      toast.success('Password reset successfully!');
      navigate('/dashboard');
    },
    onError: (error) => {
      toast.error(error.response?.data?.message || 'Password reset failed');
    },
  });
};

/**
 * Hook to check if password reset is required
 */
export const useRequiresPasswordReset = (): UseQueryResult<boolean, Error> => {
  return useQuery({
    queryKey: ['requiresPasswordReset'],
    queryFn: () => authServices.requiresPasswordReset(),
    enabled: authServices.isAuthenticated(),
    retry: false,
  });
};

/**
 * Hook to get current user
 */
export const useCurrentUser = (): EmployeeAuthInfo | null => {
  return authServices.getCurrentUser();
};

/**
 * Hook to check authentication status
 */
export const useIsAuthenticated = (): boolean => {
  return authServices.isAuthenticated();
};