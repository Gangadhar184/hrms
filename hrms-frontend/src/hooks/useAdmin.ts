import adminEmployeeServices from "@/api/services/adminEmployeeServices";
import adminPayrollServices from "@/api/services/adminPayrollServices";
import { toast } from "react-hot-toast";
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import type {
  EmployeeResponse,
  EmployeeListResponse,
  PageResponse,
  CreateEmployeeRequest,
  CreateEmployeeResponse,
  UpdateEmployeePersonalInfoRequest,
  UpdatePayInfoRequest,
  EmployeeSearchParams,
  EmployeeStatistics,
  MessageResponse,
  ErrorResponse,
  PayrollPreviewResponse,
  PayrollResponse,
  RunPayrollRequest,
  RunPayrollResponse,
  PayrollWeekStatus,
} from '@/types';
import { AxiosError } from 'axios';

/**
 * Hook to get all employees with filters
 */
export const useAllEmployees = (params: EmployeeSearchParams = {}) => {
  return useQuery<PageResponse<EmployeeListResponse>, AxiosError<ErrorResponse>>({
    queryKey: ['allEmployees', params],
    queryFn: () => adminEmployeeServices.getAllEmployees(params),
    staleTime: 2 * 60 * 1000,
    placeholderData: (previousData) => previousData,
  });
};

/**
 * Hook to get employee by ID
 */
export const useEmployeeById = (employeeId: number | undefined) => {
  return useQuery<EmployeeResponse, AxiosError<ErrorResponse>>({
    queryKey: ['employee', employeeId],
    queryFn: () => adminEmployeeServices.getEmployeeById(employeeId!),
    enabled: !!employeeId,
    staleTime: 5 * 60 * 1000,
  });
};

/**
 * Hook to create employee
 */
export const useCreateEmployee = () => {
  const queryClient = useQueryClient();

  return useMutation<
    CreateEmployeeResponse,
    AxiosError<ErrorResponse>,
    CreateEmployeeRequest
  >({
    mutationFn: adminEmployeeServices.createEmployee,
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: ['allEmployees'] });
      toast.success(
        `Employee created! Temporary password: ${data.temporaryPassword}`,
        { duration: 10000 }
      );
    },
    onError: (error) => {
      toast.error(error.response?.data?.message || 'Creation failed');
    },
  });
};

interface UpdateEmployeePersonalInfoVariables {
  employeeId: number;
  data: UpdateEmployeePersonalInfoRequest;
}

/**
 * Hook to update employee personal info
 */
export const useUpdateEmployeePersonalInfo = () => {
  const queryClient = useQueryClient();

  return useMutation<
    MessageResponse,
    AxiosError<ErrorResponse>,
    UpdateEmployeePersonalInfoVariables
  >({
    mutationFn: ({ employeeId, data }) =>
      adminEmployeeServices.updatePersonalInfo(employeeId, data),
    onSuccess: (_data, variables) => {
      queryClient.invalidateQueries({ queryKey: ['employee', variables.employeeId] });
      queryClient.invalidateQueries({ queryKey: ['allEmployees'] });
      toast.success('Employee information updated!');
    },
    onError: (error) => {
      toast.error(error.response?.data?.message || 'Update failed');
    },
  });
};

interface UpdateEmployeePayInfoVariables {
  employeeId: number;
  data: UpdatePayInfoRequest;
}

/**
 * Hook to update employee pay info
 */
export const useUpdateEmployeePayInfo = () => {
  const queryClient = useQueryClient();

  return useMutation<
    MessageResponse,
    AxiosError<ErrorResponse>,
    UpdateEmployeePayInfoVariables
  >({
    mutationFn: ({ employeeId, data }) =>
      adminEmployeeServices.updatePayInfo(employeeId, data),
    onSuccess: (_data, variables) => {
      queryClient.invalidateQueries({ queryKey: ['employee', variables.employeeId] });
      toast.success('Pay information updated!');
    },
    onError: (error) => {
      toast.error(error.response?.data?.message || 'Update failed');
    },
  });
};

