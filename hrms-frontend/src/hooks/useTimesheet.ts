import timesheetServices from "@/api/services/timesheetServices";
import { toast } from "react-hot-toast";
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import type {
  TimesheetResponse,
  UpdateTimesheetRequest,
  MessageResponse,
  ErrorResponse,
} from '@/types';
import { AxiosError } from 'axios';

/**
 * Hook to get current week timesheet
 */
export const useCurrentTimesheet = () => {
  return useQuery<TimesheetResponse, AxiosError<ErrorResponse>>({
    queryKey: ['currentTimesheet'],
    queryFn: timesheetServices.getCurrentTimesheet,
    staleTime: 2 * 60 * 1000, // 2 minutes
    refetchOnWindowFocus: true,
  });
};

/**
 * Hook to get timesheet by ID
 */
export const useTimesheetById = (timesheetId: number | undefined) => {
  return useQuery<TimesheetResponse, AxiosError<ErrorResponse>>({
    queryKey: ['timesheet', timesheetId],
    queryFn: () => timesheetServices.getTimesheetById(timesheetId!),
    enabled: !!timesheetId,
    staleTime: 5 * 60 * 1000,
  });
};

/**
 * Hook to get timesheet history
 */
export const useTimesheetHistory = () => {
  return useQuery<TimesheetResponse[], AxiosError<ErrorResponse>>({
    queryKey: ['timesheetHistory'],
    queryFn: timesheetServices.getTimesheetHistory,
    staleTime: 5 * 60 * 1000,
  });
};

interface UpdateTimesheetVariables {
  timesheetId: number;
  data: UpdateTimesheetRequest;
}

/**
 * Hook to update timesheet
 */
export const useUpdateTimesheet = () => {
  const queryClient = useQueryClient();

  return useMutation<
    TimesheetResponse,
    AxiosError<ErrorResponse>,
    UpdateTimesheetVariables
  >({
    mutationFn: ({ timesheetId, data }) =>
      timesheetServices.updateTimesheet(timesheetId, data),
    onSuccess: (_data, variables) => {
      // Invalidate related queries
      queryClient.invalidateQueries({ queryKey: ['currentTimesheet'] });
      queryClient.invalidateQueries({ queryKey: ['timesheet', variables.timesheetId] });
      queryClient.invalidateQueries({ queryKey: ['timesheetHistory'] });
      toast.success('Timesheet updated successfully!');
    },
    onError: (error) => {
      toast.error(error.response?.data?.message || 'Update failed');
    },
  });
};

/**
 * Hook to submit timesheet
 */
export const useSubmitTimesheet = () => {
  const queryClient = useQueryClient();

  return useMutation<
    MessageResponse,
    AxiosError<ErrorResponse>,
    number
  >({
    mutationFn: timesheetServices.submitTimesheet,
    onSuccess: () => {
      // Invalidate all timesheet queries
      queryClient.invalidateQueries({ queryKey: ['currentTimesheet'] });
      queryClient.invalidateQueries({ queryKey: ['timesheetHistory'] });
      toast.success('Timesheet submitted for approval!');
    },
    onError: (error) => {
      toast.error(error.response?.data?.message || 'Submission failed');
    },
  });
};