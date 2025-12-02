import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import employeeServices from "@/api/services/employeeServices";
import { toast } from "react-hot-toast";
import type {
  DashboardResponse,
  EmployeeResponse,
  PayInfoResponse,
  ContactInfoResponse,
  UpdateContactInfoRequest,
  MessageResponse,
  ErrorResponse,
} from '@/types';
import { AxiosError } from 'axios';

/**
 * Hook to get employee dashboard
 */
export const useDashboard = () => {
  return useQuery<DashboardResponse, AxiosError<ErrorResponse>>({
    queryKey: ['dashboard'],
    queryFn: employeeServices.getDashboard,
    staleTime: 5 * 60 * 1000, // 5 minutes
  });
};

/**
 * Hook to get personal information
 */
export const usePersonalInfo = () => {
  return useQuery<EmployeeResponse, AxiosError<ErrorResponse>>({
    queryKey: ['personalInfo'],
    queryFn: employeeServices.getPersonalInfo,
    staleTime: 10 * 60 * 1000, // 10 minutes
  });
};

/**
 * Hook to get pay information
 */
export const usePayInfo = () => {
  return useQuery<PayInfoResponse, AxiosError<ErrorResponse>>({
    queryKey: ['payInfo'],
    queryFn: employeeServices.getPayInfo,
    staleTime: 10 * 60 * 1000,
  });
};

/**
 * Hook to get contact information
 */
export const useContactInfo = () => {
  return useQuery<ContactInfoResponse, AxiosError<ErrorResponse>>({
    queryKey: ['contactInfo'],
    queryFn: employeeServices.getContactInfo,
    staleTime: 10 * 60 * 1000,
  });
};

/**
 * Hook to update contact information
 */
export const useUpdateContactInfo = () => {
  const queryClient = useQueryClient();

  return useMutation<
    MessageResponse,
    AxiosError<ErrorResponse>,
    UpdateContactInfoRequest
  >({
    mutationFn: employeeServices.updateContactInfo,
    onSuccess: () => {
      // Invalidate and refetch contact info
      queryClient.invalidateQueries({ queryKey: ['contactInfo'] });
      toast.success('Contact information updated successfully!');
    },
    onError: (error) => {
      toast.error(error.response?.data?.message || 'Update failed');
    },
  });
};

/**
 * Hook to get user profile
 */
export const useProfile = () => {
  return useQuery<EmployeeResponse, AxiosError<ErrorResponse>>({
    queryKey: ['profile'],
    queryFn: employeeServices.getProfile,
    staleTime: 10 * 60 * 1000,
  });
};