/**
 * Hook to get active managers
 */
export const useActiveManagers = () => {
  return useQuery<EmployeeResponse[], AxiosError<ErrorResponse>>({
    queryKey: ['activeManagers'],
    queryFn: adminEmployeeServices.getActiveManagers,
    staleTime: 10 * 60 * 1000,
  });
};

/**
 * Hook to get employee statistics
 */
export const useEmployeeStatistics = () => {
  return useQuery<EmployeeStatistics, AxiosError<ErrorResponse>>({
    queryKey: ['employeeStatistics'],
    queryFn: adminEmployeeServices.getStatistics,
    staleTime: 5 * 60 * 1000,
  });
};

// ==================== PAYROLL MANAGEMENT ====================

/**
 * Hook to preview payroll
 */
export const usePayrollPreview = (weekStartDate: string | undefined) => {
  return useQuery<PayrollPreviewResponse, AxiosError<ErrorResponse>>({
    queryKey: ['payrollPreview', weekStartDate],
    queryFn: () => adminPayrollServices.previewPayroll(weekStartDate!),
    enabled: !!weekStartDate,
    staleTime: 1 * 60 * 1000,
  });
};

/**
 * Hook to run payroll
 */
export const useRunPayroll = () => {
  const queryClient = useQueryClient();

  return useMutation<
    RunPayrollResponse,
    AxiosError<ErrorResponse>,
    RunPayrollRequest
  >({
    mutationFn: adminPayrollServices.runPayroll,
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: ['payrollHistory'] });
      queryClient.invalidateQueries({ queryKey: ['currentWeekStatus'] });
      toast.success(
        `Payroll processed! ${data.processedCount} employees, Total: $${data.totalAmount}`,
        { duration: 8000 }
      );
    },
    onError: (error) => {
      toast.error(error.response?.data?.message || 'Payroll processing failed');
    },
  });
};

/**
 * Hook to get payroll by ID
 */
export const usePayrollById = (payrollId: number | undefined) => {
  return useQuery<PayrollResponse, AxiosError<ErrorResponse>>({
    queryKey: ['payroll', payrollId],
    queryFn: () => adminPayrollServices.getPayrollById(payrollId!),
    enabled: !!payrollId,
    staleTime: 5 * 60 * 1000,
  });
};

/**
 * Hook to get payroll history
 */
export const usePayrollHistory = (startDate: string | undefined, endDate: string | undefined) => {
  return useQuery<PayrollResponse[], AxiosError<ErrorResponse>>({
    queryKey: ['payrollHistory', startDate, endDate],
    queryFn: () => adminPayrollServices.getPayrollHistory(startDate!, endDate!),
    enabled: !!startDate && !!endDate,
    staleTime: 5 * 60 * 1000,
  });
};

/**
 * Hook to mark payroll as paid
 */
export const useMarkPayrollAsPaid = () => {
  const queryClient = useQueryClient();

  return useMutation<
    void,
    AxiosError<ErrorResponse>,
    number
  >({
    mutationFn: adminPayrollServices.markPayrollAsPaid,
    onSuccess: (_data, payrollId) => {
      queryClient.invalidateQueries({ queryKey: ['payroll', payrollId] });
      queryClient.invalidateQueries({ queryKey: ['payrollHistory'] });
      toast.success('Payroll marked as paid!');
    },
    onError: (error) => {
      toast.error(error.response?.data?.message || 'Update failed');
    },
  });
};

/**
 * Hook to get current week payroll status
 */
export const useCurrentWeekStatus = () => {
  return useQuery<PayrollWeekStatus, AxiosError<ErrorResponse>>({
    queryKey: ['currentWeekStatus'],
    queryFn: adminPayrollServices.getCurrentWeekStatus,
    staleTime: 2 * 60 * 1000,
  });
};