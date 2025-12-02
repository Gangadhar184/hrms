import managerServices from "@/api/services/managerServices";
import { toast } from "react-hot-toast";
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import type {
  EmployeeResponse,
  TimesheetListResponse,
  PageResponse,
  TeamTimesheetParams,
  MessageResponse,
  ManagerStatistics,
  ErrorResponse,
} from '@/types';
import { AxiosError } from 'axios';

/**
 * Hook to get direct reports
 */
export const useDirectReports = () => {
  return useQuery<EmployeeResponse[], AxiosError<ErrorResponse>>({
    queryKey: ['directReports'],
    queryFn: managerServices.getDirectReports,
    staleTime: 5 * 60 * 1000,
  });
};

/**
 * Hook to get team timesheets with pagination
 */
export const useTeamTimesheets = (params: TeamTimesheetParams = {}) => {
  return useQuery<PageResponse<TimesheetListResponse>, AxiosError<ErrorResponse>>({
    queryKey: ['teamTimesheets', params],
    queryFn: () => managerServices.getTeamTimesheets(params),
    staleTime: 2 * 60 * 1000,
    placeholderData: (previousData) => previousData, // Keep old data while fetching new page
  });
};

/**
 * Hook to get pending timesheets count
 */
export const usePendingTimesheetsCount = () => {
  return useQuery<number, AxiosError<ErrorResponse>>({
    queryKey: ['pendingTimesheetsCount'],
    queryFn: managerServices.getPendingTimesheetsCount,
    staleTime: 1 * 60 * 1000, // 1 minute
    refetchInterval: 5 * 60 * 1000, // Auto-refetch every 5 minutes
  });
};

/**
 * Hook to approve timesheet
 */
export const useApproveTimesheet = () => {
  const queryClient = useQueryClient();

  return useMutation<
    MessageResponse,
    AxiosError<ErrorResponse>,
    number
  >({
    mutationFn: managerServices.approveTimesheet,
    onSuccess: () => {
      // Invalidate team timesheets queries
      queryClient.invalidateQueries({ queryKey: ['teamTimesheets'] });
      queryClient.invalidateQueries({ queryKey: ['pendingTimesheetsCount'] });
      queryClient.invalidateQueries({ queryKey: ['managerStatistics'] });
      toast.success('Timesheet approved successfully!');
    },
    onError: (error) => {
      toast.error(error.response?.data?.message || 'Approval failed');
    },
  });
};

interface DenyTimesheetVariables {
  timesheetId: number;
  reason: string;
}

/**
 * Hook to deny timesheet
 */
export const useDenyTimesheet = () => {
  const queryClient = useQueryClient();

  return useMutation<
    MessageResponse,
    AxiosError<ErrorResponse>,
    DenyTimesheetVariables
  >({
    mutationFn: ({ timesheetId, reason }) =>
      managerServices.denyTimesheet(timesheetId, reason),
    onSuccess: () => {
      // Invalidate team timesheets queries
      queryClient.invalidateQueries({ queryKey: ['teamTimesheets'] });
      queryClient.invalidateQueries({ queryKey: ['pendingTimesheetsCount'] });
      queryClient.invalidateQueries({ queryKey: ['managerStatistics'] });
      toast.success('Timesheet denied');
    },
    onError: (error) => {
      toast.error(error.response?.data?.message || 'Denial failed');
    },
  });
};

/**
 * Hook to get manager statistics
 */
export const useManagerStatistics = () => {
  return useQuery<ManagerStatistics, AxiosError<ErrorResponse>>({
    queryKey: ['managerStatistics'],
    queryFn: managerServices.getStatistics,
    staleTime: 5 * 60 * 1000,
  });
};